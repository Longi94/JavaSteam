package in.dragonbra.javasteam.steam.handlers.steamtrading;

import in.dragonbra.javasteam.base.ClientMsgProtobuf;
import in.dragonbra.javasteam.base.IPacketMsg;
import in.dragonbra.javasteam.enums.EMsg;
import in.dragonbra.javasteam.handlers.ClientMsgHandler;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver2.CMsgTrading_CancelTradeRequest;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver2.CMsgTrading_InitiateTradeRequest;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver2.CMsgTrading_InitiateTradeResponse;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver2.CMsgTrading_StartSession;
import in.dragonbra.javasteam.steam.handlers.steamtrading.callback.SessionStartCallback;
import in.dragonbra.javasteam.steam.handlers.steamtrading.callback.TradeProposedCallback;
import in.dragonbra.javasteam.steam.handlers.steamtrading.callback.TradeResultCallback;
import in.dragonbra.javasteam.types.SteamID;
import in.dragonbra.javasteam.util.compat.Consumer;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * This handler is used for initializing Steam trades with other clients.
 */
public class SteamTrading extends ClientMsgHandler {

    private Map<EMsg, Consumer<IPacketMsg>> dispatchMap;

    public SteamTrading() {
        dispatchMap = new HashMap<>();

        dispatchMap.put(EMsg.EconTrading_InitiateTradeProposed, this::handleTradeProposed);
        dispatchMap.put(EMsg.EconTrading_InitiateTradeResult, this::handleTradeResult);
        dispatchMap.put(EMsg.EconTrading_StartSession, this::handleStartSession);

        dispatchMap = Collections.unmodifiableMap(dispatchMap);
    }

    /**
     * Proposes a trade to another client.
     *
     * @param user The client to trade.
     */
    public void trade(SteamID user) {
        if (user == null) {
            throw new IllegalArgumentException("user is null");
        }

        ClientMsgProtobuf<CMsgTrading_InitiateTradeRequest.Builder> tradeReq =
                new ClientMsgProtobuf<>(CMsgTrading_InitiateTradeRequest.class, EMsg.EconTrading_InitiateTradeRequest);

        tradeReq.getBody().setOtherSteamid(user.convertToUInt64());

        client.send(tradeReq);
    }

    /**
     * Responds to a trade proposal.
     *
     * @param tradeId     The trade id of the  received proposal.
     * @param acceptTrade if set to <b>true</b>, the trade will be accepted.
     */
    public void respondToTrade(int tradeId, boolean acceptTrade) {
        ClientMsgProtobuf<CMsgTrading_InitiateTradeResponse.Builder> tradeResp =
                new ClientMsgProtobuf<>(CMsgTrading_InitiateTradeResponse.class, EMsg.EconTrading_InitiateTradeResponse);

        tradeResp.getBody().setTradeRequestId(tradeId);
        tradeResp.getBody().setResponse(acceptTrade ? 1 : 0);

        client.send(tradeResp);
    }

    /**
     * Cancels an already sent trade proposal.
     *
     * @param user The user.
     */
    public void cancelTrade(SteamID user) {
        if (user == null) {
            throw new IllegalArgumentException("user is null");
        }

        ClientMsgProtobuf<CMsgTrading_CancelTradeRequest.Builder> cancelTrade =
                new ClientMsgProtobuf<>(CMsgTrading_CancelTradeRequest.class, EMsg.EconTrading_CancelTradeRequest);

        cancelTrade.getBody().setOtherSteamid(user.convertToUInt64());

        client.send(cancelTrade);
    }

    @Override
    public void handleMsg(IPacketMsg packetMsg) {
        if (packetMsg == null) {
            throw new IllegalArgumentException("packetMsg is null");
        }

        Consumer<IPacketMsg> dispatcher = dispatchMap.get(packetMsg.getMsgType());
        if (dispatcher != null) {
            dispatcher.accept(packetMsg);
        }
    }

    private void handleTradeProposed(IPacketMsg packetMsg) {
        ClientMsgProtobuf<CMsgTrading_InitiateTradeRequest.Builder> tradeProp =
                new ClientMsgProtobuf<>(CMsgTrading_InitiateTradeRequest.class, packetMsg);

        client.postCallback(new TradeProposedCallback(tradeProp.getBody()));
    }

    private void handleTradeResult(IPacketMsg packetMsg) {
        ClientMsgProtobuf<CMsgTrading_InitiateTradeResponse.Builder> tradeResult =
                new ClientMsgProtobuf<>(CMsgTrading_InitiateTradeResponse.class, packetMsg);

        client.postCallback(new TradeResultCallback(tradeResult.getBody()));
    }

    private void handleStartSession(IPacketMsg packetMsg) {
        ClientMsgProtobuf<CMsgTrading_StartSession.Builder> startSess =
                new ClientMsgProtobuf<>(CMsgTrading_StartSession.class, packetMsg);

        client.postCallback(new SessionStartCallback(startSess.getBody()));

    }
}
