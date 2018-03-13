package in.dragonbra.javasteam.steam.steamclient.configuration;

import in.dragonbra.javasteam.enums.EClientPersonaStateFlag;
import in.dragonbra.javasteam.enums.EUniverse;
import in.dragonbra.javasteam.networking.steam3.ProtocolTypes;
import in.dragonbra.javasteam.steam.discovery.IServerListProvider;

import java.util.EnumSet;

/**
 * @author lngtr
 * @since 2018-02-20
 */
public class SteamConfigurationState {

    private boolean allowDirectoryFetch;
    private int cellID;
    private long connectionTimeout;
    private EnumSet<EClientPersonaStateFlag> defaultPersonaStateFlags;
    private EnumSet<ProtocolTypes> protocolTypes;
    private IServerListProvider serverListProvider;
    private EUniverse universe;
    private String webAPIBaseAddress;
    private String webAPIKey;

    public boolean isAllowDirectoryFetch() {
        return allowDirectoryFetch;
    }

    public void setAllowDirectoryFetch(boolean allowDirectoryFetch) {
        this.allowDirectoryFetch = allowDirectoryFetch;
    }

    public int getCellID() {
        return cellID;
    }

    public void setCellID(int cellID) {
        this.cellID = cellID;
    }

    public long getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(long connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public EnumSet<EClientPersonaStateFlag> getDefaultPersonaStateFlags() {
        return defaultPersonaStateFlags;
    }

    public void setDefaultPersonaStateFlags(EnumSet<EClientPersonaStateFlag> defaultPersonaStateFlags) {
        this.defaultPersonaStateFlags = defaultPersonaStateFlags;
    }

    public void setDefaultPersonaStateFlags(EClientPersonaStateFlag defaultPersonaStateFlags) {
        setDefaultPersonaStateFlags(EnumSet.of(defaultPersonaStateFlags));
    }

    public EnumSet<ProtocolTypes> getProtocolTypes() {
        return protocolTypes;
    }

    public void setProtocolTypes(ProtocolTypes protocolTypes) {
        setProtocolTypes(EnumSet.of(protocolTypes));
    }

    public void setProtocolTypes(EnumSet<ProtocolTypes> protocolTypes) {
        this.protocolTypes = protocolTypes;
    }

    public IServerListProvider getServerListProvider() {
        return serverListProvider;
    }

    public void setServerListProvider(IServerListProvider serverListProvider) {
        this.serverListProvider = serverListProvider;
    }

    public EUniverse getUniverse() {
        return universe;
    }

    public void setUniverse(EUniverse universe) {
        this.universe = universe;
    }

    public String getWebAPIBaseAddress() {
        return webAPIBaseAddress;
    }

    public void setWebAPIBaseAddress(String webAPIBaseAddress) {
        this.webAPIBaseAddress = webAPIBaseAddress;
    }

    public String getWebAPIKey() {
        return webAPIKey;
    }

    public void setWebAPIKey(String webAPIKey) {
        this.webAPIKey = webAPIKey;
    }
}
