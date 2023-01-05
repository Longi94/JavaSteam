package in.dragonbra.javasteam.rpc.service;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesChatSteamclient;
import in.dragonbra.javasteam.rpc.IClanChatRooms;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.SteamUnifiedMessages;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.UnifiedService;
import in.dragonbra.javasteam.types.JobID;

// TODO implement

@SuppressWarnings("unused")
public class ClanChatRooms extends UnifiedService implements IClanChatRooms {

    public ClanChatRooms(SteamUnifiedMessages steamUnifiedMessages) {
        super(steamUnifiedMessages);
    }

    @Override
    public JobID GetClanChatRoomInfo(SteammessagesChatSteamclient.CClanChatRooms_GetClanChatRoomInfo_Request request) {
        return null;
    }

    @Override
    public JobID SetClanChatRoomPrivate(SteammessagesChatSteamclient.CClanChatRooms_SetClanChatRoomPrivate_Request request) {
        return null;
    }
}
