package in.dragonbra.javasteam.steam.steamclient.callbacks;

import in.dragonbra.javasteam.steam.CMClient;
import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;

/**
 * This callback is received when the steamclient is physically disconnected from the Steam network.
 */
public class DisconnectedCallback extends CallbackMsg {

    private boolean userInitiated;

    public DisconnectedCallback(boolean userInitiated) {
        this.userInitiated = userInitiated;
    }

    /**
     * If true, the disconnection was initiated by calling {@link CMClient#disconnect()}.
     * If false, the disconnection was the cause of something not user-controlled, such as a network failure or
     * a forcible disconnection by the remote server.
     */
    public boolean isUserInitiated() {
        return userInitiated;
    }
}
