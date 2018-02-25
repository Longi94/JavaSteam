package in.dragonbra.javasteam.types;

import in.dragonbra.javasteam.TestBase;
import in.dragonbra.javasteam.enums.EAccountType;
import in.dragonbra.javasteam.enums.EUniverse;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author lngtr
 * @since 2018-02-19
 */
public class SteamIDTests extends TestBase {

    @Test
    public void testEmptyConstructorValid() {
        SteamID sid = new SteamID();

        assertFalse(sid.isValid());
    }


    @Test
    public void FullConstructorValid() {
        SteamID sid = new SteamID(1234, SteamID.CONSOLE_INSTANCE, EUniverse.Beta, EAccountType.Chat);

        assertEquals(1234L, sid.getAccountID());
        assertEquals(SteamID.CONSOLE_INSTANCE, sid.getAccountInstance());
        assertEquals(EUniverse.Beta, sid.getAccountUniverse());
        assertEquals(EAccountType.Chat, sid.getAccountType());


        sid = new SteamID(4321, EUniverse.Invalid, EAccountType.Pending);

        assertEquals(4321L, sid.getAccountID());
        assertEquals(SteamID.DESKTOP_INSTANCE, sid.getAccountInstance());
        assertEquals(EUniverse.Invalid, sid.getAccountUniverse());
        assertEquals(EAccountType.Pending, sid.getAccountType());
    }

    @Test
    public void LongConstructorAndSetterGetterValid() {
        SteamID sid = new SteamID(103582791432294076L);

        assertEquals(2772668L, sid.getAccountID());
        assertEquals(SteamID.ALL_INSTANCES, sid.getAccountInstance());
        assertEquals(EUniverse.Public, sid.getAccountUniverse());
        assertEquals(EAccountType.Clan, sid.getAccountType());

        sid.setFromUInt64(157626004137848889L);

        assertEquals(12345L, sid.getAccountID());
        assertEquals(SteamID.WEB_INSTANCE, sid.getAccountInstance());
        assertEquals(EUniverse.Beta, sid.getAccountUniverse());
        assertEquals(EAccountType.GameServer, sid.getAccountType());

        assertEquals(157626004137848889L, sid.convertToUInt64());
    }

    @Test
    public void Steam2CorrectParse() {
        SteamID sidEven = new SteamID("STEAM_0:0:4491990");

        assertEquals(8983980L, sidEven.getAccountID());
        assertEquals(SteamID.DESKTOP_INSTANCE, sidEven.getAccountInstance());
        assertEquals(EUniverse.Public, sidEven.getAccountUniverse());


        SteamID sidOdd = new SteamID("STEAM_0:1:4491990");

        assertEquals(8983981L, sidOdd.getAccountID());
        assertEquals(SteamID.DESKTOP_INSTANCE, sidOdd.getAccountInstance());
        assertEquals(EUniverse.Public, sidOdd.getAccountUniverse());
    }

    @Test
    public void setFromSteam3StringCorrectParse() {
        SteamID sidUser = new SteamID();
        sidUser.setFromSteam3String("[U:1:123]");
        assertEquals(123L, sidUser.getAccountID());
        assertEquals(EUniverse.Public, sidUser.getAccountUniverse());
        assertEquals(1L, sidUser.getAccountInstance());
        assertEquals(EAccountType.Individual, sidUser.getAccountType());

        SteamID sidAnonGSUser = new SteamID();
        sidAnonGSUser.setFromSteam3String("[A:1:123:456]");
        assertEquals(123L, sidAnonGSUser.getAccountID());
        assertEquals(EUniverse.Public, sidAnonGSUser.getAccountUniverse());
        assertEquals(456L, sidAnonGSUser.getAccountInstance());
        assertEquals(EAccountType.AnonGameServer, sidAnonGSUser.getAccountType());

        SteamID sidLobby = new SteamID();
        sidLobby.setFromSteam3String("[L:1:123]");
        assertEquals(123L, sidLobby.getAccountID());
        assertEquals(EUniverse.Public, sidLobby.getAccountUniverse());
        assertTrue((sidLobby.getAccountInstance() & SteamID.ChatInstanceFlags.LOBBY.code()) > 0);
        assertEquals(EAccountType.Chat, sidLobby.getAccountType());

        SteamID sidClanChat = new SteamID();
        sidClanChat.setFromSteam3String("[c:1:123]");
        assertEquals(123L, sidClanChat.getAccountID());
        assertEquals(EUniverse.Public, sidClanChat.getAccountUniverse());

        assertTrue((sidClanChat.getAccountInstance() & SteamID.ChatInstanceFlags.CLAN.code()) > 0);
        assertEquals(EAccountType.Chat, sidClanChat.getAccountType());

        SteamID sidMultiseat = new SteamID();
        sidMultiseat.setFromSteam3String("[M:1:123:456]");
        assertEquals(123L, sidMultiseat.getAccountID());
        assertEquals(EUniverse.Public, sidMultiseat.getAccountUniverse());
        assertEquals(456L, sidMultiseat.getAccountInstance());
        assertEquals(EAccountType.Multiseat, sidMultiseat.getAccountType());

        SteamID sidLowercaseI = new SteamID();
        sidLowercaseI.setFromSteam3String("[i:2:456]");
        assertEquals(456L, sidLowercaseI.getAccountID());
        assertEquals(EUniverse.Beta, sidLowercaseI.getAccountUniverse());
        assertEquals(1L, sidLowercaseI.getAccountInstance());
        assertEquals(EAccountType.Invalid, sidLowercaseI.getAccountType());
    }

