package in.dragonbra.javasteam.networking.steam3;

import in.dragonbra.javasteam.util.log.LogManager;
import in.dragonbra.javasteam.util.log.Logger;
import in.dragonbra.javasteam.util.stream.BinaryReader;
import in.dragonbra.javasteam.util.stream.BinaryWriter;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;


/**
 * @author jaymie
 * @since 2025-03-14
 */
public class Socks5Connection extends Connection {

    private static final Logger logger = LogManager.getLogger(Socks5Connection.class);

    private static final int MAGIC = 0x31305456; // "VT01"

    private Socket socket;

    private InetSocketAddress currentEndPoint;

    private BinaryWriter netWriter;

    private BinaryReader netReader;

    @SuppressWarnings("FieldCanBeLocal")
    private Thread netThread;

    private NetLoop netLoop;

    private final Object netLock = new Object();

    private final Proxy proxy;

    public Socks5Connection(Proxy proxy) {
        this.proxy = proxy;
    }

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
            logger.debug("Timed out while connecting to " + currentEndPoint);
            release(false);
            return;
        }

        logger.debug("Connected to " + currentEndPoint);

        try {
            synchronized (netLock) {
                netReader = new BinaryReader(socket.getInputStream());
                netWriter = new BinaryWriter(socket.getOutputStream());

                netLoop = new NetLoop();
                netThread = new Thread(netLoop, "Socks5Connection Thread");

                currentEndPoint = new InetSocketAddress(socket.getInetAddress(), socket.getPort());
            }

            netThread.start();

            onConnected();
        } catch (IOException e) {
            logger.debug("Exception while setting up connection to " + currentEndPoint, e);
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
    public void connect(InetSocketAddress endPoint, int timeout) {
        synchronized (netLock) {
            currentEndPoint = endPoint;
            try {
                logger.debug(String.format("use proxy [%s:/%s] Connecting to %s...", proxy.type(), proxy.address(), currentEndPoint));
                Socket underlying = new Socket(new Proxy(Proxy.Type.SOCKS, proxy.address()));
                underlying.connect(endPoint,timeout);
                socket = underlying;

                connectionCompleted(true);
            } catch (Exception e) {
                logger.debug("Socket exception while completing connection request to " + currentEndPoint, e);
                connectionCompleted(false);
            }
        }
    }

    /**
     * Disconnects this instance.
     *
     * @param userInitiated
     */
    @Override
    public void disconnect(boolean userInitiated) {
        synchronized (netLock) {
            if (netLoop != null) {
                netLoop.stop(userInitiated);
            }
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

                // looks like the only way to detect a closed connection is to try and write to it
                // afaik read also throws an exception if the connection is open but there is nothing to read
                if (netLoop != null) {
                    netLoop.stop(false);
                }
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

        private volatile boolean userRequested = false;

        void stop(boolean userRequested) {
            this.userRequested = userRequested;
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

                    onNetMsgReceived(new NetMsgEventArgs(packData, currentEndPoint));
                } catch (IOException e) {
                    logger.debug("Socket exception occurred while reading packet", e);
                    break;
                }
            }

            if (cancelRequested) {
                shutdown();
            }
            release(cancelRequested && userRequested);
        }
    }
}
