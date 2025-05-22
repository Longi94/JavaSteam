package `in`.dragonbra.javasteam.steam.handlers.steammatchmaking

import com.google.protobuf.GeneratedMessage
import `in`.dragonbra.javasteam.base.ClientMsgProtobuf
import `in`.dragonbra.javasteam.base.IPacketMsg
import `in`.dragonbra.javasteam.enums.EChatRoomEnterResponse
import `in`.dragonbra.javasteam.enums.ELobbyType
import `in`.dragonbra.javasteam.enums.EMsg
import `in`.dragonbra.javasteam.enums.EResult
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverMms.CMsgClientMMSCreateLobby
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverMms.CMsgClientMMSCreateLobbyResponse
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverMms.CMsgClientMMSGetLobbyData
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverMms.CMsgClientMMSGetLobbyList
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverMms.CMsgClientMMSGetLobbyListResponse
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverMms.CMsgClientMMSInviteToLobby
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverMms.CMsgClientMMSJoinLobby
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverMms.CMsgClientMMSJoinLobbyResponse
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverMms.CMsgClientMMSLeaveLobby
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverMms.CMsgClientMMSLeaveLobbyResponse
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverMms.CMsgClientMMSLobbyData
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverMms.CMsgClientMMSSetLobbyData
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverMms.CMsgClientMMSSetLobbyDataResponse
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverMms.CMsgClientMMSSetLobbyOwner
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverMms.CMsgClientMMSSetLobbyOwnerResponse
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverMms.CMsgClientMMSUserJoinedLobby
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverMms.CMsgClientMMSUserLeftLobby
import `in`.dragonbra.javasteam.steam.handlers.ClientMsgHandler
import `in`.dragonbra.javasteam.steam.handlers.steamfriends.SteamFriends
import `in`.dragonbra.javasteam.steam.handlers.steammatchmaking.Lobby.Companion.toByteString
import `in`.dragonbra.javasteam.steam.handlers.steammatchmaking.callback.CreateLobbyCallback
import `in`.dragonbra.javasteam.steam.handlers.steammatchmaking.callback.GetLobbyListCallback
import `in`.dragonbra.javasteam.steam.handlers.steammatchmaking.callback.JoinLobbyCallback
import `in`.dragonbra.javasteam.steam.handlers.steammatchmaking.callback.LeaveLobbyCallback
import `in`.dragonbra.javasteam.steam.handlers.steammatchmaking.callback.LobbyDataCallback
import `in`.dragonbra.javasteam.steam.handlers.steammatchmaking.callback.SetLobbyDataCallback
import `in`.dragonbra.javasteam.steam.handlers.steammatchmaking.callback.SetLobbyOwnerCallback
import `in`.dragonbra.javasteam.steam.handlers.steammatchmaking.callback.UserJoinedLobbyCallback
import `in`.dragonbra.javasteam.steam.handlers.steammatchmaking.callback.UserLeftLobbyCallback
import `in`.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg
import `in`.dragonbra.javasteam.types.AsyncJobSingle
import `in`.dragonbra.javasteam.types.JobID
import `in`.dragonbra.javasteam.types.SteamID
import `in`.dragonbra.javasteam.util.NetHelpers
import java.util.concurrent.ConcurrentHashMap

/**
 * This handler is used for creating, joining and obtaining lobby information.
 *
 * @author Lossy
 * @since 2025-05-21
 */
@Suppress("unused")
class SteamMatchmaking : ClientMsgHandler() {

    private fun getHandler(packetMsg: IPacketMsg): ((IPacketMsg) -> Unit)? = when (packetMsg.msgType) {
        EMsg.ClientMMSCreateLobbyResponse -> ::handleCreateLobbyResponse
        EMsg.ClientMMSSetLobbyDataResponse -> ::handleSetLobbyDataResponse
        EMsg.ClientMMSSetLobbyOwnerResponse -> ::handleSetLobbyOwnerResponse
        EMsg.ClientMMSLobbyData -> ::handleLobbyData
        EMsg.ClientMMSGetLobbyListResponse -> ::handleGetLobbyListResponse
        EMsg.ClientMMSJoinLobbyResponse -> ::handleJoinLobbyResponse
        EMsg.ClientMMSLeaveLobbyResponse -> ::handleLeaveLobbyResponse
        EMsg.ClientMMSUserJoinedLobby -> ::handleUserJoinedLobby
        EMsg.ClientMMSUserLeftLobby -> ::handleUserLeftLobby
        else -> null
    }

