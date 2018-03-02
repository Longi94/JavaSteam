package in.dragonbra.javasteam.networking.steam3;

import in.dragonbra.javasteam.util.log.LogManager;
import in.dragonbra.javasteam.util.log.Logger;
import in.dragonbra.javasteam.util.stream.BinaryReader;
import in.dragonbra.javasteam.util.stream.BinaryWriter;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * @author lngtr
 * @since 2018-02-21
 */
public class TcpConnection extends Connection {

    private static final Logger logger = LogManager.getLogger(TcpConnection.class);

    private static final int MAGIC = 0x31305456; // "VT01"

    private InetSocketAddress destination;

    private Socket socket;

    private InetSocketAddress currentEndPoint;

    private BinaryWriter netWriter;

    private BinaryReader netReader;

    private Thread netThread;

    private NetLoop netLoop;

    private final Object netLock = new Object();

    private void shutdown() {
        try {
            if (socket.isConnected()) {
                socket.shutdownInput();
                socket.shutdownOutput();
            }
        } catch (IOException e) {
            logger.debug(e);
        }
    }

    private void connectionCompleted(boolean success) {
        if (!success) {
            logger.debug("Timed out while connecting to " + destination);
            release(false);
            return;
        }

        logger.debug("Connected to " + destination);

        try {
            synchronized (netLock) {
                netReader = new BinaryReader(socket.getInputStream());
                netWriter = new BinaryWriter(socket.getOutputStream());

                netLoop = new NetLoop();
                netThread = new Thread(netLoop, "TcpConnection Thread");

                currentEndPoint = new InetSocketAddress(socket.getInetAddress(), socket.getPort());
            }

            netThread.start();

            onConnected();
        } catch (IOException e) {
            logger.debug("Exception while setting up connection to " + destination, e);
            release(false);
        }
    }

    private void release(boolean userRequestedDisconnect) {
        synchronized (netLock) {
            if (netWriter != null) {
                try {
                    netWriter.close();
                } catch (IOException ignored) {
                }
                netWriter = null;
            }

            if (netReader != null) {
                try {
                    netReader.close();
                } catch (IOException ignored) {
                }
                netReader = null;
            }

            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException ignored) {
                }
                socket = null;
            }
        }

        onDisconnected(userRequestedDisconnect);
    }

    @Override
    public void connect(InetSocketAddress endPoint) {
        synchronized (netLock) {
            try {
                logger.debug("Connecting to " + endPoint + "...");
                socket = new Socket(endPoint.getAddress(), endPoint.getPort());

                this.destination = endPoint;

                connectionCompleted(true);
            } catch (IOException e) {
                logger.debug("Socket exception while completing connection request to " + destination, e);
                connectionCompleted(false);
            }
        }
    }

    @Override
    public void disconnect() {
        synchronized (netLock) {
            netLoop.stop();
        }
    }

    private byte[] readPacket() throws IOException {
        int packetLen = netReader.readInt();
        int packetMagic = netReader.readInt();

        if (packetMagic != MAGIC) {
            throw new IOException("Got a packet with invalid magic!");
        }

        return netReader.readBytes(packetLen);
    }

    @Override
    public void send(byte[] data) {
        synchronized (netLock) {
            if (socket == null) {
                logger.debug("Attempting to send client data when not connected.");
                return;
            }

            try {
                netWriter.writeInt(data.length);
                netWriter.writeInt(MAGIC);
                netWriter.write(data);
            } catch (IOException e) {
                logger.debug("Socket exception while writing data.", e);
            }
        }
    }

    @Override
    public InetAddress getLocalIP() {
        synchronized (netLock) {
            if (socket == null) {
                return null;
            }

            return socket.getLocalAddress();
        }
    }

    @Override
    public InetSocketAddress getCurrentEndPoint() {
        return currentEndPoint;
    }

    @Override
    public ProtocolTypes getProtocolTypes() {
        return ProtocolTypes.TCP;
    }

    // this is now a steamkit meme

    /**
     * Nets the loop.
     */
    private class NetLoop implements Runnable {
        private static final int POLL_MS = 100;

        private volatile boolean cancelRequested = false;

        void stop() {
            cancelRequested = true;
        }

        @Override
        public void run() {
            while (!cancelRequested) {
                try {
                    Thread.sleep(POLL_MS);
                } catch (InterruptedException e) {
                    logger.debug("Thread interrupted", e);
                }

                if (cancelRequested) {
                    break;
                }

                boolean canRead;

                try {
                    canRead = netReader.available() > 0;
                } catch (IOException e) {
                    logger.debug("Socket exception while polling", e);
                    break;
                }

                if (!canRead) {
                    // nothing to read yet
                    continue;
                }

                byte[] packData;

                try {
                    packData = readPacket();

                    onNetMsgReceived(new NetMsgEventArgs(packData, destination));
                } catch (IOException e) {
                    logger.debug("Socket exception occurred while reading packet", e);
                    break;
                }
            }

            if (cancelRequested) {
                shutdown();
            }
            release(cancelRequested);
        }
    }
}
