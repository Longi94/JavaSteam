package in.dragonbra.javasteam.steam.handlers.steamfriends.callback;

import in.dragonbra.javasteam.enums.EAccountFlags;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver.CMsgClientClanState;
import in.dragonbra.javasteam.steam.handlers.steamfriends.Event;
import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;
import in.dragonbra.javasteam.types.SteamID;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

/**
 * This callback is posted when a clan's state has been changed.
 */
public class ClanStateCallback extends CallbackMsg {

    private SteamID clanID;

    private EnumSet<EAccountFlags> accountFlags;

    private boolean chatRoomPrivate;

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

        accountFlags = EAccountFlags.from(msg.getClanAccountFlags());
        chatRoomPrivate = msg.getChatRoomPrivate();

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

        events = new ArrayList<>();
        for (CMsgClientClanState.Event event : msg.getEventsList()) {
            events.add(new Event(event));
        }
        this.events = Collections.unmodifiableList(events);

        announcements = new ArrayList<>();
        for (CMsgClientClanState.Event event : msg.getAnnouncementsList()) {
            announcements.add(new Event(event));
        }
        this.announcements = Collections.unmodifiableList(announcements);
    }

    /**
     * @return the {@link SteamID} of the clan that posted this state update.
     */
    public SteamID getClanID() {
        return clanID;
    }

    /**
     * @return the account flags.
     */
    public EnumSet<EAccountFlags> getAccountFlags() {
        return accountFlags;
    }

    /**
     * @return the privacy of the chat room.
     */
    public boolean isChatRoomPrivate() {
        return chatRoomPrivate;
    }

    /**
     * @return the name of the clan.
     */
    public String getClanName() {
        return clanName;
    }

    /**
     * @return the SHA-1 avatar hash.
     */
    public byte[] getAvatarHash() {
        return avatarHash;
    }

    /**
     * @return the total number of members in this clan.
     */
    public int getMemberTotalCount() {
        return memberTotalCount;
    }

    /**
     * @return the number of members in this clan that are currently online.
     */
    public int getMemberOnlineCount() {
        return memberOnlineCount;
    }

    /**
     * @return the number of members in this clan that are currently chatting.
     */
    public int getMemberChattingCount() {
        return memberChattingCount;
    }

    /**
     * @return the number of members in this clan that are currently in-game.
     */
    public int getMemberInGameCount() {
        return memberInGameCount;
    }

    /**
     * @return any events associated with this clan state update. See {@link Event}
     */
    public List<Event> getEvents() {
        return events;
    }

    /**
     * @return any announcements associated with this clan state update. See {@link Event}
     */
    public List<Event> getAnnouncements() {
        return announcements;
    }
}