    private val lobbyManipulationRequests: ConcurrentHashMap<JobID, GeneratedMessage> = ConcurrentHashMap()

    private val lobbyCache: LobbyCache = LobbyCache()

    /**
     * Sends a request to create a lobby.
     * @param appId ID of the app the lobby will belong to.
     * @param lobbyType The lobby type.
     * @param maxMembers The maximum number of members that may occupy the lobby.
     * @param lobbyFlags The lobby flags. Defaults to 0.
     * @param metadata The metadata for the lobby. Defaults to <c>null</c> (treated as an empty dictionary).
     * @return <c>null</c>, if the request could not be submitted i.e., not yet logged in. Otherwise, an [CreateLobbyCallback].
     */
    @JvmOverloads
    fun createLobby(
        appId: Int,
        lobbyType: ELobbyType,
        maxMembers: Int,
        lobbyFlags: Int = 0,
        metadata: Map<String, String>? = null,
    ): AsyncJobSingle<CreateLobbyCallback>? {
        if (client.cellID == null) {
            return null
        }

        val personaName = client.getHandler<SteamFriends>()!!.getPersonaName()

        val createLobby = ClientMsgProtobuf<CMsgClientMMSCreateLobby.Builder>(
            CMsgClientMMSCreateLobby::class.java,
            EMsg.ClientMMSCreateLobby
        ).apply {
            body.appId = appId
            body.lobbyType = lobbyType.code()
            body.maxMembers = maxMembers
            body.lobbyFlags = lobbyFlags
            body.metadata = Lobby.encodeMetadata(metadata).toByteString()
            body.cellId = client.cellID!!
            body.publicIp = NetHelpers.getMsgIPAddress(client.publicIP!!)
            body.personaNameOwner = personaName

            sourceJobID = client.getNextJobID()
        }

        send(msg = createLobby, appId = appId)

        lobbyManipulationRequests[createLobby.sourceJobID] = createLobby.body.build()
        return attachIncompleteManipulationHandler(
            job = AsyncJobSingle(client, createLobby.sourceJobID)
        )
    }

    /**
     * Sends a request to update a lobby.
     * @param appId ID of app the lobby belongs to.
     * @param lobbySteamId The SteamID of the lobby that should be updated.
     * @param lobbyType The lobby type.
     * @param maxMembers The maximum number of members that may occupy the lobby.
     * @param lobbyFlags The lobby flags. Defaults to 0.
     * @param metadata The metadata for the lobby. Defaults to <c>null</c> (treated as an empty dictionary).
     * @return An [SetLobbyDataCallback].
     */
    @JvmOverloads
    fun setLobbyData(
        appId: Int,
        lobbySteamId: SteamID,
        lobbyType: ELobbyType,
        maxMembers: Int,
        lobbyFlags: Int = 0,
        metadata: Map<String, String>? = null,
    ): AsyncJobSingle<SetLobbyDataCallback> {
        val setLobbyData = ClientMsgProtobuf<CMsgClientMMSSetLobbyData.Builder>(
            CMsgClientMMSSetLobbyData::class.java,
            EMsg.ClientMMSSetLobbyData
        ).apply {
            body.appId = appId
            body.steamIdLobby = lobbySteamId.convertToUInt64()
            body.steamIdMember = 0
            body.lobbyType = lobbyType.code()
            body.maxMembers = maxMembers
            body.lobbyFlags = lobbyFlags
            body.metadata = Lobby.encodeMetadata(metadata).toByteString()

            sourceJobID = client.getNextJobID()
        }

        send(msg = setLobbyData, appId = appId)

        lobbyManipulationRequests[setLobbyData.sourceJobID] = setLobbyData.body.build()
        return attachIncompleteManipulationHandler(
            job = AsyncJobSingle(client, setLobbyData.sourceJobID)
        )
    }

