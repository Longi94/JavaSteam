package in.dragonbra.javasteam.rpc.service;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesParentalSteamclient;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesParentalSteamclient.*;
import in.dragonbra.javasteam.rpc.interfaces.IParental;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.SteamUnifiedMessages;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.UnifiedService;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.callback.ServiceMethodResponse;
import in.dragonbra.javasteam.types.AsyncJobSingle;
import org.jetbrains.annotations.NotNull;

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
    public AsyncJobSingle<ServiceMethodResponse> enableParentalSettings(CParental_EnableParentalSettings_Request request) {
        return sendMessage(request, "EnableParentalSettings");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> disableParentalSettings(CParental_DisableParentalSettings_Request request) {
        return sendMessage(request, "DisableParentalSettings");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> getParentalSettings(CParental_GetParentalSettings_Request request) {
        return sendMessage(request, "GetParentalSettings");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> getSignedParentalSettings(CParental_GetSignedParentalSettings_Request request) {
        return sendMessage(request, "GetSignedParentalSettings");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> setParentalSettings(CParental_SetParentalSettings_Request request) {
        return sendMessage(request, "SetParentalSettings");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> validateToken(CParental_ValidateToken_Request request) {
        return sendMessage(request, "ValidateToken");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> validatePassword(CParental_ValidatePassword_Request request) {
        return sendMessage(request, "ValidatePassword");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> lockClient(CParental_LockClient_Request request) {
        return sendMessage(request, "LockClient");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> requestRecoveryCode(CParental_RequestRecoveryCode_Request request) {
        return sendMessage(request, "RequestRecoveryCode");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> disableWithRecoveryCode(CParental_DisableWithRecoveryCode_Request request) {
        return sendMessage(request, "DisableWithRecoveryCode");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> requestFeatureAccess(CParental_RequestFeatureAccess_Request request) {
        return sendMessage(request, "RequestFeatureAccess");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> approveFeatureAccess(CParental_ApproveFeatureAccess_Request request) {
        return sendMessage(request, "ApproveFeatureAccess");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> requestPlaytime(SteammessagesParentalSteamclient.CParental_RequestPlaytime_Request request) {
        return sendMessage(request, "RequestPlaytime");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> approvePlaytime(SteammessagesParentalSteamclient.CParental_ApprovePlaytime_Request request) {
        return sendMessage(request, "ApprovePlaytime");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> getRequests(SteammessagesParentalSteamclient.CParental_GetRequests_Request request) {
        return sendMessage(request, "GetRequests");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> reportPlaytimeAndNotify(SteammessagesParentalSteamclient.CParental_ReportPlaytimeAndNotify_Request request) {
        return sendMessage(request, "ReportPlaytimeAndNotify");
    }
}
