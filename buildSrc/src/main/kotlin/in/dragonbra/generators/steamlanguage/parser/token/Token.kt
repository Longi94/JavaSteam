package `in`.dragonbra.generators.steamlanguage.parser.token

data class Token(
    val name: String?,
    val value: String,
    val source: TokenSourceInfo? = null
)
