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
                EPersonaStateFlag.code(
                        EnumSet.of(EPersonaStateFlag.InJoinableGame, EPersonaStateFlag.ClientTypeMobile)
                )
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

        handler.handleMsg(new PacketClientMsgProtobuf(EMsg.ClientPersonaState, personaState.serialize()));

        // AccountCache
        Assertions.assertTrue(handler.isLocalUser());

        // Account
        Assertions.assertEquals(sid, handler.getFriendSteamID(sid));
        Assertions.assertEquals("testpersonaname", handler.getPersonaName());
        Assertions.assertEquals(avatarHash, new String((handler.getPersonaAvatar())));

        // User
        Assertions.assertNull(handler.getFriendRelationship(sid));
        Assertions.assertEquals(EPersonaState.Online, handler.getFriendPersonaState(sid));
        Assertions.assertEquals(EnumSet.of(EPersonaStateFlag.InJoinableGame, EPersonaStateFlag.ClientTypeMobile), handler.getFriendPersonaStateFlags(sid));
        Assertions.assertEquals(440, handler.getFriendGameAppId(sid));
        Assertions.assertEquals(new GameID(440), handler.getFriendGamePlayed(sid));
        Assertions.assertEquals("Team Fortress 2", handler.getFriendGamePlayedName(sid));
    }

    @Test
    public void verifyCachedFriends() throws IOException {
        var msg = new ClientMsgProtobuf<SteammessagesClientserverFriends.CMsgClientFriendsList.Builder>(
                SteammessagesClientserverFriends.CMsgClientFriendsList.class,
                EMsg.ClientFriendsList
        );
        msg.getBody().setBincremental(false);

        List<SteammessagesClientserverFriends.CMsgClientFriendsList.Friend> list = new ArrayList<>();

        for (int idx = 0; idx < 10; idx++) {
            var friendid = new SteamID(1234 + idx);
            friendid.setAccountType(EAccountType.Individual);

            var friend = SteammessagesClientserverFriends.CMsgClientFriendsList.Friend.newBuilder();
            friend.setUlfriendid(friendid.convertToUInt64());
            friend.setEfriendrelationship(EFriendRelationship.Friend.code());

            list.add(friend.build());
        }

        msg.getBody().addAllFriends(list);

        handler.handleMsg(new PacketClientMsgProtobuf(EMsg.ClientFriendsList, msg.serialize()));

        Assertions.assertEquals(10, handler.getCachedUsers().size());
        Assertions.assertEquals(10, handler.getFriendsList().size());

        var sid2 = new SteamID(1236);
        sid2.setAccountType(EAccountType.Individual);
        Assertions.assertEquals(sid2, handler.getFriendSteamID(sid2));
        Assertions.assertEquals(sid2, handler.getFriendByIndex(2));
        Assertions.assertEquals(EFriendRelationship.Friend, handler.getFriendRelationship(sid2));
    }

    // TODO clan testing
}
