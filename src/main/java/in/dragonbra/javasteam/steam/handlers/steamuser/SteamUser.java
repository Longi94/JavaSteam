package in.dragonbra.javasteam.steam.handlers.steamuser;

import com.google.protobuf.ByteString;
import in.dragonbra.javasteam.base.ClientMsg;
import in.dragonbra.javasteam.base.ClientMsgProtobuf;
import in.dragonbra.javasteam.base.IPacketMsg;
import in.dragonbra.javasteam.enums.EAccountType;
import in.dragonbra.javasteam.enums.EMsg;
import in.dragonbra.javasteam.enums.EResult;
import in.dragonbra.javasteam.generated.MsgClientLogOnResponse;
import in.dragonbra.javasteam.generated.MsgClientLoggedOff;
import in.dragonbra.javasteam.generated.MsgClientLogon;
import in.dragonbra.javasteam.generated.MsgClientMarketingMessageUpdate2;
import in.dragonbra.javasteam.handlers.ClientMsgHandler;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesBase;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver.CMsgClientSessionToken;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver.CMsgClientWalletInfoUpdate;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver2.CMsgClientEmailAddrInfo;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver2.CMsgClientPlayingSessionState;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver2.CMsgClientUpdateMachineAuth;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver2.CMsgClientUpdateMachineAuthResponse;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver2.CMsgClientVanityURLChangedNotification;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverLogin.*;
import in.dragonbra.javasteam.steam.handlers.steamuser.callback.*;
import in.dragonbra.javasteam.types.AsyncJobSingle;
import in.dragonbra.javasteam.types.JobID;
import in.dragonbra.javasteam.types.SteamID;
import in.dragonbra.javasteam.util.HardwareUtils;
import in.dragonbra.javasteam.util.NetHelpers;
import in.dragonbra.javasteam.util.Strings;
import in.dragonbra.javasteam.util.compat.Consumer;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * This handler handles all user log on/log off related actions and callbacks.
 */
@SuppressWarnings("unused")
public class SteamUser extends ClientMsgHandler {

    private Map<EMsg, Consumer<IPacketMsg>> dispatchMap;

    public SteamUser() {
        dispatchMap = new HashMap<>();

        dispatchMap.put(EMsg.ClientLogOnResponse, this::handleLogOnResponse);
        dispatchMap.put(EMsg.ClientLoggedOff, this::handleLoggedOff);
        dispatchMap.put(EMsg.ClientNewLoginKey, this::handleLoginKey);
        dispatchMap.put(EMsg.ClientSessionToken, this::handleSessionToken);
        dispatchMap.put(EMsg.ClientUpdateMachineAuth, this::handleUpdateMachineAuth);
        dispatchMap.put(EMsg.ClientAccountInfo, this::handleAccountInfo);
        dispatchMap.put(EMsg.ClientEmailAddrInfo, this::handleEmailAddrInfo);
        dispatchMap.put(EMsg.ClientWalletInfoUpdate, this::handleWalletInfo);
        dispatchMap.put(EMsg.ClientRequestWebAPIAuthenticateUserNonceResponse, this::handleWebAPIUserNonce);
        dispatchMap.put(EMsg.ClientVanityURLChangedNotification, this::handleVanityURLChangedNotification);
        dispatchMap.put(EMsg.ClientMarketingMessageUpdate2, this::handleMarketingMessageUpdate);
        dispatchMap.put(EMsg.ClientPlayingSessionState, this::handlePlayingSessionState);

        dispatchMap = Collections.unmodifiableMap(dispatchMap);
    }

