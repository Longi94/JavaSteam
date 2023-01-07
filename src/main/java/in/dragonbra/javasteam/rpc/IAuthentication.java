package in.dragonbra.javasteam.rpc;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesAuthSteamclient.*;
import in.dragonbra.javasteam.types.JobID;

/**
 * @author Lossy
 * @since 2023-01-07
 */
@SuppressWarnings("unused")
public interface IAuthentication {

    /* CAuthentication_GetPasswordRSAPublicKey_Response */
    JobID GetPasswordRSAPublicKey(CAuthentication_GetPasswordRSAPublicKey_Request request);

    /* CAuthentication_BeginAuthSessionViaQR_Response */
    JobID BeginAuthSessionViaQR(CAuthentication_BeginAuthSessionViaQR_Request request);

    /* CAuthentication_BeginAuthSessionViaCredentials_Response */
    JobID BeginAuthSessionViaCredentials(CAuthentication_BeginAuthSessionViaCredentials_Request request);

    /* CAuthentication_PollAuthSessionStatus_Response */
    JobID PollAuthSessionStatus(CAuthentication_PollAuthSessionStatus_Request request);

    /* CAuthentication_GetAuthSessionInfo_Response */
    JobID GetAuthSessionInfo(CAuthentication_GetAuthSessionInfo_Request request);

    /* CAuthentication_UpdateAuthSessionWithMobileConfirmation_Response */
    JobID UpdateAuthSessionWithMobileConfirmation(CAuthentication_UpdateAuthSessionWithMobileConfirmation_Request request);

    /* CAuthentication_UpdateAuthSessionWithSteamGuardCode_Response */
    JobID UpdateAuthSessionWithSteamGuardCode(CAuthentication_UpdateAuthSessionWithSteamGuardCode_Request request);

    /* CAuthentication_AccessToken_GenerateForApp_Response */
    JobID GenerateAccessTokenForApp(CAuthentication_AccessToken_GenerateForApp_Request request);

    /* CAuthentication_GetAuthSessionsForAccount_Response */
    JobID GetAuthSessionsForAccount(CAuthentication_GetAuthSessionsForAccount_Request request);

    /* CAuthentication_MigrateMobileSession_Response */
    JobID MigrateMobileSession(CAuthentication_MigrateMobileSession_Request request);
}
