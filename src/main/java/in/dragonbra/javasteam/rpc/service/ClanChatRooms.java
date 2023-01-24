package in.dragonbra.javasteam.rpc.service;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesChatSteamclient.*;
import in.dragonbra.javasteam.rpc.IClanChatRooms;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.SteamUnifiedMessages;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.UnifiedService;
import in.dragonbra.javasteam.types.JobID;

/**
 * @author Lossy
 * @since 2023-01-04
 */
@SuppressWarnings("unused")
public class ClanChatRooms extends UnifiedService implements IClanChatRooms {

    public ClanChatRooms(SteamUnifiedMessages steamUnifiedMessages) {
        super(steamUnifiedMessages);
    }

    @Override
    public JobID GetClanChatRoomInfo(CClanChatRooms_GetClanChatRoomInfo_Request request) {
        return sendMessage(request);
    }

    @Override
    public JobID SetClanChatRoomPrivate(CClanChatRooms_SetClanChatRoomPrivate_Request request) {
        return sendMessage(request);
    }
}