    /**
     * Sends a request to update the current user's lobby metadata.
     * @param appId ID of app the lobby belongs to.
     * @param lobbySteamId The SteamID of the lobby that should be updated.
     * @param metadata The metadata for the lobby.
     * @return <c>null</c>, if the request could not be submitted i.e. not yet logged in. Otherwise, an [SetLobbyDataCallback].
     */
    fun setLobbyMemberData(
        appId: Int,
        lobbySteamId: SteamID,
        metadata: Map<String, String>,
    ): AsyncJobSingle<SetLobbyDataCallback>? {
        if (client.steamID == null) {
            return null
        }

        val setLobbyData = ClientMsgProtobuf<CMsgClientMMSSetLobbyData.Builder>(
            CMsgClientMMSSetLobbyData::class.java,
            EMsg.ClientMMSSetLobbyData
        ).apply {
            body.appId = appId
            body.steamIdLobby = lobbySteamId.convertToUInt64()
            body.steamIdMember = client.steamID!!.convertToUInt64()
            body.metadata = Lobby.encodeMetadata(metadata).toByteString()

            sourceJobID = client.getNextJobID()
        }

        send(msg = setLobbyData, appId = appId)

        lobbyManipulationRequests[setLobbyData.sourceJobID] = setLobbyData.body.build()
        return attachIncompleteManipulationHandler(
            job = AsyncJobSingle(client, setLobbyData.sourceJobID)
        )
    }

    /**
     * Sends a request to update the owner of a lobby.
     * @param appId ID of app the lobby belongs to.
     * @param lobbySteamId The SteamID of the lobby that should have its owner updated.
     * @param newOwner The SteamID of the owner.
     * @return An [SetLobbyOwnerCallback].
     */
    fun setLobbyOwner(
        appId: Int,
        lobbySteamId: SteamID,
        newOwner: SteamID,
    ): AsyncJobSingle<SetLobbyOwnerCallback> {
        val setLobbyOwner = ClientMsgProtobuf<CMsgClientMMSSetLobbyOwner.Builder>(
            CMsgClientMMSSetLobbyOwner::class.java,
            EMsg.ClientMMSSetLobbyOwner
        ).apply {
            body.appId = appId
            body.steamIdLobby = lobbySteamId.convertToUInt64()
            body.steamIdNewOwner = newOwner.convertToUInt64()

            sourceJobID = client.getNextJobID()
        }

        send(msg = setLobbyOwner, appId = appId)

        lobbyManipulationRequests[setLobbyOwner.sourceJobID] = setLobbyOwner.body.build()
        return attachIncompleteManipulationHandler(
            job = AsyncJobSingle(client, setLobbyOwner.sourceJobID)
        )
    }

    /**
     * Sends a request to obtain a list of lobbies matching the specified criteria.
     * @param appId The ID of app for which we're requesting a list of lobbies.
     * @param filters An optional list of filters.
     * @param maxLobbies An optional maximum number of lobbies that will be returned.
     * @return <c>null</c>, if the request could not be submitted i.e. not yet logged in. Otherwise, an [GetLobbyListCallback].
     */
    @JvmOverloads
    fun getLobbyList(
        appId: Int,
        filters: List<Filter>? = null,
        maxLobbies: Int = -1,
    ): AsyncJobSingle<GetLobbyListCallback>? {
        if (client.cellID == null) {
            return null
        }

        val getLobbies = ClientMsgProtobuf<CMsgClientMMSGetLobbyList.Builder>(
            CMsgClientMMSGetLobbyList::class.java,
            EMsg.ClientMMSGetLobbyList
        ).apply {
            body.appId = appId
            body.cellId = client.cellID!!
            body.publicIp = NetHelpers.getMsgIPAddress(client.publicIP!!)
            body.numLobbiesRequested = maxLobbies

            sourceJobID = client.getNextJobID()
        }

        filters?.forEach { filter ->
            getLobbies.body.addFilters(filter.serialize().build())
        }

        send(msg = getLobbies, appId = appId)

        return AsyncJobSingle(client, getLobbies.sourceJobID)
    }

