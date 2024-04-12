package in.dragonbra.javasteam.rpc.service;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesUseraccountSteamclient.*;
import in.dragonbra.javasteam.rpc.interfaces.IUserAccount;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.SteamUnifiedMessages;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.UnifiedService;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.callback.ServiceMethodResponse;
import in.dragonbra.javasteam.types.AsyncJobSingle;

/**
 * @author Lossy
 * @since 2023-05-11
 */
@SuppressWarnings("unused")
public class UserAccount extends UnifiedService implements IUserAccount {

    public UserAccount(SteamUnifiedMessages steamUnifiedMessages) {
        super(steamUnifiedMessages);
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> getAvailableValveDiscountPromotions(CUserAccount_GetAvailableValveDiscountPromotions_Request request) {
        return sendMessage(request, "GetAvailableValveDiscountPromotions");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> getClientWalletDetails(CUserAccount_GetClientWalletDetails_Request request) {
        return sendMessage(request, "GetClientWalletDetails");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> getAccountLinkStatus(CUserAccount_GetAccountLinkStatus_Request request) {
        return sendMessage(request, "GetAccountLinkStatus");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> cancelLicenseForApp(CUserAccount_CancelLicenseForApp_Request request) {
        return sendMessage(request, "CancelLicenseForApp");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> getUserCountry(CUserAccount_GetUserCountry_Request request) {
        return sendMessage(request, "GetUserCountry");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> createFriendInviteToken(CUserAccount_CreateFriendInviteToken_Request request) {
        return sendMessage(request, "CreateFriendInviteToken");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> getFriendInviteTokens(CUserAccount_GetFriendInviteTokens_Request request) {
        return sendMessage(request, "GetFriendInviteTokens");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> viewFriendInviteToken(CUserAccount_ViewFriendInviteToken_Request request) {
        return sendMessage(request, "ViewFriendInviteToken");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> redeemFriendInviteToken(CUserAccount_RedeemFriendInviteToken_Request request) {
        return sendMessage(request, "RedeemFriendInviteToken");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> revokeFriendInviteToken(CUserAccount_RevokeFriendInviteToken_Request request) {
        return sendMessage(request, "RevokeFriendInviteToken");
    }

    @Override
    public AsyncJobSingle<ServiceMethodResponse> registerCompatTool(CUserAccount_RegisterCompatTool_Request request) {
        return sendMessage(request, "RegisterCompatTool");
    }
}
