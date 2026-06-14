package in.dragonbra.javasteam.steam.steamclient;

import in.dragonbra.javasteam.base.IPacketMsg;
import in.dragonbra.javasteam.steam.handlers.ClientMsgHandler;
import in.dragonbra.javasteam.steam.handlers.steamapps.SteamApps;
import in.dragonbra.javasteam.steam.handlers.steamauthticket.SteamAuthTicket;
import in.dragonbra.javasteam.steam.handlers.steamcloud.SteamCloud;
import in.dragonbra.javasteam.steam.handlers.steamcontent.SteamContent;
import in.dragonbra.javasteam.steam.handlers.steamfriends.SteamFriends;
import in.dragonbra.javasteam.steam.handlers.steamgamecoordinator.SteamGameCoordinator;
import in.dragonbra.javasteam.steam.handlers.steamgameserver.SteamGameServer;
import in.dragonbra.javasteam.steam.handlers.steammasterserver.SteamMasterServer;
import in.dragonbra.javasteam.steam.handlers.steammatchmaking.SteamMatchmaking;
import in.dragonbra.javasteam.steam.handlers.steamnetworking.SteamNetworking;
import in.dragonbra.javasteam.steam.handlers.steamnotifications.SteamNotifications;
import in.dragonbra.javasteam.steam.handlers.steamscreenshots.SteamScreenshots;
import in.dragonbra.javasteam.steam.handlers.steamunifiedmessages.SteamUnifiedMessages;
import in.dragonbra.javasteam.steam.handlers.steamuser.SteamUser;
import in.dragonbra.javasteam.steam.handlers.steamuserstats.SteamUserStats;
import in.dragonbra.javasteam.steam.handlers.steamworkshop.SteamWorkshop;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

class SteamClientTest {

    private SteamClient client;

    @BeforeEach
    public void setUp() {
        client = new SteamClient();
    }

    /**
     * Check to make sure the allocated handlers size matches the initial handlers account.
     */
    @Test
    public void handlersCountCheck() {
        try {
            // get the private 'handlers' variable
            var handlersField = SteamClient.class.getDeclaredField("handlers");
            handlersField.setAccessible(true);
            var handlers = (HashMap<?, ?>) handlersField.get(client);

            // get the private static 'HANDLERS_COUNT' variable
            var handlersCountField = SteamClient.class.getDeclaredField("HANDLERS_COUNT");
            handlersCountField.setAccessible(true);
            var handlersCount = (Integer) handlersCountField.get(null);

            Assertions.assertEquals(handlersCount, handlers.size(), "Handlers size should match HANDLERS_COUNT");
        } catch (Exception e) {
            Assertions.fail(e);
        }
    }

    @Test
    public void constructorSetsInitialHandlers() {
        Assertions.assertNotNull(client.getHandler(SteamUser.class));
        Assertions.assertNotNull(client.getHandler(SteamFriends.class));
        Assertions.assertNotNull(client.getHandler(SteamApps.class));
        Assertions.assertNotNull(client.getHandler(SteamGameCoordinator.class));
        Assertions.assertNotNull(client.getHandler(SteamGameServer.class));
        Assertions.assertNotNull(client.getHandler(SteamUserStats.class));
        Assertions.assertNotNull(client.getHandler(SteamMasterServer.class));
        Assertions.assertNotNull(client.getHandler(SteamCloud.class));
        Assertions.assertNotNull(client.getHandler(SteamWorkshop.class));
        Assertions.assertNotNull(client.getHandler(SteamUnifiedMessages.class));
        Assertions.assertNotNull(client.getHandler(SteamScreenshots.class));
        Assertions.assertNotNull(client.getHandler(SteamMatchmaking.class));
        Assertions.assertNotNull(client.getHandler(SteamNetworking.class));
        Assertions.assertNotNull(client.getHandler(SteamContent.class));
        Assertions.assertNotNull(client.getHandler(SteamAuthTicket.class));
        Assertions.assertNotNull(client.getHandler(SteamNotifications.class));
    }

    @Test
    public void addHandlerAddsHandler() {
        var handler = new TestMsgHandler();
        Assertions.assertNull(client.getHandler(handler.getClass()));

        client.addHandler(handler);
        Assertions.assertEquals(handler, client.getHandler(handler.getClass()));
    }

    @Test
    public void removeHandlerRemovesHandler() {
        client.addHandler(new TestMsgHandler());
        Assertions.assertNotNull(client.getHandler(TestMsgHandler.class));

        client.removeHandler(TestMsgHandler.class);
        Assertions.assertNull(client.getHandler(TestMsgHandler.class));
    }

    @Test
    public void removeHandlerRemovesHandlerByInstance() {
        var handler = new TestMsgHandler();
        client.addHandler(handler);
        Assertions.assertNotNull(client.getHandler(TestMsgHandler.class));

        client.removeHandler(handler);
        Assertions.assertNull(client.getHandler(TestMsgHandler.class));
    }

    @Test
    public void addHandlerThrowsOnDuplicateHandler() {
        var steamClient = new SteamClient();
        steamClient.addHandler(new TestMsgHandler());

        Assertions.assertThrows(IllegalArgumentException.class, () -> steamClient.addHandler(new TestMsgHandler()));
    }

    @Test
    public void removeHandlerByTypeDoesNothingWhenNotRegistered() {
        var steamClient = new SteamClient();
        Assertions.assertNull(client.getHandler(TestMsgHandler.class));

        steamClient.removeHandler(TestMsgHandler.class);
        Assertions.assertNull(client.getHandler(TestMsgHandler.class));
    }

    @Test
    public void getRequiredHandlerReturnsHandler() {
        var steamClient = new SteamClient();
        var handler = steamClient.getRequiredHandler(SteamUser.class);

        Assertions.assertNotNull(handler);
        Assertions.assertEquals(SteamUser.class, handler.getClass());
    }

    @Test
    public void getRequiredHandlerThrowsWhenNotRegistered() {
        var steamClient = new SteamClient();

        Assertions.assertThrows(IllegalArgumentException.class, () -> steamClient.getRequiredHandler(TestMsgHandler.class));
    }

    @Test
    public void getNextJobIDSetsProcessIDToZero() {
        var jobID = client.getNextJobID();

        Assertions.assertEquals(0, jobID.getProcessID());
    }

//    @Test
//    public void getNextJobIDFillsProcessStartTime() {
//        var jobID = client.getNextJobID();
//
//        var dateNow = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
//        var processStartTime = Date.from(dateNow.toInstant(ZoneOffset.UTC));
//
//        Assertions.assertEquals(processStartTime, jobID.getStartTime());
//    }

    @Test
    public void getNextJobIDSetsBoxIDToZero() {
        var jobID = client.getNextJobID();

        Assertions.assertEquals(0, jobID.getBoxID());
    }

    static class TestMsgHandler extends ClientMsgHandler {
        @Override
        public void handleMsg(IPacketMsg packetMsg) {
            // nothing
        }
    }
}
