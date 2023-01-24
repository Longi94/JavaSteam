package in.dragonbra.javasteam.rpc.service;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesTwofactorSteamclient;
import in.dragonbra.javasteam.rpc.ITwoFactor;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.SteamUnifiedMessages;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.UnifiedService;
import in.dragonbra.javasteam.types.JobID;

/**
 * @author Lossy
 * @since 2023-01-06
 */
@SuppressWarnings("unused")
public class TwoFactor extends UnifiedService implements ITwoFactor {

    public TwoFactor(SteamUnifiedMessages steamUnifiedMessages) {
        super(steamUnifiedMessages);
    }

    @Override
    public JobID QueryStatus(SteammessagesTwofactorSteamclient.CTwoFactor_Status_Request request) {
        return sendMessage(request);
    }

    @Override
    public JobID AddAuthenticator(SteammessagesTwofactorSteamclient.CTwoFactor_AddAuthenticator_Request request) {
        return sendMessage(request);
    }

    @Override
    public JobID SendEmail(SteammessagesTwofactorSteamclient.CTwoFactor_SendEmail_Request request) {
        return sendMessage(request);
    }

    @Override
    public JobID FinalizeAddAuthenticator(SteammessagesTwofactorSteamclient.CTwoFactor_FinalizeAddAuthenticator_Request request) {
        return sendMessage(request);
    }

    @Override
    public JobID UpdateTokenVersion(SteammessagesTwofactorSteamclient.CTwoFactor_UpdateTokenVersion_Request request) {
        return sendMessage(request);
    }

    @Override
    public JobID RemoveAuthenticator(SteammessagesTwofactorSteamclient.CTwoFactor_RemoveAuthenticator_Request request) {
        return sendMessage(request);
    }

    @Override
    public JobID CreateEmergencyCodes(SteammessagesTwofactorSteamclient.CTwoFactor_CreateEmergencyCodes_Request request) {
        return sendMessage(request);
    }

    @Override
    public JobID DestroyEmergencyCodes(SteammessagesTwofactorSteamclient.CTwoFactor_DestroyEmergencyCodes_Request request) {
        return sendMessage(request);
    }

    @Override
    public JobID ValidateToken(SteammessagesTwofactorSteamclient.CTwoFactor_ValidateToken_Request request) {
        return sendMessage(request);
    }

    @Override
    public JobID RemoveAuthenticatorViaChallengeStart(SteammessagesTwofactorSteamclient.CTwoFactor_RemoveAuthenticatorViaChallengeStart_Request request) {
        return sendMessage(request);
    }

    @Override
    public JobID RemoveAuthenticatorViaChallengeContinue(SteammessagesTwofactorSteamclient.CTwoFactor_RemoveAuthenticatorViaChallengeContinue_Request request) {
        return sendMessage(request);
    }
}
