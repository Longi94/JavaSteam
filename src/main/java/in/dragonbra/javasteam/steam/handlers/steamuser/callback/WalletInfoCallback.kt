package `in`.dragonbra.javasteam.steam.handlers.steamuser.callback

import `in`.dragonbra.javasteam.base.ClientMsgProtobuf
import `in`.dragonbra.javasteam.base.IPacketMsg
import `in`.dragonbra.javasteam.enums.ECurrencyCode
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver.CMsgClientWalletInfoUpdate
import `in`.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg

/**
 * This callback is received when wallet info is received from the network.
 */
@Suppress("MemberVisibilityCanBePrivate")
class WalletInfoCallback(packetMsg: IPacketMsg) : CallbackMsg() {

    /**
     * Gets a value indicating whether this instance has wallet data.
     */
    val isHasWallet: Boolean

    /**
     * Gets the currency code for this wallet.
     */
    val currency: ECurrencyCode

    /**
     * Gets the balance of the wallet as a 32-bit integer, in cents.
     */
    val balance: Int

    /**
     * Gets the delayed (pending) balance of the wallet as a 32-bit integer, in cents.
     */
    val balanceDelayed: Int

    /**
     * Gets the balance of the wallet as a 64-bit integer, in cents.
     */
    val longBalance: Long

    /**
     * Gets the delayed (pending) balance of the wallet as a 64-bit integer, in cents.
     */
    val longBalanceDelayed: Long

    init {
        val walletInfo = ClientMsgProtobuf<CMsgClientWalletInfoUpdate.Builder>(
            CMsgClientWalletInfoUpdate::class.java,
            packetMsg
        )
        val wallet = walletInfo.body

        isHasWallet = wallet.hasWallet

        currency = ECurrencyCode.from(wallet.currency)
        balance = wallet.balance
        balanceDelayed = wallet.balanceDelayed
        longBalance = wallet.balance64
        longBalanceDelayed = wallet.balance64Delayed
    }
}
