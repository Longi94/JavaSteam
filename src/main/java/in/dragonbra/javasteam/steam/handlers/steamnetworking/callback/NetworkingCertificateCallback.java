package in.dragonbra.javasteam.steam.handlers.steamnetworking.callback;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver.*;
import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;
import in.dragonbra.javasteam.types.JobID;

/**
 * This callback is received in response to calling
 * {@link in.dragonbra.javasteam.steam.handlers.steamnetworking.SteamNetworking#requestNetworkingCertificate(int, byte[])}.
 * <p>
 * This can be used to populate a CMsgSteamDatagramCertificateSigned for socket communication.
 */
public class NetworkingCertificateCallback extends CallbackMsg {

    private final byte[] certificate;

    private final long caKeyID;

    private final byte[] caSignature;

    public NetworkingCertificateCallback(JobID jobID, CMsgClientNetworkingCertReply.Builder msg) {
        setJobID(jobID);

        this.certificate = msg.getCert().toByteArray();
        this.caKeyID = msg.getCaKeyId();
        this.caSignature = msg.getCaSignature().toByteArray();
    }

    /**
     * @return The certificate signed by the Steam CA. This contains a CMsgSteamDatagramCertificate with the supplied public key.
     */
    public byte[] getCertificate() {
        return certificate;
    }

    /**
     * @return The ID of the CA used to sign this certificate.
     */
    public long getCaKeyID() {
        return caKeyID;
    }

    /**
     * @return The signature used to verify {@link NetworkingCertificateCallback#getCertificate()}
     */
    public byte[] getCaSignature() {
        return caSignature;
    }
}
