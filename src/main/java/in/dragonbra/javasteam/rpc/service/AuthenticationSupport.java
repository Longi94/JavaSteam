package in.dragonbra.javasteam.rpc.service;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesAuthSteamclient;
import in.dragonbra.javasteam.rpc.IAuthenticationSupport;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.SteamUnifiedMessages;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.UnifiedService;
import in.dragonbra.javasteam.types.JobID;

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
    public JobID QueryRefreshTokensByAccount(SteammessagesAuthSteamclient.CAuthenticationSupport_QueryRefreshTokensByAccount_Request request) {
        return sendMessage(request);
    }

    @Override
    public JobID QueryRefreshTokenByID(SteammessagesAuthSteamclient.CAuthenticationSupport_QueryRefreshTokenByID_Request request) {
        return sendMessage(request);
    }

    @Override
    public JobID RevokeToken(SteammessagesAuthSteamclient.CAuthenticationSupport_RevokeToken_Request request) {
        return sendMessage(request);
    }

    @Override
    public JobID GetTokenHistory(SteammessagesAuthSteamclient.CAuthenticationSupport_GetTokenHistory_Request request) {
        return sendMessage(request);
    }
}