    /**
     * Logs the client into the Steam3 network.
     * The client should already have been connected at this point.
     * Results are returned in a {@link LoggedOnCallback}.
     *
     * @param details The details to use for logging on.
     */
    public void logOn(LogOnDetails details) {
        if (details == null) {
            throw new IllegalArgumentException("details is null");
        }

        if (Strings.isNullOrEmpty(details.getUsername()) || (Strings.isNullOrEmpty(details.getPassword()) &&
                Strings.isNullOrEmpty(details.getLoginKey()) && Strings.isNullOrEmpty(details.getAccessToken()))) {
            throw new IllegalArgumentException("LogOn requires a username and password to be set in 'details'.");
        }

        if (!Strings.isNullOrEmpty(details.getLoginKey()) && !details.isShouldRememberPassword()) {
            // Prevent consumers from screwing this up.
            // If should_remember_password is false, the login_key is ignored server-side.
            // The inverse is not applicable (you can log in with should_remember_password and no login_key).
            throw new IllegalArgumentException("ShouldRememberPassword is required to be set to true in order to use LoginKey.");
        }

        if (!client.isConnected()) {
            client.postCallback(new LoggedOnCallback(EResult.NoConnection));
            return;
        }

        ClientMsgProtobuf<CMsgClientLogon.Builder> logon = new ClientMsgProtobuf<>(CMsgClientLogon.class, EMsg.ClientLogon);

        SteamID steamID = new SteamID(details.getAccountID(), details.getAccountInstance(), client.getUniverse(), EAccountType.Individual);

        SteammessagesBase.CMsgIPAddress.Builder ipAddress = SteammessagesBase.CMsgIPAddress.newBuilder();
        if (details.getLoginID() != null) {
            ipAddress.setV4(details.getLoginID());

            logon.getBody().setObfuscatedPrivateIp(ipAddress.build());
        } else {
            int localIp = NetHelpers.getIPAddress(client.getLocalIP());
            ipAddress.setV4(localIp ^ MsgClientLogon.ObfuscationMask);

            logon.getBody().setObfuscatedPrivateIp(ipAddress.build());
        }

        // Legacy field, Steam client still sets it
        if (logon.getBody().getObfuscatedPrivateIp().hasV4()) {
            logon.getBody().setDeprecatedObfustucatedPrivateIp(logon.getBody().getObfuscatedPrivateIp().getV4());
        }


        logon.getProtoHeader().setClientSessionid(0);
        logon.getProtoHeader().setSteamid(steamID.convertToUInt64());

        logon.getBody().setAccountName(details.getUsername());
        if (!Strings.isNullOrEmpty(details.getPassword())) {
            logon.getBody().setPassword(details.getPassword());
        }
        logon.getBody().setShouldRememberPassword(details.isShouldRememberPassword());

        logon.getBody().setProtocolVersion(MsgClientLogon.CurrentProtocol);
        logon.getBody().setClientOsType(details.getClientOSType().code());
        logon.getBody().setClientLanguage(details.getClientLanguage());

        if (details.getCellID() != null) {
            logon.getBody().setCellId(details.getCellID());
        } else {
            logon.getBody().setCellId(client.getConfiguration().getCellID());
        }

        logon.getBody().setSteam2TicketRequest(details.isRequestSteam2Ticket());

        // we're now using the latest steamclient package version, this is required to get a proper sentry file for steam guard
        logon.getBody().setClientPackageVersion(1771); // todo: determine if this is still required
        logon.getBody().setSupportsRateLimitResponse(true);
        logon.getBody().setMachineName(details.getMachineName());
        logon.getBody().setMachineId(ByteString.copyFrom(HardwareUtils.getMachineID()));

        // steam guard
        if (!Strings.isNullOrEmpty(details.getAuthCode())) {
            logon.getBody().setAuthCode(details.getAuthCode());
        }

        if (!Strings.isNullOrEmpty(details.getTwoFactorCode())) {
            logon.getBody().setTwoFactorCode(details.getTwoFactorCode());
        }

        if (!Strings.isNullOrEmpty(details.getLoginKey())) {
            logon.getBody().setLoginKey(details.getLoginKey());
        }

        if (!Strings.isNullOrEmpty(details.getAccessToken())) {
            logon.getBody().setAccessToken(details.getAccessToken());
        }

        if (details.getSentryFileHash() != null) {
            logon.getBody().setShaSentryfile(ByteString.copyFrom(details.getSentryFileHash()));
        }
        logon.getBody().setEresultSentryfile(details.getSentryFileHash() != null ? EResult.OK.code() : EResult.FileNotFound.code());

        client.send(logon);
    }

