package in.dragonbra.javasteam.steam.handlers.steammatchmaking;

import com.google.protobuf.ByteString;
import com.google.protobuf.GeneratedMessageV3;
import in.dragonbra.javasteam.base.ClientMsgProtobuf;
import in.dragonbra.javasteam.base.IPacketMsg;
import in.dragonbra.javasteam.enums.EChatRoomEnterResponse;
import in.dragonbra.javasteam.enums.ELobbyType;
import in.dragonbra.javasteam.enums.EMsg;
import in.dragonbra.javasteam.enums.EResult;
import in.dragonbra.javasteam.handlers.ClientMsgHandler;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverMms.*;
import in.dragonbra.javasteam.steam.handlers.steamfriends.SteamFriends;
import in.dragonbra.javasteam.steam.handlers.steammatchmaking.callback.*;
import in.dragonbra.javasteam.types.JobID;
import in.dragonbra.javasteam.types.SteamID;
import in.dragonbra.javasteam.util.NetHelpers;
import in.dragonbra.javasteam.util.compat.Consumer;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author lossy
 * @since 2022-06-21
 */
public class SteamMatchmaking extends ClientMsgHandler {

    private Map<EMsg, Consumer<IPacketMsg>> dispatchMap;

    private final ConcurrentHashMap<JobID, GeneratedMessageV3> lobbyManipulationRequests = new ConcurrentHashMap<>();

    private final LobbyCache lobbyCache = new LobbyCache();

    public SteamMatchmaking() {
        dispatchMap = new HashMap<>();

        dispatchMap.put(EMsg.ClientMMSCreateLobbyResponse, new Consumer<IPacketMsg>() {
            @Override
            public void accept(IPacketMsg packetMsg) {
                handleCreateLobbyResponse(packetMsg);
            }
        });
        dispatchMap.put(EMsg.ClientMMSSetLobbyDataResponse, new Consumer<IPacketMsg>() {
            @Override
            public void accept(IPacketMsg packetMsg) {
                handleSetLobbyDataResponse(packetMsg);
            }
        });
        dispatchMap.put(EMsg.ClientMMSSetLobbyOwnerResponse, new Consumer<IPacketMsg>() {
            @Override
            public void accept(IPacketMsg packetMsg) {
                handleSetLobbyOwnerResponse(packetMsg);
            }
        });
        dispatchMap.put(EMsg.ClientMMSLobbyData, new Consumer<IPacketMsg>() {
            @Override
            public void accept(IPacketMsg packetMsg) {
                handleLobbyData(packetMsg);
            }
        });
        dispatchMap.put(EMsg.ClientMMSGetLobbyListResponse, new Consumer<IPacketMsg>() {
            @Override
            public void accept(IPacketMsg packetMsg) {
                handleGetLobbyListResponse(packetMsg);
            }
        });
        dispatchMap.put(EMsg.ClientMMSJoinLobbyResponse, new Consumer<IPacketMsg>() {
            @Override
            public void accept(IPacketMsg packetMsg) {
                handleJoinLobbyResponse(packetMsg);
            }
        });
        dispatchMap.put(EMsg.ClientMMSLeaveLobbyResponse, new Consumer<IPacketMsg>() {
            @Override
            public void accept(IPacketMsg packetMsg) {
                handleLeaveLobbyResponse(packetMsg);
            }
        });
        dispatchMap.put(EMsg.ClientMMSUserJoinedLobby, new Consumer<IPacketMsg>() {
            @Override
            public void accept(IPacketMsg packetMsg) {
                handleUserJoinedLobby(packetMsg);
            }
        });
        dispatchMap.put(EMsg.ClientMMSUserLeftLobby, new Consumer<IPacketMsg>() {
            @Override
            public void accept(IPacketMsg packetMsg) {
                handleUserLeftLobby(packetMsg);
            }
        });

        dispatchMap = Collections.unmodifiableMap(dispatchMap);
    }

