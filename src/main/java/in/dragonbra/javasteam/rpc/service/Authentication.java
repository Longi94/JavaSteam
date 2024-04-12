package in.dragonbra.javasteam.rpc.service;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesAuthSteamclient;
import in.dragonbra.javasteam.rpc.interfaces.IAuthentication;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.SteamUnifiedMessages;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.UnifiedService;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.callback.ServiceMethodResponse;
import in.dragonbra.javasteam.types.AsyncJobSingle;
import org.jetbrains.annotations.NotNull;


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
    public AsyncJobSingle<ServiceMethodResponse> getPasswordRSAPublicKey(SteammessagesAuthSteamclient.CAuthentication_GetPasswordRSAPublicKey_Request request) {
        return sendMessage(request, "GetPasswordRSAPublicKey");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> beginAuthSessionViaQR(SteammessagesAuthSteamclient.CAuthentication_BeginAuthSessionViaQR_Request request) {
        return sendMessage(request, "BeginAuthSessionViaQR");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> beginAuthSessionViaCredentials(SteammessagesAuthSteamclient.CAuthentication_BeginAuthSessionViaCredentials_Request request) {
        return sendMessage(request, "BeginAuthSessionViaCredentials");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> pollAuthSessionStatus(SteammessagesAuthSteamclient.CAuthentication_PollAuthSessionStatus_Request request) {
        return sendMessage(request, "PollAuthSessionStatus");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> getAuthSessionInfo(SteammessagesAuthSteamclient.CAuthentication_GetAuthSessionInfo_Request request) {
        return sendMessage(request, "GetAuthSessionInfo");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> updateAuthSessionWithMobileConfirmation(SteammessagesAuthSteamclient.CAuthentication_UpdateAuthSessionWithMobileConfirmation_Request request) {
        return sendMessage(request, "UpdateAuthSessionWithMobileConfirmation");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> updateAuthSessionWithSteamGuardCode(SteammessagesAuthSteamclient.CAuthentication_UpdateAuthSessionWithSteamGuardCode_Request request) {
        return sendMessage(request, "UpdateAuthSessionWithSteamGuardCode");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> generateAccessTokenForApp(SteammessagesAuthSteamclient.CAuthentication_AccessToken_GenerateForApp_Request request) {
        return sendMessage(request, "GenerateAccessTokenForApp");
    }

    @NotNull
    @Override
    public AsyncJobSingle<ServiceMethodResponse> enumerateTokens(@NotNull SteammessagesAuthSteamclient.CAuthentication_RefreshToken_Enumerate_Request request) {
        return sendMessage(request, "EnumerateTokens");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> getAuthSessionsForAccount(SteammessagesAuthSteamclient.CAuthentication_GetAuthSessionsForAccount_Request request) {
        return sendMessage(request, "GetAuthSessionsForAccount");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> migrateMobileSession(SteammessagesAuthSteamclient.CAuthentication_MigrateMobileSession_Request request) {
        return sendMessage(request, "MigrateMobileSession");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> revokeToken(SteammessagesAuthSteamclient.CAuthentication_Token_Revoke_Request request) {
        return sendMessage(request, "RevokeToken");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> revokeRefreshToken(SteammessagesAuthSteamclient.CAuthentication_RefreshToken_Revoke_Request request) {
        return sendMessage(request, "RevokeRefreshToken");
    }
}
