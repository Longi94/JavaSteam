package `in`.dragonbra.javasteam.steam.handlers.steamuser

/**
 * Represents the chat mode for logging into Steam.
 */
@Suppress("unused")
enum class ChatMode(val mode: Int) {
    /**
     * The default chat mode.
     */
    DEFAULT(0),

    /**
     * The chat mode for new Steam group chat.
     */
    NEW_STEAM_CHAT(1),
}
