package `in`.dragonbra.javasteam.steam.handlers.steammatchmaking

import com.google.protobuf.ByteString
import com.google.protobuf.GeneratedMessage
import `in`.dragonbra.javasteam.base.AClientMsgProtobuf
import `in`.dragonbra.javasteam.base.ClientMsgProtobuf
import `in`.dragonbra.javasteam.base.IPacketMsg
import `in`.dragonbra.javasteam.enums.ELobbyType
import `in`.dragonbra.javasteam.enums.EMsg
import `in`.dragonbra.javasteam.enums.EResult
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverMms.CMsgClientMMSGetLobbyData
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverMms.CMsgClientMMSLeaveLobby
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverMms.CMsgClientMMSInviteToLobby
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverMms.CMsgClientMMSCreateLobby
import `in`.dragonbra.javasteam.steam.handlers.ClientMsgHandler
import `in`.dragonbra.javasteam.steam.handlers.steamfriends.SteamFriends
import `in`.dragonbra.javasteam.steam.handlers.steammatchmaking.callback.CreateLobbyCallback
import `in`.dragonbra.javasteam.steam.handlers.steammatchmaking.callback.LeaveLobbyCallback
import `in`.dragonbra.javasteam.steam.handlers.steammatchmaking.callback.LobbyDataCallback
import `in`.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg
import `in`.dragonbra.javasteam.types.AsyncJob
import `in`.dragonbra.javasteam.types.AsyncJobSingle
import `in`.dragonbra.javasteam.types.JobID
import `in`.dragonbra.javasteam.types.SteamID
import `in`.dragonbra.javasteam.util.NetHelpers
import java.util.concurrent.ConcurrentHashMap

/**
 * This handler is used for creating, joining and obtaining lobby information.
 */
class SteamMatchmaking : ClientMsgHandler() {

    companion object {
        private fun getHandler(packetMsg: IPacketMsg): IPacketMsg? {
            return when (packetMsg.msgType) {
                EMsg.ClientMMSCreateLobbyResponse -> handleCreateLobbyResponse()
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
        }
    }

    private val lobbyManipulationRequests: ConcurrentHashMap<JobID, GeneratedMessage> =
        ConcurrentHashMap() // TODO Value

    private val lobbyCache: LobbyCache = LobbyCache()


    /// <summary>
    /// Sends a request to create a lobby.
    /// </summary>
    /// <param name="appId">ID of the app the lobby will belong to.</param>
    /// <param name="lobbyType">The lobby type.</param>
    /// <param name="maxMembers">The maximum number of members that may occupy the lobby.</param>
    /// <param name="lobbyFlags">The lobby flags. Defaults to 0.</param>
    /// <param name="metadata">The metadata for the lobby. Defaults to <c>null</c> (treated as an empty dictionary).</param>
    /// <returns><c>null</c>, if the request could not be submitted i.e. not yet logged in. Otherwise, an <see cref="AsyncJob{CreateLobbyCallback}"/>.</returns>
    @JvmOverloads
    fun createLobby(
        appId: Int,
        lobbyType: ELobbyType,
        maxMembers: Int,
        lobbyFlags: Int = 0,
        metadata: Map<String, String>? = null
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
            body.metadata = ByteString.copyFrom(Lobby.encodeMetadata(metadata))
            body.cellId = client.cellID!!
            body.publicIp = NetHelpers.getMsgIPAddress(client.publicIP)
            body.personaNameOwner = personaName

            sourceJobID = client.getNextJobID()
        }

        send(createLobby, appId)

        lobbyManipulationRequests[createLobby.sourceJobID] = createLobby.body.build()
        return attachIncompleteManipulationHandler(
            job = AsyncJobSingle<CreateLobbyCallback>(client, createLobby.sourceJobID)
        )
    }

    /// <summary>
    /// Sends a request to update a lobby.
    /// </summary>
    /// <param name="appId">ID of app the lobby belongs to.</param>
    /// <param name="lobbySteamId">The SteamID of the lobby that should be updated.</param>
    /// <param name="lobbyType">The lobby type.</param>
    /// <param name="maxMembers">The maximum number of members that may occupy the lobby.</param>
    /// <param name="lobbyFlags">The lobby flags. Defaults to 0.</param>
    /// <param name="metadata">The metadata for the lobby. Defaults to <c>null</c> (treated as an empty dictionary).</param>
    /// <returns>An <see cref="AsyncJob{SetLobbyDataCallback}"/>.</returns>
    public AsyncJob<SetLobbyDataCallback> SetLobbyData( appId: Int,  lobbySteamId: SteamID, ELobbyType lobbyType, int maxMembers, int lobbyFlags = 0,
    IReadOnlyDictionary<string, string>? metadata = null )
    {
        val setLobbyData = ClientMsgProtobuf<CMsgClientMMSSetLobbyData>(EMsg.ClientMMSSetLobbyData)
        {
            Body =
                {
                    app_id = appId,
                    steam_id_lobby = lobbySteamId,
                    steam_id_member = 0,
                    lobby_type = (int) lobbyType,
                    max_members = maxMembers,
                    lobby_flags = lobbyFlags,
                    metadata = Lobby.EncodeMetadata(metadata),
                },
            SourceJobID = client.getNextJobID()
        }

        Send(setLobbyData, appId)

        lobbyManipulationRequests[setLobbyData.sourceJobID] = setLobbyData.Body
        return AttachIncompleteManipulationHandler(
            AsyncJobSingle<SetLobbyDataCallback>(
                client,
                setLobbyData.sourceJobID
            )
        )
    }

