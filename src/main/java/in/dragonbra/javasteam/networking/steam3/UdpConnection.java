package in.dragonbra.javasteam.networking.steam3;

import in.dragonbra.javasteam.enums.EUdpPacketType;
import in.dragonbra.javasteam.generated.ChallengeData;
import in.dragonbra.javasteam.generated.ConnectData;
import in.dragonbra.javasteam.util.log.LogManager;
import in.dragonbra.javasteam.util.log.Logger;
import in.dragonbra.javasteam.util.stream.MemoryStream;
import in.dragonbra.javasteam.util.stream.SeekOrigin;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author lngtr
 * @since 2018-03-01
 */
public class UdpConnection extends Connection {

    private static final Logger logger = LogManager.getLogger(UdpConnection.class);

    /**
     * Milliseconds to wait before sending packets.
     */
    private static final long RESEND_DELAY = 3000L;

    /**
     * Milliseconds to wait before considering the connection dead.
     */
    private static final long TIMEOUT_DELAY = 60000L;

    /**
     * Maximum number of packets to resend when RESEND_DELAY is exceeded.
     */
    private static final int RESEND_COUNT = 3;

    /**
     * Maximum number of packets that we can be waiting on at a time.
     */
    private static final int AHEAD_COUNT = 3;

    /**
     * Contains information about the state of the connection, used to filter out packets that are
     * unexpected or not valid given the state of the connection.
     */
    @SuppressWarnings("FieldMayBeFinal")
    private volatile AtomicReference<State> state;

    private Thread netThread;

    @SuppressWarnings("FieldCanBeLocal")
    private NetLoop netLoop;

    private final DatagramSocket sock;

    private long timeout;

    private long nextResend;

    private static int SOURCE_CONN_ID = 512;

    private int remoteConnId;

    /**
     * The next outgoing sequence number to be used.
     */
    private int outSeq;
    /**
     * The highest sequence number of an outbound packet that has been sent.
     */
    private int outSeqSent;
    /**
     * The sequence number of the highest packet acknowledged by the server.
     */
    private int outSeqAcked;

    /**
     * The sequence number we plan on acknowledging receiving with the next Ack. All packets below or equal
     * to inSeq *must* have been received, but not necessarily handled.
     */
    private int inSeq;
    /**
     * The highest sequence number we've acknowledged receiving.
     */
    private int inSeqAcked;
    /**
     * The highest sequence number we've processed.
     */
    private int inSeqHandled;

    private final List<UdpPacket> outPackets = new ArrayList<>();
    private Map<Integer, UdpPacket> inPackets;

    private InetSocketAddress currentEndPoint;

    public UdpConnection() {
        try {
            sock = new DatagramSocket();
        } catch (SocketException e) {
            throw new IllegalStateException("couldn't create datagram socket", e);
        }

        state = new AtomicReference<>(State.DISCONNECTED);
    }

    @Override
    public void connect(InetSocketAddress endPoint, int timeout) {
        outPackets.clear();
        inPackets = new HashMap<>();

        currentEndPoint = null;
        remoteConnId = 0;

        outSeq = 1;
        outSeqSent = 0;
        outSeqAcked = 0;

        inSeq = 0;
        inSeqAcked = 0;
        inSeqHandled = 0;

        logger.debug("connecting to " + endPoint);
        netLoop = new NetLoop(endPoint);
        netThread = new Thread(netLoop, "UdpConnection Thread");
        netThread.start();
    }

    @Override
    public void disconnect() {
        if (netThread == null) {
            return;
        }

        // if we think we aren't already disconnected, apply disconnecting unless we read back disconnected
        if (state.get() != State.DISCONNECTED && state.getAndSet(State.DISCONNECTING) == State.DISCONNECTED) {
            state.set(State.DISCONNECTED);
        }

        // only notify if we actually applied the disconnecting state
        if (state.get() == State.DISCONNECTING) {
            // Play nicely and let the server know that we're done. Other party is expected to Ack this,
            // so it needs to be sent sequenced.
            sendSequenced(new UdpPacket(EUdpPacketType.Disconnect));
        }

        // Advance this the same way that steam does, when a socket gets reused.
        SOURCE_CONN_ID += 256;

        onDisconnected(true);
    }

    @Override
    public void send(byte[] data) {
        if (state.get() == State.CONNECTED) {
            sendData(new MemoryStream(data));
        }
    }

    @Override
    public InetAddress getLocalIP() {
        return sock.getLocalAddress();
    }

    @Override
    public InetSocketAddress getCurrentEndPoint() {
        return currentEndPoint;
    }

    @Override
    public ProtocolTypes getProtocolTypes() {
        return ProtocolTypes.UDP;
    }

