package in.dragonbra.javasteam.rpc.service;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesAuthSteamclient;
import in.dragonbra.javasteam.rpc.interfaces.IAuthentication;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.SteamUnifiedMessages;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.UnifiedService;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.callback.ServiceMethodResponse;
import in.dragonbra.javasteam.types.AsyncJobSingle;


/**
 * @author Lossy
 * @since 2023-01-07
 */
@SuppressWarnings("unused")
public class Authentication extends UnifiedService implements IAuthentication {

    public Authentication(SteamUnifiedMessages steamUnifiedMessages) {
        super(steamUnifiedMessages);
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> GetPasswordRSAPublicKey(SteammessagesAuthSteamclient.CAuthentication_GetPasswordRSAPublicKey_Request request) {
        return sendMessage(request, "GetPasswordRSAPublicKey");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> BeginAuthSessionViaQR(SteammessagesAuthSteamclient.CAuthentication_BeginAuthSessionViaQR_Request request) {
        return sendMessage(request, "BeginAuthSessionViaQR");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> BeginAuthSessionViaCredentials(SteammessagesAuthSteamclient.CAuthentication_BeginAuthSessionViaCredentials_Request request) {
        return sendMessage(request, "BeginAuthSessionViaCredentials");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> PollAuthSessionStatus(SteammessagesAuthSteamclient.CAuthentication_PollAuthSessionStatus_Request request) {
        return sendMessage(request, "PollAuthSessionStatus");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> GetAuthSessionInfo(SteammessagesAuthSteamclient.CAuthentication_GetAuthSessionInfo_Request request) {
        return sendMessage(request, "GetAuthSessionInfo");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> UpdateAuthSessionWithMobileConfirmation(SteammessagesAuthSteamclient.CAuthentication_UpdateAuthSessionWithMobileConfirmation_Request request) {
        return sendMessage(request, "UpdateAuthSessionWithMobileConfirmation");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> UpdateAuthSessionWithSteamGuardCode(SteammessagesAuthSteamclient.CAuthentication_UpdateAuthSessionWithSteamGuardCode_Request request) {
        return sendMessage(request, "UpdateAuthSessionWithSteamGuardCode");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> GenerateAccessTokenForApp(SteammessagesAuthSteamclient.CAuthentication_AccessToken_GenerateForApp_Request request) {
        return sendMessage(request, "GenerateAccessTokenForApp");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> GetAuthSessionsForAccount(SteammessagesAuthSteamclient.CAuthentication_GetAuthSessionsForAccount_Request request) {
        return sendMessage(request, "GetAuthSessionsForAccount");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> MigrateMobileSession(SteammessagesAuthSteamclient.CAuthentication_MigrateMobileSession_Request request) {
        return sendMessage(request, "MigrateMobileSession");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> RevokeToken(SteammessagesAuthSteamclient.CAuthentication_Token_Revoke_Request request) {
        return sendMessage(request, "RevokeToken");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> RevokeRefreshToken(SteammessagesAuthSteamclient.CAuthentication_RefreshToken_Revoke_Request request) {
        return sendMessage(request, "RevokeRefreshToken");
    }
}