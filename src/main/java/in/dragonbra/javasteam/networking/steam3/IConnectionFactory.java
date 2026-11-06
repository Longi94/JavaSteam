package in.dragonbra.javasteam.networking.steam3;

import in.dragonbra.javasteam.steam.steamclient.configuration.SteamConfiguration;
import in.dragonbra.javasteam.util.IDebugNetworkListener;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.Objects;

@FunctionalInterface
public interface IConnectionFactory {

    IConnectionFactory DEFAULT = new IConnectionFactory() {
        @Override
        public @Nullable Connection createConnection(SteamConfiguration configuration, EnumSet<ProtocolTypes> protocol) {
            return createConnection(configuration, protocol, null);
        }

        @Override
        public @Nullable Connection createConnection(
                SteamConfiguration configuration,
                EnumSet<ProtocolTypes> protocol,
                @Nullable IDebugNetworkListener debugNetworkListener
        ) {
            if (protocol.contains(ProtocolTypes.WEB_SOCKET)) {
                return new WebSocketConnection(configuration.getHttpClient());
            }
            if (protocol.contains(ProtocolTypes.TCP)) {
                return new EnvelopeEncryptedConnection(new TcpConnection(), configuration.getUniverse(), debugNetworkListener);
            }
            if (protocol.contains(ProtocolTypes.UDP)) {
                return new EnvelopeEncryptedConnection(new UdpConnection(), configuration.getUniverse(), debugNetworkListener);
            }
            return null;
        }
    };

    /**
     * If the final method returns null, an exception will be thrown.
     */
    @Nullable Connection createConnection(SteamConfiguration configuration, EnumSet<ProtocolTypes> protocol);

    /**
     * Creates a connection that reports its encryption handshake to the given debug network listener.
     * Factories that only implement {@link #createConnection(SteamConfiguration, EnumSet)} ignore the listener.
     * If the final method returns null, an exception will be thrown.
     */
    default @Nullable Connection createConnection(
            SteamConfiguration configuration,
            EnumSet<ProtocolTypes> protocol,
            @Nullable IDebugNetworkListener debugNetworkListener
    ) {
        return createConnection(configuration, protocol);
    }

    /**
     * If this method returns null, the subConnectionFactory will be used.
     */
    default IConnectionFactory thenResolve(IConnectionFactory subConnectionFactory) {
        Objects.requireNonNull(subConnectionFactory);
        return new IConnectionFactory() {
            @Override
            public @Nullable Connection createConnection(SteamConfiguration configuration, EnumSet<ProtocolTypes> protocol) {
                return createConnection(configuration, protocol, null);
            }

            @Override
            public @Nullable Connection createConnection(
                    SteamConfiguration configuration,
                    EnumSet<ProtocolTypes> protocol,
                    @Nullable IDebugNetworkListener debugNetworkListener
            ) {
                Connection connection = IConnectionFactory.this.createConnection(configuration, protocol, debugNetworkListener);
                if (connection == null) {
                    return subConnectionFactory.createConnection(configuration, protocol, debugNetworkListener);
                }
                return connection;
            }
        };
    }
}
