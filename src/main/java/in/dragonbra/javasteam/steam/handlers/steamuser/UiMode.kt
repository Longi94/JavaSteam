package `in`.dragonbra.javasteam.steam.handlers.steamuser

/**
 * Represents the ui mode for logging into Steam.
 */
@Suppress("unused")
enum class UiMode(val mode: Int) {
    /**
     * The default ui mode
     */
    DEFAULT(0),

    /**
     * Big Picture ui mode.
     */
    BIG_PICTURE(1),

    /**
     * Mobile (phone) ui mode.
     */
    MOBILE(2),
}