    /// <summary>
    /// Sends a request to update the current user's lobby metadata.
    /// </summary>
    /// <param name="appId">ID of app the lobby belongs to.</param>
    /// <param name="lobbySteamId">The SteamID of the lobby that should be updated.</param>
    /// <param name="metadata">The metadata for the lobby.</param>
    /// <returns><c>null</c>, if the request could not be submitted i.e. not yet logged in. Otherwise, an <see cref="AsyncJob{SetLobbyDataCallback}"/>.</returns>
    public AsyncJob<SetLobbyDataCallback>? SetLobbyMemberData( appId: Int,  lobbySteamId: SteamID, IReadOnlyDictionary<string, string> metadata )
    {
        if (client.SteamID == null) {
            return null
        }

        val setLobbyData = ClientMsgProtobuf<CMsgClientMMSSetLobbyData>(EMsg.ClientMMSSetLobbyData)
        {
            Body =
                {
                    app_id = appId,
                    steam_id_lobby = lobbySteamId,
                    steam_id_member = client.SteamID,
                    metadata = Lobby.EncodeMetadata(metadata)
                },
            SourceJobID = client.getNextJobID()
        }

        Send(setLobbyData, appId)

        lobbyManipulationRequests[setLobbyData.sourceJobID] = setLobbyData.Body
        return AttachIncompleteManipulationHandler(
            AsyncJobSingle<SetLobbyDataCallback>(
                client,
                setLobbyData.sourceJobID
            )
        )
    }

    /// <summary>
    /// Sends a request to update the owner of a lobby.
    /// </summary>
    /// <param name="appId">ID of app the lobby belongs to.</param>
    /// <param name="lobbySteamId">The SteamID of the lobby that should have its owner updated.</param>
    /// <param name="newOwner">The SteamID of the owner.</param>
    /// <returns>An <see cref="AsyncJob{SetLobbyOwnerCallback}"/>.</returns>
    public AsyncJob<SetLobbyOwnerCallback> SetLobbyOwner( appId: Int,  lobbySteamId: SteamID, SteamID newOwner )
    {
        val setLobbyOwner = ClientMsgProtobuf<CMsgClientMMSSetLobbyOwner>(EMsg.ClientMMSSetLobbyOwner)
        {
            Body =
                {
                    app_id = appId,
                    steam_id_lobby = lobbySteamId,
                    steam_id_new_owner = newOwner
                },
            SourceJobID = client.getNextJobID()
        }

        Send(setLobbyOwner, appId)

        lobbyManipulationRequests[setLobbyOwner.sourceJobID] = setLobbyOwner.Body
        return AttachIncompleteManipulationHandler(
            AsyncJobSingle<SetLobbyOwnerCallback>(
                client,
                setLobbyOwner.sourceJobID
            )
        )
    }

    /// <summary>
    /// Sends a request to obtains a list of lobbies matching the specified criteria.
    /// </summary>
    /// <param name="appId">The ID of app for which we're requesting a list of lobbies.</param>
    /// <param name="filters">An optional list of filters.</param>
    /// <param name="maxLobbies">An optional maximum number of lobbies that will be returned.</param>
    /// <returns><c>null</c>, if the request could not be submitted i.e. not yet logged in. Otherwise, an <see cref="AsyncJob{GetLobbyListCallback}"/>.</returns>
    public AsyncJob<GetLobbyListCallback>? GetLobbyList( appId: Int, List<Lobby.Filter>? filters = null, int maxLobbies = -1 )
    {
        if (client.CellID == null) {
            return null
        }

        val getLobbies = ClientMsgProtobuf<CMsgClientMMSGetLobbyList>(EMsg.ClientMMSGetLobbyList)
        {
            Body =
                {
                    app_id = appId,
                    cell_id = client.CellID.Value,
                    public_ip = NetHelpers.GetMsgIPAddress(client.PublicIP!),
                    num_lobbies_requested = maxLobbies
                },
            SourceJobID = client.getNextJobID()
        }

        if (filters != null) {
            foreach(val filter in filters )
            {
                getLobbies.Body.filters.Add(filter.Serialize())
            }
        }

        Send(getLobbies, appId)

        return AsyncJobSingle<GetLobbyListCallback>(client, getLobbies.sourceJobID)
    }