    /**
     * Sends the data sequenced as a single message, splitting it into multiple parts if necessary.
     *
     * @param ms The data to send.
     */
    private void sendData(MemoryStream ms) {
        UdpPacket[] packets = new UdpPacket[(int) ((ms.getLength() / UdpPacket.MAX_PAYLOAD) + 1)];

        for (int i = 0; i < packets.length; i++) {
            long index = (long) i * UdpPacket.MAX_PAYLOAD;
            long length = Math.min(UdpPacket.MAX_PAYLOAD, ms.getLength() - index);

            packets[i] = new UdpPacket(EUdpPacketType.Data, ms, length);
            packets[i].getHeader().setMsgSize((int) ms.getLength());
        }

        sendSequenced(packets);
    }

    /**
     * Sends the packet as a sequenced, reliable packet.
     *
     * @param packet The packet.
     */
    private void sendSequenced(UdpPacket packet) {
        synchronized (outPackets) {
            packet.getHeader().setSeqThis(outSeq);
            packet.getHeader().setMsgStartSeq(outSeq);
            packet.getHeader().setPacketsInMsg(1);

            outPackets.add(packet);

            outSeq++;
        }
    }

    /**
     * Sends the packets as one sequenced, reliable net message.
     *
     * @param packets The packets that make up the single net message
     */
    private void sendSequenced(UdpPacket[] packets) {
        synchronized (outPackets) {
            int msgStart = outSeq;

            for (UdpPacket packet : packets) {
                sendSequenced(packet);

                // Correct for any assumptions made for the single-packet case.
                packet.getHeader().setPacketsInMsg(packets.length);
                packet.getHeader().setMsgStartSeq(msgStart);
            }
        }
    }

    /**
     * Sends a packet immediately.
     *
     * @param packet The packet.
     */
    private void sendPacket(UdpPacket packet) {
        packet.getHeader().setSourceConnID(SOURCE_CONN_ID);
        packet.getHeader().setDestConnID(remoteConnId);
        inSeqAcked = inSeq;
        packet.getHeader().setSeqAck(inSeqAcked);

        logger.debug(String.format("Sent -> %s Seq %d Ack %d; %d bytes; Message: %d bytes %d packets",
                packet.getHeader().getPacketType(), packet.getHeader().getSeqThis(), packet.getHeader().getSeqAck(),
                packet.getHeader().getPayloadSize(), packet.getHeader().getMsgSize(), packet.getHeader().getPacketsInMsg()));

        byte[] data = packet.getData();

        try {
            sock.send(new DatagramPacket(data, 0, data.length, currentEndPoint.getAddress(), currentEndPoint.getPort()));
        } catch (IOException e) {
            logger.debug("Critical socket failure", e);
            state.set(State.DISCONNECTING);
            return;
        }

        // If we've been idle but completely acked for more than two seconds, the next sent
        // packet will trip the resend detection. This fixes that.
        if (outSeqSent == outSeqAcked) {
            nextResend = System.currentTimeMillis() + RESEND_DELAY;
        }

        // Sending should generally carry on from the packet most recently sent, even if it was a
        // resend (who knows what else was lost).
        if (packet.getHeader().getSeqThis() > 0) {
            outSeqSent = packet.getHeader().getSeqThis();
        }
    }

    /**
     * Sends a datagram Ack, used when an Ack needs to be sent but there is no data response to piggy-back on.
     */
    private void sendAck() {
        sendPacket(new UdpPacket(EUdpPacketType.Datagram));
    }

    /**
     * Sends or resends sequenced messages, if necessary. Also responsible for throttling
     * the rate at which they are sent.
     */
    private void sendPendingMessages() {
        synchronized (outPackets) {
            if (System.currentTimeMillis() > nextResend && outSeqSent > outSeqAcked) {
                // If we can't clear the send queue during a Disconnect, clear out the pending messages
                if (state.get() == State.DISCONNECTING) {
                    outPackets.clear();
                }

                logger.debug("Sequenced packet resend required");

                // Don't send more than 3 (Steam behavior?)
                for (int i = 0; i < RESEND_COUNT && i < outPackets.size(); i++) {
                    sendPacket(outPackets.get(i));
                }

                nextResend = System.currentTimeMillis() + RESEND_DELAY;
            } else if (outSeqSent < outSeqAcked + AHEAD_COUNT) {
                // I've never seen Steam send more than 4 packets before it gets an Ack, so this limits the
                // number of sequenced packets that can be sent out at one time.
                for (int i = outSeqSent - outSeqAcked; i < AHEAD_COUNT && i < outPackets.size(); i++) {
                    sendPacket(outPackets.get(i));
                }
            }
        }
    }

