package in.dragonbra.javasteam.rpc;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesChatSteamclient.*;
import in.dragonbra.javasteam.types.JobID;

/**
 * @author Lossy
 * @since 2023-01-04
 */
@SuppressWarnings("unused")
public interface IClanChatRooms {

    /*  CClanChatRooms_GetClanChatRoomInfo_Response */
    JobID GetClanChatRoomInfo(CClanChatRooms_GetClanChatRoomInfo_Request request);

    /* CClanChatRooms_SetClanChatRoomPrivate_Response */
    JobID SetClanChatRoomPrivate(CClanChatRooms_SetClanChatRoomPrivate_Request request);
}
