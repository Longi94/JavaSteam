package `in`.dragonbra.javasteam.steam.handlers.steamapps

data class AppProcessInfo(
    val processId: Int,
    val processIdParent: Int,
    val parentIsSteam: Boolean,
)
