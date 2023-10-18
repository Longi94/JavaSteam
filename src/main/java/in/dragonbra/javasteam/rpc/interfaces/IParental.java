package in.dragonbra.javasteam.rpc.interfaces;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesParentalSteamclient.*;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.callback.ServiceMethodResponse;
import in.dragonbra.javasteam.types.AsyncJobSingle;

/**
 * @author Lossy
 * @since 2023-01-04
 */
@SuppressWarnings("unused")
public interface IParental {

    /* CParental_EnableParentalSettings_Response */
    AsyncJobSingle<ServiceMethodResponse> EnableParentalSettings(CParental_EnableParentalSettings_Request request);

    /* CParental_DisableParentalSettings_Response */
    AsyncJobSingle<ServiceMethodResponse> DisableParentalSettings(CParental_DisableParentalSettings_Request request);

    /* CParental_GetParentalSettings_Response */
    AsyncJobSingle<ServiceMethodResponse> GetParentalSettings(CParental_GetParentalSettings_Request request);

    /* CParental_GetSignedParentalSettings_Response */
    AsyncJobSingle<ServiceMethodResponse> GetSignedParentalSettings(CParental_GetSignedParentalSettings_Request request);

    /* CParental_SetParentalSettings_Response */
    AsyncJobSingle<ServiceMethodResponse> SetParentalSettings(CParental_SetParentalSettings_Request request);

    /* CParental_ValidateToken_Response */
    AsyncJobSingle<ServiceMethodResponse> ValidateToken(CParental_ValidateToken_Request request);

    /* CParental_ValidatePassword_Response */
    AsyncJobSingle<ServiceMethodResponse> ValidatePassword(CParental_ValidatePassword_Request request);

    /* CParental_LockClient_Response */
    AsyncJobSingle<ServiceMethodResponse> LockClient(CParental_LockClient_Request request);

    /* CParental_RequestRecoveryCode_Response */
    AsyncJobSingle<ServiceMethodResponse> RequestRecoveryCode(CParental_RequestRecoveryCode_Request request);

    /* CParental_DisableWithRecoveryCode_Response */
    AsyncJobSingle<ServiceMethodResponse> DisableWithRecoveryCode(CParental_DisableWithRecoveryCode_Request request);

    /* CParental_RequestFeatureAccess_Response */
    AsyncJobSingle<ServiceMethodResponse> RequestFeatureAccess(CParental_RequestFeatureAccess_Request request);

    /* CParental_ApproveFeatureAccess_Response */
    AsyncJobSingle<ServiceMethodResponse> ApproveFeatureAccess(CParental_ApproveFeatureAccess_Request request);
}
