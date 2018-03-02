package in.dragonbra.javasteam.util;

import in.dragonbra.javasteam.enums.EMsg;
import in.dragonbra.javasteam.util.log.LogManager;
import in.dragonbra.javasteam.util.log.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Dump any network messages sent to and received from the Steam server that the client is connected to.
 * These messages are dumped to file, and can be analyzed further with NetHookAnalyzer, a hex editor, or your own purpose-built tools.
 *
 * Be careful with this, sensitive data may be written to the disk (such as your Steam password).
 */
public class NetHookNetworkListener implements IDebugNetworkListener {
    private static final Logger logger = LogManager.getLogger(NetHookNetworkListener.class);

    private static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy_MM_dd_H_m_s_S");

    private AtomicLong messageNumber = new AtomicLong(0L);

    private File logDirectory;

    public NetHookNetworkListener() {
        this("netlogs");
    }

    public NetHookNetworkListener(String path) {

        File dir = new File(path);
        dir.mkdir();

        logDirectory = new File(dir, FORMAT.format(new Date()));
        logDirectory.mkdir();
    }

    @Override
    public void onIncomingNetworkMessage(EMsg msgType, byte[] data) {
        try {
            Files.write(Paths.get(new File(logDirectory, getFile("in", msgType)).getAbsolutePath()), data);
        } catch (IOException e) {
            logger.debug(e);
        }
    }

    @Override
    public void onOutgoingNetworkMessage(EMsg msgType, byte[] data) {
        try {
            Files.write(Paths.get(new File(logDirectory, getFile("out", msgType)).getAbsolutePath()), data);
        } catch (IOException e) {
            logger.debug(e);
        }
    }

    private String getFile(String direction, EMsg msgType) {
        return String.format("%d_%s_%d_k_EMsg%s.bin", messageNumber.getAndIncrement(), direction, msgType.code(), msgType.toString());
    }
}
