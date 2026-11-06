package in.dragonbra.javasteam.networking.steam3;

/**
 * @author lngtr
 * @since 2018-02-24
 */
public interface INetFilterEncryption {
    byte[] processIncoming(byte[] data);
    byte[] processOutgoing(byte[] data);
}
