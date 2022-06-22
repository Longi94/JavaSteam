package in.dragonbra.javasteam.steam.handlers.steamfriends;

import in.dragonbra.javasteam.enums.EClanRelationship;
import in.dragonbra.javasteam.enums.EFriendRelationship;
import in.dragonbra.javasteam.enums.EPersonaState;
import in.dragonbra.javasteam.enums.EPersonaStateFlag;
import in.dragonbra.javasteam.steam.handlers.HandlerTestBase;
import in.dragonbra.javasteam.types.GameID;
import in.dragonbra.javasteam.types.SteamID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.EnumSet;

import static org.junit.Assert.*;

/**
 * @author lossy
 * @since 2022-06-21
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class FriendCacheTest extends HandlerTestBase<SteamFriends> {

    @Override
    protected SteamFriends createHandler() {
        return new SteamFriends();
    }

    @Test
    public void testAbstractAccountClass() {
        FriendCache.User abstractAccount = new FriendCache.User();

        SteamID steamID = new SteamID(76561198003805806L);
        String avatarHash = "cfa928ab4119dd137e50d728e8fe703e4e970aff";

        // add random info to User.
        abstractAccount.setSteamID(steamID);
        abstractAccount.setName("Some Abstract Name");
        abstractAccount.setAvatarHash(avatarHash.getBytes());

        assertEquals(76561198003805806L, abstractAccount.getSteamID().convertToUInt64());
        assertEquals("Some Abstract Name", abstractAccount.getName());
        assertEquals(avatarHash, new String(abstractAccount.getAvatarHash()));
    }

    @Test
    public void testUserCacheAccount() {
        FriendCache.User user = new FriendCache.User();

        GameID gameID = new GameID(420, "Research and Development");
        EnumSet<EPersonaStateFlag> stateFlags = EnumSet.of(EPersonaStateFlag.ClientTypeVR);

        // add random info to User.
        user.setRelationship(EFriendRelationship.Friend);
        user.setPersonaState(EPersonaState.Online);
        user.setPersonaStateFlags(stateFlags);
        user.setGameAppID(420);
        user.setGameID(gameID);
        user.setGameName("Some Game");

        assertEquals(EFriendRelationship.Friend, user.getRelationship());
        assertEquals(EPersonaState.Online, user.getPersonaState());
        assertTrue(user.getPersonaStateFlags().contains(EPersonaStateFlag.ClientTypeVR));
        assertEquals(420, user.getGameAppID());
        assertTrue(user.getGameID().isMod());
        assertEquals(420, user.getGameID().getAppID());
        assertEquals(new GameID(0x8db24e81010001a4L), user.getGameID());
        assertEquals("Some Game", user.getGameName());
    }

    @Test
    public void testClanCacheAccount() {
        FriendCache.Clan clan = new FriendCache.Clan();

        // add random info to Clan
        clan.setRelationship(EClanRelationship.PendingApproval);

        assertEquals(EClanRelationship.PendingApproval, clan.getRelationship());
    }

    /**
     * Test coverage for {@link FriendCache.AccountCache}.
     * <p>
     * Mostly to verify both Type Erasure and ConcurrentHashMap work, see {@link FriendCache.AccountList}
     */
    @Test
    public void textAccountClass() {
        FriendCache.AccountCache accountCache = new FriendCache.AccountCache();

        /* Test Clan Stuff */
        SteamID clan1 = new SteamID(103582791434671111L);
        SteamID clan2 = new SteamID(103582791429522222L);
        SteamID clan3 = new SteamID(103582791429523333L);

        assertTrue(clan1.isClanAccount());
        assertTrue(clan2.isClanAccount());
        assertTrue(clan3.isClanAccount());

        FriendCache.Clan clan1exist = accountCache.getClan(clan1);
        FriendCache.Clan clan2exist = accountCache.getClan(clan2);
        FriendCache.Clan clan3exist = accountCache.getClan(clan3);

        assertTrue(accountCache.getClans().contains(clan1exist));
        assertTrue(accountCache.getClans().contains(clan2exist));
        assertTrue(accountCache.getClans().contains(clan3exist));
        assertEquals(clan1,  clan1exist.getSteamID());
        assertEquals(clan2,  clan2exist.getSteamID());
        assertEquals(clan3,  clan3exist.getSteamID());

        assertEquals(3, accountCache.getClans().size());

        /* Test User Stuff */
        SteamID user1 = new SteamID(76561197960265700L);
        SteamID user2 = new SteamID(76561197960265711L);

        assertTrue(user1.isIndividualAccount());
        assertTrue(user2.isIndividualAccount());

        FriendCache.User user1Exist = accountCache.getUser(user1);
        FriendCache.User user2Exist = accountCache.getUser(user2);

        assertTrue(accountCache.getUsers().contains(user1Exist));
        assertTrue(accountCache.getUsers().contains(user2Exist));
        assertEquals(user1, user1Exist.getSteamID());
        assertEquals(user2, user2Exist.getSteamID());

        assertEquals(2, accountCache.getUsers().size());

        /* Test Local User */
        SteamID localSteamID = steamClient.getSteamID();
        accountCache.getLocalUser().setSteamID(localSteamID);

        assertTrue(accountCache.isLocalUser(localSteamID));
        assertSame(localSteamID, accountCache.getLocalUser().getSteamID());
        assertSame(localSteamID, accountCache.getUser(localSteamID).getSteamID());
    }
}
