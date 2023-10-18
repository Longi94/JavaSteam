package in.dragonbra.javasteam.rpc.interfaces;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesAuthSteamclient.*;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.callback.ServiceMethodResponse;
import in.dragonbra.javasteam.types.AsyncJobSingle;

/**
 * @author Lossy
 * @since 2023-01-07
 */
@SuppressWarnings("unused")
public interface IAuthentication {

    /* CAuthentication_GetPasswordRSAPublicKey_Response */
    AsyncJobSingle<ServiceMethodResponse> GetPasswordRSAPublicKey(CAuthentication_GetPasswordRSAPublicKey_Request request);

    /* CAuthentication_BeginAuthSessionViaQR_Response */
    AsyncJobSingle<ServiceMethodResponse> BeginAuthSessionViaQR(CAuthentication_BeginAuthSessionViaQR_Request request);

    /* CAuthentication_BeginAuthSessionViaCredentials_Response */
    AsyncJobSingle<ServiceMethodResponse> BeginAuthSessionViaCredentials(CAuthentication_BeginAuthSessionViaCredentials_Request request);

    /* CAuthentication_PollAuthSessionStatus_Response */
    AsyncJobSingle<ServiceMethodResponse> PollAuthSessionStatus(CAuthentication_PollAuthSessionStatus_Request request);

    /* CAuthentication_GetAuthSessionInfo_Response */
    AsyncJobSingle<ServiceMethodResponse> GetAuthSessionInfo(CAuthentication_GetAuthSessionInfo_Request request);

    /* CAuthentication_UpdateAuthSessionWithMobileConfirmation_Response */
    AsyncJobSingle<ServiceMethodResponse> UpdateAuthSessionWithMobileConfirmation(CAuthentication_UpdateAuthSessionWithMobileConfirmation_Request request);

    /* CAuthentication_UpdateAuthSessionWithSteamGuardCode_Response */
    AsyncJobSingle<ServiceMethodResponse> UpdateAuthSessionWithSteamGuardCode(CAuthentication_UpdateAuthSessionWithSteamGuardCode_Request request);

    /* CAuthentication_AccessToken_GenerateForApp_Response */
    AsyncJobSingle<ServiceMethodResponse> GenerateAccessTokenForApp(CAuthentication_AccessToken_GenerateForApp_Request request);

    /* CAuthentication_GetAuthSessionsForAccount_Response */
    AsyncJobSingle<ServiceMethodResponse> GetAuthSessionsForAccount(CAuthentication_GetAuthSessionsForAccount_Request request);

    /* CAuthentication_MigrateMobileSession_Response */
    AsyncJobSingle<ServiceMethodResponse> MigrateMobileSession(CAuthentication_MigrateMobileSession_Request request);

    /* CAuthentication_Token_Revoke_Response */
    AsyncJobSingle<ServiceMethodResponse> RevokeToken(CAuthentication_Token_Revoke_Request request);

    /* CAuthentication_RefreshToken_Revoke_Response */
    AsyncJobSingle<ServiceMethodResponse> RevokeRefreshToken(CAuthentication_RefreshToken_Revoke_Request request);
}
