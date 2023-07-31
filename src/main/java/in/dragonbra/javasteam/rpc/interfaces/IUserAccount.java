package in.dragonbra.javasteam.rpc.interfaces;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesUseraccountSteamclient.*;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.callback.ServiceMethodResponse;
import in.dragonbra.javasteam.types.AsyncJobSingle;

/**
 * @author Lossy
 * @since 2023-05-11
 */
@SuppressWarnings("unused")
public interface IUserAccount {

    /* CUserAccount_GetAvailableValveDiscountPromotions_Response */
    AsyncJobSingle<ServiceMethodResponse> GetAvailableValveDiscountPromotions(CUserAccount_GetAvailableValveDiscountPromotions_Request request);

    /* CUserAccount_GetWalletDetails_Response */
    AsyncJobSingle<ServiceMethodResponse> GetClientWalletDetails(CUserAccount_GetClientWalletDetails_Request request);

    /* CUserAccount_GetAccountLinkStatus_Response */
    AsyncJobSingle<ServiceMethodResponse> GetAccountLinkStatus(CUserAccount_GetAccountLinkStatus_Request request);

    /* CUserAccount_CancelLicenseForApp_Response */
    AsyncJobSingle<ServiceMethodResponse> CancelLicenseForApp(CUserAccount_CancelLicenseForApp_Request request);

    /* CUserAccount_GetUserCountry_Response */
    AsyncJobSingle<ServiceMethodResponse> GetUserCountry(CUserAccount_GetUserCountry_Request request);

    /* CUserAccount_CreateFriendInviteToken_Response */
    AsyncJobSingle<ServiceMethodResponse> CreateFriendInviteToken(CUserAccount_CreateFriendInviteToken_Request request);

    /* CUserAccount_GetFriendInviteTokens_Response */
    AsyncJobSingle<ServiceMethodResponse> GetFriendInviteTokens(CUserAccount_GetFriendInviteTokens_Request request);

    /* CUserAccount_ViewFriendInviteToken_Response */
    AsyncJobSingle<ServiceMethodResponse> ViewFriendInviteToken(CUserAccount_ViewFriendInviteToken_Request request);

    /* CUserAccount_RedeemFriendInviteToken_Response */
    AsyncJobSingle<ServiceMethodResponse> RedeemFriendInviteToken(CUserAccount_RedeemFriendInviteToken_Request request);

    /* CUserAccount_RevokeFriendInviteToken_Response */
    AsyncJobSingle<ServiceMethodResponse> RevokeFriendInviteToken(CUserAccount_RevokeFriendInviteToken_Request request);

    /* CUserAccount_RegisterCompatTool_Response */
    AsyncJobSingle<ServiceMethodResponse> RegisterCompatTool(CUserAccount_RegisterCompatTool_Request request);
}
