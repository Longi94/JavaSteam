package in.dragonbra.javasteam.steam.handlers.steamnotifications.callback;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver2.CMsgClientItemAnnouncements;
import in.dragonbra.javasteam.steam.handlers.steamnotifications.SteamNotifications;
import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;

/**
 * Fired in response to calling {@link SteamNotifications#requestItemAnnouncements()}.
 */
public class ItemAnnouncementsCallback extends CallbackMsg {

    private int count;

    public ItemAnnouncementsCallback(CMsgClientItemAnnouncements.Builder msg) {
        count = msg.getCountNewItems();
    }

    /**
     * @return the number of new items
     */
    public int getCount() {
        return count;
    }
}