    /**
     * Returns the number of message parts in the next message.
     *
     * @return Non-zero number of message parts if a message is ready to be handled, 0 otherwise
     */
    private int readyMessageParts() {
        UdpPacket packet;

        // Make sure that the first packet of the next message to handle is present
        packet = inPackets.get(inSeqHandled + 1);
        if (packet == null) {
            return 0;
        }

        // ...and if relevant, all subparts of the message too
        for (int i = 1; i < packet.getHeader().getPacketsInMsg(); i++) {
            if (!inPackets.containsKey(inSeqHandled + 1 + i)) {
                return 0;
            }
        }

        return packet.getHeader().getPacketsInMsg();
    }

    /**
     * Dispatches up to one message to the rest of SteamKit
     *
     * @return True if a message was dispatched, false otherwise
     */
    private boolean dispatchMessage() {
        int numPackets = readyMessageParts();

        if (numPackets == 0) {
            return false;
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (int i = 0; i < numPackets; i++) {
            UdpPacket packet;

            packet = inPackets.get(++inSeqHandled);
            inPackets.remove(inSeqHandled);

            try {
                baos.write(packet.getPayload().toByteArray());
            } catch (IOException ignored) {
            }
        }

        byte[] data = baos.toByteArray();

        logger.debug("Dispatchin message: " + data.length + " bytes");

        onNetMsgReceived(new NetMsgEventArgs(data, currentEndPoint));

        return true;
    }

    /**
     * Receives the packet, performs all sanity checks and then passes it along as necessary.
     *
     * @param packet The packet.
     */
    private void receivePacket(UdpPacket packet) {
        // Check for a malformed packet
        if (!packet.isValid()) {
            return;
        }

        if (remoteConnId > 0 && packet.getHeader().getSourceConnID() != remoteConnId) {
            return;
        }

        logger.debug(String.format("<- Recv'd %s Seq %d Ack %d; %d bytes; Message: %d bytes %d packets",
                packet.getHeader().getPacketType(), packet.getHeader().getSeqThis(), packet.getHeader().getSeqAck(),
                packet.getHeader().getPayloadSize(), packet.getHeader().getMsgSize(), packet.getHeader().getPacketsInMsg()));

        // Throw away any duplicate messages we've already received, making sure to
        // re-ack it in case it got lost.
        if (packet.getHeader().getPacketType() == EUdpPacketType.Data && packet.getHeader().getSeqThis() < inSeq) {
            sendAck();
            return;
        }

        // When we get a SeqAck, all packets with sequence numbers below that have been safely received by
        // the server; we are now free to remove our copies
        if (outSeqAcked < packet.getHeader().getSeqAck()) {
            outSeqAcked = packet.getHeader().getSeqAck();

            // outSeqSent can be less than this in a very rare case involving resent packets.
            if (outSeqSent < outSeqAcked) {
                outSeqSent = outSeqAcked;
            }

            outPackets.removeIf(udpPacket -> udpPacket.getHeader().getSeqThis() <= outSeqAcked);
            nextResend = System.currentTimeMillis() + RESEND_DELAY;
        }

        // inSeq should always be the latest value that we can ack, so advance it as far as is possible.
        if (packet.getHeader().getSeqThis() == inSeq + 1) {
            do {
                inSeq++;
            } while (inPackets.containsKey(inSeq + 1));
        }

        switch (packet.getHeader().getPacketType()) {
            case Challenge:
                receiveChallenge(packet);
                break;
            case Accept:
                receiveAccept(packet);
                break;
            case Data:
                receiveData(packet);
                break;
            case Disconnect:
                logger.debug("Disconnected by server");
                state.set(State.DISCONNECTED);
                return;
            case Datagram:
                break;
            default:
                logger.debug("Received unexpected packet type " + packet.getHeader().getPacketType());
                break;
        }
    }

    /**
     * Receives the challenge and responds with a Connect request
     *
     * @param packet The packet.
     */
    private void receiveChallenge(UdpPacket packet) {
        if (!state.compareAndSet(State.CHALLENGE_REQ_SENT, State.CONNECT_SENT)) {
            return;
        }
        try {
            ChallengeData cr = new ChallengeData();
            cr.deserialize(packet.getPayload());

            ConnectData cd = new ConnectData();
            cd.setChallengeValue(cr.getChallengeValue() ^ ConnectData.CHALLENGE_MASK);

            MemoryStream ms = new MemoryStream();
            cd.serialize(ms.asOutputStream());
            ms.seek(0, SeekOrigin.BEGIN);

            sendSequenced(new UdpPacket(EUdpPacketType.Connect, ms));

            inSeqHandled = packet.getHeader().getSeqThis();
        } catch (IOException e) {
            logger.debug(e);
        }
    }

    private void receiveAccept(UdpPacket packet) {
        if (!state.compareAndSet(State.CONNECT_SENT, State.CONNECTED)) {
            return;
        }

        logger.debug("Connection established");
        remoteConnId = packet.getHeader().getSourceConnID();
        inSeqHandled = packet.getHeader().getSeqThis();

        onConnected();
    }

    private void receiveData(UdpPacket packet) {
        // Data packets are unexpected if a valid connection has not been established
        if (state.get() != State.CONNECTED && state.get() != State.DISCONNECTING) {
            return;
        }

        // If we receive a packet that we've already processed (e.g. it got resent due to a lost ack)
        // or that is already waiting to be processed, do nothing.
        if (packet.getHeader().getSeqThis() <= inSeqHandled || inPackets.containsKey(packet.getHeader().getSeqThis())) {
            return;
        }

        inPackets.put(packet.getHeader().getSeqThis(), packet);

        //noinspection StatementWithEmptyBody
        while (dispatchMessage()) ;
    }

    /**
     * Processes incoming packets, maintains connection consistency, and oversees outgoing packets.
     */
    private class NetLoop implements Runnable {

        NetLoop(InetSocketAddress endPoint) {
            currentEndPoint = endPoint;
        }

        @Override
        public void run() {
            // Variables that will be used deeper in the function; locating them here avoids recreating
            // them since they don't need to be.
            boolean userRequestDisconnect = false;
            byte[] buf = new byte[2048];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            boolean received = false;

            try {
                sock.setSoTimeout(150);
            } catch (SocketException e) {
                logger.debug(e);
            }

            if (currentEndPoint != null) {
                timeout = System.currentTimeMillis() + TIMEOUT_DELAY;
                nextResend = System.currentTimeMillis() + RESEND_DELAY;

                if (!state.compareAndSet(State.DISCONNECTED, State.CHALLENGE_REQ_SENT)) {
                    state.set(State.DISCONNECTING);
                    userRequestDisconnect = true;
                } else {
                    // Begin by sending off the challenge request
                    sendPacket(new UdpPacket(EUdpPacketType.ChallengeReq));
                }
            }

            while (state.get() != State.DISCONNECTED) {
                try {
                    try {
                        sock.receive(packet);
                        received = true;
                    } catch (SocketTimeoutException e) {
                        if (System.currentTimeMillis() > timeout) {
                            logger.debug("Connection timed out", e);
                            state.set(State.DISCONNECTED);
                            break;
                        }
                    }

                    // By using a 10ms wait, we allow for multiple packets sent at the time to all be processed before moving on
                    // to processing output and therefore Acks (the more we process at the same time, the fewer acks we have to send)


                    sock.setSoTimeout(10);
                    while (received) {

                        // Ignore packets that aren't sent by the server we're connected to.
                        if (!packet.getAddress().equals(currentEndPoint.getAddress()) && packet.getPort() != packet.getPort()) {
                            continue;
                        }

                        timeout = System.currentTimeMillis() + TIMEOUT_DELAY;

                        MemoryStream ms = new MemoryStream(packet.getData());
                        UdpPacket udpPacket = new UdpPacket(ms);

                        receivePacket(udpPacket);
                        try {
                            sock.receive(packet);
                            //noinspection DataFlowIssue
                            received = true;
                        } catch (SocketTimeoutException e) {
                            received = false;
                        }
                    }
                } catch (IOException e) {
                    logger.debug("Exception while reading packer", e);
                    state.set(State.DISCONNECTED);
                    break;
                }

                // Send or resend any sequenced packets; a call to ReceivePacket can set our state to disconnected
                // so don't send anything we have queued in that case
                if (state.get() != State.DISCONNECTED) {
                    sendPendingMessages();
                }

                // If we received data but had no data to send back, we need to manually Ack (usually tags along with
                // outgoing data); also acks disconnections
                if (inSeq != inSeqAcked) {
                    sendAck();
                }

                // If a graceful shutdown has been requested, nothing in the outgoing queue is discarded.
                // Once it's empty, we exit, since the last packet was our disconnect notification.
                if (state.get() == State.DISCONNECTING && outPackets.isEmpty()) {
                    logger.debug("Graceful disconnect completed");
                    state.set(State.DISCONNECTED);
                    userRequestDisconnect = true;
                    break;
                }
            }

            sock.close();

            logger.debug("Calling onDisconnected");
            onDisconnected(userRequestDisconnect);
        }
    }

    private enum State {
        DISCONNECTED,
        CHALLENGE_REQ_SENT,
        CONNECT_SENT,
        CONNECTED,
        DISCONNECTING
    }
}
