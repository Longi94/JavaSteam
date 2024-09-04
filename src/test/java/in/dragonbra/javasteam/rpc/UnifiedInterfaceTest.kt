package `in`.dragonbra.javasteam.rpc

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.io.File

/**
 * This test class make sures there are a certain number of Unified classes/interfaces.
 *
 * Any updates to .proto files, either adding or removing services would need to reflect these tests.
 */
class UnifiedInterfaceTest {

    @Test
    fun testInterfaceCount() {
        val interfaceDir = File(INTERFACE_PATH)

        Assertions.assertTrue(
            interfaceDir.exists() && interfaceDir.isDirectory,
            "${interfaceDir.name} should exist to test"
        )

        val fileCount = interfaceDir.listFiles()

        Assertions.assertNotNull(fileCount, "Couldn't count files")

        Assertions.assertTrue(
            knownServiceTypes.count() == fileCount!!.size,
            "Interface count doesn't match known file types! Did something change in the .proto files?"
        )
    }

    @Test
    fun testKnownInterfaces() {
        for (filename in knownServiceTypes) {
            val file = File(INTERFACE_PATH, filename)
            Assertions.assertTrue(file.exists() && file.isFile, "File I$filename should exist")
        }
    }

    private companion object {
        const val DIR_PATH = "build/generated/source/javasteam/main/java/in/dragonbra/javasteam/rpc/"
        const val INTERFACE_PATH = "$DIR_PATH/interfaces"

        /**
         * Any changes to then number of interfaces would need to reflect here. Otherwise, the test should fail.
         */
        val knownServiceTypes = arrayOf(
            "IAccountLinking.kt",
            "IAuthentication.kt",
            "IAuthenticationSupport.kt",
            "IChat.kt",
            "IChatRoom.kt",
            "IChatRoomClient.kt",
            "IChatUsability.kt",
            "IChatUsabilityClient.kt",
            "IClanChatRooms.kt",
            "ICloudGaming.kt",
            "IContentServerDirectory.kt",
            "IEmbeddedClient.kt",
            "IFriendMessages.kt",
            "IFriendMessagesClient.kt",
            "IInventory.kt",
            "IInventoryClient.kt",
            "IParental.kt",
            "IParentalClient.kt",
            "IPlayer.kt",
            "IPlayerClient.kt",
            "IRemoteClient.kt",
            "IRemoteClientSteamClient.kt",
            "ITwoFactor.kt",
            "IUserAccount.kt",
        )
    }
}
