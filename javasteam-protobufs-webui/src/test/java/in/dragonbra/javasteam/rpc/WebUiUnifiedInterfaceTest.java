package in.dragonbra.javasteam.rpc;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;

/**
 * This test class makes sure there are a certain number of Unified classes/interfaces
 * generated from the webui .proto files.
 * <p>
 * Any updates to .proto files, either adding or removing services would need to reflect these tests.
 */
public class WebUiUnifiedInterfaceTest {

    private static final String SERVICE_PATH =
            "build/generated/source/javasteam/main/java/in/dragonbra/javasteam/rpc/service/webui";

    /**
     * Any changes to the number of interfaces would need to reflect here. Otherwise, the test should fail.
     */
    private static final String[] KNOWN_SERVICE_TYPES = {
            "ClientComm.kt",
            "CloudConfigStore.kt",
            "CloudConfigStoreClient.kt",
            "FriendsList.kt",
            "FriendsListClient.kt",
            "RemoteClient.kt",
            "RemoteClientSteamClient.kt",
            "Store.kt",
            "StoreClient.kt",
    };

    @Test
    public void testServiceCount() {
        File interfaceDir = new File(SERVICE_PATH);

        Assertions.assertTrue(
                interfaceDir.exists() && interfaceDir.isDirectory(),
                interfaceDir.getName() + " should exist to test"
        );

        File[] files = interfaceDir.listFiles(File::isFile);

        Assertions.assertNotNull(files, "Couldn't count files");

        Assertions.assertEquals(
                KNOWN_SERVICE_TYPES.length,
                files.length,
                "Interface count doesn't match known file types! Did something change in the .proto files?"
        );
    }

    @Test
    public void testKnownServices() {
        for (String filename : KNOWN_SERVICE_TYPES) {
            File file = new File(SERVICE_PATH, filename);
            Assertions.assertTrue(file.exists() && file.isFile(), "File " + filename + " should exist");
        }
    }
}
