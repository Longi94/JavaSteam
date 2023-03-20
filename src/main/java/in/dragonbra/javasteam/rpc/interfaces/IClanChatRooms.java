package in.dragonbra.javasteam.rpc.interfaces;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesChatSteamclient.*;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.callback.ServiceMethodResponse;
import in.dragonbra.javasteam.types.AsyncJobSingle;

/**
 * @author Lossy
 * @since 2023-01-04
 */
@SuppressWarnings("unused")
public interface IClanChatRooms {

    /*  CClanChatRooms_GetClanChatRoomInfo_Response */
    AsyncJobSingle<ServiceMethodResponse> GetClanChatRoomInfo(CClanChatRooms_GetClanChatRoomInfo_Request request);

    /* CClanChatRooms_SetClanChatRoomPrivate_Response */
    AsyncJobSingle<ServiceMethodResponse> SetClanChatRoomPrivate(CClanChatRooms_SetClanChatRoomPrivate_Request request);
}
