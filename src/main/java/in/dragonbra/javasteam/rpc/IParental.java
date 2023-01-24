package in.dragonbra.javasteam.rpc;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesParentalSteamclient.*;
import in.dragonbra.javasteam.types.JobID;

/**
 * @author Lossy
 * @since 2023-01-04
 */
@SuppressWarnings("unused")
public interface IParental {

    /* CParental_EnableParentalSettings_Response */
    JobID EnableParentalSettings(CParental_EnableParentalSettings_Request request);

    /* CParental_DisableParentalSettings_Response */
    JobID DisableParentalSettings(CParental_DisableParentalSettings_Request request);

    /* CParental_GetParentalSettings_Response */
    JobID GetParentalSettings(CParental_GetParentalSettings_Request request);

    /* CParental_GetSignedParentalSettings_Response */
    JobID GetSignedParentalSettings(CParental_GetSignedParentalSettings_Request request);

    /* CParental_SetParentalSettings_Response */
    JobID SetParentalSettings(CParental_SetParentalSettings_Request request);

    /* CParental_ValidateToken_Response */
    JobID ValidateToken(CParental_ValidateToken_Request request);

    /* CParental_ValidatePassword_Response */
    JobID ValidatePassword(CParental_ValidatePassword_Request request);

    /* CParental_LockClient_Response */
    JobID LockClient(CParental_LockClient_Request request);

    /* CParental_RequestRecoveryCode_Response */
    JobID RequestRecoveryCode(CParental_RequestRecoveryCode_Request request);

    /* CParental_DisableWithRecoveryCode_Response */
    JobID DisableWithRecoveryCode(CParental_DisableWithRecoveryCode_Request request);
}