    @Test
    public void SetFromOldStyleSteam3StringCorrectParse() {
        SteamID sidMultiseat = new SteamID();
        sidMultiseat.setFromSteam3String("[M:1:123(456)]");
        assertEquals(123L, sidMultiseat.getAccountID());
        assertEquals(EUniverse.Public, sidMultiseat.getAccountUniverse());
        assertEquals(456L, sidMultiseat.getAccountInstance());
        assertEquals(EAccountType.Multiseat, sidMultiseat.getAccountType());

        SteamID sidAnonGSUser = new SteamID();
        sidAnonGSUser.setFromSteam3String("[A:1:123(456)]");
        assertEquals(123L, sidAnonGSUser.getAccountID());
        assertEquals(EUniverse.Public, sidAnonGSUser.getAccountUniverse());
        assertEquals(456L, sidAnonGSUser.getAccountInstance());
        assertEquals(EAccountType.AnonGameServer, sidAnonGSUser.getAccountType());
    }

    @Test
    public void Steam3StringSymmetric() {
        String[] steamIds = new String[]{
                "[U:1:123]",
                "[U:1:123:2]",
                "[G:1:626]",
                "[A:2:165:1234]"
        };

        for (String steamId : steamIds) {
            SteamID sid = new SteamID();
            boolean parsed = sid.setFromSteam3String(steamId);
            assertTrue(parsed);
            assertEquals(steamId, sid.render());
        }
    }

    @Test
    public void setFromStringHandlesInvalid() {
        SteamID sid = new SteamID();

        boolean setFromNullString = sid.setFromString(null, EUniverse.Public);
        assertFalse(setFromNullString);

        boolean setFromEmptyString = sid.setFromString("", EUniverse.Public);
        assertFalse(setFromEmptyString);

        boolean setFromInvalidString = sid.setFromString("NOT A STEAMID!", EUniverse.Public);
        assertFalse(setFromInvalidString);

        boolean setFromInvalidAccountId = sid.setFromString("STEAM_0:1:999999999999999999999999999999", EUniverse.Public);
        assertFalse(setFromInvalidAccountId);

        boolean universeOutOfRange = sid.setFromSteam3String("STEAM_5:0:123");
        assertFalse(universeOutOfRange);
    }

    @Test
    public void setFromSteam3StringHandlesInvalid() {
        SteamID sid = new SteamID();

        boolean setFromNullString = sid.setFromSteam3String(null);
        assertFalse(setFromNullString);

        boolean setFromEmptyString = sid.setFromSteam3String("");
        assertFalse(setFromEmptyString);

        boolean setFromInvalidString = sid.setFromSteam3String("NOT A STEAMID!");
        assertFalse(setFromInvalidString);

        boolean setFromInvalidAccountId = sid.setFromSteam3String("STEAM_0:1:999999999999999999999999999999");
        assertFalse(setFromInvalidAccountId);

        boolean setFromSteam2String = sid.setFromSteam3String("STEAM_0:1:4491990");
        assertFalse(setFromSteam2String);

        boolean mixingBracketsAndColons1 = sid.setFromSteam3String("[A:1:2:345)]");
        assertFalse(mixingBracketsAndColons1);

        boolean mixingBracketsAndColons2 = sid.setFromSteam3String("[A:1:2(345]");
        assertFalse(mixingBracketsAndColons2);

        boolean universeOutOfRange = sid.setFromSteam3String("[U:5:123]");
        assertFalse(universeOutOfRange);
    }

