package in.dragonbra.javasteam.steam.handlers.steamnotifications;

import in.dragonbra.javasteam.base.ClientMsgProtobuf;
import in.dragonbra.javasteam.base.IPacketMsg;
import in.dragonbra.javasteam.enums.EMsg;
import in.dragonbra.javasteam.handlers.ClientMsgHandler;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver2.*;
import in.dragonbra.javasteam.steam.handlers.steamnotifications.callback.CommentNotificationsCallback;
import in.dragonbra.javasteam.steam.handlers.steamnotifications.callback.ItemAnnouncementsCallback;
import in.dragonbra.javasteam.steam.handlers.steamnotifications.callback.OfflineMessageNotificationCallback;
import in.dragonbra.javasteam.steam.handlers.steamnotifications.callback.UserNotificationsCallback;
import in.dragonbra.javasteam.util.compat.Consumer;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * This handler handles steam notifications.
 */
public class SteamNotifications extends ClientMsgHandler {

    private Map<EMsg, Consumer<IPacketMsg>> dispatchMap;

    public SteamNotifications() {
        dispatchMap = new HashMap<>();

        dispatchMap.put(EMsg.ClientUserNotifications, this::handleUserNotifications);
        dispatchMap.put(EMsg.ClientChatOfflineMessageNotification, this::handleOfflineMessageNotification);
        dispatchMap.put(EMsg.ClientCommentNotifications, this::handleCommentNotifications);
        dispatchMap.put(EMsg.ClientItemAnnouncements, this::handleItemAnnouncements);

        dispatchMap = Collections.unmodifiableMap(dispatchMap);
    }

    /**
     * Request comment notifications.
     * Results are returned in a {@link CommentNotificationsCallback}.
     */
    public void requestCommentNotifications() {
        ClientMsgProtobuf<CMsgClientRequestCommentNotifications.Builder> request =
                new ClientMsgProtobuf<>(CMsgClientRequestCommentNotifications.class, EMsg.ClientRequestCommentNotifications);

        client.send(request);
    }

    /**
     * Request new items notifications.
     * Results are returned in a {@link ItemAnnouncementsCallback}.
     */
    public void requestItemAnnouncements() {
        ClientMsgProtobuf<CMsgClientRequestItemAnnouncements.Builder> request =
                new ClientMsgProtobuf<>(CMsgClientRequestItemAnnouncements.class, EMsg.ClientRequestItemAnnouncements);

        client.send(request);
    }

    /**
     * Request offline message count.
     * Results are returned in a {@link OfflineMessageNotificationCallback}.
     */
    public void requestOfflineMessageCount() {
        ClientMsgProtobuf<CMsgClientRequestOfflineMessageCount.Builder> request =
                new ClientMsgProtobuf<>(CMsgClientRequestOfflineMessageCount.class, EMsg.ClientChatRequestOfflineMessageCount);

        client.send(request);
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

    private void handleUserNotifications(IPacketMsg packetMsg) {
        ClientMsgProtobuf<CMsgClientUserNotifications.Builder> msg =
                new ClientMsgProtobuf<>(CMsgClientUserNotifications.class, packetMsg);

        client.postCallback(new UserNotificationsCallback(msg.getBody()));
    }

    private void handleOfflineMessageNotification(IPacketMsg packetMsg) {
        ClientMsgProtobuf<CMsgClientOfflineMessageNotification.Builder> msg =
                new ClientMsgProtobuf<>(CMsgClientOfflineMessageNotification.class, packetMsg);

        client.postCallback(new OfflineMessageNotificationCallback(msg.getBody()));
    }

    private void handleCommentNotifications(IPacketMsg packetMsg) {
        ClientMsgProtobuf<CMsgClientCommentNotifications.Builder> msg =
                new ClientMsgProtobuf<>(CMsgClientCommentNotifications.class, packetMsg);

        client.postCallback(new CommentNotificationsCallback(msg.getBody()));
    }

    private void handleItemAnnouncements(IPacketMsg packetMsg) {
        ClientMsgProtobuf<CMsgClientItemAnnouncements.Builder> msg =
                new ClientMsgProtobuf<>(CMsgClientItemAnnouncements.class, packetMsg);

        client.postCallback(new ItemAnnouncementsCallback(msg.getBody()));
    }
}
