package in.dragonbra.javasteam.networking.steam3;

import in.dragonbra.javasteam.util.event.EventArgs;

/**
 * @author lngtr
 * @since 2018-02-20
 */
public class DisconnectedEventArgs extends EventArgs {

    private final boolean userInitiated;

    public DisconnectedEventArgs(boolean userInitiated) {
        this.userInitiated = userInitiated;
    }

    public boolean isUserInitiated() {
        return userInitiated;
    }
}
