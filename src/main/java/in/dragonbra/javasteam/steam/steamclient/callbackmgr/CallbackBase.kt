package in.dragonbra.javasteam.steam.steamclient.callbackmgr;

/**
 * This is the base class for the utility {@link Callback} class.
 * This is for internal use only, and shouldn't be used directly.
 */
abstract class CallbackBase {
    abstract Class<?> getCallbackType();

    abstract void run(Object callback);
}