    /**
     * Sends a request to create a new lobby.
     * <p>
     * Returns nothing if the request could not be submitted i.e. not yet logged in. Otherwise, an {@link CreateLobbyCallback}.
     *
     * @param appId      ID of the app the lobby will belong to.
     * @param lobbyType  The new lobby type.
     * @param maxMembers The new maximum number of members that may occupy the lobby.
     * @param lobbyFlags The new lobby flags. Defaults to 0.
     * @param metadata   The new metadata for the lobby. Defaults to <strong>null</strong> (treated as an empty dictionary).
     */
    public void createLobby(int appId, ELobbyType lobbyType, int maxMembers, Integer lobbyFlags, HashMap<String, String> metadata) {
        if (client.getCellID() == null) {
            return;
        }

        String personaName = client.getHandler(SteamFriends.class).getPersonaName();

        ClientMsgProtobuf<CMsgClientMMSCreateLobby.Builder> createLobby =
                new ClientMsgProtobuf<>(CMsgClientMMSCreateLobby.class, EMsg.ClientMMSCreateLobby);

        CMsgClientMMSCreateLobby.Builder body = createLobby
                .getBody()
                .setAppId(appId)
                .setLobbyType(lobbyType.code())
                .setMaxMembers(maxMembers)
                .setLobbyFlags(lobbyFlags != null ? lobbyFlags : 0)
                .setMetadata(ByteString.copyFrom(Lobby.encodeMetadata(metadata)))
                .setCellId(client.getCellID())
                .setPublicIp(NetHelpers.getMsgIPAddress(client.getPublicIP()))
                .setPersonaNameOwner(personaName);

        createLobby.setBody(body);
        createLobby.getProtoHeader().setJobidSource(client.getNextJobID().getValue());

        send(createLobby, appId);

        lobbyManipulationRequests.put(createLobby.getSourceJobID(), createLobby.getBody().build());
    }

    /**
     * Sends a request to update a lobby.
     * <p>
     * Returns {@link SetLobbyDataCallback}.
     *
     * @param appId        ID of app the lobby belongs to.
     * @param lobbySteamId The SteamID of the lobby that should be updated.
     * @param lobbyType    The new lobby type.
     * @param maxMembers   The new maximum number of members that may occupy the lobby.
     * @param lobbyFlags   The new lobby flags. Defaults to 0.
     * @param metadata     The new metadata for the lobby. Defaults to <strong>null</strong> (treated as an empty dictionary).
     */
    public void setLobbyData(int appId, SteamID lobbySteamId, ELobbyType lobbyType, int maxMembers, Integer lobbyFlags, HashMap<String, String> metadata) {
        ClientMsgProtobuf<CMsgClientMMSSetLobbyData.Builder> setLobbyData =
                new ClientMsgProtobuf<>(CMsgClientMMSSetLobbyData.class, EMsg.ClientMMSSetLobbyData);

        CMsgClientMMSSetLobbyData.Builder body = setLobbyData
                .getBody()
                .setAppId(appId)
                .setSteamIdLobby(lobbySteamId.convertToUInt64())
                .setSteamIdMember(0L)
                .setLobbyType(lobbyType.code())
                .setMaxMembers(maxMembers)
                .setLobbyFlags((lobbyFlags != null) ? lobbyFlags : 0)
                .setMetadata(ByteString.copyFrom(Lobby.encodeMetadata(metadata)));

        setLobbyData.setBody(body);
        setLobbyData.setSourceJobID(client.getNextJobID());

        send(setLobbyData, appId);

        lobbyManipulationRequests.put(setLobbyData.getSourceJobID(), setLobbyData.getBody().build());
    }

    /**
     * Sends a request to update the current user's lobby metadata.
     * <p>
     * Returns nothing if the request could not be submitted i.e. not yet logged in.
     * Otherwise, an {@link SetLobbyDataCallback}
     *
     * @param appId        ID of app the lobby belongs to.
     * @param lobbySteamId The SteamID of the lobby that should be updated.
     * @param metadata     The new metadata for the lobby.
     */
    public void setLobbyMemberData(int appId, SteamID lobbySteamId, HashMap<String, String> metadata) {
        if (client.getSteamID() == null) {
            return;
        }

        ClientMsgProtobuf<CMsgClientMMSSetLobbyData.Builder> setLobbyData =
                new ClientMsgProtobuf<>(CMsgClientMMSSetLobbyData.class, EMsg.ClientMMSSetLobbyData);

        CMsgClientMMSSetLobbyData.Builder body = setLobbyData
                .getBody()
                .setAppId(appId)
                .setSteamIdLobby(lobbySteamId.convertToUInt64())
                .setSteamIdMember(client.getSteamID().convertToUInt64())
                .setMetadata(ByteString.copyFrom(Lobby.encodeMetadata(metadata)));

        setLobbyData.setBody(body);
        setLobbyData.setSourceJobID(client.getNextJobID());

        send(setLobbyData, appId);

        lobbyManipulationRequests.put(setLobbyData.getSourceJobID(), setLobbyData.getBody().build());
    }

