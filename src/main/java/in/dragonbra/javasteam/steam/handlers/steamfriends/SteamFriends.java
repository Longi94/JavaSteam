package in.dragonbra.javasteam.steam.handlers.steamfriends;

import com.google.protobuf.ByteString;
import in.dragonbra.javasteam.base.ClientMsg;
import in.dragonbra.javasteam.base.ClientMsgProtobuf;
import in.dragonbra.javasteam.base.IPacketMsg;
import in.dragonbra.javasteam.enums.*;
import in.dragonbra.javasteam.generated.*;
import in.dragonbra.javasteam.handlers.ClientMsgHandler;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver.CMsgClientChatInvite;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver.CMsgClientClanState;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver2.CMsgClientChatGetFriendMessageHistory;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver2.CMsgClientChatGetFriendMessageHistoryForOfflineMessages;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver2.CMsgClientChatGetFriendMessageHistoryResponse;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverFriends.*;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverLogin.CMsgClientAccountInfo;
import in.dragonbra.javasteam.steam.handlers.steamfriends.cache.AccountCache;
import in.dragonbra.javasteam.steam.handlers.steamfriends.cache.Clan;
import in.dragonbra.javasteam.steam.handlers.steamfriends.cache.User;
import in.dragonbra.javasteam.steam.handlers.steamfriends.callback.*;
import in.dragonbra.javasteam.steam.handlers.steamuser.callback.AccountInfoCallback;
import in.dragonbra.javasteam.steam.steamclient.configuration.SteamConfiguration;
import in.dragonbra.javasteam.types.GameID;
import in.dragonbra.javasteam.types.JobID;
import in.dragonbra.javasteam.types.SteamID;
import in.dragonbra.javasteam.util.compat.Consumer;
import in.dragonbra.javasteam.util.log.LogManager;
import in.dragonbra.javasteam.util.log.Logger;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;

/**
 * This handler handles all interaction with other users on the Steam3 network.
 */
@SuppressWarnings("unused")
public class SteamFriends extends ClientMsgHandler {

    private static final Logger logger = LogManager.getLogger(SteamFriends.class);

    private final Object listLock = new Object();
    private List<SteamID> friendList = Collections.synchronizedList(new ArrayList<SteamID>());
    private List<SteamID> clanList = Collections.synchronizedList(new ArrayList<SteamID>());

    private AccountCache cache = new AccountCache();

    private Map<EMsg, Consumer<IPacketMsg>> dispatchMap;

    public SteamFriends() {
        dispatchMap = new HashMap<>();

        dispatchMap.put(EMsg.ClientPersonaState, new Consumer<IPacketMsg>() {
            @Override
            public void accept(IPacketMsg packetMsg) {
                handlePersonaState(packetMsg);
            }
        });
        dispatchMap.put(EMsg.ClientClanState, new Consumer<IPacketMsg>() {
            @Override
            public void accept(IPacketMsg packetMsg) {
                handleClanState(packetMsg);
            }
        });
        dispatchMap.put(EMsg.ClientFriendsList, new Consumer<IPacketMsg>() {
            @Override
            public void accept(IPacketMsg packetMsg) {
                handleFriendsList(packetMsg);
            }
        });
        dispatchMap.put(EMsg.ClientFriendMsgIncoming, new Consumer<IPacketMsg>() {
            @Override
            public void accept(IPacketMsg packetMsg) {
                handleFriendMsg(packetMsg);
            }
        });
        dispatchMap.put(EMsg.ClientFriendMsgEchoToSender, new Consumer<IPacketMsg>() {
            @Override
            public void accept(IPacketMsg packetMsg) {
                handleFriendEchoMsg(packetMsg);
            }
        });
        dispatchMap.put(EMsg.ClientFSGetFriendMessageHistoryResponse, new Consumer<IPacketMsg>() {
            @Override
            public void accept(IPacketMsg packetMsg) {
                handleFriendMessageHistoryResponse(packetMsg);
            }
        });
        dispatchMap.put(EMsg.ClientAccountInfo, new Consumer<IPacketMsg>() {
            @Override
            public void accept(IPacketMsg packetMsg) {
                handleAccountInfo(packetMsg);
            }
        });
        dispatchMap.put(EMsg.ClientAddFriendResponse, new Consumer<IPacketMsg>() {
            @Override
            public void accept(IPacketMsg packetMsg) {
                handleFriendResponse(packetMsg);
            }
        });
        dispatchMap.put(EMsg.ClientChatEnter, new Consumer<IPacketMsg>() {
            @Override
            public void accept(IPacketMsg packetMsg) {
                handleChatEnter(packetMsg);
            }
        });
        dispatchMap.put(EMsg.ClientChatMsg, new Consumer<IPacketMsg>() {
            @Override
            public void accept(IPacketMsg packetMsg) {
                handleChatMsg(packetMsg);
            }
        });
        dispatchMap.put(EMsg.ClientChatMemberInfo, new Consumer<IPacketMsg>() {
            @Override
            public void accept(IPacketMsg packetMsg) {
                handleChatMemberInfo(packetMsg);
            }
        });
        dispatchMap.put(EMsg.ClientChatRoomInfo, new Consumer<IPacketMsg>() {
            @Override
            public void accept(IPacketMsg packetMsg) {
                handleChatRoomInfo(packetMsg);
            }
        });
        dispatchMap.put(EMsg.ClientChatActionResult, new Consumer<IPacketMsg>() {
            @Override
            public void accept(IPacketMsg packetMsg) {
                handleChatActionResult(packetMsg);
            }
        });
        dispatchMap.put(EMsg.ClientChatInvite, new Consumer<IPacketMsg>() {
            @Override
            public void accept(IPacketMsg packetMsg) {
                handleChatInvite(packetMsg);
            }
        });
        dispatchMap.put(EMsg.ClientSetIgnoreFriendResponse, new Consumer<IPacketMsg>() {
            @Override
            public void accept(IPacketMsg packetMsg) {
                handleIgnoreFriendResponse(packetMsg);
            }
        });
        dispatchMap.put(EMsg.ClientFriendProfileInfoResponse, new Consumer<IPacketMsg>() {
            @Override
            public void accept(IPacketMsg packetMsg) {
                handleProfileInfoResponse(packetMsg);
            }
        });
        dispatchMap.put(EMsg.ClientPersonaChangeResponse, new Consumer<IPacketMsg>() {
            @Override
            public void accept(IPacketMsg packetMsg) {
                handlePersonaChangeResponse(packetMsg);
            }
        });

        dispatchMap = Collections.unmodifiableMap(dispatchMap);
    }