    /**
     * Sends a request to join a lobby.
     * @param appId ID of app the lobby belongs to.
     * @param lobbySteamId The SteamID of the lobby that should be joined.
     * @return <c>null</c>, if the request could not be submitted i.e. not yet logged in. Otherwise, an [JoinLobbyCallback].
     */
    fun joinLobby(
        appId: Int,
        lobbySteamId: SteamID,
    ): AsyncJobSingle<JoinLobbyCallback>? {
        val personaName = client.getHandler<SteamFriends>()?.getPersonaName()

        if (personaName == null) {
            return null
        }

        val joinLobby = ClientMsgProtobuf<CMsgClientMMSJoinLobby.Builder>(
            CMsgClientMMSJoinLobby::class.java,
            EMsg.ClientMMSJoinLobby
        ).apply {
            body.appId = appId
            body.personaName = personaName
            body.steamIdLobby = lobbySteamId.convertToUInt64()

            sourceJobID = client.getNextJobID()
        }

        send(msg = joinLobby, appId = appId)

        return AsyncJobSingle(client, joinLobby.sourceJobID)
    }

    /**
     * Sends a request to leave a lobby.
     * @param appId ID of app the lobby belongs to.
     * @param lobbySteamId The SteamID of the lobby that should be left.
     * @return An [LeaveLobbyCallback].
     */
    fun leaveLobby(appId: Int, lobbySteamId: SteamID): AsyncJobSingle<LeaveLobbyCallback> {
        val leaveLobby = ClientMsgProtobuf<CMsgClientMMSLeaveLobby.Builder>(
            CMsgClientMMSLeaveLobby::class.java,
            EMsg.ClientMMSLeaveLobby
        ).apply {
            body.appId = appId
            body.steamIdLobby = lobbySteamId.convertToUInt64()

            sourceJobID = client.getNextJobID()
        }

        send(msg = leaveLobby, appId = appId)

        return AsyncJobSingle(client, leaveLobby.sourceJobID)
    }

    /**
     * Sends a request to obtain a lobby's data.
     * @param appId The ID of app which we're attempting to obtain lobby data for.
     * @param lobbySteamId The SteamID of the lobby whose data is being requested.
     * @return An [LobbyDataCallback].
     */
    fun getLobbyData(appId: Int, lobbySteamId: SteamID): AsyncJobSingle<LobbyDataCallback> {
        val getLobbyData = ClientMsgProtobuf<CMsgClientMMSGetLobbyData.Builder>(
            CMsgClientMMSGetLobbyData::class.java,
            EMsg.ClientMMSGetLobbyData
        ).apply {
            body.appId = appId
            body.steamIdLobby = lobbySteamId.convertToUInt64()

            sourceJobID = client.getNextJobID()
        }

        send(msg = getLobbyData, appId = appId)

        return AsyncJobSingle(client, getLobbyData.sourceJobID)
    }

    /**
     * Sends a lobby invite request.
     * NOTE: Steam provides no functionality to determine if the user was successfully invited.
     * @param appId The ID of app which owns the lobby we're inviting a user to.
     * @param lobbySteamId The SteamID of the lobby we're inviting a user to.
     * @param userSteamId The SteamID of the user we're inviting.
     */
    fun inviteToLobby(appId: Int, lobbySteamId: SteamID, userSteamId: SteamID) {
        val getLobbyData = ClientMsgProtobuf<CMsgClientMMSInviteToLobby.Builder>(
            CMsgClientMMSInviteToLobby::class.java,
            EMsg.ClientMMSInviteToLobby
        ).apply {
            body.appId = appId
            body.steamIdLobby = lobbySteamId.convertToUInt64()
            body.steamIdUserInvited = userSteamId.convertToUInt64()
        }

        send(msg = getLobbyData, appId = appId)
    }

    /**
     *  Obtains a [Lobby] by its [SteamID], if the data is cached locally.
     *  This method does not send a network request.
     *  @param appId The ID of app which we're attempting to obtain a lobby for.
     *  @param lobbySteamId The SteamID of the lobby that should be returned.
     *  @return The [Lobby] corresponding with the specified app and lobby ID, if cached. Otherwise, <c>null</c>.
     */
    fun getLobby(appId: Int, lobbySteamId: SteamID): Lobby? = lobbyCache.getLobby(appId, lobbySteamId)

    /**
     * Sends a matchmaking message for a specific app.
     * @param msg The matchmaking message to send.
     * @param appId The ID of the app this message pertains to.
     */
    fun <T : GeneratedMessage.Builder<T>> send(msg: ClientMsgProtobuf<T>, appId: Int) {
        msg.protoHeader.routingAppid = appId
        client.send(msg)
    }

    /**
     * Handles a client message. This should not be called directly.
     * @param packetMsg The packet message that contains the data.
     */
    override fun handleMsg(packetMsg: IPacketMsg) {
        getHandler(packetMsg)?.invoke(packetMsg)
    }

