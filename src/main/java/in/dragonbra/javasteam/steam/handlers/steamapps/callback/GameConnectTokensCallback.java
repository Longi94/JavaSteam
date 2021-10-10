package in.dragonbra.javasteam.steam.handlers.steamapps.callback;

import com.google.protobuf.ByteString;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver.CMsgClientGameConnectTokens;
import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This callback is fired when the client receives a list of game connect tokens.
 */
public class GameConnectTokensCallback extends CallbackMsg {

    private int tokensToKeep;

    private List<byte[]> tokens;

    public GameConnectTokensCallback(CMsgClientGameConnectTokens.Builder msg) {
        tokensToKeep = msg.getMaxTokensToKeep();

        List<byte[]> temp = new ArrayList<>();
        for (ByteString token : msg.getTokensList()) {
            temp.add(token.toByteArray());
        }

        tokens = Collections.unmodifiableList(temp);
    }

    /**
     * @return a count of tokens to keep.
     */
    public int getTokensToKeep() {
        return tokensToKeep;
    }

    /**
     * @return the list of tokens.
     */
    public List<byte[]> getTokens() {
        return tokens;
    }
}