    /// <summary>
    /// Sends a request to join a lobby.
    /// </summary>
    /// <param name="appId">ID of app the lobby belongs to.</param>
    /// <param name="lobbySteamId">The SteamID of the lobby that should be joined.</param>
    /// <returns><c>null</c>, if the request could not be submitted i.e. not yet logged in. Otherwise, an <see cref="AsyncJob{JoinLobbyCallback}"/>.</returns>
    public AsyncJob<JoinLobbyCallback>? JoinLobby( appId: Int,  lobbySteamId: SteamID )
    {
        val personaName = client.GetHandler<SteamFriends>()?.GetPersonaName()

        if (personaName == null) {
            return null
        }

        val joinLobby = ClientMsgProtobuf<CMsgClientMMSJoinLobby>(EMsg.ClientMMSJoinLobby)
        {
            Body =
                {
                    app_id = appId,
                    persona_name = personaName,
                    steam_id_lobby = lobbySteamId
                },
            SourceJobID = client.getNextJobID()
        }

        Send(joinLobby, appId)

        return AsyncJobSingle<JoinLobbyCallback>(client, joinLobby.sourceJobID)
    }

    /// <summary>
    /// Sends a request to leave a lobby.
    /// </summary>
    /// <param name="appId">ID of app the lobby belongs to.</param>
    /// <param name="lobbySteamId">The SteamID of the lobby that should be left.</param>
    /// <returns>An <see cref="AsyncJob{LeaveLobbyCallback}"/>.</returns>
    fun leaveLobby(appId: Int, lobbySteamId: SteamID): AsyncJobSingle<LeaveLobbyCallback> {
        val leaveLobby = ClientMsgProtobuf<CMsgClientMMSLeaveLobby.Builder>(
            CMsgClientMMSLeaveLobby::class.java,
                EMsg . ClientMMSLeaveLobby
        ).apply {
           body. appId = appId
           body. steamIdLobby = lobbySteamId.convertToUInt64()

            sourceJobID = client.getNextJobID()
        }

        send(leaveLobby, appId)

        return AsyncJobSingle(client, leaveLobby.sourceJobID)
    }

    /// <summary>
    /// Sends a request to obtain a lobby's data.
    /// </summary>
    /// <param name="appId">The ID of app which we're attempting to obtain lobby data for.</param>
    /// <param name="lobbySteamId">The SteamID of the lobby whose data is being requested.</param>
    /// <returns>An <see cref="AsyncJob{LobbyDataCallback}"/>.</returns>
    fun getLobbyData(appId: Int, lobbySteamId: SteamID): AsyncJobSingle<LobbyDataCallback> {
        val getLobbyData = ClientMsgProtobuf<CMsgClientMMSGetLobbyData.Builder>(
            CMsgClientMMSGetLobbyData::class.java,
            EMsg.ClientMMSGetLobbyData
        ).apply {
            body.appId = appId
            body.steamIdLobby = lobbySteamId.convertToUInt64()

            sourceJobID = client.getNextJobID()
        }

        send(getLobbyData, appId)

        return AsyncJobSingle(client, getLobbyData.sourceJobID)
    }

    /// <summary>
    /// Sends a lobby invite request.
    /// NOTE: Steam provides no functionality to determine if the user was successfully invited.
    /// </summary>
    /// <param name="appId">The ID of app which owns the lobby we're inviting a user to.</param>
    /// <param name="lobbySteamId">The SteamID of the lobby we're inviting a user to.</param>
    /// <param name="userSteamId">The SteamID of the user we're inviting.</param>
    fun inviteToLobby(appId: Int, lobbySteamId: SteamID, userSteamId: SteamID) {
        val getLobbyData = ClientMsgProtobuf<CMsgClientMMSInviteToLobby.Builder>(
            CMsgClientMMSInviteToLobby::class.java,
            EMsg.ClientMMSInviteToLobby
        ).apply {
            body.appId = appId
            body.steamIdLobby = lobbySteamId.convertToUInt64()
            body.steamIdUserInvited = userSteamId.convertToUInt64()
        }

        send(getLobbyData, appId)
    }

    /// <summary>
    /// Obtains a <see cref="Lobby"/>, by its SteamID, if the data is cached locally.
    /// This method does not send a network request.
    /// </summary>
    /// <param name="appId">The ID of app which we're attempting to obtain a lobby for.</param>
    /// <param name="lobbySteamId">The SteamID of the lobby that should be returned.</param>
    /// <returns>The <see cref="Lobby"/> corresponding with the specified app and lobby ID, if cached. Otherwise, <c>null</c>.</returns>
    fun getLobby(appId: Int, lobbySteamId: SteamID): Lobby? = lobbyCache.getLobby(appId, lobbySteamId)

    /// <summary>
    /// Sends a matchmaking message for a specific app.
    /// </summary>
    /// <param name="msg">The matchmaking message to send.</param>
    /// <param name="appId">The ID of the app this message pertains to.</param>
    fun send(msg: AClientMsgProtobuf, appId: Int) {
        msg.protoHeader.routingAppid = appId
        client.send(msg)
    }

    /**
     * Handles a client message. This should not be called directly.
     * @param packetMsg The packet message that contains the data.
     */
    // TODO this is using the old CB style
    override fun handleMsg(packetMsg: IPacketMsg) {
        val handler = getHandler(packetMsg) ?: return

        // handler?.Invoke(packetMsg)
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

    // TODO

    // endregion
}
