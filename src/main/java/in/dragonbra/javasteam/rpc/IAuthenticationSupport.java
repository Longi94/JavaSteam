package in.dragonbra.javasteam.rpc;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesAuthSteamclient.*;
import in.dragonbra.javasteam.types.JobID;

/**
 * @author Lossy
 * @since 2023-01-07
 */
@SuppressWarnings("unused")
public interface IAuthenticationSupport {

    /* CAuthenticationSupport_QueryRefreshTokensByAccount_Response */
    JobID QueryRefreshTokensByAccount(CAuthenticationSupport_QueryRefreshTokensByAccount_Request request);

    /* CAuthenticationSupport_QueryRefreshTokenByID_Response */
    JobID QueryRefreshTokenByID(CAuthenticationSupport_QueryRefreshTokenByID_Request request);

    /* CAuthenticationSupport_RevokeToken_Response */
    JobID RevokeToken(CAuthenticationSupport_RevokeToken_Request request);

    /* CAuthenticationSupport_GetTokenHistory_Response */
    JobID GetTokenHistory(CAuthenticationSupport_GetTokenHistory_Request request);
}
