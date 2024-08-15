package in.dragonbra.javasteam.steam.handlers.steamfriends;

import com.google.protobuf.ByteString;
import in.dragonbra.javasteam.base.ClientMsgProtobuf;
import in.dragonbra.javasteam.base.PacketClientMsgProtobuf;
import in.dragonbra.javasteam.enums.*;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverFriends;
import in.dragonbra.javasteam.steam.handlers.HandlerTestBase;
import in.dragonbra.javasteam.types.GameID;
import in.dragonbra.javasteam.types.SteamID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

// Maybe some cold snowy day this can be condensed down into some test packets.

/**
 * @author Lossy
 * @since 2024-08-14
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class FriendCacheTest extends HandlerTestBase<SteamFriends> {

    @Override
    protected SteamFriends createHandler() {
        return new SteamFriends();
    }

    @Test
    public void verifyLocalUser() throws IOException {
        var sid = steamClient.getSteamID();
        var avatarHash = "fef49e7fa7e1997310d705b2a6158ff8dc1cdfeb";
        var personaStateSet = EnumSet.of(EPersonaStateFlag.InJoinableGame, EPersonaStateFlag.ClientTypeMobile);

        var friendsListMsg = getPacket(EMsg.ClientFriendsList, true);
        handler.handleMsg(friendsListMsg);

        sid.setAccountType(EAccountType.Individual);

        var localUser = SteammessagesClientserverFriends.CMsgClientPersonaState.Friend.newBuilder();
        localUser.setAvatarHash(ByteString.copyFromUtf8(avatarHash));
        localUser.setFriendid(sid.convertToUInt64());
        localUser.setGameName("Team Fortress 2");
        localUser.setGameid(440);
        localUser.setGamePlayedAppId(440);
        localUser.setPersonaState(EPersonaState.Online.code());
        localUser.setPlayerName("testpersonaname");
        localUser.setPersonaStateFlags(
                EPersonaStateFlag.code(personaStateSet)
        );

        var personaState = new ClientMsgProtobuf<SteammessagesClientserverFriends.CMsgClientPersonaState.Builder>(
                SteammessagesClientserverFriends.CMsgClientPersonaState.class,
                EMsg.ClientPersonaState
        );
        personaState.getBody().setStatusFlags(
                EClientPersonaStateFlag.code(
                        EnumSet.of(EClientPersonaStateFlag.PlayerName, EClientPersonaStateFlag.Presence, EClientPersonaStateFlag.GameDataBlob)
                )
        );
        personaState.getBody().addFriends(localUser.build());

        var packet = new PacketClientMsgProtobuf(EMsg.ClientPersonaState, personaState.serialize());
        handler.handleMsg(packet);

        // AccountCache
        Assertions.assertTrue(handler.isLocalUser());

        // Account
        Assertions.assertEquals(sid, handler.getFriendSteamID(sid));
        Assertions.assertEquals("testpersonaname", handler.getPersonaName());
        Assertions.assertNotNull(handler.getPersonaAvatar());
        Assertions.assertEquals(avatarHash, new String((handler.getPersonaAvatar())));

        // User
        Assertions.assertNull(handler.getFriendRelationship(sid));
        Assertions.assertEquals(EPersonaState.Online, handler.getFriendPersonaState(sid));
        Assertions.assertEquals(personaStateSet, handler.getFriendPersonaStateFlags(sid));
        Assertions.assertEquals(440, handler.getFriendGameAppId(sid));
        Assertions.assertEquals(new GameID(440), handler.getFriendGamePlayed(sid));
        Assertions.assertEquals("Team Fortress 2", handler.getFriendGamePlayedName(sid));
    }

    @Test
    public void verifyCachedFriends() throws IOException {
        List<SteammessagesClientserverFriends.CMsgClientFriendsList.Friend> list = new ArrayList<>();

        for (int idx = 0; idx < 10; idx++) {
            var friendid = new SteamID(1234 + idx);
            friendid.setAccountType(EAccountType.Individual);

            var friend = SteammessagesClientserverFriends.CMsgClientFriendsList.Friend.newBuilder();
            friend.setUlfriendid(friendid.convertToUInt64());
            friend.setEfriendrelationship(EFriendRelationship.Friend.code());

            list.add(friend.build());
        }

        var msg = new ClientMsgProtobuf<SteammessagesClientserverFriends.CMsgClientFriendsList.Builder>(
                SteammessagesClientserverFriends.CMsgClientFriendsList.class,
                EMsg.ClientFriendsList
        );
        msg.getBody().setBincremental(false);
        msg.getBody().addAllFriends(list);

        var packet = new PacketClientMsgProtobuf(EMsg.ClientFriendsList, msg.serialize());
        handler.handleMsg(packet);

        Assertions.assertEquals(10, handler.getCachedUsers().size());
        Assertions.assertEquals(10, handler.getFriendCount());

        var sid2 = new SteamID(1236);
        sid2.setAccountType(EAccountType.Individual);

        Assertions.assertEquals(sid2, handler.getFriendSteamID(sid2));
        Assertions.assertEquals(sid2, handler.getFriendByIndex(2));
        Assertions.assertEquals(EFriendRelationship.Friend, handler.getFriendRelationship(sid2));
    }

    @Test
    public void verifyCachedClans() throws IOException {
        List<SteammessagesClientserverFriends.CMsgClientFriendsList.Friend> list = new ArrayList<>();

        for (int idx = 0; idx < 10; idx++) {
            var clanid = new SteamID(1234 + idx);
            clanid.setAccountType(EAccountType.Clan);

            var clan = SteammessagesClientserverFriends.CMsgClientFriendsList.Friend.newBuilder();
            clan.setUlfriendid(clanid.convertToUInt64());

            list.add(clan.build());
        }

        var msg = new ClientMsgProtobuf<SteammessagesClientserverFriends.CMsgClientFriendsList.Builder>(
                SteammessagesClientserverFriends.CMsgClientFriendsList.class,
                EMsg.ClientFriendsList
        );
        msg.getBody().setBincremental(false);
        msg.getBody().addAllFriends(list);

        var packet = new PacketClientMsgProtobuf(EMsg.ClientFriendsList, msg.serialize());
        handler.handleMsg(packet);

        Assertions.assertEquals(10, handler.getCachedClans().size());
        Assertions.assertEquals(10, handler.getClanCount());

        var sid2 = new SteamID(1236);
        sid2.setAccountType(EAccountType.Clan);

        Assertions.assertEquals(sid2, handler.getClanSteamID(sid2));
        Assertions.assertEquals(sid2, handler.getClanByIndex(2));
    }
}
