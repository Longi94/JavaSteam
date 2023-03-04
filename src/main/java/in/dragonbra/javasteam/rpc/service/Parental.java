package in.dragonbra.javasteam.rpc.service;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesParentalSteamclient.*;
import in.dragonbra.javasteam.rpc.IParental;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.SteamUnifiedMessages;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.UnifiedService;
import in.dragonbra.javasteam.types.JobID;

/**
 * @author Lossy
 * @since 2023-01-04
 */
@SuppressWarnings("unused")
public class Parental extends UnifiedService implements IParental {

    public Parental(SteamUnifiedMessages steamUnifiedMessages) {
        super(steamUnifiedMessages);
    }

    @Override
    public JobID EnableParentalSettings(CParental_EnableParentalSettings_Request request) {
        return sendMessage(request, "EnableParentalSettings");
    }

    @Override
    public JobID DisableParentalSettings(CParental_DisableParentalSettings_Request request) {
        return sendMessage(request, "DisableParentalSettings");
    }

    @Override
    public JobID GetParentalSettings(CParental_GetParentalSettings_Request request) {
        return sendMessage(request, "GetParentalSettings");
    }

    @Override
    public JobID GetSignedParentalSettings(CParental_GetSignedParentalSettings_Request request) {
        return sendMessage(request, "GetSignedParentalSettings");
    }

    @Override
    public JobID SetParentalSettings(CParental_SetParentalSettings_Request request) {
        return sendMessage(request, "SetParentalSettings");
    }

    @Override
    public JobID ValidateToken(CParental_ValidateToken_Request request) {
        return sendMessage(request, "ValidateToken");
    }

    @Override
    public JobID ValidatePassword(CParental_ValidatePassword_Request request) {
        return sendMessage(request, "ValidatePassword");
    }

    @Override
    public JobID LockClient(CParental_LockClient_Request request) {
        return sendMessage(request, "LockClient");
    }

    @Override
    public JobID RequestRecoveryCode(CParental_RequestRecoveryCode_Request request) {
        return sendMessage(request, "RequestRecoveryCode");
    }

    @Override
    public JobID DisableWithRecoveryCode(CParental_DisableWithRecoveryCode_Request request) {
        return sendMessage(request, "DisableWithRecoveryCode");
    }
}