    /**
     * Logs the client into the Steam3 network as an anonymous user.
     * The client should already have been connected at this point.
     * Results are returned in a {@link LoggedOnCallback}.
     */
    public void logOnAnonymous() {
        logOnAnonymous(new AnonymousLogOnDetails());
    }

    /**
     * Logs the client into the Steam3 network as an anonymous user.
     * The client should already have been connected at this point.
     * Results are returned in a {@link LoggedOnCallback}.
     *
     * @param details The details to use for logging on.
     */
    public void logOnAnonymous(AnonymousLogOnDetails details) {
        if (details == null) {
            throw new IllegalArgumentException("details is null");
        }

        if (!client.isConnected()) {
            client.postCallback(new LoggedOnCallback(EResult.NoConnection));
            return;
        }

        ClientMsgProtobuf<CMsgClientLogon.Builder> logon = new ClientMsgProtobuf<>(CMsgClientLogon.class, EMsg.ClientLogon);

        SteamID auId = new SteamID(0, 0, client.getUniverse(), EAccountType.AnonUser);

        logon.getProtoHeader().setClientSessionid(0);
        logon.getProtoHeader().setSteamid(auId.convertToUInt64());

        logon.getBody().setProtocolVersion(MsgClientLogon.CurrentProtocol);
        logon.getBody().setClientOsType(details.getClientOSType().code());
        logon.getBody().setClientLanguage(details.getClientLanguage());

        if (details.getCellID() != null) {
            logon.getBody().setCellId(details.getCellID());
        } else {
            logon.getBody().setCellId(client.getConfiguration().getCellID());
        }

        logon.getBody().setMachineId(ByteString.copyFrom(HardwareUtils.getMachineID()));

        client.send(logon);
    }

    /**
     * Informs the Steam servers that this client wishes to log off from the network.
     * The Steam server will disconnect the client, and a
     * {@link in.dragonbra.javasteam.steam.steamclient.callbacks.DisconnectedCallback DisconnectedCallback} will be posted.
     */
    public void logOff() {
        setExpectDisconnection(true);

        ClientMsgProtobuf<CMsgClientLogOff.Builder> logOff = new ClientMsgProtobuf<>(CMsgClientLogOff.class, EMsg.ClientLogOff);
        client.send(logOff);

        // TODO: 2018-02-28 it seems like the socket is not closed after getting logged of or I am doing something horribly wrong, let's disconnect here
        client.disconnect();
    }

    /**
     * Sends a machine auth response.
     * This should normally be used in response to a {@link UpdateMachineAuthCallback}.
     *
     * @param details The details pertaining to the response.
     * @deprecated Steam no longer sends machine auth as of 2023, use SteamAuthentication.
     */
    @Deprecated
    public void sendMachineAuthResponse(MachineAuthDetails details) {
        if (details == null) {
            throw new IllegalArgumentException("details is null");
        }

        ClientMsgProtobuf<CMsgClientUpdateMachineAuthResponse.Builder> response = new ClientMsgProtobuf<>(CMsgClientUpdateMachineAuthResponse.class, EMsg.ClientUpdateMachineAuthResponse);

        // so we respond to the correct message
        response.getProtoHeader().setJobidTarget(details.getJobID().getValue());

        response.getBody().setCubwrote(details.getBytesWritten());
        response.getBody().setEresult(details.getEResult().code());

        response.getBody().setFilename(details.getFileName());
        response.getBody().setFilesize(details.getFileSize());

        response.getBody().setGetlasterror(details.getLastError());
        response.getBody().setOffset(details.getOffset());

        response.getBody().setShaFile(ByteString.copyFrom(details.getSentryFileHash()));

        response.getBody().setOtpIdentifier(details.getOneTimePassword().getIdentifier());
        response.getBody().setOtpType(details.getOneTimePassword().getType());
        response.getBody().setOtpValue(details.getOneTimePassword().getValue());

        client.send(response);
    }

