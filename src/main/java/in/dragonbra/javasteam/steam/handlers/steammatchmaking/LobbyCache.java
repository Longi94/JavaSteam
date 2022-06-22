package in.dragonbra.javasteam.steam.handlers.steammatchmaking;

import in.dragonbra.javasteam.types.SteamID;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author lossy
 * @since 2022-06-21
 */
public class LobbyCache {

    private ConcurrentMap<Integer, ConcurrentHashMap<SteamID, Lobby>> lobbies = new ConcurrentHashMap<>();

    public Lobby getLobby(int appId, SteamID lobbySteamId) {
        return getAppLobbies(appId).get(lobbySteamId); // getOrDefault jdk 8
    }

    public Lobby getLobby(int appId, long lobbySteamId) {
        return getAppLobbies(appId).get(new SteamID(lobbySteamId)); // getOrDefault jdk 8
    }

    public Lobby cacheLobby(int appId, Lobby lobby) {
        return getAppLobbies(appId).put(lobby.getSteamID(), lobby);
    }

    public Lobby.Member addLobbyMember(int appId, Lobby lobby, long memberId, String personaName) {
        return addLobbyMember(appId, lobby, new SteamID(memberId), personaName);
    }

    public Lobby.Member addLobbyMember(int appId, Lobby lobby, SteamID memberId, String personaName) {
        Lobby.Member existingMember = null;

        for (Lobby.Member member : lobby.getMembers()) {
            if (member.getSteamID().equals(memberId)) {
                existingMember = member;
                break;
            }
        }

        if (existingMember != null) {
            // Already in lobby.
            return null;
        }

        Lobby.Member addedMember = new Lobby.Member(memberId, personaName, null);

        List<Lobby.Member> members = new ArrayList<>(lobby.getMembers().size() + 1);
        members.addAll(lobby.getMembers());
        members.add(addedMember);

        updateLobbyMembers(appId, lobby, members);

        return addedMember;
    }

    public Lobby.Member removeLobbyMember(int appId, Lobby lobby, long memberId) {
        return removeLobbyMember(appId, lobby, new SteamID(memberId));
    }

    public Lobby.Member removeLobbyMember(int appId, Lobby lobby, SteamID memberId) {
        Lobby.Member removedMember = null;

        for (Lobby.Member member : lobby.getMembers()) {
            if (member.getSteamID().equals(memberId)) {
                removedMember = member;
                break;
            }
        }

        if (removedMember == null) {
            return null;
        }

        List<Lobby.Member> members = new ArrayList<>();

        // I think this is correct for jdk 7 style
        for (Lobby.Member member : lobby.getMembers()) {
            if (!member.equals(removedMember)) {
                members.add(member);
            }
        }

        if (members.size() > 0) {
            updateLobbyMembers(appId, lobby, members);
        } else {
            // Steam deletes lobbies that contain no members.
            deleteLobby(appId, lobby.getSteamID());
        }

        return removedMember;
    }

    public void clearLobbyMembers(int appId, long lobbySteamId) {
        clearLobbyMembers(appId, new SteamID(lobbySteamId));
    }

    public void clearLobbyMembers(int appId, SteamID lobbySteamId) {
        Lobby lobby = getLobby(appId, lobbySteamId);

        if (lobby != null) {
            updateLobbyMembers(appId, lobby, null, null);
        }
    }

    public void updateLobbyOwner(int appId, long lobbySteamId, long ownerSteamId) {
        updateLobbyOwner(appId, new SteamID(lobbySteamId), new SteamID(ownerSteamId));
    }

    public void updateLobbyOwner(int appId, SteamID lobbySteamId, SteamID ownerSteamId) {
        Lobby lobby = getLobby(appId, lobbySteamId);

        if (lobby != null) {
            updateLobbyMembers(appId, lobby, ownerSteamId, lobby.getMembers());
        }
    }

    public void updateLobbyMembers(int appId, Lobby lobby, List<Lobby.Member> members) {
        updateLobbyMembers(appId, lobby, lobby.getOwnerSteamID(), members);
    }

    public void clear() {
        lobbies.clear();
    }

    void updateLobbyMembers(int appId, Lobby lobby, SteamID owner, List<Lobby.Member> members) {
        cacheLobby(appId, new Lobby(
                lobby.getSteamID(),
                lobby.getLobbyType(),
                lobby.getLobbyFlags(),
                owner,
                lobby.getMetadata(),
                lobby.getMaxMembers(),
                lobby.getNumMembers(),
                members,
                lobby.getDistance(),
                lobby.getWeight()
        ));
    }

    public ConcurrentMap<SteamID, Lobby> getAppLobbies(int appId) {
        return lobbies.putIfAbsent(appId, new ConcurrentHashMap<SteamID, Lobby>());
    }

    public Lobby deleteLobby(int appId, SteamID lobbySteamId) {
        ConcurrentHashMap<SteamID, Lobby> appLobbies = lobbies.get(appId);

        if (appLobbies == null) {
            return null;
        }

        return appLobbies.remove(lobbySteamId);
    }
}
