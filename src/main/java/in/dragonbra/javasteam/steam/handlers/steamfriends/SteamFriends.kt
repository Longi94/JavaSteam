package `in`.dragonbra.javasteam.steam.handlers.steamfriends

import com.google.protobuf.ByteString
import `in`.dragonbra.javasteam.base.ClientMsg
import `in`.dragonbra.javasteam.base.ClientMsgProtobuf
import `in`.dragonbra.javasteam.base.IPacketMsg
import `in`.dragonbra.javasteam.enums.EChatAction
import `in`.dragonbra.javasteam.enums.EChatEntryType
import `in`.dragonbra.javasteam.enums.EChatInfoType
import `in`.dragonbra.javasteam.enums.EChatMemberStateChange
import `in`.dragonbra.javasteam.enums.EClanRelationship
import `in`.dragonbra.javasteam.enums.EClientPersonaStateFlag
import `in`.dragonbra.javasteam.enums.EFriendRelationship
import `in`.dragonbra.javasteam.enums.EMsg
import `in`.dragonbra.javasteam.enums.EPersonaState
import `in`.dragonbra.javasteam.enums.EPersonaStateFlag
import `in`.dragonbra.javasteam.generated.MsgClientChatAction
import `in`.dragonbra.javasteam.generated.MsgClientChatMemberInfo
import `in`.dragonbra.javasteam.generated.MsgClientChatMsg
import `in`.dragonbra.javasteam.generated.MsgClientJoinChat
import `in`.dragonbra.javasteam.generated.MsgClientSetIgnoreFriend
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver.CMsgClientAMGetPersonaNameHistory
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver.CMsgClientChatInvite
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver2.CMsgClientChatGetFriendMessageHistory
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver2.CMsgClientChatGetFriendMessageHistoryForOfflineMessages
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver2.CMsgClientChatGetFriendMessageHistoryResponse
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverFriends.CMsgClientAddFriend
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverFriends.CMsgClientChangeStatus
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverFriends.CMsgClientFriendMsg
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverFriends.CMsgClientFriendProfileInfo
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverFriends.CMsgClientFriendsList
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverFriends.CMsgClientPersonaState
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverFriends.CMsgClientRemoveFriend
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverFriends.CMsgClientRequestFriendData
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverFriends.CMsgClientSetPlayerNickname
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverFriends.CMsgPersonaChangeResponse
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverLogin
import `in`.dragonbra.javasteam.steam.handlers.ClientMsgHandler
import `in`.dragonbra.javasteam.steam.handlers.steamfriends.cache.AccountCache
import `in`.dragonbra.javasteam.steam.handlers.steamfriends.cache.Clan
import `in`.dragonbra.javasteam.steam.handlers.steamfriends.cache.User
import `in`.dragonbra.javasteam.steam.handlers.steamfriends.callback.AliasHistoryCallback
import `in`.dragonbra.javasteam.steam.handlers.steamfriends.callback.ChatActionResultCallback
import `in`.dragonbra.javasteam.steam.handlers.steamfriends.callback.ChatEnterCallback
import `in`.dragonbra.javasteam.steam.handlers.steamfriends.callback.ChatInviteCallback
import `in`.dragonbra.javasteam.steam.handlers.steamfriends.callback.ChatMemberInfoCallback
import `in`.dragonbra.javasteam.steam.handlers.steamfriends.callback.ChatMsgCallback
import `in`.dragonbra.javasteam.steam.handlers.steamfriends.callback.ChatRoomInfoCallback
import `in`.dragonbra.javasteam.steam.handlers.steamfriends.callback.ClanStateCallback
import `in`.dragonbra.javasteam.steam.handlers.steamfriends.callback.FriendAddedCallback
import `in`.dragonbra.javasteam.steam.handlers.steamfriends.callback.FriendMsgCallback
import `in`.dragonbra.javasteam.steam.handlers.steamfriends.callback.FriendMsgEchoCallback
import `in`.dragonbra.javasteam.steam.handlers.steamfriends.callback.FriendMsgHistoryCallback
import `in`.dragonbra.javasteam.steam.handlers.steamfriends.callback.FriendsListCallback
import `in`.dragonbra.javasteam.steam.handlers.steamfriends.callback.IgnoreFriendCallback
import `in`.dragonbra.javasteam.steam.handlers.steamfriends.callback.NicknameCallback
import `in`.dragonbra.javasteam.steam.handlers.steamfriends.callback.NicknameListCallback
import `in`.dragonbra.javasteam.steam.handlers.steamfriends.callback.PersonaChangeCallback
import `in`.dragonbra.javasteam.steam.handlers.steamfriends.callback.PersonaStateCallback
import `in`.dragonbra.javasteam.steam.handlers.steamfriends.callback.ProfileInfoCallback
import `in`.dragonbra.javasteam.steam.handlers.steamuser.callback.AccountInfoCallback
import `in`.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg
import `in`.dragonbra.javasteam.steam.steamclient.configuration.SteamConfiguration
import `in`.dragonbra.javasteam.types.AsyncJobSingle
import `in`.dragonbra.javasteam.types.GameID
import `in`.dragonbra.javasteam.types.JobID
import `in`.dragonbra.javasteam.types.SteamID
import `in`.dragonbra.javasteam.util.log.LogManager
import `in`.dragonbra.javasteam.util.log.Logger
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.*