    /**
     * Sends a request to update the owner of a lobby.
     * <p>
     * Returns an {@link  SetLobbyOwnerCallback}
     *
     * @param appId        ID of app the lobby belongs to.
     * @param lobbySteamId The SteamID of the lobby that should have its owner updated.
     * @param newOwner     The SteamID of the new owner.
     */
    public void setLobbyOwner(int appId, SteamID lobbySteamId, SteamID newOwner) {
        ClientMsgProtobuf<CMsgClientMMSSetLobbyOwner.Builder> setLobbyOwner =
                new ClientMsgProtobuf<>(CMsgClientMMSSetLobbyOwner.class, EMsg.ClientMMSSetLobbyOwner);

        CMsgClientMMSSetLobbyOwner.Builder body = setLobbyOwner
                .getBody()
                .setAppId(appId)
                .setSteamIdLobby(lobbySteamId.convertToUInt64())
                .setSteamIdNewOwner(newOwner.convertToUInt64());

        setLobbyOwner.setBody(body);
        setLobbyOwner.setSourceJobID(client.getNextJobID());

        send(setLobbyOwner, appId);

        lobbyManipulationRequests.put(setLobbyOwner.getSourceJobID(), setLobbyOwner.getBody().build());
    }

    /**
     * Sends a request to obtains a list of lobbies matching the specified criteria.
     * <p>
     * Returns nothing if the request could not be submitted i.e. not yet logged in.
     * Otherwise, an {@link  GetLobbyListCallback}.
     *
     * @param appId      The ID of app for which we're requesting a list of lobbies.
     * @param filters    An optional list of filters.
     * @param maxLobbies An optional maximum number of lobbies that will be returned.
     */
    public void getLobbyList(int appId, List<Lobby.Filter> filters, Integer maxLobbies) {
        if (client.getCellID() == null) {
            return;
        }

        ClientMsgProtobuf<CMsgClientMMSGetLobbyList.Builder> getLobbies =
                new ClientMsgProtobuf<>(CMsgClientMMSGetLobbyList.class, EMsg.ClientMMSGetLobbyList);

        int maxLobbiesReq = -1;
        if (maxLobbies != null) {
            maxLobbiesReq = maxLobbies;
        }

        CMsgClientMMSGetLobbyList.Builder body = getLobbies
                .getBody()
                .setAppId(appId)
                .setCellId(client.getCellID())
                .setPublicIp(NetHelpers.getMsgIPAddress(client.getPublicIP()))
                .setNumLobbiesRequested(maxLobbiesReq);

        getLobbies.setBody(body);
        getLobbies.setSourceJobID(client.getNextJobID());

        if (filters != null) {
            for (Lobby.Filter filter : filters) {
                getLobbies.getBody().addFilters(filter.serialize().build());

            }
        }

        send(getLobbies, appId);
    }

    /**
     * Sends a request to join a lobby.
     * <p>
     * Returns nothing if the request could not be submitted i.e. not yet logged in.
     * Otherwise, an {@link JoinLobbyCallback}.
     *
     * @param appId        ID of app the lobby belongs to.
     * @param lobbySteamId The SteamID of the lobby that should be joined.
     */
    public void joinLobby(int appId, SteamID lobbySteamId) {
        String personaName = client.getHandler(SteamFriends.class).getPersonaName();

        if (personaName == null) {
            return;
        }

        ClientMsgProtobuf<CMsgClientMMSJoinLobby.Builder> joinLobby =
                new ClientMsgProtobuf<>(CMsgClientMMSJoinLobby.class, EMsg.ClientMMSJoinLobby);

        CMsgClientMMSJoinLobby.Builder body = joinLobby
                .getBody()
                .setAppId(appId)
                .setPersonaName(personaName)
                .setSteamIdLobby(lobbySteamId.convertToUInt64());

        joinLobby.setBody(body);
        joinLobby.setSourceJobID(client.getNextJobID());

        send(joinLobby, appId);
    }

