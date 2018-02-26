package in.dragonbra.javasteam.steam.handlers.steamfriends.callback;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver.CMsgClientClanState;
import in.dragonbra.javasteam.steam.handlers.steamfriends.Event;
import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;
import in.dragonbra.javasteam.types.SteamID;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This callback is posted when a clan's state has been changed.
 */
public class ClanStateCallback extends CallbackMsg {

    private SteamID clanID;

    private int statusFlags;

    private int accountFlags;

    private String clanName;

    private byte[] avatarHash;

    private int memberTotalCount;

    private int memberOnlineCount;

    private int memberChattingCount;

    private int memberInGameCount;

    private List<Event> events;

    private List<Event> announcements;

    public ClanStateCallback(CMsgClientClanState.Builder msg) {
        clanID = new SteamID(msg.getSteamidClan());

        statusFlags = msg.getMUnStatusFlags();
        accountFlags = msg.getClanAccountFlags();

        if (msg.hasNameInfo()) {
            clanName = msg.getNameInfo().getClanName();
            avatarHash = msg.getNameInfo().getShaAvatar().toByteArray();
        }

        if (msg.hasUserCounts()) {
            memberTotalCount = msg.getUserCounts().getMembers();
            memberOnlineCount = msg.getUserCounts().getOnline();
            memberChattingCount = msg.getUserCounts().getChatting();
            memberInGameCount = msg.getUserCounts().getInGame();
        }

        List<Event> events = msg.getEventsList().stream().map(Event::new).collect(Collectors.toList());
        this.events = Collections.unmodifiableList(events);

        List<Event> announcements = msg.getAnnouncementsList().stream().map(Event::new).collect(Collectors.toList());
        this.announcements = Collections.unmodifiableList(announcements);
    }

    public SteamID getClanID() {
        return clanID;
    }

    public int getStatusFlags() {
        return statusFlags;
    }

    public int getAccountFlags() {
        return accountFlags;
    }

    public String getClanName() {
        return clanName;
    }

    public byte[] getAvatarHash() {
        return avatarHash;
    }

    public int getMemberTotalCount() {
        return memberTotalCount;
    }

    public int getMemberOnlineCount() {
        return memberOnlineCount;
    }

    public int getMemberChattingCount() {
        return memberChattingCount;
    }

    public int getMemberInGameCount() {
        return memberInGameCount;
    }

    public List<Event> getEvents() {
        return events;
    }

    public List<Event> getAnnouncements() {
        return announcements;
    }
}
