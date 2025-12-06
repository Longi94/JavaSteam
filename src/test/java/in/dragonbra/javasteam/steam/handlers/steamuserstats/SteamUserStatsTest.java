package in.dragonbra.javasteam.steam.handlers.steamuserstats;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.google.protobuf.ByteString;

import java.text.SimpleDateFormat;

import in.dragonbra.javasteam.base.ClientMsgProtobuf;
import in.dragonbra.javasteam.base.IPacketMsg;
import in.dragonbra.javasteam.enums.EMsg;
import in.dragonbra.javasteam.enums.EResult;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverUserstats.CMsgClientGetUserStatsResponse;
import in.dragonbra.javasteam.steam.CMClient;
import in.dragonbra.javasteam.steam.handlers.HandlerTestBase;
import in.dragonbra.javasteam.steam.handlers.steamuserstats.callback.UserStatsCallback;
import in.dragonbra.javasteam.types.KeyValue;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for SteamUserStats handler, specifically testing achievement
 * parsing functionality.
 * "ClientGetUserStatsResponse" returns a copy of Dredge
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class SteamUserStatsTest extends HandlerTestBase<SteamUserStats> {

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    protected SteamUserStats createHandler() {
        return new SteamUserStats();
    }

    @Test
    public void testHandleUserStatsResponse() {
        IPacketMsg testMsg = getPacket(EMsg.ClientGetUserStatsResponse, true);

        // Call handler to process the message
        handler.handleMsg(testMsg);

        // Verify the callback was posted
        UserStatsCallback callback = verifyCallback();

        // Verify basic callback data
        Assertions.assertEquals(EResult.OK, callback.getResult());
        Assertions.assertEquals(1562430, callback.getGameId());

        // CRC values can be long, but protobuf java converts uint32 to integers.
        Assertions.assertEquals(3020832857L, Integer.toUnsignedLong(callback.getCrcStats()));

        // Verify stats
        List<Stats> stats = callback.getStats();
        Assertions.assertNotNull(stats);
        Assertions.assertFalse(stats.isEmpty());
        Assertions.assertEquals(2, stats.size());

        // Verify achievement blocks
        List<AchievementBlocks> blocks = callback.getAchievementBlocks();
        Assertions.assertNotNull(blocks);
        Assertions.assertFalse(blocks.isEmpty());
        Assertions.assertEquals(2, blocks.size());

        // Verify schema size
        var schema = callback.getSchema();
        Assertions.assertNotNull(schema);
        Assertions.assertFalse(schema.isEmpty());
        Assertions.assertEquals(72959, schema.size());

        // Verify version and game name.
        Assertions.assertEquals("Dredge", callback.getSchemaKeyValues().get("gamename").asString());
        Assertions.assertEquals(24, callback.getSchemaKeyValues().get("version").asInteger());
    }

    @Test
    public void testUserStatsResponseStats() {
        IPacketMsg testMsg = getPacket(EMsg.ClientGetUserStatsResponse, true);

        // Call handler to process the message
        handler.handleMsg(testMsg);

        // Verify the callback was posted
        UserStatsCallback callback = verifyCallback();

        // Grab a few random stats

        Stats statFirst = callback.getStats().getFirst();
        Assertions.assertEquals(17, statFirst.getStatId());
        Assertions.assertEquals(2737815391L, Integer.toUnsignedLong(statFirst.getStatValue()));

        Stats statLast = callback.getStats().getLast();
        Assertions.assertEquals(19, statLast.getStatId());
        Assertions.assertEquals(487, statLast.getStatValue());
    }

    @Test
    public void testUserStatsResponseBlocks() {
        IPacketMsg testMsg = getPacket(EMsg.ClientGetUserStatsResponse, true);

        // Call handler to process the message
        handler.handleMsg(testMsg);

        // Verify the callback was posted
        UserStatsCallback callback = verifyCallback();

        // Grab a few random achievement blocks

        AchievementBlocks blockFirst = callback.getAchievementBlocks().getFirst();
        Assertions.assertEquals(17, blockFirst.getAchievementId());
        Assertions.assertEquals(1733977234, blockFirst.getUnlockTime().getFirst());

        AchievementBlocks blockSecond = callback.getAchievementBlocks().getLast();
        Assertions.assertEquals(19, blockSecond.getAchievementId());
        Assertions.assertEquals(1733721477, blockSecond.getUnlockTime().get(8));
    }

    @Test
    public void testSchemaParsingHandlesCorruptData() {
        // Create a packet with invalid schema data
        ClientMsgProtobuf<CMsgClientGetUserStatsResponse.Builder> msg =
                new ClientMsgProtobuf<>(CMsgClientGetUserStatsResponse.class, EMsg.ClientGetUserStatsResponse);

        CMsgClientGetUserStatsResponse.Builder body = msg.getBody();
        body.setGameId(440L);
        body.setEresult(EResult.OK.code());
        body.setCrcStats(123456);
        body.setSchema(ByteString.copyFrom(new byte[]{0x01, 0x02, 0x03})); // Invalid schema

        // Serialize and convert to IPacketMsg
        IPacketMsg packetMsg = CMClient.getPacketMsg(msg.serialize());

        // Call handler to process the message
        handler.handleMsg(packetMsg);

        // Verify the callback was posted
        UserStatsCallback callback = verifyCallback();

        // Should not throw exception, just have empty schema
        Assertions.assertNotNull(callback);
        Assertions.assertEquals(EResult.OK, callback.getResult());

        // getExpandedAchievements should still work and fall back to empty list
        List<AchievementBlocks> expanded = callback.getExpandedAchievements();
        Assertions.assertNotNull(expanded);
        Assertions.assertTrue(expanded.isEmpty());
    }

    @Test
    public void testGetExpandedAchievements() {
        IPacketMsg testMsg = getPacket(EMsg.ClientGetUserStatsResponse, true);

        // Call handler to process the message
        handler.handleMsg(testMsg);

        // Verify the callback was posted
        UserStatsCallback callback = verifyCallback();

        // Get expanded achievements
        List<AchievementBlocks> expandedAchievements = callback.getExpandedAchievements();

        Assertions.assertNotNull(expandedAchievements);
        Assertions.assertFalse(expandedAchievements.isEmpty());
        Assertions.assertEquals(60, expandedAchievements.size());

        // Verify an unlocked achievement
        AchievementBlocks ach0 = expandedAchievements.get(0);
        Assertions.assertEquals("CATCH_FISH_ROD_1", ach0.getName());
        Assertions.assertEquals("Lifted From the Deep", ach0.getDisplayName());
        Assertions.assertEquals("Catch 250 fish using rods.", ach0.getDescription());
        Assertions.assertNotNull(ach0.getIcon());
        Assertions.assertTrue(ach0.getIcon().contains("12ee49fe9ad45969bb4d106c099517279a940521.jpg"));
        Assertions.assertNotNull(ach0.getIconGray());
        Assertions.assertFalse(ach0.getHidden());
        Assertions.assertTrue(ach0.isUnlocked());
        Assertions.assertEquals(1733977234, ach0.getUnlockTimestamp());
        Assertions.assertEquals(dateFormat.format(new Date(1733977234 * 1000L)), ach0.getFormattedUnlockTime());

        // Verify a locked achievement
        AchievementBlocks ach1 = expandedAchievements.get(5);
        Assertions.assertEquals("DISCARD_FISH", ach1.getName());
        Assertions.assertEquals("Unwanted", ach1.getDisplayName());
        Assertions.assertEquals("Discard 25 fish.", ach1.getDescription());
        Assertions.assertFalse(ach1.getHidden());
        Assertions.assertFalse(ach1.isUnlocked());
        Assertions.assertEquals(0, ach1.getUnlockTimestamp());
        Assertions.assertNull(ach1.getFormattedUnlockTime());

        // Verify an unlocked DLC achievement
        // TODO

        // Verify a locked DLC achievement
        AchievementBlocks ach3 = expandedAchievements.get(40);
        Assertions.assertEquals("DLC_3_1", ach3.getName());
        Assertions.assertEquals("Polar Angler", ach3.getDisplayName());
        Assertions.assertEquals("Catch all known species of fish in The Pale Reach.", ach3.getDescription());
        Assertions.assertFalse(ach3.getHidden());
        Assertions.assertFalse(ach3.isUnlocked());
        Assertions.assertEquals(0, ach3.getUnlockTimestamp());
        Assertions.assertNull(ach1.getFormattedUnlockTime());
    }

    @Test
    public void testGetExpandedAchievementsWithoutSchema() throws IOException {
        // Test with no schema - should fall back to original blocks
        IPacketMsg testMsg = createUserStatsResponseMessage(false);

        // Call handler to process the message
        handler.handleMsg(testMsg);

        // Verify the callback was posted
        UserStatsCallback callback = verifyCallback();

        List<AchievementBlocks> expandedAchievements = callback.getExpandedAchievements();

        // Without schema, should return original blocks
        assertNotNull(expandedAchievements);
        assertEquals(2, expandedAchievements.size());

        // Verify blocks have no enriched metadata
        AchievementBlocks block21 = expandedAchievements.get(0);
        assertEquals(21, block21.getAchievementId());
        assertNull(block21.getName());
        assertNull(block21.getDisplayName());
        assertNull(block21.getDescription());

        AchievementBlocks block22 = expandedAchievements.get(1);
        assertEquals(22, block22.getAchievementId());
        assertNull(block22.getName());
        assertNull(block22.getDisplayName());
    }


    @Test
    public void testAchievementBlockWithManyUnlocks() throws IOException {
        // Test with a block that has many achievements unlocked
        ClientMsgProtobuf<CMsgClientGetUserStatsResponse.Builder> msg = new ClientMsgProtobuf<>(
                CMsgClientGetUserStatsResponse.class, EMsg.ClientGetUserStatsResponse);

        CMsgClientGetUserStatsResponse.Builder body = msg.getBody();
        body.setGameId(440L);
        body.setEresult(EResult.OK.code());
        body.setCrcStats(123456);
        body.setSchema(ByteString.copyFrom(createMockSchema()));

        // Create a block with 32 achievements (maximum per block)
        CMsgClientGetUserStatsResponse.Achievement_Blocks.Builder block = CMsgClientGetUserStatsResponse.Achievement_Blocks
                .newBuilder();
        block.setAchievementId(21);

        // Add 32 unlock times (some locked, some unlocked)
        for (int i = 0; i < 32; i++) {
            if (i < 3) {
                // First 3 achievements have unlock times (matching our schema)
                block.addUnlockTime(1609459200 + i * 86400);
            } else {
                // Rest are locked
                block.addUnlockTime(0);
            }
        }
        body.addAchievementBlocks(block);

        // Serialize and convert to IPacketMsg
        IPacketMsg packetMsg = CMClient.getPacketMsg(msg.serialize());

        // Call handler to process the message
        handler.handleMsg(packetMsg);

        // Verify the callback was posted
        UserStatsCallback callback = verifyCallback();
        List<AchievementBlocks> expanded = callback.getExpandedAchievements();

        // Should only expand the 3 achievements that have schema entries
        Assertions.assertEquals(3, expanded.size());

        // Verify unlocked achievements have correct timestamps
        Assertions.assertTrue(expanded.get(0).isUnlocked());
        Assertions.assertTrue(expanded.get(1).isUnlocked());
        Assertions.assertTrue(expanded.get(2).isUnlocked());
    }

    /**
     * Helper method to create a mock UserStatsResponse packet with achievement
     * data.
     */
    private IPacketMsg createUserStatsResponseMessage(boolean includeSchema) throws IOException {
        ClientMsgProtobuf<CMsgClientGetUserStatsResponse.Builder> msg = new ClientMsgProtobuf<>(
                CMsgClientGetUserStatsResponse.class, EMsg.ClientGetUserStatsResponse);

        CMsgClientGetUserStatsResponse.Builder body = msg.getBody();
        body.setGameId(440L); // Team Fortress 2
        body.setEresult(EResult.OK.code());
        body.setCrcStats(123456);

        // Add schema if requested
        if (includeSchema) {
            body.setSchema(ByteString.copyFrom(createMockSchema()));
        }

        // Add achievement block 21 with 3 achievements, 0 and 2 are unlocked, achievement 1 is locked
        CMsgClientGetUserStatsResponse.Achievement_Blocks.Builder block21 = CMsgClientGetUserStatsResponse.Achievement_Blocks
                .newBuilder();
        block21.setAchievementId(21);
        block21.addUnlockTime(1609459200); // Achievement 0 unlocked on Jan 1, 2021
        block21.addUnlockTime(0); // Achievement 1 locked
        block21.addUnlockTime(1640995200); // Achievement 2 unlocked on Jan 1, 2022
        body.addAchievementBlocks(block21);

        // Add achievement block 22 with 2 achievements
        // Achievement 0 is unlocked, achievement 1 is locked
        CMsgClientGetUserStatsResponse.Achievement_Blocks.Builder block22 = CMsgClientGetUserStatsResponse.Achievement_Blocks
                .newBuilder();
        block22.setAchievementId(22);
        block22.addUnlockTime(1672531200); // Achievement 0 unlocked on Jan 1, 2023
        block22.addUnlockTime(0); // Achievement 1 locked
        body.addAchievementBlocks(block22);

        // Add some stats for completeness
        body.addStats(CMsgClientGetUserStatsResponse.Stats.newBuilder()
                .setStatId(1)
                .setStatValue(100));
        body.addStats(CMsgClientGetUserStatsResponse.Stats.newBuilder()
                .setStatId(2)
                .setStatValue(50));

        // Serialize and convert to IPacketMsg
        return CMClient.getPacketMsg(msg.serialize());
    }

    /**
     * Helper method to create a mock schema with achievement metadata. This
     * simulates the KeyValue schema structure returned by Steam.
     * <p>
     * The schema needs to be structured so that when read by
     * KeyValue.tryReadAsBinary(), it creates: schemaKeyValues -> stats ->
     * [blocks] This means we need to write binary that starts with a wrapper
     * node containing stats. ! This is a decently clunky was of doing it.
     * Opento suggestions/input on how to improve it.
     */
    private byte[] createMockSchema() throws IOException {
        // Create a wrapper so schemaKeyValues will have "stats" as a child, not be
        // "stats" itself
        KeyValue wrapper = new KeyValue("UserGameStatsSchema"); // Mimicking Steam's actual format
        KeyValue stats = new KeyValue("stats");

        // Block 21 - Base game achievements (3 achievements)
        KeyValue block21 = new KeyValue("21");
        block21.getChildren().add(new KeyValue("type", "4")); // Type 4 = achievement block

        KeyValue bits21 = new KeyValue("bits");

        // Achievement 0 in block 21
        KeyValue bit0 = new KeyValue("0");
        bit0.getChildren().add(new KeyValue("bit", "0")); // Bit index as a property
        bit0.getChildren().add(new KeyValue("name", "ACH_FIRST_BLOOD"));
        KeyValue display0 = new KeyValue("display");
        KeyValue name0 = new KeyValue("name");
        name0.getChildren().add(new KeyValue("english", "First Blood"));
        display0.getChildren().add(name0);
        KeyValue desc0 = new KeyValue("desc");
        desc0.getChildren().add(new KeyValue("english", "Kill your first enemy"));
        display0.getChildren().add(desc0);
        display0.getChildren().add(new KeyValue("icon", "achievement_0.jpg"));
        display0.getChildren().add(new KeyValue("icon_gray", "achievement_0_gray.jpg"));
        display0.getChildren().add(new KeyValue("hidden", "0"));
        bit0.getChildren().add(display0);
        bits21.getChildren().add(bit0);

        // Achievement 1 in block 21
        KeyValue bit1 = new KeyValue("1");
        bit1.getChildren().add(new KeyValue("bit", "1")); // Bit index as a property
        bit1.getChildren().add(new KeyValue("name", "ACH_VETERAN"));
        KeyValue display1 = new KeyValue("display");
        KeyValue name1 = new KeyValue("name");
        name1.getChildren().add(new KeyValue("english", "Veteran"));
        display1.getChildren().add(name1);
        KeyValue desc1 = new KeyValue("desc");
        desc1.getChildren().add(new KeyValue("english", "Reach level 10"));
        display1.getChildren().add(desc1);
        display1.getChildren().add(new KeyValue("icon", "achievement_1.jpg"));
        display1.getChildren().add(new KeyValue("icon_gray", "achievement_1_gray.jpg"));
        display1.getChildren().add(new KeyValue("hidden", "0"));
        bit1.getChildren().add(display1);
        bits21.getChildren().add(bit1);

        // Achievement 2 in block 21 (hidden achievement)
        KeyValue bit2 = new KeyValue("2");
        bit2.getChildren().add(new KeyValue("bit", "2")); // Bit index as a property
        bit2.getChildren().add(new KeyValue("name", "ACH_SECRET"));
        KeyValue display2 = new KeyValue("display");
        KeyValue name2 = new KeyValue("name");
        name2.getChildren().add(new KeyValue("english", "Secret Achievement"));
        display2.getChildren().add(name2);
        KeyValue desc2 = new KeyValue("desc");
        desc2.getChildren().add(new KeyValue("english", "Find the secret"));
        display2.getChildren().add(desc2);
        display2.getChildren().add(new KeyValue("icon", "achievement_2.jpg"));
        display2.getChildren().add(new KeyValue("icon_gray", "achievement_2_gray.jpg"));
        display2.getChildren().add(new KeyValue("hidden", "1")); // Hidden achievement
        bit2.getChildren().add(display2);
        bits21.getChildren().add(bit2);

        block21.getChildren().add(bits21);
        stats.getChildren().add(block21);

        // Block 22 - DLC achievements (2 achievements)
        KeyValue block22 = new KeyValue("22");
        block22.getChildren().add(new KeyValue("type", "4"));

        KeyValue bits22 = new KeyValue("bits");

        // Achievement 0 in block 22 (DLC)
        KeyValue bit22_0 = new KeyValue("0");
        bit22_0.getChildren().add(new KeyValue("bit", "0")); // Bit index as a property
        bit22_0.getChildren().add(new KeyValue("name", "ACH_DLC_MASTER"));
        KeyValue display22_0 = new KeyValue("display");
        KeyValue name22_0 = new KeyValue("name");
        name22_0.getChildren().add(new KeyValue("english", "DLC Master"));
        display22_0.getChildren().add(name22_0);
        KeyValue desc22_0 = new KeyValue("desc");
        desc22_0.getChildren().add(new KeyValue("english", "Complete all DLC missions"));
        display22_0.getChildren().add(desc22_0);
        display22_0.getChildren().add(new KeyValue("icon", "achievement_dlc_0.jpg"));
        display22_0.getChildren().add(new KeyValue("icon_gray", "achievement_dlc_0_gray.jpg"));
        display22_0.getChildren().add(new KeyValue("hidden", "0"));
        bit22_0.getChildren().add(display22_0);
        bits22.getChildren().add(bit22_0);

        // Achievement 1 in block 22 (DLC)
        KeyValue bit22_1 = new KeyValue("1");
        bit22_1.getChildren().add(new KeyValue("bit", "1")); // Bit index as a property
        bit22_1.getChildren().add(new KeyValue("name", "ACH_DLC_EXPERT"));
        KeyValue display22_1 = new KeyValue("display");
        KeyValue name22_1 = new KeyValue("name");
        name22_1.getChildren().add(new KeyValue("english", "DLC Expert"));
        display22_1.getChildren().add(name22_1);
        KeyValue desc22_1 = new KeyValue("desc");
        desc22_1.getChildren().add(new KeyValue("english", "Master the DLC content"));
        display22_1.getChildren().add(desc22_1);
        display22_1.getChildren().add(new KeyValue("icon", "achievement_dlc_1.jpg"));
        display22_1.getChildren().add(new KeyValue("icon_gray", "achievement_dlc_1_gray.jpg"));
        display22_1.getChildren().add(new KeyValue("hidden", "0"));
        bit22_1.getChildren().add(display22_1);
        bits22.getChildren().add(bit22_1);

        block22.getChildren().add(bits22);
        stats.getChildren().add(block22);

        // Add stats to wrapper
        wrapper.getChildren().add(stats);

        // Serialize to binary format
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        wrapper.saveToStream(baos, true); // Binary format
        return baos.toByteArray();
    }

    private static void printKeyValue(KeyValue keyvalue, int depth) {
        if (keyvalue.getChildren().isEmpty())
            System.out.println(" ".repeat(depth * 4) + " " + keyvalue.getName() + ": " + keyvalue.getValue());
        else {
            System.out.println(" ".repeat(depth * 4) + " " + keyvalue.getName() + ":");
            for (KeyValue child : keyvalue.getChildren())
                printKeyValue(child, depth + 1);
        }
    }
}
