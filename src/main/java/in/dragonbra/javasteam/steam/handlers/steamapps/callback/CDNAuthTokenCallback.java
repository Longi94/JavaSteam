package in.dragonbra.javasteam.steam.handlers.steamapps.callback;

import in.dragonbra.javasteam.enums.EResult;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver2.CMsgClientGetCDNAuthTokenResponse;
import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;
import in.dragonbra.javasteam.types.JobID;

import java.util.Date;

/**
 * This callback is received when a CDN auth token is received
 */
public class CDNAuthTokenCallback extends CallbackMsg {

    private EResult result;

    private String token;

    private Date expiration;

    public CDNAuthTokenCallback(JobID jobID, CMsgClientGetCDNAuthTokenResponse.Builder msg) {
        setJobID(jobID);

        result = EResult.from(msg.getEresult());
        token = msg.getToken();
        expiration = new Date(msg.getExpirationTime() * 1000L);
    }

    /**
     * @return the result of the operation.
     */
    public EResult getResult() {
        return result;
    }

    /**
     * @return the CDN auth token.
     */
    public String getToken() {
        return token;
    }

    /**
     * @return the token expiration date.
     */
    public Date getExpiration() {
        return expiration;
    }
}
