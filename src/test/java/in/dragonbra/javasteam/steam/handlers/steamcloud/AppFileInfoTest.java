package in.dragonbra.javasteam.steam.handlers.steamcloud;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesCloudSteamclient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AppFileInfoTest {

    @Test
    public void hasPathPrefixIndexIsFalseWhenFieldIsMissing() {
        var response = SteammessagesCloudSteamclient.CCloud_AppFileInfo.newBuilder()
                .setFileName("save.dat")
                .build();

        var info = new AppFileInfo(response);

        Assertions.assertFalse(info.getHasPathPrefixIndex());
    }

    @Test
    public void hasPathPrefixIndexIsTrueWhenIndexIsZero() {
        var response = SteammessagesCloudSteamclient.CCloud_AppFileInfo.newBuilder()
                .setFileName("save.dat")
                .setPathPrefixIndex(0)
                .build();

        var info = new AppFileInfo(response);

        Assertions.assertTrue(info.getHasPathPrefixIndex());
        Assertions.assertEquals(0, info.getPathPrefixIndex());
    }
}
