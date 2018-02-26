package in.dragonbra.javasteam.steam.handlers.steamfriends.callback;

import in.dragonbra.javasteam.enums.EChatInfoType;
import in.dragonbra.javasteam.enums.EChatMemberStateChange;
import in.dragonbra.javasteam.generated.MsgClientChatMemberInfo;
import in.dragonbra.javasteam.steam.handlers.steamfriends.ChatMemberInfo;
import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;
import in.dragonbra.javasteam.types.SteamID;
import in.dragonbra.javasteam.util.stream.BinaryReader;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * This callback is fired in response to chat member info being recieved.
 */
public class ChatMemberInfoCallback extends CallbackMsg {
    private SteamID chatRoomID;

    private EChatInfoType type;

    private StateChangeDetails stateChangeInfo;

    public ChatMemberInfoCallback(MsgClientChatMemberInfo msg, byte[] payload) {
        chatRoomID = msg.getSteamIdChat();
        type = msg.getType();

        switch (type) {
            case StateChange:
                stateChangeInfo = new StateChangeDetails(payload);
                break;
            // todo: handle more types
            // based off disassembly
            //   - for InfoUpdate, a ChatMemberInfo object is present
            //   - for MemberLimitChange, looks like an ignored uint64 (probably steamid) followed
            //     by an int which likely represents the member limit
        }
    }

    public SteamID getChatRoomID() {
        return chatRoomID;
    }

    public EChatInfoType getType() {
        return type;
    }

    public StateChangeDetails getStateChangeInfo() {
        return stateChangeInfo;
    }

    /**
     * Represents state change information.
     */
    public static class StateChangeDetails {
        private SteamID chatterActedOn;

        private EChatMemberStateChange stateChange;

        private SteamID chatterActedBy;

        private ChatMemberInfo memberInfo;

        public StateChangeDetails(byte[] data) {
            try (BinaryReader br = new BinaryReader(new ByteArrayInputStream(data))) {
                chatterActedOn = new SteamID(br.readLong());
                stateChange = EChatMemberStateChange.from(br.readInt());
                chatterActedBy = new SteamID(br.readLong());

                if (stateChange == EChatMemberStateChange.Entered) {
                    memberInfo = new ChatMemberInfo();
                    memberInfo.readFromStream(br);
                }
            } catch (IOException ignored) {
            }
        }
    }
}
