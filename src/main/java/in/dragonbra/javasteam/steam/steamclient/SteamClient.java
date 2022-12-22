package in.dragonbra.javasteam.steam.steamclient;

import in.dragonbra.javasteam.base.ClientMsgProtobuf;
import in.dragonbra.javasteam.base.IPacketMsg;
import in.dragonbra.javasteam.enums.EMsg;
import in.dragonbra.javasteam.handlers.ClientMsgHandler;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver.CMsgClientCMList;
import in.dragonbra.javasteam.steam.CMClient;
import in.dragonbra.javasteam.steam.handlers.steamapps.SteamApps;
import in.dragonbra.javasteam.steam.handlers.steamcloud.SteamCloud;
import in.dragonbra.javasteam.steam.handlers.steamfriends.SteamFriends;
import in.dragonbra.javasteam.steam.handlers.steamgamecoordinator.SteamGameCoordinator;
import in.dragonbra.javasteam.steam.handlers.steamgameserver.SteamGameServer;
import in.dragonbra.javasteam.steam.handlers.steammasterserver.SteamMasterServer;
import in.dragonbra.javasteam.steam.handlers.steamnotifications.SteamNotifications;
import in.dragonbra.javasteam.steam.handlers.steamscreenshots.SteamScreenshots;
import in.dragonbra.javasteam.steam.handlers.steamtrading.SteamTrading;
import in.dragonbra.javasteam.steam.handlers.steamuser.SteamUser;
import in.dragonbra.javasteam.steam.handlers.steamuserstats.SteamUserStats;
import in.dragonbra.javasteam.steam.handlers.steamworkshop.SteamWorkshop;
import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;
import in.dragonbra.javasteam.steam.steamclient.callbackmgr.ICallbackMsg;
import in.dragonbra.javasteam.steam.steamclient.callbacks.CMListCallback;
import in.dragonbra.javasteam.steam.steamclient.callbacks.ConnectedCallback;
import in.dragonbra.javasteam.steam.steamclient.callbacks.DisconnectedCallback;
import in.dragonbra.javasteam.steam.steamclient.configuration.SteamConfiguration;
import in.dragonbra.javasteam.types.JobID;
import in.dragonbra.javasteam.util.compat.Consumer;
import in.dragonbra.javasteam.util.log.LogManager;
import in.dragonbra.javasteam.util.log.Logger;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Represents a single client that connects to the Steam3 network.
 * This class is also responsible for handling the registration of client message handlers and callbacks.
 */
public class SteamClient extends CMClient {

    private static final Logger logger = LogManager.getLogger(SteamClient.class);

    private Map<Class<? extends ClientMsgHandler>, ClientMsgHandler> handlers = new HashMap<>();

    private AtomicLong currentJobId = new AtomicLong(0L);

    private Date processStartTime;

    private final Object callbackLock = new Object();

    private Queue<ICallbackMsg> callbackQueue = new LinkedList<>();

    private Map<EMsg, Consumer<IPacketMsg>> dispatchMap = new HashMap<>();

    /**
     * Initializes a new instance of the {@link SteamClient} class with the default configuration.
     */
    public SteamClient() {
        this(SteamConfiguration.createDefault());
    }

    /**
     * Initializes a new instance of the {@link SteamClient} class with a specific configuration.
     *
     * @param configuration The configuration to use for this client.
     */
    public SteamClient(SteamConfiguration configuration) {
        super(configuration);

        // add this library's handlers
        // notice: SteamFriends should be added before SteamUser due to AccountInfoCallback
        addHandler(new SteamFriends());
        addHandler(new SteamUser());
        addHandler(new SteamNotifications());
        addHandler(new SteamTrading());
        addHandler(new SteamApps());
        addHandler(new SteamCloud());
        addHandler(new SteamScreenshots());
        addHandler(new SteamUserStats());
        addHandler(new SteamWorkshop());
        addHandler(new SteamMasterServer());
        addHandler(new SteamGameServer());
        addHandler(new SteamGameCoordinator());

        processStartTime = new Date();

        dispatchMap.put(EMsg.ClientCMList, this::handleCMList);
        dispatchMap.put(EMsg.JobHeartbeat, this::handleJobHeartbeat);
        dispatchMap.put(EMsg.DestJobFailed, this::handleJobFailed);
    }