    /**
     * Requests a new WebAPI authentication user nonce.
     * Results are returned in a {@link WebAPIUserNonceCallback}.
     * The returned {@link  AsyncJobSingle} can also be awaited to retrieve the callback result.
     *
     * @return The Job ID of the request. This can be used to find the appropriate {@link WebAPIUserNonceCallback}.
     */
    public AsyncJobSingle<WebAPIUserNonceCallback> requestWebAPIUserNonce() {
        ClientMsgProtobuf<CMsgClientRequestWebAPIAuthenticateUserNonce.Builder> reqMsg =
                new ClientMsgProtobuf<>(CMsgClientRequestWebAPIAuthenticateUserNonce.class, EMsg.ClientRequestWebAPIAuthenticateUserNonce);
        JobID jobID = client.getNextJobID();
        reqMsg.setSourceJobID(jobID);

        client.send(reqMsg);

        return new AsyncJobSingle<>(this.client, reqMsg.getSourceJobID());
    }

    /**
     * Accepts the new Login Key provided by a {@link LoginKeyCallback}.
     *
     * @param callback The callback containing the new Login Key.
     * @deprecated "Steam no longer sends new login keys as of March 2023, use SteamAuthentication."
     */
    @Deprecated
    public void acceptNewLoginKey(LoginKeyCallback callback) {
        if (callback == null) {
            throw new IllegalArgumentException("callback is null");
        }

        ClientMsgProtobuf<CMsgClientNewLoginKeyAccepted.Builder> acceptance = new ClientMsgProtobuf<>(CMsgClientNewLoginKeyAccepted.class, EMsg.ClientNewLoginKeyAccepted);
        acceptance.getBody().setUniqueId(callback.getUniqueID());

        client.send(acceptance);
    }

    @Override
    public void handleMsg(IPacketMsg packetMsg) {
        if (packetMsg == null) {
            throw new IllegalArgumentException("packetMsg is null");
        }

        Consumer<IPacketMsg> dispatcher = dispatchMap.get(packetMsg.getMsgType());
        if (dispatcher != null) {
            dispatcher.accept(packetMsg);
        }
    }

    public SteamID getSteamID() {
        return client.getSteamID();
    }

    private void handleLogOnResponse(IPacketMsg packetMsg) {
        if (packetMsg.isProto()) {
            ClientMsgProtobuf<CMsgClientLogonResponse.Builder> logonResp = new ClientMsgProtobuf<>(CMsgClientLogonResponse.class, packetMsg);

            client.postCallback(new LoggedOnCallback(logonResp.getBody()));
        } else {
            ClientMsg<MsgClientLogOnResponse> logonResp = new ClientMsg<>(MsgClientLogOnResponse.class, packetMsg);

            client.postCallback(new LoggedOnCallback(logonResp.getBody()));
        }
    }

    private void handleLoggedOff(IPacketMsg packetMsg) {
        EResult result;

        if (packetMsg.isProto()) {
            ClientMsgProtobuf<CMsgClientLoggedOff.Builder> loggedOff = new ClientMsgProtobuf<>(CMsgClientLoggedOff.class, packetMsg);
            result = EResult.from(loggedOff.getBody().getEresult());
        } else {
            ClientMsg<MsgClientLoggedOff> loggedOff = new ClientMsg<>(MsgClientLoggedOff.class, packetMsg);
            result = loggedOff.getBody().getResult();
        }

        client.postCallback(new LoggedOffCallback(result));

        // TODO: 2018-02-28 it seems like the socket is not closed after getting logged of or I am doing something horribly wrong, let's disconnect here
        client.disconnect();
    }

