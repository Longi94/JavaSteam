package in.dragonbra.javasteam.rpc.interfaces;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesTwofactorSteamclient.*;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.callback.ServiceMethodResponse;
import in.dragonbra.javasteam.types.AsyncJobSingle;

/**
 * @author Lossy
 * @since 2023-01-06
 */
@SuppressWarnings("unused")
public interface ITwoFactor {

    /* CTwoFactor_Time_Response */
    AsyncJobSingle<ServiceMethodResponse> QueryTime(CTwoFactor_Time_Request request);

    /* CTwoFactor_Status_Response */
    AsyncJobSingle<ServiceMethodResponse> QueryStatus(CTwoFactor_Status_Request request);

    /* CTwoFactor_AddAuthenticator_Response */
    AsyncJobSingle<ServiceMethodResponse> AddAuthenticator(CTwoFactor_AddAuthenticator_Request request);

    /* CTwoFactor_SendEmail_Response */
    AsyncJobSingle<ServiceMethodResponse> SendEmail(CTwoFactor_SendEmail_Request request);

    /* CTwoFactor_FinalizeAddAuthenticator_Response */
    AsyncJobSingle<ServiceMethodResponse> FinalizeAddAuthenticator(CTwoFactor_FinalizeAddAuthenticator_Request request);

    /* CTwoFactor_UpdateTokenVersion_Response */
    AsyncJobSingle<ServiceMethodResponse> UpdateTokenVersion(CTwoFactor_UpdateTokenVersion_Request request);

    /* CTwoFactor_RemoveAuthenticator_Response */
    AsyncJobSingle<ServiceMethodResponse> RemoveAuthenticator(CTwoFactor_RemoveAuthenticator_Request request);

    /* CTwoFactor_RemoveAuthenticatorViaChallengeStart_Response */
    AsyncJobSingle<ServiceMethodResponse> RemoveAuthenticatorViaChallengeStart(CTwoFactor_RemoveAuthenticatorViaChallengeStart_Request request);

    /* CTwoFactor_RemoveAuthenticatorViaChallengeContinue_Response */
    AsyncJobSingle<ServiceMethodResponse> RemoveAuthenticatorViaChallengeContinue(CTwoFactor_RemoveAuthenticatorViaChallengeContinue_Request request);
}
