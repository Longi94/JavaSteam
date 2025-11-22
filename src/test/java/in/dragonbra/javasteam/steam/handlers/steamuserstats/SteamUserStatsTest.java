package in.dragonbra.javasteam.steam.handlers.steamuserstats;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.google.protobuf.ByteString;

import in.dragonbra.javasteam.base.ClientMsgProtobuf;
import in.dragonbra.javasteam.base.IPacketMsg;
import in.dragonbra.javasteam.enums.EMsg;
import in.dragonbra.javasteam.enums.EResult;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverUserstats.CMsgClientGetUserStatsResponse;
import in.dragonbra.javasteam.steam.CMClient;
import in.dragonbra.javasteam.steam.handlers.HandlerTestBase;
import in.dragonbra.javasteam.steam.handlers.steamuserstats.callback.UserStatsCallback;
import in.dragonbra.javasteam.types.KeyValue;

/**
 * Unit tests for SteamUserStats handler, specifically testing achievement
 * parsing functionality.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class SteamUserStatsTest extends HandlerTestBase<SteamUserStats> {

    @Override
    protected SteamUserStats createHandler() {
        return new SteamUserStats();
    }

    /**
     * Helper method to create a mock schema with achievement metadata. This
     * simulates the KeyValue schema structure returned by Steam.
     *
     * The schema needs to be structured so that when read by
     * KeyValue.tryReadAsBinary(), it creates: schemaKeyValues -> stats ->
     * [blocks] This means we need to write binary that starts with a wrapper
     * node containing stats. ! This is a decently clunky was of doing it.
     * Opento suggestions/input on how to improve it.
     */
    private byte[] createMockSchema() throws IOException {
        // Create a wrapper so schemaKeyValues will have "stats" as a child, not be "stats" itself
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

    /**
     * Helper method to create a mock UserStatsResponse packet with achievement
     * data.
     */
    private IPacketMsg createUserStatsResponseMessage(boolean includeSchema) throws IOException {
        ClientMsgProtobuf<CMsgClientGetUserStatsResponse.Builder> msg
                = new ClientMsgProtobuf<>(CMsgClientGetUserStatsResponse.class, EMsg.ClientGetUserStatsResponse);

        CMsgClientGetUserStatsResponse.Builder body = msg.getBody();
        body.setGameId(440L); // Team Fortress 2
        body.setEresult(EResult.OK.code());
        body.setCrcStats(123456);

        // Add schema if requested
        if (includeSchema) {
            body.setSchema(ByteString.copyFrom(createMockSchema()));
        }

        // Add achievement block 21 with 3 achievements
        // Achievements 0 and 2 are unlocked, achievement 1 is locked
        CMsgClientGetUserStatsResponse.Achievement_Blocks.Builder block21
                = CMsgClientGetUserStatsResponse.Achievement_Blocks.newBuilder();
        block21.setAchievementId(21);
        block21.addUnlockTime(1609459200); // Achievement 0 unlocked on Jan 1, 2021
        block21.addUnlockTime(0);           // Achievement 1 locked
        block21.addUnlockTime(1640995200); // Achievement 2 unlocked on Jan 1, 2022
        body.addAchievementBlocks(block21);

        // Add achievement block 22 with 2 achievements
        // Achievement 0 is unlocked, achievement 1 is locked
        CMsgClientGetUserStatsResponse.Achievement_Blocks.Builder block22
                = CMsgClientGetUserStatsResponse.Achievement_Blocks.newBuilder();
        block22.setAchievementId(22);
        block22.addUnlockTime(1672531200); // Achievement 0 unlocked on Jan 1, 2023
        block22.addUnlockTime(0);           // Achievement 1 locked
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

    @Test
    public void testHandleUserStatsResponse() throws IOException {
        IPacketMsg testMsg = createUserStatsResponseMessage(true);

        // Call handler to process the message
        handler.handleMsg(testMsg);

        // Verify the callback was posted
        UserStatsCallback callback = verifyCallback();

        // Verify basic callback data
        assertEquals(EResult.OK, callback.getResult());
        assertEquals(440L, callback.getGameId());
        assertEquals(123456, callback.getCrcStats());

        // Verify achievement blocks
        List<AchievementBlocks> blocks = callback.getAchievementBlocks();
        assertNotNull(blocks);
        assertEquals(2, blocks.size());

        // Verify block 21
        AchievementBlocks block21 = blocks.get(0);
        assertEquals(21, block21.getAchievementId());
        assertEquals(3, block21.getUnlockTime().size());

        // Verify block 22
        AchievementBlocks block22 = blocks.get(1);
        assertEquals(22, block22.getAchievementId());
        assertEquals(2, block22.getUnlockTime().size());
    }

    @Test
    public void testGetExpandedAchievements() throws IOException {
        IPacketMsg testMsg = createUserStatsResponseMessage(true);

        // Call handler to process the message
        handler.handleMsg(testMsg);

        // Verify the callback was posted
        UserStatsCallback callback = verifyCallback();

        // Get expanded achievements
        List<AchievementBlocks> expandedAchievements = callback.getExpandedAchievements();

        assertNotNull(expandedAchievements);
        assertEquals(5, expandedAchievements.size()); // 3 from block 21 + 2 from block 22

        // Verify first achievement (block 21, bit 0)
        AchievementBlocks ach0 = expandedAchievements.get(0);
        assertEquals("ACH_FIRST_BLOOD", ach0.getName());
        assertEquals("First Blood", ach0.getDisplayName());
        assertEquals("Kill your first enemy", ach0.getDescription());
        assertNotNull(ach0.getIcon());
        assertTrue(ach0.getIcon().contains("achievement_0.jpg"));
        assertNotNull(ach0.getIconGray());
        assertFalse(ach0.getHidden());
        assertTrue(ach0.isUnlocked());
        assertEquals(1609459200, ach0.getUnlockTimestamp());
        assertEquals("2021-01-01 00:00:00", ach0.getFormattedUnlockTime());

        // Verify second achievement (block 21, bit 1) - locked
        AchievementBlocks ach1 = expandedAchievements.get(1);
        assertEquals("ACH_VETERAN", ach1.getName());
        assertEquals("Veteran", ach1.getDisplayName());
        assertEquals("Reach level 10", ach1.getDescription());
        assertFalse(ach1.getHidden());
        assertFalse(ach1.isUnlocked());
        assertEquals(0, ach1.getUnlockTimestamp());
        assertNull(ach1.getFormattedUnlockTime());

        // Verify third achievement (block 21, bit 2) - hidden and unlocked
        AchievementBlocks ach2 = expandedAchievements.get(2);
        assertEquals("ACH_SECRET", ach2.getName());
        assertEquals("Secret Achievement", ach2.getDisplayName());
        assertTrue(ach2.getHidden());
        assertTrue(ach2.isUnlocked());
        assertEquals(1640995200, ach2.getUnlockTimestamp());
        assertEquals("2022-01-01 00:00:00", ach2.getFormattedUnlockTime());

        // Verify first DLC achievement (block 22, bit 0) - unlocked
        AchievementBlocks ach3 = expandedAchievements.get(3);
        assertEquals("ACH_DLC_MASTER", ach3.getName());
        assertEquals("DLC Master", ach3.getDisplayName());
        assertEquals("Complete all DLC missions", ach3.getDescription());
        assertFalse(ach3.getHidden());
        assertTrue(ach3.isUnlocked());
        assertEquals(1672531200, ach3.getUnlockTimestamp());
        assertEquals("2023-01-01 00:00:00", ach3.getFormattedUnlockTime());

        // Verify second DLC achievement (block 22, bit 1) - locked
        AchievementBlocks ach4 = expandedAchievements.get(4);
        assertEquals("ACH_DLC_EXPERT", ach4.getName());
        assertEquals("DLC Expert", ach4.getDisplayName());
        assertFalse(ach4.getHidden());
        assertFalse(ach4.isUnlocked());
        assertNull(ach4.getFormattedUnlockTime());
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
    public void testSchemaParsingHandlesCorruptData() {
        // Create a packet with invalid schema data
        ClientMsgProtobuf<CMsgClientGetUserStatsResponse.Builder> msg
                = new ClientMsgProtobuf<>(CMsgClientGetUserStatsResponse.class, EMsg.ClientGetUserStatsResponse);

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
        assertNotNull(callback);
        assertEquals(EResult.OK, callback.getResult());

        // getExpandedAchievements should still work and fall back to empty list
        List<AchievementBlocks> expanded = callback.getExpandedAchievements();
        assertNotNull(expanded);
        assertTrue(expanded.isEmpty());
    }

    @Test
    public void testAchievementBlockWithManyUnlocks() throws IOException {
        // Test with a block that has many achievements unlocked
        ClientMsgProtobuf<CMsgClientGetUserStatsResponse.Builder> msg
                = new ClientMsgProtobuf<>(CMsgClientGetUserStatsResponse.class, EMsg.ClientGetUserStatsResponse);

        CMsgClientGetUserStatsResponse.Builder body = msg.getBody();
        body.setGameId(440L);
        body.setEresult(EResult.OK.code());
        body.setCrcStats(123456);
        body.setSchema(ByteString.copyFrom(createMockSchema()));

        // Create a block with 32 achievements (maximum per block)
        CMsgClientGetUserStatsResponse.Achievement_Blocks.Builder block
                = CMsgClientGetUserStatsResponse.Achievement_Blocks.newBuilder();
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
        assertEquals(3, expanded.size());

        // Verify unlocked achievements have correct timestamps
        assertTrue(expanded.get(0).isUnlocked());
        assertTrue(expanded.get(1).isUnlocked());
        assertTrue(expanded.get(2).isUnlocked());
    }
}
