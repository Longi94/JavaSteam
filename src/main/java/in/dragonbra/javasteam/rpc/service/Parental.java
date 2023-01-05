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
        return sendMessage(CParental_EnableParentalSettings_Request.class, request.toBuilder());
    }

    @Override
    public JobID DisableParentalSettings(CParental_DisableParentalSettings_Request request) {
        return sendMessage(CParental_DisableParentalSettings_Request.class, request.toBuilder());
    }

    @Override
    public JobID GetParentalSettings(CParental_GetParentalSettings_Request request) {
        return sendMessage(CParental_GetParentalSettings_Request.class, request.toBuilder());
    }

    @Override
    public JobID GetSignedParentalSettings(CParental_GetSignedParentalSettings_Request request) {
        return sendMessage(CParental_GetSignedParentalSettings_Request.class, request.toBuilder());
    }

    @Override
    public JobID SetParentalSettings(CParental_SetParentalSettings_Request request) {
        return sendMessage(CParental_SetParentalSettings_Request.class, request.toBuilder());
    }

    @Override
    public JobID ValidateToken(CParental_ValidateToken_Request request) {
        return sendMessage(CParental_ValidateToken_Request.class, request.toBuilder());
    }

    @Override
    public JobID ValidatePassword(CParental_ValidatePassword_Request request) {
        return sendMessage(CParental_ValidatePassword_Request.class, request.toBuilder());
    }

    @Override
    public JobID LockClient(CParental_LockClient_Request request) {
        return sendMessage(CParental_LockClient_Request.class, request.toBuilder());
    }

    @Override
    public JobID RequestRecoveryCode(CParental_RequestRecoveryCode_Request request) {
        return sendMessage(CParental_RequestRecoveryCode_Request.class, request.toBuilder());
    }

    @Override
    public JobID DisableWithRecoveryCode(CParental_DisableWithRecoveryCode_Request request) {
        return sendMessage(CParental_DisableWithRecoveryCode_Request.class, request.toBuilder());
    }
}