    internal fun clearLobbyCache() {
        lobbyCache.clear()
    }

    // TODO verify...
    private fun <T : CallbackMsg> attachIncompleteManipulationHandler(job: AsyncJobSingle<T>): AsyncJobSingle<T> {
        // Manipulation requests typically complete (and are removed from lobbyManipulationRequests) when
        // a message is handled. However, jobs can also be faulted, or be cancelled (e.g. when SteamClient
        // disconnects.) Thus, when a job fails we remove the JobID/request from lobbyManipulationRequests.
        job.toFuture().exceptionally { task ->
            lobbyManipulationRequests.remove(job.jobID)
            null
        }
        return job
    }

    // region ClientMsg Handlers

    private fun handleCreateLobbyResponse(packetMsg: IPacketMsg) {
        val createLobbyResponse = ClientMsgProtobuf<CMsgClientMMSCreateLobbyResponse.Builder>(
            CMsgClientMMSCreateLobbyResponse::class.java,
            packetMsg
        )
        val body = createLobbyResponse.body

        lobbyManipulationRequests.remove(createLobbyResponse.targetJobID)?.let { request ->
            if (body.eresult == EResult.OK.code()) {
                val createLobby = request as CMsgClientMMSCreateLobby
                val members = List(1) {
                    Member(client.steamID!!, createLobby.personaNameOwner)
                }

                lobbyCache.cacheLobby(
                    createLobby.appId,
                    Lobby(
                        steamID = SteamID(body.steamIdLobby),
                        lobbyType = ELobbyType.from(createLobby.lobbyType),
                        lobbyFlags = createLobby.lobbyFlags,
                        ownerSteamID = client.steamID,
                        metadata = Lobby.decodeMetadata(createLobby.metadata),
                        maxMembers = createLobby.maxMembers,
                        numMembers = 1,
                        members = members,
                        distance = null,
                        weight = null
                    )
                )
            }
        }

        CreateLobbyCallback(
            jobID = createLobbyResponse.targetJobID,
            appID = body.appId,
            result = EResult.from(body.eresult),
            lobbySteamID = SteamID(body.steamIdLobby)
        ).also(client::postCallback)
    }

    fun handleSetLobbyDataResponse(packetMsg: IPacketMsg) {
        val setLobbyDataResponse = ClientMsgProtobuf<CMsgClientMMSSetLobbyDataResponse.Builder>(
            CMsgClientMMSSetLobbyDataResponse::class.java,
            packetMsg
        )
        val body = setLobbyDataResponse.body

        lobbyManipulationRequests.remove(setLobbyDataResponse.targetJobID)?.let { request ->
            if (body.eresult == EResult.OK.code()) {
                val setLobbyData = request as CMsgClientMMSSetLobbyData
                val lobby = lobbyCache.getLobby(appId = setLobbyData.appId, lobbySteamId = setLobbyData.steamIdLobby)

                if (lobby != null) {
                    val metadata = Lobby.decodeMetadata(setLobbyData.metadata)

                    if (setLobbyData.steamIdMember == 0L) {
                        lobbyCache.cacheLobby(
                            appId = setLobbyData.appId,
                            lobby = Lobby(
                                steamID = lobby.steamID,
                                lobbyType = ELobbyType.from(setLobbyData.lobbyType),
                                lobbyFlags = setLobbyData.lobbyFlags,
                                ownerSteamID = lobby.ownerSteamID,
                                metadata = metadata,
                                maxMembers = setLobbyData.maxMembers,
                                numMembers = lobby.numMembers,
                                members = lobby.members,
                                distance = lobby.distance,
                                weight = lobby.weight
                            )
                        )
                    } else {
                        val members = lobby.members.map { m ->
                            if (m.steamID.convertToUInt64() == setLobbyData.steamIdMember) {
                                Member(steamID = m.steamID, personaName = m.personaName, metadata = metadata)
                            } else {
                                m
                            }
                        }

                        lobbyCache.updateLobbyMembers(appId = setLobbyData.appId, lobby = lobby, members = members)
                    }
                }
            }
        }

        SetLobbyDataCallback(
            jobID = setLobbyDataResponse.targetJobID,
            appID = body.appId,
            result = EResult.from(body.eresult),
            lobbySteamID = SteamID(body.steamIdLobby)
        ).also(client::postCallback)
    }

