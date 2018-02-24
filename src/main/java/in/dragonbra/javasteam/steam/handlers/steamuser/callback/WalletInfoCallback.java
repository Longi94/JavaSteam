package in.dragonbra.javasteam.steam.handlers.steamuser.callback;

import in.dragonbra.javasteam.enums.ECurrencyCode;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver.CMsgClientWalletInfoUpdate;
import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;

/**
 * This callback is received when wallet info is received from the network.
 */
public class WalletInfoCallback extends CallbackMsg {

    private boolean hasWallet;

    private ECurrencyCode currency;

    private int balance;

    public WalletInfoCallback(CMsgClientWalletInfoUpdate.Builder wallet) {
        hasWallet = wallet.getHasWallet();

        currency = ECurrencyCode.from(wallet.getCurrency());
        balance = wallet.getBalance();
    }

    public boolean isHasWallet() {
        return hasWallet;
    }

    public ECurrencyCode getCurrency() {
        return currency;
    }

    public int getBalance() {
        return balance;
    }
}
