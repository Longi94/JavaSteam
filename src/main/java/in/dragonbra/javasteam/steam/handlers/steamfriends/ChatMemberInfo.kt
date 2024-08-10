package in.dragonbra.javasteam.steam.handlers.steamfriends;

import in.dragonbra.javasteam.enums.EChatPermission;
import in.dragonbra.javasteam.enums.EClanPermission;
import in.dragonbra.javasteam.types.KeyValue;
import in.dragonbra.javasteam.types.MessageObject;
import in.dragonbra.javasteam.types.SteamID;

import java.util.EnumSet;

/**
 * Represents the details of a user which is a member of a chatroom.
 */
public class ChatMemberInfo extends MessageObject {

    public ChatMemberInfo(KeyValue keyValues) {
        super(keyValues);
    }

    public ChatMemberInfo() {
        super();
    }

    /**
     * @return the clan permission details of this chat member.
     */
    public EnumSet<EClanPermission> getDetails() {
        return keyValues.get("Details").asEnum(EClanPermission.class, EnumSet.of(EClanPermission.Nobody));
    }

    /**
     * @return the permissions this user has with the chatroom.
     */
    public EnumSet<EChatPermission> getPermissions() {
        return keyValues.get("Details").asEnum(EChatPermission.class, EChatPermission.EveryoneDefault);
    }

    /**
     * @return the {@link SteamID} of this user.
     */
    public SteamID steamID() {
        return new SteamID(keyValues.get("SteamID").asLong());
    }
}
