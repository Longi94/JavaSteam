package in.dragonbra.javasteam.steam.handlers.steamuser.callback;

import in.dragonbra.javasteam.enums.EResult;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverLogin.CMsgClientRequestWebAPIAuthenticateUserNonceResponse;
import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;
import in.dragonbra.javasteam.types.JobID;

/**
 * This callback is received when requesting a new WebAPI authentication user nonce.
 */
public class WebAPIUserNonceCallback extends CallbackMsg {

    private final EResult result;

    private final String nonce;

    public WebAPIUserNonceCallback(JobID jobID, CMsgClientRequestWebAPIAuthenticateUserNonceResponse.Builder body) {
        setJobID(jobID);

        result = EResult.from(body.getEresult());
        nonce = body.getWebapiAuthenticateUserNonce();
    }

    /**
     * @return the result of the request as {@link EResult}.
     */
    public EResult getResult() {
        return result;
    }

    /**
     * @return the authentication nonce.
     */
    public String getNonce() {
        return nonce;
    }
}