    fun handleSetLobbyOwnerResponse(packetMsg: IPacketMsg) {
        val setLobbyOwnerResponse = ClientMsgProtobuf<CMsgClientMMSSetLobbyOwnerResponse.Builder>(
            CMsgClientMMSSetLobbyOwnerResponse::class.java,
            packetMsg
        )
        val body = setLobbyOwnerResponse.body

        lobbyManipulationRequests.remove(setLobbyOwnerResponse.targetJobID)?.let { request ->
            if (body.eresult == EResult.OK.code()) {
                val setLobbyOwner = request as CMsgClientMMSSetLobbyOwner
                lobbyCache.updateLobbyOwner(
                    appId = body.appId,
                    lobbySteamId = body.steamIdLobby,
                    ownerSteamId = setLobbyOwner.steamIdNewOwner
                )
            }
        }

        SetLobbyOwnerCallback(
            jobID = setLobbyOwnerResponse.targetJobID,
            appID = body.appId,
            result = EResult.from(body.eresult),
            lobbySteamID = SteamID(body.steamIdLobby)
        ).also(client::postCallback)
    }

    fun handleGetLobbyListResponse(packetMsg: IPacketMsg) {
        val lobbyListResponse = ClientMsgProtobuf<CMsgClientMMSGetLobbyListResponse.Builder>(
            CMsgClientMMSGetLobbyListResponse::class.java,
            packetMsg
        )
        val body = lobbyListResponse.body

        val lobbyList = body.lobbiesList.map { lobby ->
            val existingLobby = lobbyCache.getLobby(appId = body.appId, lobbySteamId = lobby.steamId)
            val members = existingLobby?.members
            Lobby(
                steamID = SteamID(lobby.steamId),
                lobbyType = ELobbyType.from(lobby.lobbyType),
                lobbyFlags = lobby.lobbyFlags,
                ownerSteamID = existingLobby?.ownerSteamID,
                metadata = Lobby.decodeMetadata(lobby.metadata),
                maxMembers = lobby.maxMembers,
                numMembers = lobby.numMembers,
                members = members ?: listOf(),
                distance = lobby.distance,
                weight = lobby.weight
            )
        }

        lobbyList.forEach { lobby ->
            lobbyCache.cacheLobby(appId = body.appId, lobby = lobby)
        }

        GetLobbyListCallback(
            jobID = lobbyListResponse.targetJobID,
            appID = body.appId,
            result = EResult.from(body.eresult),
            lobbies = lobbyList
        ).also(client::postCallback)
    }

    fun handleJoinLobbyResponse(packetMsg: IPacketMsg) {
        val joinLobbyResponse = ClientMsgProtobuf<CMsgClientMMSJoinLobbyResponse.Builder>(
            CMsgClientMMSJoinLobbyResponse::class.java,
            packetMsg
        )
        val body = joinLobbyResponse.body

        var joinedLobby: Lobby? = null

        if (body.hasSteamIdLobby()) {
            val members = body.membersList.map { member ->
                Member(
                    steamID = SteamID(member.steamId),
                    personaName = member.personaName,
                    metadata = Lobby.decodeMetadata(member.metadata),
                )
            }

            val cachedLobby = lobbyCache.getLobby(appId = body.appId, lobbySteamId = body.steamIdLobby)

            joinedLobby = Lobby(
                steamID = SteamID(body.steamIdLobby),
                lobbyType = ELobbyType.from(body.lobbyType),
                lobbyFlags = body.lobbyFlags,
                ownerSteamID = SteamID(body.steamIdOwner),
                metadata = Lobby.decodeMetadata(body.metadata),
                maxMembers = body.maxMembers,
                numMembers = members.size,
                members = members,
                distance = cachedLobby?.distance,
                weight = cachedLobby?.weight
            )

            lobbyCache.cacheLobby(appId = body.appId, lobby = joinedLobby)
        }

        JoinLobbyCallback(
            jobID = joinLobbyResponse.targetJobID,
            appID = body.appId,
            chatRoomEnterResponse = EChatRoomEnterResponse.from(body.chatRoomEnterResponse),
            lobby = joinedLobby
        ).also(client::postCallback)
    }

