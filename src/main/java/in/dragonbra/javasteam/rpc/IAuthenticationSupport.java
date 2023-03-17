package in.dragonbra.javasteam.rpc;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesAuthSteamclient.*;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.callback.ServiceMethodResponse;
import in.dragonbra.javasteam.types.AsyncJobSingle;

/**
 * @author Lossy
 * @since 2023-01-07
 */
@SuppressWarnings("unused")
public interface IAuthenticationSupport {

    /* CAuthenticationSupport_QueryRefreshTokensByAccount_Response */
    AsyncJobSingle<ServiceMethodResponse> QueryRefreshTokensByAccount(CAuthenticationSupport_QueryRefreshTokensByAccount_Request request);

    /* CAuthenticationSupport_QueryRefreshTokenByID_Response */
    AsyncJobSingle<ServiceMethodResponse> QueryRefreshTokenByID(CAuthenticationSupport_QueryRefreshTokenByID_Request request);

    /* CAuthenticationSupport_RevokeToken_Response */
    AsyncJobSingle<ServiceMethodResponse> RevokeToken(CAuthenticationSupport_RevokeToken_Request request);

    /* CAuthenticationSupport_GetTokenHistory_Response */
    AsyncJobSingle<ServiceMethodResponse> GetTokenHistory(CAuthenticationSupport_GetTokenHistory_Request request);
}
