package in.dragonbra.javasteam.rpc.service;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesTwofactorSteamclient;
import in.dragonbra.javasteam.rpc.interfaces.ITwoFactor;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.SteamUnifiedMessages;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.UnifiedService;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.callback.ServiceMethodResponse;
import in.dragonbra.javasteam.types.AsyncJobSingle;

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
    public AsyncJobSingle<ServiceMethodResponse> QueryStatus(SteammessagesTwofactorSteamclient.CTwoFactor_Status_Request request) {
        return sendMessage(request, "QueryStatus");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> AddAuthenticator(SteammessagesTwofactorSteamclient.CTwoFactor_AddAuthenticator_Request request) {
        return sendMessage(request, "AddAuthenticator");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> SendEmail(SteammessagesTwofactorSteamclient.CTwoFactor_SendEmail_Request request) {
        return sendMessage(request, "SendEmail");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> FinalizeAddAuthenticator(SteammessagesTwofactorSteamclient.CTwoFactor_FinalizeAddAuthenticator_Request request) {
        return sendMessage(request, "FinalizeAddAuthenticator");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> UpdateTokenVersion(SteammessagesTwofactorSteamclient.CTwoFactor_UpdateTokenVersion_Request request) {
        return sendMessage(request, "UpdateTokenVersion");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> RemoveAuthenticator(SteammessagesTwofactorSteamclient.CTwoFactor_RemoveAuthenticator_Request request) {
        return sendMessage(request, "RemoveAuthenticator");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> CreateEmergencyCodes(SteammessagesTwofactorSteamclient.CTwoFactor_CreateEmergencyCodes_Request request) {
        return sendMessage(request, "CreateEmergencyCodes");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> DestroyEmergencyCodes(SteammessagesTwofactorSteamclient.CTwoFactor_DestroyEmergencyCodes_Request request) {
        return sendMessage(request, "DestroyEmergencyCodes");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> ValidateToken(SteammessagesTwofactorSteamclient.CTwoFactor_ValidateToken_Request request) {
        return sendMessage(request, "ValidateToken");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> RemoveAuthenticatorViaChallengeStart(SteammessagesTwofactorSteamclient.CTwoFactor_RemoveAuthenticatorViaChallengeStart_Request request) {
        return sendMessage(request, "RemoveAuthenticatorViaChallengeStart");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> RemoveAuthenticatorViaChallengeContinue(SteammessagesTwofactorSteamclient.CTwoFactor_RemoveAuthenticatorViaChallengeContinue_Request request) {
        return sendMessage(request, "RemoveAuthenticatorViaChallengeContinue");
    }
}