    fun handleLeaveLobbyResponse(packetMsg: IPacketMsg) {
        val leaveLobbyResponse = ClientMsgProtobuf<CMsgClientMMSLeaveLobbyResponse.Builder>(
            CMsgClientMMSLeaveLobbyResponse::class.java,
            packetMsg
        )
        val body = leaveLobbyResponse.body

        if (body.eresult == EResult.OK.code()) {
            lobbyCache.clearLobbyMembers(appId = body.appId, lobbySteamId = body.steamIdLobby)
        }

        LeaveLobbyCallback(
            jobID = leaveLobbyResponse.targetJobID,
            appID = body.appId,
            result = EResult.from(body.eresult),
            lobbySteamID = SteamID(body.steamIdLobby)
        ).also(client::postCallback)
    }

    fun handleLobbyData(packetMsg: IPacketMsg) {
        val lobbyDataResponse = ClientMsgProtobuf<CMsgClientMMSLobbyData.Builder>(
            CMsgClientMMSLobbyData::class.java,
            packetMsg
        )
        val body = lobbyDataResponse.body

        val cachedLobby = lobbyCache.getLobby(appId = body.appId, lobbySteamId = body.steamIdLobby)
        val members = if (body.membersList.isEmpty()) {
            cachedLobby?.members
        } else {
            body.membersList.map { member ->
                Member(
                    steamID = SteamID(member.steamId),
                    personaName = member.personaName,
                    metadata = Lobby.decodeMetadata(member.metadata)
                )
            }
        }

        val updatedLobby = Lobby(
            steamID = SteamID(body.steamIdLobby),
            lobbyType = ELobbyType.from(body.lobbyType),
            lobbyFlags = body.lobbyFlags,
            ownerSteamID = SteamID(body.steamIdOwner),
            metadata = Lobby.decodeMetadata(body.metadata),
            maxMembers = body.maxMembers,
            numMembers = body.numMembers,
            members = members ?: listOf(),
            distance = cachedLobby?.distance,
            weight = cachedLobby?.weight
        )

        lobbyCache.cacheLobby(appId = body.appId, lobby = updatedLobby)

        LobbyDataCallback(
            jobID = lobbyDataResponse.targetJobID,
            appID = body.appId,
            lobby = updatedLobby
        ).also(client::postCallback)
    }

    fun handleUserJoinedLobby(packetMsg: IPacketMsg) {
        val userJoinedLobby = ClientMsgProtobuf<CMsgClientMMSUserJoinedLobby.Builder>(
            CMsgClientMMSUserJoinedLobby::class.java,
            packetMsg
        )
        val body = userJoinedLobby.body

        val lobby = lobbyCache.getLobby(appId = body.appId, lobbySteamId = body.steamIdLobby)

        if (lobby != null && lobby.members.isNotEmpty()) {
            val joiningMember = lobbyCache.addLobbyMember(
                appId = body.appId,
                lobby = lobby,
                memberId = body.steamIdUser,
                personaName = body.personaName
            )

            if (joiningMember != null) {
                UserJoinedLobbyCallback(
                    appID = body.appId,
                    lobbySteamID = SteamID(body.steamIdLobby),
                    user = joiningMember
                ).also(client::postCallback)
            }
        }
    }

    fun handleUserLeftLobby(packetMsg: IPacketMsg) {
        val userLeftLobby = ClientMsgProtobuf<CMsgClientMMSUserLeftLobby.Builder>(
            CMsgClientMMSUserLeftLobby::class.java,
            packetMsg
        )
        val body = userLeftLobby.body

        val lobby = lobbyCache.getLobby(appId = body.appId, lobbySteamId = body.steamIdLobby)

        if (lobby != null && lobby.members.isNotEmpty()) {
            val leavingMember = lobbyCache.removeLobbyMember(
                appId = body.appId,
                lobby = lobby,
                memberId = body.steamIdUser
            )

            if (leavingMember == null) {
                return
            }

            if (leavingMember.steamID == client.steamID) {
                lobbyCache.clearLobbyMembers(appId = body.appId, lobbySteamId = body.steamIdLobby)
            }

            UserLeftLobbyCallback(
                appID = body.appId,
                lobbySteamID = SteamID(body.steamIdLobby),
                user = leavingMember
            ).also(client::postCallback)
        }
    }

    // endregion
}