    @Test
    public void SetValidAndHandlesClan() {
        SteamID sid = new SteamID();

        sid.set(1234L, EUniverse.Internal, EAccountType.ContentServer);

        assertEquals(1234L, sid.getAccountID());
        assertEquals(EUniverse.Internal, sid.getAccountUniverse());
        assertEquals(EAccountType.ContentServer, sid.getAccountType());
        assertEquals(SteamID.DESKTOP_INSTANCE, sid.getAccountInstance());


        sid.set(4321L, EUniverse.Public, EAccountType.Clan);

        assertEquals(4321L, sid.getAccountID());
        assertEquals(EUniverse.Public, sid.getAccountUniverse());
        assertEquals(EAccountType.Clan, sid.getAccountType());
        assertEquals(0L, sid.getAccountInstance());
    }

    @Test
    public void Steam2RenderIsValid() {
        SteamID sid = new SteamID(76561197969249708L);

        assertEquals("STEAM_0:0:4491990", sid.render(false));

        sid.setAccountUniverse(EUniverse.Beta);
        assertEquals("STEAM_2:0:4491990", sid.render(false));

        sid.setAccountType(EAccountType.GameServer);
        assertEquals("157625991261918636", sid.render(false));
    }

    @Test
    public void RendersSteam3ByDefault() {
        SteamID sid = new SteamID(76561197969249708L);

        assertEquals("[U:1:8983980]", sid.render());
        assertEquals("[U:1:8983980]", sid.toString());
    }

    @Test
    public void SteamIDsEquality() {
        SteamID sid = new SteamID(76561197969249708L);
        SteamID sid2 = new SteamID(76561197969249708L);

        assertTrue(sid.equals(sid2));

        assertFalse(sid.equals(new Object()));
        SteamID sid3 = new SteamID(12345L);

        assertFalse(sid.equals(sid3));
    }

    @Test
    public void SteamIDHashCodeUsesLongHashCode() {
        SteamID sid = new SteamID(172376458626834L);
        Long longValue = 172376458626834L;

        assertTrue(sid.hashCode() == longValue.hashCode());
    }

    @Test
    public void InitializesInstancesCorrectly() {
        SteamID sid = new SteamID();

        sid.setFromSteam3String("[g:1:1234]");
        assertEquals(EUniverse.Public, sid.getAccountUniverse());
        assertEquals(EAccountType.Clan, sid.getAccountType());
        assertEquals(0L, sid.getAccountInstance());
        assertEquals(1234L, sid.getAccountID());

        sid.setFromSteam3String("[T:1:1234]");
        assertEquals(EUniverse.Public, sid.getAccountUniverse());
        assertEquals(EAccountType.Chat, sid.getAccountType());
        assertEquals(0L, sid.getAccountInstance());
        assertEquals(1234L, sid.getAccountID());

        sid.setFromSteam3String("[c:1:1234]");
        assertEquals(EUniverse.Public, sid.getAccountUniverse());
        assertEquals(EAccountType.Chat, sid.getAccountType());
        assertEquals(SteamID.ChatInstanceFlags.CLAN.code(), sid.getAccountInstance());
        assertEquals(1234L, sid.getAccountID());

        sid.setFromSteam3String("[L:1:1234]");
        assertEquals(EUniverse.Public, sid.getAccountUniverse());
        assertEquals(EAccountType.Chat, sid.getAccountType());
        assertEquals(SteamID.ChatInstanceFlags.LOBBY.code(), sid.getAccountInstance());
        assertEquals(1234L, sid.getAccountID());
    }

    @Test
    public void RendersOutOfRangeAccountTypeAsLowercaseI() {
        SteamID sid = new SteamID(123, EUniverse.Beta, EAccountType.from(-1));
        assertEquals("[i:2:123]", sid.render());
    }

}
