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
    public AsyncJobSingle<ServiceMethodResponse> queryTime(SteammessagesTwofactorSteamclient.CTwoFactor_Time_Request request) {
        return sendMessage(request, "QueryTime");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> queryStatus(SteammessagesTwofactorSteamclient.CTwoFactor_Status_Request request) {
        return sendMessage(request, "QueryStatus");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> addAuthenticator(SteammessagesTwofactorSteamclient.CTwoFactor_AddAuthenticator_Request request) {
        return sendMessage(request, "AddAuthenticator");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> sendEmail(SteammessagesTwofactorSteamclient.CTwoFactor_SendEmail_Request request) {
        return sendMessage(request, "SendEmail");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> finalizeAddAuthenticator(SteammessagesTwofactorSteamclient.CTwoFactor_FinalizeAddAuthenticator_Request request) {
        return sendMessage(request, "FinalizeAddAuthenticator");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> updateTokenVersion(SteammessagesTwofactorSteamclient.CTwoFactor_UpdateTokenVersion_Request request) {
        return sendMessage(request, "UpdateTokenVersion");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> removeAuthenticator(SteammessagesTwofactorSteamclient.CTwoFactor_RemoveAuthenticator_Request request) {
        return sendMessage(request, "RemoveAuthenticator");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> removeAuthenticatorViaChallengeStart(SteammessagesTwofactorSteamclient.CTwoFactor_RemoveAuthenticatorViaChallengeStart_Request request) {
        return sendMessage(request, "RemoveAuthenticatorViaChallengeStart");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> removeAuthenticatorViaChallengeContinue(SteammessagesTwofactorSteamclient.CTwoFactor_RemoveAuthenticatorViaChallengeContinue_Request request) {
        return sendMessage(request, "RemoveAuthenticatorViaChallengeContinue");
    }
}