    /**
     * Sends a request to leave a lobby.
     * <p>
     * Returns an {@link  LeaveLobbyCallback}.
     *
     * @param appId        ID of app the lobby belongs to.
     * @param lobbySteamId The SteamID of the lobby that should be left.
     */
    public void leaveLobby(int appId, SteamID lobbySteamId) {
        ClientMsgProtobuf<CMsgClientMMSLeaveLobby.Builder> leaveLobby =
                new ClientMsgProtobuf<>(CMsgClientMMSLeaveLobby.class, EMsg.ClientMMSLeaveLobby);

        CMsgClientMMSLeaveLobby.Builder body = leaveLobby
                .getBody()
                .setAppId(appId)
                .setSteamIdLobby(lobbySteamId.convertToUInt64());

        leaveLobby.setBody(body);
        leaveLobby.setSourceJobID(client.getNextJobID());

        send(leaveLobby, appId);
    }

    /**
     * Sends a request to obtain a lobby's data.
     * <p>
     * Returns an {@link LobbyDataCallback}
     *
     * @param appId        The ID of app which we're attempting to obtain lobby data for.
     * @param lobbySteamId The SteamID of the lobby whose data is being requested.
     */
    public void getLobbyData(Integer appId, SteamID lobbySteamId) {
        ClientMsgProtobuf<CMsgClientMMSGetLobbyData.Builder> getLobbyData =
                new ClientMsgProtobuf<>(CMsgClientMMSGetLobbyData.class, EMsg.ClientMMSGetLobbyData);

        CMsgClientMMSGetLobbyData.Builder body = getLobbyData
                .getBody()
                .setAppId(appId)
                .setSteamIdLobby(lobbySteamId.convertToUInt64());

        getLobbyData.setBody(body);
        getLobbyData.setSourceJobID(client.getNextJobID());

        send(getLobbyData, appId);
    }

    /**
     * Sends a lobby invite request.
     * NOTE: Steam provides no functionality to determine if the user was successfully invited.
     *
     * @param appId        The ID of app which owns the lobby we're inviting a user to.
     * @param lobbySteamId The SteamID of the lobby we're inviting a user to.
     * @param userSteamId  The SteamID of the user we're inviting.
     */
    public void inviteToLobby(int appId, SteamID lobbySteamId, SteamID userSteamId) {
        ClientMsgProtobuf<CMsgClientMMSInviteToLobby.Builder> lobbyData =
                new ClientMsgProtobuf<>(CMsgClientMMSInviteToLobby.class, EMsg.ClientMMSInviteToLobby);

        CMsgClientMMSInviteToLobby.Builder body = lobbyData
                .getBody()
                .setAppId(appId)
                .setSteamIdLobby(lobbySteamId.convertToUInt64())
                .setSteamIdUserInvited(userSteamId.convertToUInt64());

        lobbyData.setBody(body);

        send(lobbyData, appId);
    }

    /**
     * Obtains a {@link Lobby}, by its SteamID, if the data is cached locally.
     * This method does not send a network request.
     *
     * @param appId        The ID of app which we're attempting to obtain a lobby for.
     * @param lobbySteamId The SteamID of the lobby that should be returned.
     * @return The {@link Lobby} corresponding with the specified app and lobby ID, if cached. Otherwise, <strong>null</strong>.
     */
    public Lobby getLobby(int appId, SteamID lobbySteamId) {
        return lobbyCache.getLobby(appId, lobbySteamId);
    }

    /**
     * Sends a matchmaking message for a specific app.
     *
     * @param msg   The matchmaking message to send.
     * @param appId The ID of the app this message pertains to.
     */
    public void send(ClientMsgProtobuf<?> msg, int appId) {
        if (msg == null) {
            throw new NullPointerException("msg is null");
        }

        msg.getProtoHeader().setRoutingAppid(appId);
        client.send(msg);
    }

    void clearLobbyCache() {
        lobbyCache.clear();
    }

    /**
     * Handles a client message. This should not be called directly.
     *
     * @param packetMsg The packet message that contains the data.
     */
    @Override
    public void handleMsg(IPacketMsg packetMsg) {
        if (packetMsg == null) {
            throw new IllegalArgumentException("packetMsg is null");
        }

        Consumer<IPacketMsg> dispatcher = dispatchMap.get(packetMsg.getMsgType());
        if (dispatcher != null) {
            dispatcher.accept(packetMsg);
        }
    }

