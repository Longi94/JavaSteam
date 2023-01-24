package in.dragonbra.javasteam.steam.handlers.steamuser.callback;

import in.dragonbra.javasteam.enums.ECurrencyCode;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver.CMsgClientWalletInfoUpdate;
import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;

/**
 * This callback is received when wallet info is received from the network.
 */
public class WalletInfoCallback extends CallbackMsg {

    private final boolean hasWallet;

    private final ECurrencyCode currency;

    private final int balance;

    private final long longBalance;

    public WalletInfoCallback(CMsgClientWalletInfoUpdate.Builder wallet) {
        hasWallet = wallet.getHasWallet();

        currency = ECurrencyCode.from(wallet.getCurrency());
        balance = wallet.getBalance();
        longBalance = wallet.getBalance64();
    }

    /**
     * @return a value indicating whether this instance has wallet data.
     */
    public boolean isHasWallet() {
        return hasWallet;
    }

    /**
     * @return the currency code for this wallet.
     */
    public ECurrencyCode getCurrency() {
        return currency;
    }

    /**
     * @return the balance of the wallet as a 32-bit integer, in cents.
     */
    public int getBalance() {
        return balance;
    }

    /**
     * @return the balance of the wallet as a 64-bit integer, in cents.
     */
    public long getLongBalance() {
        return longBalance;
    }
}
