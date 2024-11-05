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
    fun testServiceCount() {
        val interfaceDir = File(SERVICE_PATH)

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
            val file = File(INTERFACE_PATH, "I$filename")
            Assertions.assertTrue(file.exists() && file.isFile, "File I$filename should exist")
        }
    }

    @Test
    fun testKnownServices() {
        for (filename in knownServiceTypes) {
            val file = File(SERVICE_PATH, filename)
            Assertions.assertTrue(file.exists() && file.isFile, "File $filename should exist")
        }
    }

    private companion object {
        const val DIR_PATH = "build/generated/source/javasteam/main/java/in/dragonbra/javasteam/rpc/"
        const val INTERFACE_PATH = "$DIR_PATH/interfaces"
        const val SERVICE_PATH = "$DIR_PATH/service"

        /**
         * Any changes to then number of interfaces would need to reflect here. Otherwise, the test should fail.
         */
        val knownServiceTypes = arrayOf(
            "AccountLinking.kt",
            "Authentication.kt",
            "AuthenticationSupport.kt",
            "Chat.kt",
            "ChatRoom.kt",
            "ChatRoomClient.kt",
            "ChatUsability.kt",
            "ChatUsabilityClient.kt",
            "ClanChatRooms.kt",
            "CloudGaming.kt",
            "ContentServerDirectory.kt",
            "EmbeddedClient.kt",
            "FriendMessages.kt",
            "FriendMessagesClient.kt",
            "GameNotifications.kt",
            "GameNotificationsClient.kt",
            "Inventory.kt",
            "InventoryClient.kt",
            "Parental.kt",
            "ParentalClient.kt",
            "Player.kt",
            "PlayerClient.kt",
            "RemoteClient.kt",
            "RemoteClientSteamClient.kt",
            "TwoFactor.kt",
            "UserAccount.kt",
        )
    }
}