    void handleCreateLobbyResponse(IPacketMsg packetMsg) {
        ClientMsgProtobuf<CMsgClientMMSCreateLobbyResponse.Builder> createLobbyResponse =
                new ClientMsgProtobuf<>(CMsgClientMMSCreateLobbyResponse.class, packetMsg);

        CMsgClientMMSCreateLobbyResponse.Builder body = createLobbyResponse.getBody();

        GeneratedMessageV3 request = lobbyManipulationRequests.remove(createLobbyResponse.getTargetJobID());

        if (body.getEresult() == EResult.OK.code() && request != null) {
            CMsgClientMMSCreateLobby createLobby = (CMsgClientMMSCreateLobby) request;

            List<Lobby.Member> members = new ArrayList<>(1);

            members.add(new Lobby.Member(client.getSteamID(), createLobby.getPersonaNameOwner(), null));

            lobbyCache.cacheLobby(
                    createLobby.getAppId(),
                    new Lobby(
                            new SteamID(body.getSteamIdLobby()),
                            ELobbyType.from(createLobby.getLobbyType()),
                            createLobby.getLobbyFlags(),
                            client.getSteamID(),
                            Lobby.decodeMetadata(createLobby.getMetadata().toByteArray()),
                            createLobby.getMaxMembers(),
                            1,
                            members,
                            null,
                            null
                    )
            );
        }

        CreateLobbyCallback callback = new CreateLobbyCallback(
                createLobbyResponse.getTargetJobID(),
                body.getAppId(),
                EResult.from(body.getEresult()),
                new SteamID(body.getSteamIdLobby())
        );
        client.postCallback(callback);
    }

    void handleSetLobbyDataResponse(IPacketMsg packetMsg) {
        ClientMsgProtobuf<CMsgClientMMSSetLobbyDataResponse.Builder> setLobbyDataResponse =
                new ClientMsgProtobuf<>(CMsgClientMMSSetLobbyDataResponse.class, packetMsg);

        CMsgClientMMSSetLobbyDataResponse.Builder body = setLobbyDataResponse.getBody();

        GeneratedMessageV3 request = lobbyManipulationRequests.remove(setLobbyDataResponse.getTargetJobID());

        if (body.getEresult() == EResult.OK.code() && request != null) {
            CMsgClientMMSSetLobbyData setLobbyData = (CMsgClientMMSSetLobbyData) request;
            Lobby lobby = lobbyCache.getLobby(setLobbyData.getAppId(), setLobbyData.getSteamIdLobby());

            if (lobby != null) {
                HashMap<String, String> metadata = Lobby.decodeMetadata(setLobbyData.getMetadata().toByteArray());

                if (setLobbyData.getSteamIdMember() == 0) {
                    lobbyCache.cacheLobby(
                            setLobbyData.getAppId(),
                            new Lobby(
                                    lobby.getSteamID(),
                                    ELobbyType.from(setLobbyData.getLobbyType()),
                                    setLobbyData.getLobbyFlags(),
                                    lobby.getOwnerSteamID(),
                                    metadata,
                                    setLobbyData.getMaxMembers(),
                                    lobby.getNumMembers(),
                                    lobby.getMembers(),
                                    lobby.getDistance(),
                                    lobby.getWeight()
                            )
                    );
                } else {
                    // I think this is right?
                    List<Lobby.Member> members = new ArrayList<>();
                    for (Lobby.Member member : lobby.getMembers()) {
                        if (member.getSteamID().convertToUInt64() == setLobbyData.getSteamIdMember()) {
                            members.add(new Lobby.Member(member.getSteamID(), member.getPersonaName(), metadata));
                        } else {
                            members.add(member);
                        }
                    }

                    lobbyCache.updateLobbyMembers(setLobbyData.getAppId(), lobby, members);
                }
            }
        }

        SetLobbyDataCallback callback = new SetLobbyDataCallback(
                setLobbyDataResponse.getTargetJobID(),
                body.getAppId(),
                EResult.from(body.getEresult()),
                new SteamID(body.getSteamIdLobby())
        );
        client.postCallback(callback);
    }

