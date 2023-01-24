package in.dragonbra.javasteam.steam.steamclient.callbackmgr;

import java.util.UUID;

/**
 * @author lngtr
 * @since 2018-02-25
 */
public class CallbackForTest extends CallbackMsg {

    private UUID uuid;

    public CallbackForTest(UUID uuid) {
        this.uuid = uuid;
    }

    public CallbackForTest() {
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }
}
