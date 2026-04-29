package `in`.dragonbra.javasteam.steam.handlers.steamcloud

import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesCloudSteamclient.CCloud_AppFileInfo
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class AppFileInfoTest {

    @Test
    fun hasPathPrefixIndexIsFalseWhenFieldIsMissing() {
        val response = CCloud_AppFileInfo.newBuilder()
            .setFileName("save.dat")
            .build()

        val info = AppFileInfo(response)

        assertFalse(info.hasPathPrefixIndex)
    }

    @Test
    fun hasPathPrefixIndexIsTrueWhenIndexIsZero() {
        val response = CCloud_AppFileInfo.newBuilder()
            .setFileName("save.dat")
            .setPathPrefixIndex(0)
            .build()

        val info = AppFileInfo(response)

        assertTrue(info.hasPathPrefixIndex)
        assertEquals(0, info.pathPrefixIndex)
    }
}