/**
 * This handler handles all interaction with other users on the Steam3 network.
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
class SteamFriends : ClientMsgHandler() {

    var friendsList: MutableList<SteamID> = mutableListOf()
        private set
    var clanList: MutableList<SteamID> = mutableListOf()
        private set

    private var cache: AccountCache = AccountCache()

    /**
     * Gets a list of all caches users.
     *
     * @return a list of [User]
     */
    fun getCachedUsers(): List<User> = cache.users.getList()

    /**
     * Gets a list of all cached clans.
     *
     * @return a list of [Clan]
     */
    fun getCachedClans(): List<Clan> = cache.clans.getList()

    /**
     * Gets result if the given steam ID is the local user or not.
     *
     * @param steamID the steam ID of the local logged-in user
     * @return true if the account is the local user, otherwise false.
     */
    @JvmOverloads
    fun isLocalUser(steamID: SteamID? = null): Boolean = cache.isLocalUser(steamID ?: client.steamID)

    /**
     * Gets the steam ID from the cached account.
     *
     * @param steamID the steam ID to check the cache.
     * @return The [SteamID] of the cached user.
     */
    fun getFriendSteamID(steamID: SteamID): SteamID = cache.getUser(steamID).steamID // Why not...

    /**
     * Gets the steam ID from the cached clans account.
     *
     * @param steamID the steam ID to check the cache.
     * @return The [SteamID] of the cached clan.
     */
    fun getClanSteamID(steamID: SteamID): SteamID = cache.clans.getAccount(steamID).steamID // Why not...

    /**
     * Gets the local user's persona name. Will be null before user initialization.
     * User initialization is performed prior to [AccountInfoCallback] callback.
     *
     * @return The name.
     */
    fun getPersonaName(): String? = cache.localUser.name

    /**
     * Gets the local user's persona avatar hash. Will be null before user initialization.
     *
     * @return The avatar hash.
     */
    fun getPersonaAvatar(): ByteArray? = cache.localUser.avatarHash

    /**
     * Sets the local user's persona name and broadcasts it over the network.
     * Results are returned in a [PersonaChangeCallback] callback.
     *
     * @param name The name.
     */
    fun setPersonaName(name: String) {
        // cache the local name right away, so that early calls to SetPersonaState don't reset the set name
        cache.localUser.name = name

        ClientMsgProtobuf<CMsgClientChangeStatus.Builder>(
            CMsgClientChangeStatus::class.java,
            EMsg.ClientChangeStatus
        ).apply {
            body.personaState = cache.localUser.personaState.code()
            body.playerName = name
        }.also(client::send)
    }

    /**
     * Gets the local user's persona state.
     * @return The persona state.
     */
    fun getPersonaState(): EPersonaState = cache.localUser.personaState

    /**
     * Sets the local user's persona state and broadcasts it over the network.
     * Results are returned in a[PersonaChangeCallback] callback.
     *
     * @param state The state.
     */
    fun setPersonaState(state: EPersonaState) {
        cache.localUser.personaState = state

        ClientMsgProtobuf<CMsgClientChangeStatus.Builder>(
            CMsgClientChangeStatus::class.java,
            EMsg.ClientChangeStatus
        ).apply {
            body.personaState = state.code()
            body.personaSetByUser = true
        }.also(client::send)
    }

    /**
     * JavaSteam addition:
     * Sets the local user's persona state flag back to normal desktop mode.
     */
    fun resetPersonaStateFlag() {
        ClientMsgProtobuf<CMsgClientChangeStatus.Builder>(
            CMsgClientChangeStatus::class.java,
            EMsg.ClientChangeStatus
        ).apply {
            body.personaSetByUser = true
            body.personaStateFlags = 0
        }.also(client::send)
    }

    /**
     * JavaSteam addition:
     * Sets the local user's persona state flag to a valid ClientType
     *
     * @param flag one of the following
     * [EPersonaStateFlag.ClientTypeWeb],
     * [EPersonaStateFlag.ClientTypeMobile],
     * [EPersonaStateFlag.ClientTypeTenfoot],
     * or [EPersonaStateFlag.ClientTypeVR].
     */
    fun setPersonaStateFlag(flag: EPersonaStateFlag) {
        require(!(flag.code() < EPersonaStateFlag.ClientTypeWeb.code() || flag.code() > EPersonaStateFlag.ClientTypeVR.code())) { "Persona State Flag was not a valid ClientType" }

        ClientMsgProtobuf<CMsgClientChangeStatus.Builder>(
            CMsgClientChangeStatus::class.java,
            EMsg.ClientChangeStatus
        ).apply {
            body.personaSetByUser = true
            body.personaStateFlags = flag.code()
        }.also(client::send)
    }

    /**
     * Gets the friend count of the local user.
     *
     * @return The number of friends.
     */
    fun getFriendCount(): Int = friendsList.size

    /**
     * Gets a friend by index.
     *
     * @param index The index.
     * @return A valid steamid of a friend if the index is in range; otherwise a steamid representing 0.
     */
    fun getFriendByIndex(index: Int): SteamID =
        if (index < 0 || index >= friendsList.size) SteamID(0) else friendsList[index]

    /**
     * Gets the persona name of a friend.
     *
     * @param steamID The steam id.
     * @return The name.
     */
    fun getFriendPersonaName(steamID: SteamID): String? = cache.getUser(steamID).name

    /**
     * Gets the persona state of a friend.
     *
     * @param steamID The steam id.
     * @return The persona state.
     */
    fun getFriendPersonaState(steamID: SteamID): EPersonaState = cache.getUser(steamID).personaState

    /**
     * Gets the relationship of a friend.
     *
     * @param steamID The steam id.
     * @return The relationship of the friend to the local user.
     */
    fun getFriendRelationship(steamID: SteamID): EFriendRelationship? = cache.getUser(steamID).relationship

    /**
     * Gets the game name of a friend playing a game.
     *
     * @param steamID The steam id.
     * @return The game name of a friend playing a game, or null if they haven't been cached yet.
     */
    fun getFriendGamePlayedName(steamID: SteamID): String? = cache.getUser(steamID).gameName

    /**
     * Gets the GameID of a friend playing a game.
     *
     * @param steamID The steam id.
     * @return The gameid of a friend playing a game, or 0 if they haven't been cached yet.
     */
    fun getFriendGamePlayed(steamID: SteamID): GameID = cache.getUser(steamID).gameID

    /**
     * Gets an SHA-1 hash representing the friend's avatar.
     *
     * @param steamID >The SteamID of the friend to get the avatar of.
     * @return A byte array representing a SHA-1 hash of the friend's avatar.
     */
    fun getFriendAvatar(steamID: SteamID): ByteArray? = cache.getUser(steamID).avatarHash

    /**
     * Gets the PersonaState Flags of a friend.
     *
     * @param steamID The steam id.
     * @return and [EnumSet] of [EPersonaStateFlag]
     */
    fun getFriendPersonaStateFlags(steamID: SteamID): EnumSet<EPersonaStateFlag>? =
        cache.getUser(steamID).personaStateFlags

    /**
     * Gets the game app id of a friend.
     *
     * @param steamID The steam id.
     * @return the game app id or 0 if not playing.
     */
    fun getFriendGameAppId(steamID: SteamID): Int = cache.getUser(steamID).gameAppID

    /**
     * Gets the count of clans the local user is a member of.
     * @return The number of clans this user is a member of.
     */
    fun getClanCount(): Int = clanList.size

    /**
     * Gets a clan SteamID by index.
     *
     * @param index The index.
     * @return A valid steamid of a clan if the index is in range; otherwise a steamid representing 0.
     */
    fun getClanByIndex(index: Int): SteamID =
        if (index < 0 || index >= clanList.size) SteamID(0) else clanList[index]

    /**
     * Gets the name of a clan.
     *
     * @param steamID The clan SteamID.
     * @return The name.
     */
    fun getClanName(steamID: SteamID): String? = cache.clans.getAccount(steamID).name

    /**
     * Gets the relationship of a clan.
     *
     * @param steamID The clan steamid.
     * @return The relationship of the clan to the local user.
     */
    fun getClanRelationship(steamID: SteamID): EClanRelationship? = cache.clans.getAccount(steamID).relationship

    /**
     * Gets an SHA-1 hash representing the clan's avatar.
     *
     * @param steamID The SteamID of the clan to get the avatar of.
     * @return A byte array representing a SHA-1 hash of the clan's avatar, or null if the clan could not be found.
     */
    fun getClanAvatar(steamID: SteamID): ByteArray? = cache.clans.getAccount(steamID).avatarHash

    /**
     * Sends a chat message to a friend.
     *
     * @param target  The target to send to.
     * @param type    The type of message to send.
     * @param message The message to send.
     */
    fun sendChatMessage(target: SteamID, type: EChatEntryType, message: String) {
        ClientMsgProtobuf<CMsgClientFriendMsg.Builder>(
            CMsgClientFriendMsg::class.java,
            EMsg.ClientFriendMsg
        ).apply {
            body.steamid = target.convertToUInt64()
            body.chatEntryType = type.code()
            body.message = ByteString.copyFrom(message, StandardCharsets.UTF_8)
        }.also(client::send)
    }

    /**
     * Sends a friend request to a user.
     *
     * @param accountNameOrEmail The account name or email of the user.
     */
    fun addFriend(accountNameOrEmail: String) {
        ClientMsgProtobuf<CMsgClientAddFriend.Builder>(
            CMsgClientAddFriend::class.java,
            EMsg.ClientAddFriend
        ).apply {
            body.accountnameOrEmailToAdd = accountNameOrEmail
        }.also(client::send)
    }

    /**
     * Sends a friend request to a user.
     *
     * @param steamID The SteamID of the friend to add.
     */
    fun addFriend(steamID: SteamID) {
        ClientMsgProtobuf<CMsgClientAddFriend.Builder>(
            CMsgClientAddFriend::class.java,
            EMsg.ClientAddFriend
        ).apply {
            body.steamidToAdd = steamID.convertToUInt64()
        }.also(client::send)
    }

    /**
     * Removes a friend from your friends list.
     *
     * @param steamID The SteamID of the friend to remove.
     */
    fun removeFriend(steamID: SteamID) {
        ClientMsgProtobuf<CMsgClientRemoveFriend.Builder>(
            CMsgClientRemoveFriend::class.java,
            EMsg.ClientRemoveFriend
        ).apply {
            body.friendid = steamID.convertToUInt64()
        }.also(client::send)
    }

    /**
     * Attempts to join a chat room.
     *
     * @param steamID The SteamID of the chat room.
     */
    fun joinChat(steamID: SteamID) {
        val chatID: SteamID = fixChatID(steamID) // copy the steamid so we don't modify it

        ClientMsg(MsgClientJoinChat::class.java).apply {
            body.steamIdChat = chatID
        }.also(client::send)
    }

    /**
     * Attempts to leave a chat room.
     *
     * @param steamID The SteamID of the chat room.
     */
    fun leaveChat(steamID: SteamID) {
        val chatID: SteamID = fixChatID(steamID) // copy the steamid so we don't modify it

        ClientMsg(MsgClientChatMemberInfo::class.java).apply {
            body.steamIdChat = chatID
            body.type = EChatInfoType.StateChange

            // SteamID can be null if not connected - will be ultimately ignored in Client.Send.
            val localSteamID = client.steamID?.convertToUInt64() ?: SteamID().convertToUInt64()

            try {
                writeLong(localSteamID) // ChatterActedOn
                writeInt(EChatMemberStateChange.Left.code()) // StateChange
                writeLong(localSteamID) // ChatterActedBy
            } catch (e: IOException) {
                logger.debug(e)
            }
        }.also(client::send)
    }

    /**
     * Sends a message to a chat room.
     *
     * @param steamIdChat The SteamID of the chat room.
     * @param type        The message type.
     * @param message     The message.
     */
    fun sendChatRoomMessage(steamIdChat: SteamID, type: EChatEntryType, message: String) {
        val chatID: SteamID = fixChatID(steamIdChat) // copy the steamid so we don't modify it

        ClientMsg(MsgClientChatMsg::class.java).apply {
            body.chatMsgType = type
            body.steamIdChatRoom = chatID
            body.steamIdChatter = client.steamID

            try {
                writeNullTermString(message, StandardCharsets.UTF_8)
            } catch (e: IOException) {
                logger.debug(e)
            }
        }.also(client::send)
    }

    /**
     * Invites a user to a chat room.
     * The results of this action will be available through the [ChatActionResultCallback] callback.
     *
     * @param steamIdUser The SteamID of the user to invite.
     * @param steamIdChat The SteamID of the chat room to invite the user to.
     */
    fun inviteUserToChat(steamIdUser: SteamID, steamIdChat: SteamID) {
        val chatID: SteamID = fixChatID(steamIdChat) // copy the steamid so we don't modify it

        ClientMsgProtobuf<CMsgClientChatInvite.Builder>(
            CMsgClientChatInvite::class.java,
            EMsg.ClientChatInvite
        ).apply {
            body.steamIdChat = chatID.convertToUInt64()
            body.steamIdInvited = steamIdUser.convertToUInt64()
            // steamclient also sends the steamid of the user that did the invitation
            // we'll mimic that behavior
            body.steamIdPatron = client.steamID?.convertToUInt64() ?: SteamID().convertToUInt64()
        }.also(client::send)
    }

    /**
     * Kicks the specified chat member from the given chat room.
     *
     * @param steamIdChat   The SteamID of chat room to kick the member from.
     * @param steamIdMember The SteamID of the member to kick from the chat.
     */
    fun kickChatMember(steamIdChat: SteamID, steamIdMember: SteamID) {
        val chatID: SteamID = fixChatID(steamIdChat) // copy the steamid so we don't modify it

        ClientMsg(MsgClientChatAction::class.java).apply {
            body.steamIdChat = chatID
            body.steamIdUserToActOn = steamIdMember

            body.chatAction = EChatAction.Kick
        }.also(client::send)
    }

    /**
     * Bans the specified chat member from the given chat room.
     *
     * @param steamIdChat   The SteamID of chat room to ban the member from.
     * @param steamIdMember The SteamID of the member to ban from the chat.
     */
    fun banChatMember(steamIdChat: SteamID, steamIdMember: SteamID) {
        val chatID: SteamID = fixChatID(steamIdChat) // copy the steamid so we don't modify it

        ClientMsg(MsgClientChatAction::class.java).apply {
            body.steamIdChat = chatID
            body.steamIdUserToActOn = steamIdMember

            body.chatAction = EChatAction.Ban
        }.also(client::send)
    }

    /**
     * Unbans the specified chat member from the given chat room.
     *
     * @param steamIdChat   The SteamID of chat room to unban the member from.
     * @param steamIdMember The SteamID of the member to unban from the chat.
     */
    fun unbanChatMember(steamIdChat: SteamID, steamIdMember: SteamID) {
        val chatID: SteamID = fixChatID(steamIdChat) // copy the steamid so we don't modify it

        ClientMsg(MsgClientChatAction::class.java).apply {
            body.steamIdChat = chatID
            body.steamIdUserToActOn = steamIdMember

            body.chatAction = EChatAction.UnBan
        }.also(client::send)
    }

    /**
     * Requests persona state for a list of specified SteamID.
     * Results are returned in [PersonaStateCallback].
     *
     * @param steamIdList   A list of SteamIDs to request the info of.
     * @param requestedInfo The requested info flags. If none specified, this uses [SteamConfiguration.defaultPersonaStateFlags].
     */
    @JvmOverloads
    fun requestFriendInfo(steamIdList: List<SteamID>, requestedInfo: Int = 0) {
        var info = requestedInfo

        if (info == 0) {
            info = EClientPersonaStateFlag.code(client.configuration.defaultPersonaStateFlags)
        }

        ClientMsgProtobuf<CMsgClientRequestFriendData.Builder>(
            CMsgClientRequestFriendData::class.java,
            EMsg.ClientRequestFriendData
        ).apply {
            body.addAllFriends(steamIdList.map { it.convertToUInt64() })
            body.personaStateRequested = info
        }.also(client::send)
    }

    /**
     * Requests persona state for a specified SteamID.
     * Results are returned in [PersonaStateCallback].
     *
     * @param steamID A SteamID to request the info of.
     * @param requestedInfo The requested info flags. If none specified, this uses [SteamConfiguration.defaultPersonaStateFlags].
     */
    @JvmOverloads
    fun requestFriendInfo(steamID: SteamID, requestedInfo: Int = 0) {
        requestFriendInfo(listOf(steamID), requestedInfo)
    }

    /**
     * Ignores or un-ignores a friend on Steam.
     * Results are returned in a [IgnoreFriendCallback].
     *
     * @param steamID The SteamID of the friend to ignore or un-ignore.
     * @param setIgnore if set to **true**, the friend will be ignored; otherwise, they will be un-ignored.
     * @return The Job ID of the request. This can be used to find the appropriate [IgnoreFriendCallback].
     */
    @JvmOverloads
    fun ignoreFriend(steamID: SteamID, setIgnore: Boolean = true): AsyncJobSingle<IgnoreFriendCallback> {
        val ignore = ClientMsg(MsgClientSetIgnoreFriend::class.java).apply {
            sourceJobID = client.getNextJobID()

            body.mySteamId = client.steamID
            body.ignore = if (setIgnore) 1.toByte() else 0.toByte()
            body.steamIdFriend = steamID
        }

        client.send(ignore)

        return AsyncJobSingle(client, ignore.sourceJobID)
    }

    /**
     * Requests profile information for the given [SteamID]
     * Results are returned in a [ProfileInfoCallback]
     *
     * @param steamID The SteamID of the friend to request the details of.
     * @return The Job ID of the request. This can be used to find the appropriate [ProfileInfoCallback].
     */
    fun requestProfileInfo(steamID: SteamID): AsyncJobSingle<ProfileInfoCallback> {
        val request = ClientMsgProtobuf<CMsgClientFriendProfileInfo.Builder>(
            CMsgClientFriendProfileInfo::class.java,
            EMsg.ClientFriendProfileInfo
        ).apply {
            sourceJobID = client.getNextJobID()

            body.steamidFriend = steamID.convertToUInt64()
        }

        client.send(request)

        return AsyncJobSingle(client, request.sourceJobID)
    }

    /**
     * Requests the last few chat messages with a friend.
     * Results are returned in a [FriendMsgHistoryCallback]
     *
     * @param steamID SteamID of the friend
     */
    fun requestMessageHistory(steamID: SteamID) {
        ClientMsgProtobuf<CMsgClientChatGetFriendMessageHistory.Builder>(
            CMsgClientChatGetFriendMessageHistory::class.java,
            EMsg.ClientChatGetFriendMessageHistory
        ).apply {
            body.steamid = steamID.convertToUInt64()
        }.also(client::send)
    }

    /**
     * Requests all offline messages.
     * This also marks them as read server side.
     * Results are returned in a [FriendMsgHistoryCallback].
     */
    fun requestOfflineMessages() {
        ClientMsgProtobuf<CMsgClientChatGetFriendMessageHistoryForOfflineMessages.Builder>(
            CMsgClientChatGetFriendMessageHistoryForOfflineMessages::class.java,
            EMsg.ClientChatGetFriendMessageHistoryForOfflineMessages
        ).also(client::send)
    }

    /**
     * Set the nickname of a friend.
     * The result is returned in a [NicknameCallback].
     *
     * @param friendID the steam id of the friend
     * @param nickname the nickname to set to
     * @return The Job ID of the request. This can be used to find the appropriate [NicknameCallback].
     */
    fun setFriendNickname(friendID: SteamID, nickname: String): JobID {
        val jobID: JobID = client.getNextJobID()
        val request = ClientMsgProtobuf<CMsgClientSetPlayerNickname.Builder>(
            CMsgClientSetPlayerNickname::class.java,
            EMsg.AMClientSetPlayerNickname
        ).apply {
            sourceJobID = jobID

            body.steamid = friendID.convertToUInt64()
            body.nickname = nickname
        }

        client.send(request)

        return jobID
    }

    /**
     * Request the alias history of the account of the given steam id.
     * The result is returned in a [AliasHistoryCallback].
     *
     * @param steamID the steam id
     * @return The Job ID of the request. This can be used to find the appropriate [AliasHistoryCallback].
     */
    fun requestAliasHistory(steamID: SteamID): JobID = requestAliasHistory(listOf(steamID))

    /**
     * Request the alias history of the accounts of the given steam ids.
     * The result is returned in a [AliasHistoryCallback].
     *
     * @param steamIDs the steam ids
     * @return The Job ID of the request. This can be used to find the appropriate [AliasHistoryCallback].
     */
    fun requestAliasHistory(steamIDs: List<SteamID>): JobID {
        val jobID: JobID = client.getNextJobID()
        val request = ClientMsgProtobuf<CMsgClientAMGetPersonaNameHistory.Builder>(
            CMsgClientAMGetPersonaNameHistory::class.java,
            EMsg.ClientAMGetPersonaNameHistory
        ).apply {
            sourceJobID = jobID

            body.addAllIds(
                steamIDs.map {
                    CMsgClientAMGetPersonaNameHistory.IdInstance.newBuilder().setSteamid(it.convertToUInt64()).build()
                }
            )

            body.idCount = body.idsCount
        }

        client.send(request)

        return jobID
    }

    private fun fixChatID(steamIdChat: SteamID): SteamID {
        var chatID = SteamID(steamIdChat.convertToUInt64()) // copy the steamid so we don't modify it

        if (chatID.isClanAccount) {
            // this steamid is incorrect, so we'll fix it up
            chatID = chatID.toChatID()
        }

        return chatID
    }

    /**
     * Handles a client message. This should not be called directly.
     * @param packetMsg The packet message that contains the data.
     */
    override fun handleMsg(packetMsg: IPacketMsg) {
        val callback = getCallback(packetMsg)

        // Ignore messages that we don't have a handler function for
        if (callback != null) {
            client.postCallback(callback)
            return
        }

        // Special handling for some messages because they need access to client or post callbacks differently
        when (packetMsg.msgType) {
            EMsg.ClientPersonaState -> handlePersonaState(packetMsg)
            EMsg.ClientFriendsList -> handleFriendsList(packetMsg)
            EMsg.ClientChatGetFriendMessageHistoryResponse -> handleFriendMessageHistoryResponse(packetMsg)
            EMsg.ClientAccountInfo -> handleAccountInfo(packetMsg)
            EMsg.ClientPersonaChangeResponse -> handlePersonaChangeResponse(packetMsg)
            else -> Unit
        }
    }

    private fun handlePersonaState(packetMsg: IPacketMsg) {
        val perState = ClientMsgProtobuf<CMsgClientPersonaState.Builder>(
            CMsgClientPersonaState::class.java,
            packetMsg
        )

        val flags = EClientPersonaStateFlag.from(perState.body.statusFlags)

        perState.body.friendsList.forEach { friend ->
            val friendID = SteamID(friend.friendid)

            if (friendID.isIndividualAccount) {
                val cacheFriend = cache.getUser(friendID)

                if (EClientPersonaStateFlag.PlayerName in flags) {
                    cacheFriend.name = friend.playerName
                }

                if (EClientPersonaStateFlag.Presence in flags) {
                    cacheFriend.avatarHash = friend.avatarHash.toByteArray()
                    cacheFriend.personaState = EPersonaState.from(friend.personaState) ?: EPersonaState.Offline
                    cacheFriend.personaStateFlags = EPersonaStateFlag.from(friend.personaStateFlags)
                }

                if (EClientPersonaStateFlag.GameDataBlob in flags) {
                    cacheFriend.gameName = friend.gameName
                    cacheFriend.gameID = GameID(friend.gameid)
                    cacheFriend.gameAppID = friend.gamePlayedAppId
                }
            } else if (friendID.isClanAccount) {
                val cacheClan = cache.clans.getAccount(friendID)

                if (EClientPersonaStateFlag.PlayerName in flags) {
                    cacheClan.name = friend.playerName
                }

                if (EClientPersonaStateFlag.Presence in flags) {
                    cacheClan.avatarHash = friend.avatarHash.toByteArray()
                }
            } else {
                logger.debug("Unknown item in handlePersonaState(): $friendID")
            }

            // todo: (SK) cache other details/account types?
        }

        perState.body.friendsList.forEach { friend ->
            PersonaStateCallback(friend, flags).also(client::postCallback)
        }
    }

    private fun handleFriendsList(packetMsg: IPacketMsg) {
        val list = ClientMsgProtobuf<CMsgClientFriendsList.Builder>(CMsgClientFriendsList::class.java, packetMsg)

        client.steamID?.let {
            cache.localUser.steamID = it
        }

        if (!list.body.bincremental) {
            // if we're not an incremental update, the message contains all friends, so we should clear our current list
            friendsList.clear()
            clanList.clear()
        }

        // we have to request information for all of our friends because steam only sends persona information for online friends
        val reqInfo = ClientMsgProtobuf<CMsgClientRequestFriendData.Builder>(
            CMsgClientRequestFriendData::class.java,
            EMsg.ClientRequestFriendData
        )

        reqInfo.body.setPersonaStateRequested(
            EClientPersonaStateFlag.code(client.configuration.defaultPersonaStateFlags)
        )

        val friendsToRemove = mutableListOf<SteamID>()
        val clansToRemove = mutableListOf<SteamID>()

        list.body.friendsList.forEach { friendObj ->
            val friendID = SteamID(friendObj.ulfriendid)

            if (friendID.isIndividualAccount) {
                val user = cache.getUser(friendID)

                user.relationship = EFriendRelationship.from(friendObj.efriendrelationship)

                if (friendsList.contains(friendID)) {
                    // if this is a friend on our list, and they removed us, mark them for removal
                    if (user.relationship == EFriendRelationship.None) {
                        friendsToRemove.add(friendID)
                    }
                } else {
                    // we don't know about this friend yet, lets add them
                    friendsList.add(friendID)
                }
            } else if (friendID.isClanAccount) {
                val clan = cache.clans.getAccount(friendID)

                clan.relationship = EClanRelationship.from(friendObj.efriendrelationship) ?: EClanRelationship.None

                if (clanList.contains(friendID)) {
                    // mark clans we were removed/kicked from
                    // note: not actually sure about the kicked relationship, but I'm using it for good measure
                    if (clan.relationship == EClanRelationship.None || clan.relationship == EClanRelationship.Kicked) {
                        clansToRemove.add(friendID)
                    }
                } else {
                    // don't know about this clan, add it
                    clanList.add(friendID)
                }
            }

            if (!list.body.bincremental) {
                // request persona state for our friend & clan list when it's a non-incremental update
                reqInfo.body.addFriends(friendObj.ulfriendid)
            }
        }

        // remove anything we marked for removal
        friendsToRemove.forEach(friendsList::remove)
        clansToRemove.forEach(clanList::remove)

        if (reqInfo.body.friendsList.isNotEmpty()) {
            client.send(reqInfo)
        }

        FriendsListCallback(list.body).also(client::postCallback)
    }

    private fun handleFriendMessageHistoryResponse(packetMsg: IPacketMsg) {
        val historyResponse = ClientMsgProtobuf<CMsgClientChatGetFriendMessageHistoryResponse.Builder>(
            CMsgClientChatGetFriendMessageHistoryResponse::class.java,
            packetMsg
        )

        FriendMsgHistoryCallback(historyResponse.body, client.universe).also(client::postCallback)
    }

    private fun handleAccountInfo(packetMsg: IPacketMsg) {
        val accInfo = ClientMsgProtobuf<SteammessagesClientserverLogin.CMsgClientAccountInfo.Builder>(
            SteammessagesClientserverLogin.CMsgClientAccountInfo::class.java,
            packetMsg
        )

        // cache off our local name
        cache.localUser.name = accInfo.body.personaName
    }

    private fun handlePersonaChangeResponse(packetMsg: IPacketMsg) {
        val response = ClientMsgProtobuf<CMsgPersonaChangeResponse.Builder>(
            CMsgPersonaChangeResponse::class.java,
            packetMsg
        )

        // update our cache to what steam says our name is
        cache.localUser.name = response.body.playerName

        PersonaChangeCallback(response.targetJobID, response.body).also(client::postCallback)
    }

    companion object {
        private val logger: Logger = LogManager.getLogger(SteamFriends::class.java)

        private fun getCallback(packetMsg: IPacketMsg): CallbackMsg? = when (packetMsg.msgType) {
            EMsg.ClientClanState -> ClanStateCallback(packetMsg)
            EMsg.ClientFriendMsgIncoming -> FriendMsgCallback(packetMsg)
            EMsg.ClientFriendMsgEchoToSender -> FriendMsgEchoCallback(packetMsg)
            EMsg.ClientAddFriendResponse -> FriendAddedCallback(packetMsg)
            EMsg.ClientChatEnter -> ChatEnterCallback(packetMsg)
            EMsg.ClientChatMsg -> ChatMsgCallback(packetMsg)
            EMsg.ClientChatMemberInfo -> ChatMemberInfoCallback(packetMsg)
            EMsg.ClientChatRoomInfo -> ChatRoomInfoCallback(packetMsg)
            EMsg.ClientChatActionResult -> ChatActionResultCallback(packetMsg)
            EMsg.ClientChatInvite -> ChatInviteCallback(packetMsg)
            EMsg.ClientSetIgnoreFriendResponse -> IgnoreFriendCallback(packetMsg)
            EMsg.ClientFriendProfileInfoResponse -> ProfileInfoCallback(packetMsg)
            EMsg.ClientAMGetPersonaNameHistoryResponse -> AliasHistoryCallback(packetMsg)
            EMsg.ClientPlayerNicknameList -> NicknameListCallback(packetMsg)
            EMsg.AMClientSetPlayerNicknameResponse -> NicknameCallback(packetMsg)
            else -> null
        }
    }
}
