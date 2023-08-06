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

    private final int balanceDelayed;

    private final long longBalance;

    private final long longBalanceDelayed;

    public WalletInfoCallback(CMsgClientWalletInfoUpdate.Builder wallet) {
        hasWallet = wallet.getHasWallet();

        currency = ECurrencyCode.from(wallet.getCurrency());
        balance = wallet.getBalance();
        balanceDelayed = wallet.getBalanceDelayed();
        longBalance = wallet.getBalance64();
        longBalanceDelayed = wallet.getBalance64Delayed();
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
     * @return Gets the delayed (pending) balance of the wallet as a 32-bit integer, in cents.
     */
    public int getBalanceDelayed() {
        return balanceDelayed;
    }

    /**
     * @return the balance of the wallet as a 64-bit integer, in cents.
     */
    public long getLongBalance() {
        return longBalance;
    }

    /**
     * @return Gets the delayed (pending) balance of the wallet as a 64-bit integer, in cents.
     */
    public long getLongBalanceDelayed() {
        return longBalanceDelayed;
    }
}