    /**
     * Gets the local user's persona name. Will be null before user initialization.
     * User initialization is performed prior to {@link AccountInfoCallback} callback.
     *
     * @return The name.
     */
    public String getPersonaName() {
        return cache.getLocalUser().getName();
    }

    /**
     * Sets the local user's persona name and broadcasts it over the network.
     * Results are returned in a{@link PersonaChangeCallback} callback.
     *
     * @param name The name.
     */
    public void setPersonaName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("name is null");
        }

        cache.getLocalUser().setName(name);

        ClientMsgProtobuf<CMsgClientChangeStatus.Builder> stateMsg = new ClientMsgProtobuf<>(CMsgClientChangeStatus.class, EMsg.ClientChangeStatus);
        stateMsg.setSourceJobID(client.getNextJobID());

        stateMsg.getBody().setPersonaState(cache.getLocalUser().getPersonaState().code());
        stateMsg.getBody().setPlayerName(name);

        client.send(stateMsg);
    }

    /**
     * Gets the local user's persona state.
     *
     * @return The persona state.
     */
    public EPersonaState getPersonaState() {
        return cache.getLocalUser().getPersonaState();
    }

    /**
     * Sets the local user's persona state and broadcasts it over the network.
     * Results are returned in a{@link PersonaChangeCallback} callback.
     *
     * @param state The state.
     */
    public void setPersonaState(EPersonaState state) {
        if (state == null) {
            throw new IllegalArgumentException("state is null");
        }

        cache.getLocalUser().setPersonaState(state);

        ClientMsgProtobuf<CMsgClientChangeStatus.Builder> stateMsg = new ClientMsgProtobuf<>(CMsgClientChangeStatus.class, EMsg.ClientChangeStatus);
        stateMsg.setSourceJobID(client.getNextJobID());

        stateMsg.getBody().setPersonaState(state.code());

        client.send(stateMsg);
    }

    /**
     * Gets the friend count of the local user.
     *
     * @return The number of friends.
     */
    public int getFriendsCount() {
        synchronized (listLock) {
            return friendList.size();
        }
    }

    /**
     * Gets a friend by index.
     *
     * @param index The index.
     * @return A valid steamid of a friend if the index is in range; otherwise a steamid representing 0
     */
    public SteamID getFriendByIndex(int index) {
        synchronized (listLock) {
            if (index < 0 || index >= friendList.size()) {
                return new SteamID();
            }

            return friendList.get(index);
        }
    }

    /**
     * Gets the persona name of a friend.
     *
     * @param steamID The steam id.
     * @return The name.
     */
    public String getPersonaName(SteamID steamID) {
        if (steamID == null) {
            throw new IllegalArgumentException("steamID is null");
        }

        return cache.getUser(steamID).getName();
    }

    /**
     * Gets the persona state of a friend.
     *
     * @param steamID The steam id.
     * @return he persona state.
     */
    public EPersonaState getFriendPersonaState(SteamID steamID) {
        if (steamID == null) {
            throw new IllegalArgumentException("steamID is null");
        }

        return cache.getUser(steamID).getPersonaState();
    }

    /**
     * Gets the relationship of a friend.
     *
     * @param steamID The steam id.
     * @return The relationship of the friend to the local user.
     */
    public EFriendRelationship getFriendRelationship(SteamID steamID) {
        if (steamID == null) {
            throw new IllegalArgumentException("steamID is null");
        }

        return cache.getUser(steamID).getRelationship();
    }

    /**
     * Gets the game name of a friend playing a game.
     *
     * @param steamID The steam id
     * @return The game name of a friend playing a game, or null if they haven't been cached yet
     */
    public String getFriendGamePlayedName(SteamID steamID) {
        if (steamID == null) {
            throw new IllegalArgumentException("steamID is null");
        }

        return cache.getUser(steamID).getGameName();
    }

    /**
     * Gets the GameID of a friend playing a game.
     *
     * @param steamID The steam id.
     * @return The gameid of a friend playing a game, or 0 if they haven't been cached yet.
     */
    public GameID getFriendGamePlayed(SteamID steamID) {
        if (steamID == null) {
            throw new IllegalArgumentException("steamID is null");
        }

        return cache.getUser(steamID).getGameID();
    }

    /**
     * Gets a SHA-1 hash representing the friend's avatar.
     *
     * @param steamID The SteamID of the friend to get the avatar of.
     * @return A byte array representing a SHA-1 hash of the friend's avatar.
     */
    public byte[] getFriendAvatar(SteamID steamID) {
        if (steamID == null) {
            throw new IllegalArgumentException("steamID is null");
        }

        return cache.getUser(steamID).getAvatarHash();
    }

    /**
     * Gets the count of clans the local user is a member of.
     *
     * @return The number of clans this user is a member of.
     */
    public int getClanCount() {
        synchronized (listLock) {
            return clanList.size();
        }
    }

    /**
     * Gets a clan SteamID by index.
     *
     * @param index The index.
     * @return A valid steamid of a clan if the index is in range; otherwise a steamid representing 0.
     */
    public SteamID getClanByIndex(int index) {
        synchronized (listLock) {
            if (index < 0 || index >= clanList.size()) {
                return new SteamID();
            }

            return clanList.get(index);
        }
    }

    /**
     * Gets the name of a clan.
     *
     * @param steamID The clan SteamID.
     * @return The name.
     */
    public String getClanName(SteamID steamID) {
        if (steamID == null) {
            throw new IllegalArgumentException("steamID is null");
        }

        return cache.getClan(steamID).getName();
    }

    /**
     * Gets the relationship of a clan.
     *
     * @param steamID The clan steamid.
     * @return The relationship of the clan to the local user.
     */
    public EClanRelationship getClanRelationship(SteamID steamID) {
        if (steamID == null) {
            throw new IllegalArgumentException("steamID is null");
        }

        return cache.getClan(steamID).getRelationship();
    }

    /**
     * Gets a SHA-1 hash representing the clan's avatar.
     *
     * @param steamID The SteamID of the clan to get the avatar of.
     * @return A byte array representing a SHA-1 hash of the clan's avatar, or null if the clan could not be found.
     */
    public byte[] getClanAvatar(SteamID steamID) {
        if (steamID == null) {
            throw new IllegalArgumentException("steamID is null");
        }

        return cache.getClan(steamID).getAvatarHash();
    }

    /**
     * Sends a chat message to a friend.
     *
     * @param target  The target to send to.
     * @param type    The type of message to send.
     * @param message The message to send.
     */
    public void sendChatMessage(SteamID target, EChatEntryType type, String message) {
        if (target == null) {
            throw new IllegalArgumentException("target is null");
        }

        if (type == null) {
            throw new IllegalArgumentException("type is null");
        }

        if (message == null) {
            throw new IllegalArgumentException("message is null");
        }

        ClientMsgProtobuf<CMsgClientFriendMsg.Builder> chatMsg = new ClientMsgProtobuf<>(CMsgClientFriendMsg.class, EMsg.ClientFriendMsg);

        chatMsg.getBody().setSteamid(target.convertToUInt64());
        chatMsg.getBody().setChatEntryType(type.code());
        chatMsg.getBody().setMessage(ByteString.copyFrom(message, Charset.forName("UTF-8")));

        client.send(chatMsg);
    }

    /**
     * Sends a friend request to a user.
     *
     * @param accountNameOrEmail The account name or email of the user.
     */
    public void addFriend(String accountNameOrEmail) {
        if (accountNameOrEmail == null) {
            throw new IllegalArgumentException("accountNameOrEmail is null");
        }

        ClientMsgProtobuf<CMsgClientAddFriend.Builder> addFriend = new ClientMsgProtobuf<>(CMsgClientAddFriend.class, EMsg.ClientAddFriend);

        addFriend.getBody().setAccountnameOrEmailToAdd(accountNameOrEmail);

        client.send(addFriend);
    }

    /**
     * Sends a friend request to a user.
     *
     * @param steamID The SteamID of the friend to add.
     */
    public void addFriend(SteamID steamID) {
        if (steamID == null) {
            throw new IllegalArgumentException("steamID is null");
        }

        ClientMsgProtobuf<CMsgClientAddFriend.Builder> addFriend = new ClientMsgProtobuf<>(CMsgClientAddFriend.class, EMsg.ClientAddFriend);

        addFriend.getBody().setSteamidToAdd(steamID.convertToUInt64());

        client.send(addFriend);
    }

    /**
     * Removes a friend from your friends list.
     *
     * @param steamID The SteamID of the friend to remove.
     */
    public void removeFriend(SteamID steamID) {
        if (steamID == null) {
            throw new IllegalArgumentException("steamID is null");
        }

        ClientMsgProtobuf<CMsgClientRemoveFriend.Builder> removeFriend = new ClientMsgProtobuf<>(CMsgClientRemoveFriend.class, EMsg.ClientRemoveFriend);

        removeFriend.getBody().setFriendid(steamID.convertToUInt64());

        client.send(removeFriend);
    }

    /**
     * Attempts to join a chat room.
     *
     * @param steamID The SteamID of the chat room.
     */
    public void joinChat(SteamID steamID) {
        if (steamID == null) {
            throw new IllegalArgumentException("steamID is null");
        }

        SteamID chatID = fixChatID(steamID); // copy the steamid so we don't modify it

        ClientMsg<MsgClientJoinChat> joinChat = new ClientMsg<>(MsgClientJoinChat.class);

        joinChat.getBody().setSteamIdChat(chatID);

        client.send(joinChat);
    }

    /**
     * Attempts to leave a chat room.
     *
     * @param steamID The SteamID of the chat room.
     */
    public void leaveChat(SteamID steamID) {
        if (steamID == null) {
            throw new IllegalArgumentException("steamID is null");
        }

        SteamID chatID = fixChatID(steamID); // copy the steamid so we don't modify it

        ClientMsg<MsgClientChatMemberInfo> leaveChat = new ClientMsg<>(MsgClientChatMemberInfo.class);

        leaveChat.getBody().setSteamIdChat(chatID);
        leaveChat.getBody().setType(EChatInfoType.StateChange);

        try {
            leaveChat.write(client.getSteamID().convertToUInt64()); // ChatterActedOn
            leaveChat.write(EChatMemberStateChange.Left.code()); // StateChange
            leaveChat.write(client.getSteamID().convertToUInt64()); // ChatterActedBy
        } catch (IOException e) {
            logger.debug(e);
        }

        client.send(leaveChat);
    }

    /**
     * Sends a message to a chat room.
     *
     * @param steamIdChat The SteamID of the chat room.
     * @param type        The message type.
     * @param message     The message.
     */
    public void sendChatRoomMessage(SteamID steamIdChat, EChatEntryType type, String message) {
        if (steamIdChat == null) {
            throw new IllegalArgumentException("steamIdChat is null");
        }

        if (type == null) {
            throw new IllegalArgumentException("type is null");
        }

        if (message == null) {
            throw new IllegalArgumentException("message is null");
        }

        SteamID chatID = fixChatID(steamIdChat); // copy the steamid so we don't modify it

        ClientMsg<MsgClientChatMsg> chatMsg = new ClientMsg<>(MsgClientChatMsg.class);

        chatMsg.getBody().setChatMsgType(type);
        chatMsg.getBody().setSteamIdChatRoom(chatID);
        chatMsg.getBody().setSteamIdChatter(client.getSteamID());

        try {
            chatMsg.writeNullTermString(message, Charset.forName("UTF-8"));
        } catch (IOException e) {
            logger.debug(e);
        }

        client.send(chatMsg);
    }

    /**
     * Invites a user to a chat room.
     * The results of this action will be available through the {@link ChatActionResultCallback} callback.
     *
     * @param steamIdUser The SteamID of the user to invite.
     * @param steamIdChat The SteamID of the chat room to invite the user to.
     */
    public void inviteUserToChat(SteamID steamIdUser, SteamID steamIdChat) {
        if (steamIdChat == null) {
            throw new IllegalArgumentException("steamIdChat is null");
        }

        if (steamIdUser == null) {
            throw new IllegalArgumentException("steamIdUser is null");
        }

        SteamID chatID = fixChatID(steamIdChat); // copy the steamid so we don't modify it

        ClientMsgProtobuf<CMsgClientChatInvite.Builder> inviteMsg = new ClientMsgProtobuf<>(CMsgClientChatInvite.class, EMsg.ClientChatInvite);

        inviteMsg.getBody().setSteamIdChat(chatID.convertToUInt64());
        inviteMsg.getBody().setSteamIdInvited(steamIdUser.convertToUInt64());

        // steamclient also sends the steamid of the user that did the invitation
        // we'll mimic that behavior
        inviteMsg.getBody().setSteamIdPatron(client.getSteamID().convertToUInt64());

        client.send(inviteMsg);
    }

    /**
     * Kicks the specified chat member from the given chat room.
     *
     * @param steamIdChat   The SteamID of chat room to kick the member from.
     * @param steamIdMember The SteamID of the member to kick from the chat.
     */
    public void kickChatMember(SteamID steamIdChat, SteamID steamIdMember) {
        if (steamIdChat == null) {
            throw new IllegalArgumentException("steamIdChat is null");
        }

        if (steamIdMember == null) {
            throw new IllegalArgumentException("steamIdMember is null");
        }

        SteamID chatID = fixChatID(steamIdChat); // copy the steamid so we don't modify it

        ClientMsg<MsgClientChatAction> kickMember = new ClientMsg<>(MsgClientChatAction.class);

        kickMember.getBody().setSteamIdChat(chatID);
        kickMember.getBody().setSteamIdUserToActOn(steamIdMember);

        kickMember.getBody().setChatAction(EChatAction.Kick);

        client.send(kickMember);
    }

    /**
     * Bans the specified chat member from the given chat room.
     *
     * @param steamIdChat   The SteamID of chat room to ban the member from.
     * @param steamIdMember The SteamID of the member to ban from the chat.
     */
    public void banChatMember(SteamID steamIdChat, SteamID steamIdMember) {
        if (steamIdChat == null) {
            throw new IllegalArgumentException("steamIdChat is null");
        }

        if (steamIdMember == null) {
            throw new IllegalArgumentException("steamIdMember is null");
        }

        SteamID chatID = fixChatID(steamIdChat); // copy the steamid so we don't modify it

        ClientMsg<MsgClientChatAction> kickMember = new ClientMsg<>(MsgClientChatAction.class);

        kickMember.getBody().setSteamIdChat(chatID);
        kickMember.getBody().setSteamIdUserToActOn(steamIdMember);

        kickMember.getBody().setChatAction(EChatAction.Ban);

        client.send(kickMember);
    }

    /**
     * Unbans the specified chat member from the given chat room.
     *
     * @param steamIdChat   The SteamID of chat room to unban the member from.
     * @param steamIdMember The SteamID of the member to unban from the chat.
     */
    public void unbanChatMember(SteamID steamIdChat, SteamID steamIdMember) {
        if (steamIdChat == null) {
            throw new IllegalArgumentException("steamIdChat is null");
        }

        if (steamIdMember == null) {
            throw new IllegalArgumentException("steamIdMember is null");
        }

        SteamID chatID = fixChatID(steamIdChat); // copy the steamid so we don't modify it

        ClientMsg<MsgClientChatAction> kickMember = new ClientMsg<>(MsgClientChatAction.class);

        kickMember.getBody().setSteamIdChat(chatID);
        kickMember.getBody().setSteamIdUserToActOn(steamIdMember);

        kickMember.getBody().setChatAction(EChatAction.UnBan);

        client.send(kickMember);
    }

    /**
     * Requests persona state for a list of specified SteamID.
     * Results are returned in {@link PersonaState}.
     *
     * @param steamIdList   A list of SteamIDs to request the info of.
     * @param requestedInfo The requested info flags. If none specified, this uses {@link SteamConfiguration#getDefaultPersonaStateFlags()}.
     */
    public void requestFriendInfo(List<SteamID> steamIdList, int requestedInfo) {
        if (steamIdList == null) {
            throw new IllegalArgumentException("steamIdList is null");
        }

        if (requestedInfo == 0) {
            requestedInfo = client.getConfiguration().getDefaultPersonaStateFlags();
        }

        ClientMsgProtobuf<CMsgClientRequestFriendData.Builder> request = new ClientMsgProtobuf<>(CMsgClientRequestFriendData.class, EMsg.ClientRequestFriendData);

        for (SteamID steamID : steamIdList) {
            request.getBody().addFriends(steamID.convertToUInt64());
        }
        request.getBody().setPersonaStateRequested(requestedInfo);

        client.send(request);
    }

    /**
     * Requests persona state for a specified SteamID.
     * Results are returned in {@link PersonaState}.
     *
     * @param steamID       A SteamID to request the info of.
     * @param requestedInfo The requested info flags. If none specified, this uses {@link SteamConfiguration#getDefaultPersonaStateFlags()}.
     */
    public void requestFriendInfo(SteamID steamID, int requestedInfo) {
        if (steamID == null) {
            throw new IllegalArgumentException("steamID is null");
        }

        List<SteamID> list = new ArrayList<>();
        list.add(steamID);
        requestFriendInfo(list, requestedInfo);
    }

    /**
     * Ignores a friend on Steam.
     * Results are returned in a {@link IgnoreFriendCallback}.
     *
     * @param steamID The SteamID of the friend to ignore or unignore.
     */
    public void ignoreFriend(SteamID steamID) {
        ignoreFriend(steamID, true);
    }

    /**
     * Ignores or unignores a friend on Steam.
     * Results are returned in a {@link IgnoreFriendCallback}.
     *
     * @param steamID   The SteamID of the friend to ignore or unignore.
     * @param setIgnore if set to <b>true</b>, the friend will be ignored; otherwise, they will be unignored.
     */
    public void ignoreFriend(SteamID steamID, boolean setIgnore) {
        if (steamID == null) {
            throw new IllegalArgumentException("steamID is null");
        }

        ClientMsg<MsgClientSetIgnoreFriend> ignore = new ClientMsg<>(MsgClientSetIgnoreFriend.class);
        ignore.setSourceJobID(client.getNextJobID());

        ignore.getBody().setMySteamId(client.getSteamID());
        ignore.getBody().setIgnore(setIgnore ? (byte) 1 : (byte) 0);
        ignore.getBody().setSteamIdFriend(steamID);

        client.send(ignore);
    }

    /**
     * Requests profile information for the given {@link SteamID}
     * Results are returned in a {@link ProfileInfoCallback}
     *
     * @param steamID The SteamID of the friend to request the details of.
     */
    public void requestProfileInfo(SteamID steamID) {
        if (steamID == null) {
            throw new IllegalArgumentException("steamID is null");
        }

        ClientMsgProtobuf<CMsgClientFriendProfileInfo.Builder> request = new ClientMsgProtobuf<>(CMsgClientFriendProfileInfo.class, EMsg.ClientFriendProfileInfo);
        request.setSourceJobID(client.getNextJobID());

        request.getBody().setSteamidFriend(steamID.convertToUInt64());

        client.send(request);
    }

    /**
     * Requests the last few chat messages with a friend.
     * Results are returned in a {@link FriendMsgHistoryCallback}
     *
     * @param steamID SteamID of the friend
     */
    public void requestMessageHistory(SteamID steamID) {
        if (steamID == null) {
            throw new IllegalArgumentException("steamID is null");
        }

        ClientMsgProtobuf<CMsgClientChatGetFriendMessageHistory.Builder> request =
                new ClientMsgProtobuf<>(CMsgClientChatGetFriendMessageHistory.class, EMsg.ClientFSGetFriendMessageHistory);

        request.getBody().setSteamid(steamID.convertToUInt64());

        client.send(request);
    }

    /**
     * Requests all offline messages.
     * This also marks them as read server side.
     * Results are returned in a {@link FriendMsgHistoryCallback}.
     */
    public void requestOfflineMessages() {
        ClientMsgProtobuf<CMsgClientChatGetFriendMessageHistoryForOfflineMessages.Builder> request =
                new ClientMsgProtobuf<>(CMsgClientChatGetFriendMessageHistoryForOfflineMessages.class, EMsg.ClientFSGetFriendMessageHistoryForOfflineMessages);
        client.send(request);
    }

    @Override
    public void handleMsg(IPacketMsg packetMsg) {
        if (packetMsg == null) {
            throw new IllegalArgumentException("packetMsg is null");
        }

        if (dispatchMap.containsKey(packetMsg.getMsgType())) {
            dispatchMap.get(packetMsg.getMsgType()).accept(packetMsg);
        }
    }

    private SteamID fixChatID(SteamID steamIdChat) {
        SteamID chatID = new SteamID(steamIdChat.convertToUInt64()); // copy the steamid so we don't modify it

        if (chatID.isClanAccount()) {
            // this steamid is incorrect, so we'll fix it up
            chatID.setAccountInstance(SteamID.ChatInstanceFlags.CLAN.code());
            chatID.setAccountType(EAccountType.Chat);
        }

        return chatID;
    }

    private void handleAccountInfo(IPacketMsg packetMsg) {
        ClientMsgProtobuf<CMsgClientAccountInfo.Builder> accInfo = new ClientMsgProtobuf<>(CMsgClientAccountInfo.class, packetMsg);
        cache.getLocalUser().setName(accInfo.getBody().getPersonaName());
    }

    private void handleFriendMsg(IPacketMsg packetMsg) {
        ClientMsgProtobuf<CMsgClientFriendMsgIncoming.Builder> friendMsg = new ClientMsgProtobuf<>(CMsgClientFriendMsgIncoming.class, packetMsg);
        client.postCallback(new FriendMsgCallback(friendMsg.getBody()));
    }

    private void handleFriendEchoMsg(IPacketMsg packetMsg) {
        ClientMsgProtobuf<CMsgClientFriendMsgIncoming.Builder> friendMsg = new ClientMsgProtobuf<>(CMsgClientFriendMsgIncoming.class, packetMsg);
        client.postCallback(new FriendMsgEchoCallback(friendMsg.getBody()));
    }

    private void handleFriendMessageHistoryResponse(IPacketMsg packetMsg) {
        ClientMsgProtobuf<CMsgClientChatGetFriendMessageHistoryResponse.Builder> historyResponse =
                new ClientMsgProtobuf<>(CMsgClientChatGetFriendMessageHistoryResponse.class, packetMsg);
        client.postCallback(new FriendMsgHistoryCallback(historyResponse.getBody(), client.getUniverse()));
    }

    private void handleFriendsList(IPacketMsg packetMsg) {
        ClientMsgProtobuf<CMsgClientFriendsList.Builder> list = new ClientMsgProtobuf<>(CMsgClientFriendsList.class, packetMsg);

        cache.getLocalUser().setSteamID(client.getSteamID());

        if (!list.getBody().getBincremental()) {
            // if we're not an incremental update, the message contains all friends, so we should clear our current list
            synchronized (listLock) {
                friendList.clear();
                clanList.clear();
            }
        }

        // we have to request information for all of our friends because steam only sends persona information for online friends
        ClientMsgProtobuf<CMsgClientRequestFriendData.Builder> reqInfo = new ClientMsgProtobuf<>(CMsgClientRequestFriendData.class, EMsg.ClientRequestFriendData);

        reqInfo.getBody().setPersonaStateRequested(client.getConfiguration().getDefaultPersonaStateFlags());

        synchronized (listLock) {
            List<SteamID> friendsToRemove = new ArrayList<>();
            List<SteamID> clansToRemove = new ArrayList<>();

            for (CMsgClientFriendsList.Friend friendObj : list.getBody().getFriendsList()) {
                SteamID friendID = new SteamID(friendObj.getUlfriendid());

                if (friendID.isIndividualAccount()) {
                    User user = cache.getUser(friendID);

                    user.setRelationship(EFriendRelationship.from(friendObj.getEfriendrelationship()));

                    if (friendList.contains(friendID)) {
                        // if this is a friend on our list, and they removed us, mark them for removal
                        if (user.getRelationship() == EFriendRelationship.None) {
                            friendsToRemove.add(friendID);
                        }
                    } else {
                        // we don't know about this friend yet, lets add them
                        friendList.add(friendID);
                    }
                } else if (friendID.isClanAccount()) {
                    Clan clan = cache.getClan(friendID);

                    clan.setRelationship(EClanRelationship.from(friendObj.getEfriendrelationship()));

                    if (clanList.contains(friendID)) {
                        // mark clans we were removed/kicked from
                        // note: not actually sure about the kicked relationship, but i'm using it for good measure
                        if (clan.getRelationship() == EClanRelationship.None || clan.getRelationship() == EClanRelationship.Kicked) {
                            clansToRemove.add(friendID);
                        }
                    } else {
                        clanList.add(friendID);
                    }
                }

                if (!list.getBody().getBincremental()) {
                    // request persona state for our friend & clan list when it's a non-incremental update
                    reqInfo.getBody().addFriends(friendID.convertToUInt64());
                }
            }

            // remove anything we marked for removal
            for (SteamID f : friendsToRemove) {
                friendList.remove(f);
            }
            for (SteamID c : clansToRemove) {
                clanList.remove(c);
            }
        }

        if (reqInfo.getBody().getFriendsCount() > 0) {
            client.send(reqInfo);
        }

        client.postCallback(new FriendsListCallback(list.getBody()));
    }

    private void handlePersonaState(IPacketMsg packetMsg) {
        ClientMsgProtobuf<CMsgClientPersonaState.Builder> persState = new ClientMsgProtobuf<>(CMsgClientPersonaState.class, packetMsg);

        int flags = persState.getBody().getStatusFlags();

        for (CMsgClientPersonaState.Friend friend : persState.getBody().getFriendsList()) {
            SteamID friendID = new SteamID(friend.getFriendid());

            if (friendID.isIndividualAccount()) {
                User cacheFriend = cache.getUser(friendID);

                if ((flags & EClientPersonaStateFlag.PlayerName.code()) == EClientPersonaStateFlag.PlayerName.code()) {
                    cacheFriend.setName(friend.getPlayerName());
                }

                if ((flags & EClientPersonaStateFlag.Presence.code()) == EClientPersonaStateFlag.Presence.code()) {
                    cacheFriend.setAvatarHash(friend.getAvatarHash().toByteArray());
                    cacheFriend.setPersonaState(EPersonaState.from(friend.getPersonaState()));
                    cacheFriend.setPersonaStateFlags(friend.getPersonaStateFlags());
                }

                if ((flags & EClientPersonaStateFlag.GameDataBlob.code()) == EClientPersonaStateFlag.GameDataBlob.code()) {
                    cacheFriend.setGameName(friend.getGameName());
                    cacheFriend.setGameID(new GameID(friend.getGameid()));
                    cacheFriend.setGameAppID(friend.getGamePlayedAppId());
                }
            } else if (friendID.isClanAccount()) {
                Clan cacheClan = cache.getClan(friendID);

                if ((flags & EClientPersonaStateFlag.PlayerName.code()) == EClientPersonaStateFlag.PlayerName.code()) {
                    cacheClan.setName(friend.getPlayerName());
                }

                if ((flags & EClientPersonaStateFlag.Presence.code()) == EClientPersonaStateFlag.Presence.code()) {
                    cacheClan.setAvatarHash(friend.getAvatarHash().toByteArray());
                }
            }

            // TODO: 2018-02-26 cache other details/account types?
        }

        client.postCallback(new PersonaStatesCallback(persState.getBody()));
    }

    private void handleClanState(IPacketMsg packetMsg) {
        ClientMsgProtobuf<CMsgClientClanState.Builder> clanState = new ClientMsgProtobuf<>(CMsgClientClanState.class, packetMsg);
        client.postCallback(new ClanStateCallback(clanState.getBody()));
    }

    private void handleFriendResponse(IPacketMsg packetMsg) {
        ClientMsgProtobuf<CMsgClientAddFriendResponse.Builder> friendResponse = new ClientMsgProtobuf<>(CMsgClientAddFriendResponse.class, packetMsg);
        client.postCallback(new FriendAddedCallback(friendResponse.getBody()));
    }

    private void handleChatEnter(IPacketMsg packetMsg) {
        ClientMsg<MsgClientChatEnter> chatEnter = new ClientMsg<>(MsgClientChatEnter.class, packetMsg);
        byte[] payload = chatEnter.getPayload().toByteArray();
        client.postCallback(new ChatEnterCallback(chatEnter.getBody(), payload));
    }

    private void handleChatMsg(IPacketMsg packetMsg) {
        ClientMsg<MsgClientChatMsg> chatMsg = new ClientMsg<>(MsgClientChatMsg.class, packetMsg);
        byte[] payload = chatMsg.getPayload().toByteArray();
        client.postCallback(new ChatMsgCallback(chatMsg.getBody(), payload));
    }

    private void handleChatMemberInfo(IPacketMsg packetMsg) {
        ClientMsg<MsgClientChatMemberInfo> membInfo = new ClientMsg<>(MsgClientChatMemberInfo.class, packetMsg);
        byte[] payload = membInfo.getPayload().toByteArray();
        client.postCallback(new ChatMemberInfoCallback(membInfo.getBody(), payload));
    }

    private void handleChatRoomInfo(IPacketMsg packetMsg) {
        ClientMsg<MsgClientChatRoomInfo> roomInfo = new ClientMsg<>(MsgClientChatRoomInfo.class, packetMsg);
        byte[] payload = roomInfo.getPayload().toByteArray();
        client.postCallback(new ChatRoomInfoCallback(roomInfo.getBody(), payload));
    }

    private void handleChatActionResult(IPacketMsg packetMsg) {
        ClientMsg<MsgClientChatActionResult> actionResult = new ClientMsg<>(MsgClientChatActionResult.class, packetMsg);
        client.postCallback(new ChatActionResultCallback(actionResult.getBody()));
    }

    private void handleChatInvite(IPacketMsg packetMsg) {
        ClientMsgProtobuf<CMsgClientChatInvite.Builder> chatInvite = new ClientMsgProtobuf<>(CMsgClientChatInvite.class, packetMsg);
        client.postCallback(new ChatInviteCallback(chatInvite.getBody()));
    }

    private void handleIgnoreFriendResponse(IPacketMsg packetMsg) {
        ClientMsg<MsgClientSetIgnoreFriendResponse> response = new ClientMsg<>(MsgClientSetIgnoreFriendResponse.class, packetMsg);
        client.postCallback(new IgnoreFriendCallback(response.getTargetJobID(), response.getBody()));
    }

    private void handleProfileInfoResponse(IPacketMsg packetMsg) {
        ClientMsgProtobuf<CMsgClientFriendProfileInfoResponse.Builder> response = new ClientMsgProtobuf<>(CMsgClientFriendProfileInfoResponse.class, packetMsg);
        client.postCallback(new ProfileInfoCallback(new JobID(packetMsg.getTargetJobID()), response.getBody()));
    }

    private void handlePersonaChangeResponse(IPacketMsg packetMsg) {
        ClientMsgProtobuf<CMsgPersonaChangeResponse.Builder> response = new ClientMsgProtobuf<>(CMsgPersonaChangeResponse.class, packetMsg);

        // update our cache to what steam says our name is
        cache.getLocalUser().setName(response.getBody().getPlayerName());

        client.postCallback(new PersonaChangeCallback(new JobID(packetMsg.getTargetJobID()), response.getBody()));
    }

}
