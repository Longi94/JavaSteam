package in.dragonbra.javasteam.steam.discovery;

import in.dragonbra.javasteam.networking.steam3.ProtocolTypes;
import in.dragonbra.javasteam.protobufs.steam.discovery.BasicServerListProtos.BasicServer;
import in.dragonbra.javasteam.protobufs.steam.discovery.BasicServerListProtos.BasicServerList;
import in.dragonbra.javasteam.util.log.LogManager;
import in.dragonbra.javasteam.util.log.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Server provider that stores servers in a file using protobuf.
 */
public class FileServerListProvider implements IServerListProvider {

    private static final Logger logger = LogManager.getLogger(FileServerListProvider.class);

    private File file;

    /**
     * Instantiates a {@link FileServerListProvider} object.
     *
     * @param file the file that will store the servers
     */
    public FileServerListProvider(File file) {
        if (file == null) {
            throw new IllegalArgumentException("file is null");
        }
        this.file = file;

        try {
            if (!file.exists()) {
                file.getAbsoluteFile().getParentFile().mkdirs();
                file.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<ServerRecord> fetchServerList() {
        try (FileInputStream fis = new FileInputStream(file)) {
            List<ServerRecord> records = new ArrayList<>();
            BasicServerList serverList = BasicServerList.parseFrom(fis);
            for (int i = 0; i < serverList.getServersCount(); i++) {
                BasicServer server = serverList.getServers(i);
                records.add(ServerRecord.createServer(
                        server.getAddress(),
                        server.getPort(),
                        ProtocolTypes.from(server.getProtocol())
                ));
            }

            fis.close();

            return records;
        } catch (FileNotFoundException e) {
            logger.debug("servers list file not found");
        } catch (IOException e) {
            logger.debug("Failed to read server list file " + file.getAbsolutePath());
        }
        return null;
    }

    @Override
    public void updateServerList(List<ServerRecord> endpoints) {
        BasicServerList.Builder builder = BasicServerList.newBuilder();

        for (ServerRecord endpoint : endpoints) {
            builder.addServers(
                    BasicServer.newBuilder()
                            .setAddress(endpoint.getHost())
                            .setPort(endpoint.getPort())
                            .setProtocol(ProtocolTypes.code(endpoint.getProtocolTypes()))
            );
        }

        try (FileOutputStream fos = new FileOutputStream(file, false)) {
            builder.build().writeTo(fos);
            fos.flush();
        } catch (IOException e) {
            logger.debug("Failed to write servers to file " + file.getAbsolutePath(), e);
        }
    }
}
