package in.dragonbra.javasteam.steam.discovery;

import in.dragonbra.javasteam.networking.steam3.ProtocolTypes;

import java.util.Date;

/**
 * @author lngtr
 * @since 2018-02-20
 */
public class ServerInfo {
    private ServerRecord record;

    private ProtocolTypes protocol;

    private Date lastBadConnection;

    public ServerInfo(ServerRecord record, ProtocolTypes protocol) {
        this.record = record;
        this.protocol = protocol;
    }

    public ServerRecord getRecord() {
        return record;
    }

    public ProtocolTypes getProtocol() {
        return protocol;
    }

    public Date getLastBadConnection() {
        return lastBadConnection;
    }

    public void setLastBadConnection(Date lastBadConnection) {
        this.lastBadConnection = lastBadConnection;
    }
}