    private void handleLoginKey(IPacketMsg packetMsg) {
        ClientMsgProtobuf<CMsgClientNewLoginKey.Builder> loginKey = new ClientMsgProtobuf<>(CMsgClientNewLoginKey.class, packetMsg);
        client.postCallback(new LoginKeyCallback(loginKey.getBody()));
    }

    private void handleSessionToken(IPacketMsg packetMsg) {
        ClientMsgProtobuf<CMsgClientSessionToken.Builder> sessToken = new ClientMsgProtobuf<>(CMsgClientSessionToken.class, packetMsg);
        client.postCallback(new SessionTokenCallback(sessToken.getBody()));
    }

    private void handleUpdateMachineAuth(IPacketMsg packetMsg) {
        ClientMsgProtobuf<CMsgClientUpdateMachineAuth.Builder> machineAuth = new ClientMsgProtobuf<>(CMsgClientUpdateMachineAuth.class, packetMsg);
        client.postCallback(new UpdateMachineAuthCallback(new JobID(packetMsg.getSourceJobID()), machineAuth.getBody()));
    }

    private void handleAccountInfo(IPacketMsg packetMsg) {
        ClientMsgProtobuf<CMsgClientAccountInfo.Builder> accInfo = new ClientMsgProtobuf<>(CMsgClientAccountInfo.class, packetMsg);
        client.postCallback(new AccountInfoCallback(accInfo.getBody()));
    }

    private void handleEmailAddrInfo(IPacketMsg packetMsg) {
        ClientMsgProtobuf<CMsgClientEmailAddrInfo.Builder> emailAddrInfo = new ClientMsgProtobuf<>(CMsgClientEmailAddrInfo.class, packetMsg);
        client.postCallback(new EmailAddrInfoCallback(emailAddrInfo.getBody()));
    }

    private void handleWalletInfo(IPacketMsg packetMsg) {
        ClientMsgProtobuf<CMsgClientWalletInfoUpdate.Builder> walletInfo = new ClientMsgProtobuf<>(CMsgClientWalletInfoUpdate.class, packetMsg);
        client.postCallback(new WalletInfoCallback(walletInfo.getBody()));
    }

    private void handleWebAPIUserNonce(IPacketMsg packetMsg) {
        ClientMsgProtobuf<CMsgClientRequestWebAPIAuthenticateUserNonceResponse.Builder> userNonce = new ClientMsgProtobuf<>(CMsgClientRequestWebAPIAuthenticateUserNonceResponse.class, packetMsg);
        client.postCallback(new WebAPIUserNonceCallback(userNonce.getTargetJobID(), userNonce.getBody()));
    }

    private void handleVanityURLChangedNotification(IPacketMsg packetMsg) {
        ClientMsgProtobuf<CMsgClientVanityURLChangedNotification.Builder> vanityUrl = new ClientMsgProtobuf<>(CMsgClientVanityURLChangedNotification.class, packetMsg);
        client.postCallback(new VanityURLChangedCallback(vanityUrl.getTargetJobID(), vanityUrl.getBody()));
    }

    private void handleMarketingMessageUpdate(IPacketMsg packetMsg) {
        ClientMsg<MsgClientMarketingMessageUpdate2> marketingMessage = new ClientMsg<>(MsgClientMarketingMessageUpdate2.class, packetMsg);

        byte[] payload = marketingMessage.getPayload().toByteArray();

        client.postCallback(new MarketingMessageCallback(marketingMessage.getBody(), payload));
    }

    private void handlePlayingSessionState(IPacketMsg packetMsg) {
        ClientMsgProtobuf<CMsgClientPlayingSessionState.Builder> playingSessionState = new ClientMsgProtobuf<>(CMsgClientPlayingSessionState.class, packetMsg);

        client.postCallback(new PlayingSessionStateCallback(playingSessionState.getTargetJobID(), playingSessionState.getBody()));
    }
}
