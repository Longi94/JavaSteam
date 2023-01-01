package in.dragonbra.javasteam.rpc;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesChatSteamclient.*;

public interface IClanChatRooms {
    CClanChatRooms_GetClanChatRoomInfo_Response GetClanChatRoomInfo(CClanChatRooms_GetClanChatRoomInfo_Request request);

    CClanChatRooms_SetClanChatRoomPrivate_Response SetClanChatRoomPrivate(CClanChatRooms_SetClanChatRoomPrivate_Request request);
}