    /**
     * Adds a new handler to the internal list of message handlers.
     *
     * @param handler The handler to add.
     */
    public void addHandler(ClientMsgHandler handler) {
        if (handlers.containsKey(handler.getClass())) {
            throw new IllegalArgumentException("A handler of type " + handler.getClass() + " is already registered.");
        }

        handler.setup(this);
        handlers.put(handler.getClass(), handler);
    }

    /**
     * Removes a registered handler by name.
     *
     * @param handler The handler name to remove.
     */
    public void removeHandler(Class<? extends ClientMsgHandler> handler) {
        handlers.remove(handler);
    }

    /**
     * Removes a registered handler.
     *
     * @param handler The handler name to remove.
     */
    public void removeHandler(ClientMsgHandler handler) {
        removeHandler(handler.getClass());
    }

    /**
     * Returns a registered handler.
     *
     * @param type The type of the handler to cast to. Must derive from ClientMsgHandler.
     * @param <T>  The type of the handler to cast to. Must derive from ClientMsgHandler.
     * @return A registered handler on success, or null if the handler could not be found.
     */
    @SuppressWarnings("unchecked")
    public <T extends ClientMsgHandler> T getHandler(Class<T> type) {
        return (T) handlers.get(type);
    }

    /**
     * Gets the next callback object in the queue.
     * This function does not dequeue the callback, you must call FreeLastCallback after processing it.
     *
     * @return The next callback in the queue, or null if no callback is waiting.
     */
    public ICallbackMsg getCallback() {
        return getCallback(false);
    }

    /**
     * Gets the next callback object in the queue, and optionally frees it.
     *
     * @param freeLast if set to <b>true</b> this function also frees the last callback if one existed.
     * @return The next callback in the queue, or null if no callback is waiting.
     */
    public ICallbackMsg getCallback(boolean freeLast) {
        synchronized (callbackLock) {
            if (!callbackQueue.isEmpty()) {
                return freeLast ? callbackQueue.poll() : callbackQueue.peek();
            }
        }

        return null;
    }

    /**
     * Blocks the calling thread until a callback object is posted to the queue.
     * This function does not dequeue the callback, you must call FreeLastCallback after processing it.
     *
     * @return The callback object from the queue.
     */
    public ICallbackMsg waitForCallback() {
        return waitForCallback(false);
    }

    /**
     * Blocks the calling thread until a callback object is posted to the queue, or null after the timeout has elapsed.
     * This function does not dequeue the callback, you must call FreeLastCallback after processing it.
     *
     * @param timeout The length of time to block in ms.
     * @return A callback object from the queue if a callback has been posted, or null if the timeout has elapsed.
     */
    public ICallbackMsg waitForCallback(long timeout) {
        synchronized (callbackLock) {
            if (callbackQueue.isEmpty()) {
                try {
                    callbackLock.wait(timeout);
                } catch (final InterruptedException e) {
                    logger.debug(e);
                }

                if (callbackQueue.isEmpty()) {
                    return null;
                }
            }

            return callbackQueue.peek();
        }
    }

    /**
     * Blocks the calling thread until a callback object is posted to the queue, and optionally frees it.
     *
     * @param freeLast if set to <b>true</b> this function also frees the last callback if one existed.
     * @return The callback object from the queue.
     */
    public ICallbackMsg waitForCallback(boolean freeLast) {
        synchronized (callbackLock) {
            if (callbackQueue.isEmpty()) {
                try {
                    callbackLock.wait();
                } catch (final InterruptedException e) {
                    logger.debug(e);
                }

                if (callbackQueue.isEmpty()) {
                    return null;
                }
            }

            return freeLast ? callbackQueue.poll() : callbackQueue.peek();
        }
    }

