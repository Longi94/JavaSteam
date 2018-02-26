package in.dragonbra.javasteam.steam.handlers.steamfriends;

import in.dragonbra.javasteam.types.KeyValue;
import in.dragonbra.javasteam.types.MessageObject;

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

    // TODO: 2018-02-26
}
