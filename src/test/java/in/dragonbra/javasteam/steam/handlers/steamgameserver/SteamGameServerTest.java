package in.dragonbra.javasteam.steam.handlers.steamgameserver;

import in.dragonbra.javasteam.enums.EResult;
import in.dragonbra.javasteam.steam.handlers.HandlerTestBase;
import in.dragonbra.javasteam.steam.handlers.steamuser.callback.LoggedOnCallback;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * @author Lossy
 * @since 2026-3-22
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class SteamGameServerTest extends HandlerTestBase<SteamGameServer> {

    @Override
    protected SteamGameServer createHandler() {
        return new SteamGameServer();
    }

    @Test
    public void logOnPostsLoggedOnCallbackWhenNoConnection() {
        Mockito.when(steamClient.isConnected()).thenReturn(false);

        var details = new LogOnDetails("SuperSecretToken", 0);
        var asyncJob = handler.logOn(details);

        var callback = getCallback();
        Assertions.assertNotNull(callback);
        Assertions.assertEquals(LoggedOnCallback.class, callback.getClass());

        var loc = (LoggedOnCallback) callback;
        Assertions.assertEquals(EResult.NoConnection, loc.getResult());
        Assertions.assertEquals(asyncJob.getJobID(), loc.getJobID());
    }

    @Test
    public void logOnAnonymousPostsLoggedOnCallbackWhenNoConnection() {
        Mockito.when(steamClient.isConnected()).thenReturn(false);

        var asyncJob = handler.logOnAnonymous();

        var callback = getCallback();
        Assertions.assertNotNull(callback);
        Assertions.assertEquals(LoggedOnCallback.class, callback.getClass());

        var loc = (LoggedOnCallback) callback;
        Assertions.assertEquals(EResult.NoConnection, loc.getResult());
        Assertions.assertEquals(asyncJob.getJobID(), loc.getJobID());
    }
}
