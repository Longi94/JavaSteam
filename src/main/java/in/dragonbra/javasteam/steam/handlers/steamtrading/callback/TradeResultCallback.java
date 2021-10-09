package in.dragonbra.javasteam.steam.handlers.steamtrading.callback;

import in.dragonbra.javasteam.enums.EEconTradeResponse;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver2.CMsgTrading_InitiateTradeResponse;
import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;
import in.dragonbra.javasteam.types.SteamID;

/**
 * This callback is fired when this client receives the response from a trade proposal.
 */
public class TradeResultCallback extends CallbackMsg {

    private int tradeID;

    private EEconTradeResponse response;

    private SteamID otherClient;

    private int numDaysSteamGuardRequired;

    private int numDaysNewDeviceCooldown;

    private int defaultNumDaysPasswordResetProbation;

    private int numDaysPasswordResetProbation;

    public TradeResultCallback(CMsgTrading_InitiateTradeResponse.Builder msg) {
        tradeID = msg.getTradeRequestId();
        response = EEconTradeResponse.from(msg.getResponse());
        otherClient = new SteamID(msg.getOtherSteamid());
        numDaysSteamGuardRequired = msg.getSteamguardRequiredDays();
        numDaysNewDeviceCooldown = msg.getNewDeviceCooldownDays();
        defaultNumDaysPasswordResetProbation = msg.getDefaultPasswordResetProbationDays();
        numDaysPasswordResetProbation = msg.getPasswordResetProbationDays();
    }

    /**
     * @return the Trade ID that this result is for.
     */
    public int getTradeID() {
        return tradeID;
    }

    /**
     * @return the response of the trade proposal.
     */
    public EEconTradeResponse getResponse() {
        return response;
    }

    /**
     * @return the {@link SteamID} of the client that responded to the proposal.
     */
    public SteamID getOtherClient() {
        return otherClient;
    }

    /**
     * @return the number of days Steam Guard is required to have been active on this account.
     */
    public int getNumDaysSteamGuardRequired() {
        return numDaysSteamGuardRequired;
    }

    /**
     * @return the number of days a new device cannot trade for.
     */
    public int getNumDaysNewDeviceCooldown() {
        return numDaysNewDeviceCooldown;
    }

    /**
     * @return the default number of days one cannot trade for after a password reset.
     */
    public int getDefaultNumDaysPasswordResetProbation() {
        return defaultNumDaysPasswordResetProbation;
    }

    /**
     * @return the number of days one cannot trade for after a password reset.
     */
    public int getNumDaysPasswordResetProbation() {
        return numDaysPasswordResetProbation;
    }
}
