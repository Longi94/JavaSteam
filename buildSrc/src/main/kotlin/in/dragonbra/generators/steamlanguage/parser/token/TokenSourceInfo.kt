package `in`.dragonbra.generators.steamlanguage.parser.token

data class TokenSourceInfo(
    val fileName: String,
    val startLineNumber: Int,
    val startColumnNumber: Int,
    val endLineNumber: Int,
    val endColumnNumber: Int
)
