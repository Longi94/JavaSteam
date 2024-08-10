package in.dragonbra.javasteam.steam.handlers.steammasterserver;

import in.dragonbra.javasteam.enums.ERegionCode;

import java.net.InetAddress;

/**
 * Details used when performing a server list query.
 */
public class QueryDetails {

    private int appID;

    private String filter;

    private ERegionCode region;

    private InetAddress geoLocatedIP;

    private int maxServers;

    /**
     * @return the AppID used when querying servers
     */
    public int getAppID() {
        return appID;
    }

    /**
     * @param appID the AppID used when querying servers
     */
    public void setAppID(int appID) {
        this.appID = appID;
    }

    /**
     * Check <a href="https://developer.valvesoftware.com/wiki/Master_Server_Query_Protocol">https://developer.valvesoftware.com/wiki/Master_Server_Query_Protocol</a> for details on how the filter is structured.
     *
     * @return the filter used for querying the master server
     */
    public String getFilter() {
        return filter;
    }

    /**
     * Check <a href="https://developer.valvesoftware.com/wiki/Master_Server_Query_Protocol">https://developer.valvesoftware.com/wiki/Master_Server_Query_Protocol</a> for details on how the filter is structured.
     *
     * @param filter the filter used for querying the master server
     */
    public void setFilter(String filter) {
        this.filter = filter;
    }

    /**
     * @return the region that servers will be returned from
     */
    public ERegionCode getRegion() {
        return region;
    }

    /**
     * @param region the region that servers will be returned from
     */
    public void setRegion(ERegionCode region) {
        this.region = region;
    }

    /**
     * This is done to return servers closer to this location.
     *
     * @return the IP address that will be GeoIP located
     */
    public InetAddress getGeoLocatedIP() {
        return geoLocatedIP;
    }

    /**
     * This is done to return servers closer to this location.
     *
     * @param geoLocatedIP the IP address that will be GeoIP located
     */
    public void setGeoLocatedIP(InetAddress geoLocatedIP) {
        this.geoLocatedIP = geoLocatedIP;
    }

    /**
     * @return the maximum number of servers to return
     */
    public int getMaxServers() {
        return maxServers;
    }

    /**
     * @param maxServers the maximum number of servers to return
     */
    public void setMaxServers(int maxServers) {
        this.maxServers = maxServers;
    }
}
