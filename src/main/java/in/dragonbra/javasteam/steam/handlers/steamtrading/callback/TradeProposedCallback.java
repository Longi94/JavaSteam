package in.dragonbra.javasteam.steam.handlers.steamtrading.callback;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver2.CMsgTrading_InitiateTradeRequest;
import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;
import in.dragonbra.javasteam.types.SteamID;

/**
 * This callback is fired when this client receives a trade proposal.
 */
public class TradeProposedCallback extends CallbackMsg {

    private int tradeID;

    private SteamID otherClient;

    public TradeProposedCallback(CMsgTrading_InitiateTradeRequest.Builder msg) {
        tradeID = msg.getTradeRequestId();
        otherClient = new SteamID(msg.getOtherSteamid());
    }

    /**
     * @return the Trade ID of his proposal, used for replying.
     */
    public int getTradeID() {
        return tradeID;
    }

    /**
     * @return the SteamID of the client that sent the proposal.
     */
    public SteamID getOtherClient() {
        return otherClient;
    }
}
