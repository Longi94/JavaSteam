package in.dragonbra.javasteam.types;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


/**
 * @author lngtr
 * @since 2019-01-14
 */
public class GameIDTest {

    @Test
    public void modCrcCorrect() {
        var gameId = new GameID(420, "Research and Development");

        Assertions.assertTrue(gameId.isMod());
        Assertions.assertEquals(420, gameId.getAppID());
        Assertions.assertEquals(new GameID(0x8db24e81010001a4L), gameId);

        var gameId2 = new GameID(215, "hidden");

        Assertions.assertTrue(gameId2.isMod());
        Assertions.assertEquals(215, gameId2.getAppID());
        Assertions.assertEquals(new GameID(0x885de9bd010000d7L), gameId2);
    }

    @Test
    public void shortcutCrcCorrect() {
        var gameId = new GameID("\"C:\\Program Files (x86)\\Git\\mingw64\\bin\\wintoast.exe\"", "Git for Windows");

        Assertions.assertTrue(gameId.isShortcut());
        Assertions.assertEquals(new GameID(0xb102133802000000L), gameId);
    }

    @Test
    public void gameIDsKHashcodeEquality() {
        var gameId1 = new GameID(570);
        var gameId2 = new GameID(570);
        var gameId3 = new GameID(570, "test");
        var gameid4 = new GameID(440);

        Assertions.assertEquals(gameId1.hashCode(), gameId2.hashCode());
        Assertions.assertNotEquals(gameId1.hashCode(), gameId3.hashCode());
        Assertions.assertNotEquals(gameId2.hashCode(), gameId3.hashCode());
        Assertions.assertNotEquals(gameId1.hashCode(), gameid4.hashCode());
    }
}
