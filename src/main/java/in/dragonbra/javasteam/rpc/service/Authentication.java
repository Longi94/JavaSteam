package in.dragonbra.javasteam.rpc.service;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesAuthSteamclient;
import in.dragonbra.javasteam.rpc.IAuthentication;
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
        return sendMessage(request);
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> BeginAuthSessionViaQR(SteammessagesAuthSteamclient.CAuthentication_BeginAuthSessionViaQR_Request request) {
        return sendMessage(request);
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> BeginAuthSessionViaCredentials(SteammessagesAuthSteamclient.CAuthentication_BeginAuthSessionViaCredentials_Request request) {
        return sendMessage(request);
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> PollAuthSessionStatus(SteammessagesAuthSteamclient.CAuthentication_PollAuthSessionStatus_Request request) {
        return sendMessage(request);
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> GetAuthSessionInfo(SteammessagesAuthSteamclient.CAuthentication_GetAuthSessionInfo_Request request) {
        return sendMessage(request);
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> UpdateAuthSessionWithMobileConfirmation(SteammessagesAuthSteamclient.CAuthentication_UpdateAuthSessionWithMobileConfirmation_Request request) {
        return sendMessage(request);
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> UpdateAuthSessionWithSteamGuardCode(SteammessagesAuthSteamclient.CAuthentication_UpdateAuthSessionWithSteamGuardCode_Request request) {
        return sendMessage(request);
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> GenerateAccessTokenForApp(SteammessagesAuthSteamclient.CAuthentication_AccessToken_GenerateForApp_Request request) {
        return sendMessage(request);
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> GetAuthSessionsForAccount(SteammessagesAuthSteamclient.CAuthentication_GetAuthSessionsForAccount_Request request) {
        return sendMessage(request);
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> MigrateMobileSession(SteammessagesAuthSteamclient.CAuthentication_MigrateMobileSession_Request request) {
        return sendMessage(request);
    }
}
