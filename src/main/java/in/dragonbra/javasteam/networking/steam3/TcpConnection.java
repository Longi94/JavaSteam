package in.dragonbra.javasteam.networking.steam3;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.DataInputStream;
import java.io.DataOutputStream;
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

    private DataOutputStream netWriter;

    private DataInputStream netReader;

    private Thread netThread;

    private NetLoop netLoop;

    private final Object netLock = new Object();

    private void shutdown() {
        try {
            socket.shutdownInput();
            socket.shutdownOutput();
        } catch (IOException e) {
            logger.debug(e);
        }
    }

    private void tryConnect(int timeout) throws IOException {
        socket.connect(new InetSocketAddress(destination.getAddress(), destination.getPort()), timeout);
    }

    private void connectionCompleted(boolean success) throws IOException {
        if (!success) {
            logger.debug("Timed out while connecting to " + destination);
            release(false);
            return;
        }

        logger.debug("Connected to " + destination);

        try {
            synchronized (netLock) {
                netReader = new DataInputStream(socket.getInputStream());
                netWriter = new DataOutputStream(socket.getOutputStream());

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

    private void release(boolean userRequestedDisconnect) throws IOException {
        synchronized (netLock) {
            if (netWriter != null) {
                netWriter.close();
                netWriter = null;
            }

            if (netReader != null) {
                netReader.close();
                netReader = null;
            }

            if (socket != null) {
                socket.close();
                socket = null;
            }
        }

        onDisconnected(userRequestedDisconnect);
    }

    @Override
    public void connect(InetSocketAddress endPoint, long timeout) {
        synchronized (netLock) {
            try {
                socket = new Socket(endPoint.getAddress(), endPoint.getPort());
                socket.setSoTimeout((int) timeout);

                this.destination = endPoint;

                logger.debug("Connecting to " + destination + "...");
            } catch (IOException e) {
                logger.debug(e);
            }
        }
    }

    @Override
    public void disconnect() {
        synchronized (netLock) {
            netLoop.stop();

            try {
                netThread.join();
            } catch (InterruptedException e) {
                logger.debug(e);
            }

            onDisconnected(true);
        }
    }

    private byte[] readPacket() throws IOException {
        int packetLen = netReader.readInt();
        int packetMagic = netReader.readInt();

        if (packetMagic != MAGIC) {
            throw new IOException("Got a packet with invalid magic!");
        }

        byte[] packData = new byte[packetLen];
        netReader.readFully(packData, 0, packData.length);

        return packData;
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
            while (true) {
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

            try {
                shutdown();
                release(false);
            } catch (IOException e) {
                logger.debug(e);
            }
        }
    }
}