    void handleSetLobbyOwnerResponse(IPacketMsg packetMsg) {
        ClientMsgProtobuf<CMsgClientMMSSetLobbyOwnerResponse.Builder> setLobbyOwnerResponse =
                new ClientMsgProtobuf<>(CMsgClientMMSSetLobbyOwnerResponse.class, packetMsg);

        CMsgClientMMSSetLobbyOwnerResponse.Builder body = setLobbyOwnerResponse.getBody();

        GeneratedMessageV3 request = lobbyManipulationRequests.remove(setLobbyOwnerResponse.getTargetJobID());

        if (body.getEresult() == EResult.OK.code() && request != null) {
            CMsgClientMMSSetLobbyOwner setLobbyOwner = (CMsgClientMMSSetLobbyOwner) request;
            lobbyCache.updateLobbyOwner(body.getAppId(), body.getSteamIdLobby(), setLobbyOwner.getSteamIdNewOwner());
        }

        SetLobbyOwnerCallback callback = new SetLobbyOwnerCallback(
                setLobbyOwnerResponse.getTargetJobID(),
                body.getAppId(),
                EResult.from(body.getEresult()),
                new SteamID(body.getSteamIdLobby())
        );
        client.postCallback(callback);
    }

    void handleGetLobbyListResponse(IPacketMsg packetMsg) {
        ClientMsgProtobuf<CMsgClientMMSGetLobbyListResponse.Builder> lobbyListResponse =
                new ClientMsgProtobuf<>(CMsgClientMMSGetLobbyListResponse.class, packetMsg);

        CMsgClientMMSGetLobbyListResponse.Builder body = lobbyListResponse.getBody();

        List<Lobby> lobbyList = new ArrayList<>();
        for (CMsgClientMMSGetLobbyListResponse.Lobby lobby : body.getLobbiesList()) {
            Lobby existingLobby = lobbyCache.getLobby(body.getAppId(), lobby.getSteamId());
            List<Lobby.Member> members = existingLobby.getMembers();

            lobbyList.add(
                    new Lobby(
                            new SteamID(lobby.getSteamId()),
                            ELobbyType.from(lobby.getLobbyType()),
                            lobby.getLobbyFlags(),
                            existingLobby.getOwnerSteamID(),
                            Lobby.decodeMetadata(lobby.getMetadata().toByteArray()),
                            lobby.getMaxMembers(),
                            lobby.getNumMembers(),
                            members,
                            lobby.getDistance(),
                            lobby.getWeight()
                    )
            );
        }

        for (Lobby lobby : lobbyList) {
            lobbyCache.cacheLobby(body.getAppId(), lobby);
        }

        GetLobbyListCallback callback = new GetLobbyListCallback(
                lobbyListResponse.getTargetJobID(),
                body.getAppId(),
                EResult.from(body.getAppId()),
                lobbyList
        );
        client.postCallback(callback);
    }

    void handleJoinLobbyResponse(IPacketMsg packetMsg) {
        ClientMsgProtobuf<CMsgClientMMSJoinLobbyResponse.Builder> joinLobbyResponse =
                new ClientMsgProtobuf<>(CMsgClientMMSJoinLobbyResponse.class, packetMsg);

        CMsgClientMMSJoinLobbyResponse.Builder body = joinLobbyResponse.getBody();

        Lobby joinedLobby = null;

        List<Lobby.Member> members = new ArrayList<>();
        if (body.getSteamIdLobby() != 0L) { // This conditional could be problematic
            for (CMsgClientMMSJoinLobbyResponse.Member member : body.getMembersList()) {
                members.add(
                        new Lobby.Member(
                                member.getSteamId(),
                                member.getPersonaName(),
                                Lobby.decodeMetadata(member.getMetadata().toByteArray())
                        )
                );
            }

            Lobby cachedLobby = lobbyCache.getLobby(body.getAppId(), body.getSteamIdLobby());

            joinedLobby = new Lobby(
                    new SteamID(body.getSteamIdLobby()),
                    ELobbyType.from(body.getLobbyType()),
                    body.getLobbyFlags(),
                    new SteamID(body.getSteamIdOwner()),
                    Lobby.decodeMetadata(body.getMetadata().toByteArray()),
                    body.getMaxMembers(),
                    members.size(),
                    members,
                    cachedLobby.getDistance(),
                    cachedLobby.getWeight()
            );

            lobbyCache.cacheLobby(body.getAppId(), joinedLobby);
        }

        JoinLobbyCallback callback = new JoinLobbyCallback(
                joinLobbyResponse.getTargetJobID(),
                body.getAppId(),
                EChatRoomEnterResponse.from(body.getChatRoomEnterResponse()),
                joinedLobby
        );
        client.postCallback(callback);
    }

