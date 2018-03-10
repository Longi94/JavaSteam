package in.dragonbra.javasteam.steam.handlers.steamscreenshots;

import in.dragonbra.javasteam.base.ClientMsgProtobuf;
import in.dragonbra.javasteam.base.IPacketMsg;
import in.dragonbra.javasteam.enums.EMsg;
import in.dragonbra.javasteam.handlers.ClientMsgHandler;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver2.CMsgClientUCMAddScreenshot;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver2.CMsgClientUCMAddScreenshotResponse;
import in.dragonbra.javasteam.steam.handlers.steamscreenshots.callback.ScreenshotAddedCallback;
import in.dragonbra.javasteam.util.compat.Consumer;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * This handler is used for screenshots.
 */
public class SteamScreenshots extends ClientMsgHandler {

    /**
     * Width of a screenshot thumnail
     */
    public static final int SCREENSHOT_THUMBNAIL_WIDTH = 200;

    private Map<EMsg, Consumer<IPacketMsg>> dispatchMap;

    public SteamScreenshots() {
        dispatchMap = new HashMap<>();

        dispatchMap.put(EMsg.ClientUCMAddScreenshotResponse, new Consumer<IPacketMsg>() {
            @Override
            public void accept(IPacketMsg packetMsg) {
                handleUCMAddScreenshot(packetMsg);
            }
        });

        dispatchMap = Collections.unmodifiableMap(dispatchMap);
    }

    /**
     * Adds a screenshot to the user's screenshot library. The screenshot image and thumbnail must already exist on the UFS.
     * Results are returned in a {@link ScreenshotAddedCallback}.
     *
     * @param details The details of the screenshot.
     */
    public void addScreenshot(ScreenshotDetails details) {
        if (details == null) {
            throw new IllegalArgumentException("details is null");
        }

        ClientMsgProtobuf<CMsgClientUCMAddScreenshot.Builder> msg =
                new ClientMsgProtobuf<>(CMsgClientUCMAddScreenshot.class, EMsg.ClientUCMAddScreenshot);
        msg.setSourceJobID(client.getNextJobID());

        if (details.getGameID() != null) {
            msg.getBody().setAppid(details.getGameID().getAppID());
        }

        msg.getBody().setCaption(details.getCaption());
        msg.getBody().setFilename(details.getUfsImageFilePath());
        msg.getBody().setPermissions(details.getPrivacy().code());
        msg.getBody().setThumbname(details.getUsfThumbnailFilePath());
        msg.getBody().setWidth(details.getWidth());
        msg.getBody().setHeight(details.getHeight());
        msg.getBody().setRtime32Created((int) (details.getCreationTime().getTime() / 1000L));

        client.send(msg);
    }

    @Override
    public void handleMsg(IPacketMsg packetMsg) {
        if (packetMsg == null) {
            throw new IllegalArgumentException("packetMsg is null");
        }

        if (dispatchMap.containsKey(packetMsg.getMsgType())) {
            dispatchMap.get(packetMsg.getMsgType()).accept(packetMsg);
        }
    }

    private void handleUCMAddScreenshot(IPacketMsg packetMsg) {
        ClientMsgProtobuf<CMsgClientUCMAddScreenshotResponse.Builder> resp =
                new ClientMsgProtobuf<>(CMsgClientUCMAddScreenshotResponse.class, packetMsg);

        client.postCallback(new ScreenshotAddedCallback(resp.getTargetJobID(), resp.getBody()));
    }
}
