package in.dragonbra.javasteam.types;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author lngtr
 * @since 2019-01-14
 */
public class GameIDTest {

    @Test
    public void modCrcCorrect() {
        GameID gameId = new GameID(420, "Research and Development");

        assertTrue(gameId.isMod());
        assertEquals(420, gameId.getAppID());
        assertEquals(new GameID(0x8db24e81010001a4L), gameId);

        GameID gameId2 = new GameID(215, "hidden");

        assertTrue(gameId2.isMod());
        assertEquals(215, gameId2.getAppID());
        assertEquals(new GameID(0x885de9bd010000d7L), gameId2);
    }

    @Test
    public void shortcutCrcCorrect() {
        GameID gameId = new GameID("\"C:\\Program Files (x86)\\Git\\mingw64\\bin\\wintoast.exe\"", "Git for Windows");

        assertTrue(gameId.isShortcut());
        assertEquals(new GameID(0xb102133802000000L), gameId);
    }
}