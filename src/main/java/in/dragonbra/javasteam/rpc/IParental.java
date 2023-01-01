package in.dragonbra.javasteam.rpc;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesParentalSteamclient.*;

public interface IParental {
    CParental_EnableParentalSettings_Response EnableParentalSettings(CParental_EnableParentalSettings_Request request);

    CParental_DisableParentalSettings_Response DisableParentalSettings(CParental_DisableParentalSettings_Request request);

    CParental_GetParentalSettings_Response GetParentalSettings(CParental_GetParentalSettings_Request request);

    CParental_GetSignedParentalSettings_Response GetSignedParentalSettings(CParental_GetSignedParentalSettings_Request request);

    CParental_SetParentalSettings_Response SetParentalSettings(CParental_SetParentalSettings_Request request);

    CParental_ValidateToken_Response ValidateToken(CParental_ValidateToken_Request request);

    CParental_ValidatePassword_Response ValidatePassword(CParental_ValidatePassword_Request request);

    CParental_LockClient_Response LockClient(CParental_LockClient_Request request);

    CParental_RequestRecoveryCode_Response RequestRecoveryCode(CParental_RequestRecoveryCode_Request request);

    CParental_DisableWithRecoveryCode_Response DisableWithRecoveryCode(CParental_DisableWithRecoveryCode_Request request);
}
