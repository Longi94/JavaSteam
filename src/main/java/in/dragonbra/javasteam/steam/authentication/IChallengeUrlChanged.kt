package `in`.dragonbra.javasteam.steam.authentication

/**
 * Interface to tell the listening class that the QR Challenge URL has changed.
 */
fun interface IChallengeUrlChanged {
    fun onChanged(qrAuthSession: QrAuthSession)
}
