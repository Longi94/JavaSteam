package in.dragonbra.javasteam.networking.steam3;

import in.dragonbra.javasteam.steam.steamclient.configuration.SteamConfiguration;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.Objects;

@FunctionalInterface
public interface IConnectionFactory {

    IConnectionFactory DEFAULT = (configuration, protocol) -> {
        if (protocol.contains(ProtocolTypes.WEB_SOCKET)) {
            return new WebSocketConnection();
        }
        if (protocol.contains(ProtocolTypes.TCP)) {
            return new EnvelopeEncryptedConnection(new TcpConnection(), configuration.getUniverse());
        }
        if (protocol.contains(ProtocolTypes.UDP)) {
            return new EnvelopeEncryptedConnection(new UdpConnection(), configuration.getUniverse());
        }
        return null;
    };

    /**
     * If the final method returns null, an exception will be thrown.
     */
    @Nullable Connection createConnection(SteamConfiguration configuration, EnumSet<ProtocolTypes> protocol);

    /**
     * If this method returns null, the subConnectionFactory will be used.
     */
    default IConnectionFactory thenResolve(IConnectionFactory subConnectionFactory) {
        Objects.requireNonNull(subConnectionFactory);
        return (configuration, protocol) -> {
            Connection connection = createConnection(configuration, protocol);
            if (connection == null) {
                return subConnectionFactory.createConnection(configuration, protocol);
            }
            return connection;
        };
    }
}
