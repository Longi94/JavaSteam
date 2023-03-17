package in.dragonbra.javasteam.rpc.service;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesAuthSteamclient;
import in.dragonbra.javasteam.rpc.IAuthenticationSupport;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.SteamUnifiedMessages;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.UnifiedService;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.callback.ServiceMethodResponse;
import in.dragonbra.javasteam.types.AsyncJobSingle;

/**
 * @author Lossy
 * @since 2023-01-07
 */
@SuppressWarnings("unused")
public class AuthenticationSupport extends UnifiedService implements IAuthenticationSupport {

    public AuthenticationSupport(SteamUnifiedMessages steamUnifiedMessages) {
        super(steamUnifiedMessages);
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> QueryRefreshTokensByAccount(SteammessagesAuthSteamclient.CAuthenticationSupport_QueryRefreshTokensByAccount_Request request) {
        return sendMessage(request);
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> QueryRefreshTokenByID(SteammessagesAuthSteamclient.CAuthenticationSupport_QueryRefreshTokenByID_Request request) {
        return sendMessage(request);
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> RevokeToken(SteammessagesAuthSteamclient.CAuthenticationSupport_RevokeToken_Request request) {
        return sendMessage(request);
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> GetTokenHistory(SteammessagesAuthSteamclient.CAuthenticationSupport_GetTokenHistory_Request request) {
        return sendMessage(request);
    }
}