    void handleLeaveLobbyResponse(IPacketMsg packetMsg) {
        ClientMsgProtobuf<CMsgClientMMSLeaveLobbyResponse.Builder> leaveLobbyResponse =
                new ClientMsgProtobuf<>(CMsgClientMMSLeaveLobbyResponse.class, packetMsg);

        CMsgClientMMSLeaveLobbyResponse.Builder body = leaveLobbyResponse.getBody();

        if (body.getEresult() == EResult.OK.code()) {
            lobbyCache.clearLobbyMembers(body.getAppId(), body.getSteamIdLobby());
        }

        LeaveLobbyCallback callback = new LeaveLobbyCallback(
                leaveLobbyResponse.getTargetJobID(),
                body.getAppId(),
                body.getEresult(),
                body.getSteamIdLobby()
        );
        client.postCallback(callback);
    }

    void handleLobbyData(IPacketMsg packetMsg) {
        ClientMsgProtobuf<CMsgClientMMSLobbyData.Builder> lobbyDataResponse =
                new ClientMsgProtobuf<>(CMsgClientMMSLobbyData.class, packetMsg);

        CMsgClientMMSLobbyData.Builder body = lobbyDataResponse.getBody();

        Lobby cachedLobby = lobbyCache.getLobby(body.getAppId(), body.getSteamIdLobby());

        List<Lobby.Member> members = new ArrayList<>();

        if (!body.getMembersList().isEmpty()) {
            for (CMsgClientMMSLobbyData.Member member : body.getMembersList()) {
                members.add(
                        new Lobby.Member(
                                member.getSteamId(),
                                member.getPersonaName(),
                                Lobby.decodeMetadata(member.getMetadata().toByteArray()))
                );
            }
        } else {
            members = cachedLobby.getMembers();
        }

        Lobby updatedLobby = new Lobby(
                new SteamID(body.getSteamIdLobby()),
                ELobbyType.from(body.getLobbyType()),
                body.getLobbyFlags(),
                new SteamID(body.getSteamIdOwner()),
                Lobby.decodeMetadata(body.getMetadata().toByteArray()),
                body.getMaxMembers(),
                body.getNumMembers(),
                members,
                cachedLobby.getDistance(),
                cachedLobby.getWeight()
        );

        lobbyCache.cacheLobby(body.getAppId(), updatedLobby);

        LobbyDataCallback callback = new LobbyDataCallback(lobbyDataResponse.getTargetJobID(), body.getAppId(), updatedLobby);
        client.postCallback(callback);
    }

    void handleUserJoinedLobby(IPacketMsg packetMsg) {
        ClientMsgProtobuf<CMsgClientMMSUserJoinedLobby.Builder> userJoinedLobby =
                new ClientMsgProtobuf<>(CMsgClientMMSUserJoinedLobby.class, packetMsg);

        CMsgClientMMSUserJoinedLobby.Builder body = userJoinedLobby.getBody();

        Lobby lobby = lobbyCache.getLobby(body.getAppId(), body.getSteamIdLobby());

        if (lobby != null && lobby.getMembers().size() > 0) {
            Lobby.Member joiningMember = lobbyCache.addLobbyMember(body.getAppId(), lobby, body.getSteamIdUser(), body.getPersonaName());

            if (joiningMember != null) {
                UserJoinedLobbyCallback callback = new UserJoinedLobbyCallback(body.getAppId(), body.getSteamIdLobby(), joiningMember);

                client.postCallback(callback);
            }
        }
    }

    void handleUserLeftLobby(IPacketMsg packetMsg) {
        ClientMsgProtobuf<CMsgClientMMSUserLeftLobby.Builder> userLeftLobby =
                new ClientMsgProtobuf<>(CMsgClientMMSUserLeftLobby.class, packetMsg);

        CMsgClientMMSUserLeftLobby.Builder body = userLeftLobby.getBody();

        Lobby lobby = lobbyCache.getLobby(body.getAppId(), body.getSteamIdLobby());

        if (lobby != null && lobby.getMembers().size() > 0) {
            Lobby.Member leavingMember = lobbyCache.removeLobbyMember(body.getAppId(), lobby, body.getSteamIdUser());
            if (leavingMember == null) {
                return;
            }

            if (leavingMember.getSteamID() == client.getSteamID()) {
                lobbyCache.clearLobbyMembers(body.getAppId(), body.getSteamIdLobby());
            }

            UserLeftLobbyCallback callback = new UserLeftLobbyCallback(body.getAppId(), body.getSteamIdLobby(), leavingMember);
            client.postCallback(callback);
        }
    }
}
