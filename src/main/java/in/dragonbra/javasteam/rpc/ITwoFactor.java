package in.dragonbra.javasteam.rpc;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesTwofactorSteamclient.*;
import in.dragonbra.javasteam.types.JobID;

/**
 * @author Lossy
 * @since 2023-01-06
 */
@SuppressWarnings("unused")
public interface ITwoFactor {

    /* CTwoFactor_Status_Response */
    JobID QueryStatus(CTwoFactor_Status_Request request);

    /* CTwoFactor_AddAuthenticator_Response */
    JobID AddAuthenticator(CTwoFactor_AddAuthenticator_Request request);

    /* CTwoFactor_SendEmail_Response */
    JobID SendEmail(CTwoFactor_SendEmail_Request request);

    /* CTwoFactor_FinalizeAddAuthenticator_Response */
    JobID FinalizeAddAuthenticator(CTwoFactor_FinalizeAddAuthenticator_Request request);

    /* CTwoFactor_UpdateTokenVersion_Response */
    JobID UpdateTokenVersion(CTwoFactor_UpdateTokenVersion_Request request);

    /* CTwoFactor_RemoveAuthenticator_Response */
    JobID RemoveAuthenticator(CTwoFactor_RemoveAuthenticator_Request request);

    /* CTwoFactor_CreateEmergencyCodes_Response */
    JobID CreateEmergencyCodes(CTwoFactor_CreateEmergencyCodes_Request request);

    /* CTwoFactor_DestroyEmergencyCodes_Response */
    JobID DestroyEmergencyCodes(CTwoFactor_DestroyEmergencyCodes_Request request);

    /* CTwoFactor_ValidateToken_Response */
    JobID ValidateToken(CTwoFactor_ValidateToken_Request request);

    /* CTwoFactor_RemoveAuthenticatorViaChallengeStart_Response */
    JobID RemoveAuthenticatorViaChallengeStart(CTwoFactor_RemoveAuthenticatorViaChallengeStart_Request request);

    /* CTwoFactor_RemoveAuthenticatorViaChallengeContinue_Response */
    JobID RemoveAuthenticatorViaChallengeContinue(CTwoFactor_RemoveAuthenticatorViaChallengeContinue_Request request);
}
