package in.dragonbra.javasteam.rpc.service;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesParentalSteamclient.*;
import in.dragonbra.javasteam.rpc.interfaces.IParental;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.SteamUnifiedMessages;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.UnifiedService;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.callback.ServiceMethodResponse;
import in.dragonbra.javasteam.types.AsyncJobSingle;

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
    public AsyncJobSingle<ServiceMethodResponse> EnableParentalSettings(CParental_EnableParentalSettings_Request request) {
        return sendMessage(request, "EnableParentalSettings");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> DisableParentalSettings(CParental_DisableParentalSettings_Request request) {
        return sendMessage(request, "DisableParentalSettings");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> GetParentalSettings(CParental_GetParentalSettings_Request request) {
        return sendMessage(request, "GetParentalSettings");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> GetSignedParentalSettings(CParental_GetSignedParentalSettings_Request request) {
        return sendMessage(request, "GetSignedParentalSettings");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> SetParentalSettings(CParental_SetParentalSettings_Request request) {
        return sendMessage(request, "SetParentalSettings");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> ValidateToken(CParental_ValidateToken_Request request) {
        return sendMessage(request, "ValidateToken");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> ValidatePassword(CParental_ValidatePassword_Request request) {
        return sendMessage(request, "ValidatePassword");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> LockClient(CParental_LockClient_Request request) {
        return sendMessage(request, "LockClient");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> RequestRecoveryCode(CParental_RequestRecoveryCode_Request request) {
        return sendMessage(request, "RequestRecoveryCode");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> DisableWithRecoveryCode(CParental_DisableWithRecoveryCode_Request request) {
        return sendMessage(request, "DisableWithRecoveryCode");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> RequestFeatureAccess(CParental_RequestFeatureAccess_Request request) {
        return sendMessage(request, "RequestFeatureAccess");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> ApproveFeatureAccess(CParental_ApproveFeatureAccess_Request request) {
        return sendMessage(request, "ApproveFeatureAccess");
    }
}