    /**
     * Blocks the calling thread until a callback object is posted to the queue, and optionally frees it.
     *
     * @param freeLast if set to <b>true</b> this function also frees the last callback if one existed.
     * @param timeout  The length of time to block.
     * @return A callback object from the queue if a callback has been posted, or null if the timeout has elapsed.
     */
    public ICallbackMsg waitForCallback(boolean freeLast, long timeout) {
        synchronized (callbackLock) {
            if (callbackQueue.isEmpty()) {
                try {
                    callbackLock.wait(timeout);
                } catch (final InterruptedException e) {
                    logger.debug(e);
                }
            }

            return freeLast ? callbackQueue.poll() : callbackQueue.peek();
        }
    }

    /**
     * Blocks the calling thread until the queue contains a callback object. Returns all callbacks, and optionally frees them.
     *
     * @param freeLast if set to <b>true</b> this function also frees all callbacks.
     * @param timeout  The length of time to block.
     * @return All current callback objects in the queue.
     */
    public List<ICallbackMsg> getAllCallbacks(boolean freeLast, long timeout) {
        List<ICallbackMsg> callbacks;

        synchronized (callbackLock) {
            if (callbackQueue.isEmpty()) {
                try {
                    callbackLock.wait(timeout);
                } catch (InterruptedException e) {
                    logger.debug(e);
                }

                if (callbackQueue.isEmpty()) {
                    return new ArrayList<>();
                }
            }

            callbacks = new ArrayList<>(callbackQueue);

            if (freeLast) {
                callbackQueue.clear();
            }
        }

        return callbacks;
    }

    /**
     * Frees the last callback in the queue.
     */
    public void freeLastCallback() {
        synchronized (callbackLock) {
            if (callbackQueue.isEmpty()) {
                return;
            }

            callbackQueue.poll();
        }
    }

    /**
     * Posts a callback to the queue. This is normally used directly by client message handlers.
     *
     * @param msg The message.
     */
    public void postCallback(CallbackMsg msg) {
        if (msg == null) {
            return;
        }

        synchronized (callbackLock) {
            callbackQueue.offer(msg);
            callbackLock.notify();
        }
    }

    /**
     * Returns the next available JobID for job based messages.
     *
     * @return The next available JobID.
     */
    public JobID getNextJobID() {
        long sequence = currentJobId.incrementAndGet();

        JobID jobID = new JobID();
        jobID.setBoxID(0L);
        jobID.setProcessID(0L);
        jobID.setSequentialCount(sequence);
        jobID.setStartTime(processStartTime);

        return jobID;
    }

    @Override
    protected boolean onClientMsgReceived(IPacketMsg packetMsg) {
        if (!super.onClientMsgReceived(packetMsg)) {
            return false;
        }

        Consumer<IPacketMsg> dispatcher = dispatchMap.get(packetMsg.getMsgType());
        if (dispatcher != null) {
            // we want to handle some of the clientmsgs before we pass them along to registered handlers
            dispatcher.accept(packetMsg);
        }

        for (Map.Entry<Class<? extends ClientMsgHandler>, ClientMsgHandler> entry : handlers.entrySet()) {
            try {
                entry.getValue().handleMsg(packetMsg);
            } catch (Exception e) {
                logger.debug("Unhandled exception from " + entry.getKey().getName() + " handlers", e);
                SteamClient.this.disconnect();
                return false;
            }
        }

        return true;
    }

    @Override
    protected void onClientConnected() {
        super.onClientConnected();

        postCallback(new ConnectedCallback());
    }

    @Override
    protected void onClientDisconnected(boolean userInitiated) {
        super.onClientDisconnected(userInitiated);

        postCallback(new DisconnectedCallback(userInitiated));
    }

    private void handleCMList(IPacketMsg packetMsg) {
        ClientMsgProtobuf<CMsgClientCMList.Builder> cmMsg = new ClientMsgProtobuf<>(CMsgClientCMList.class, packetMsg);

        postCallback(new CMListCallback(cmMsg.getBody()));
    }

    private void handleJobHeartbeat(IPacketMsg packetMsg) {
        // TODO: 2018-02-23  
    }

    private void handleJobFailed(IPacketMsg packetMsg) {
        // TODO: 2018-02-23
    }
}
