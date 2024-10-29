package `in`.dragonbra.javasteam.util.lzma.compress.lzma

class State {
    var index: Int = 0

    fun init() {
        index = 0
    }

    fun updateChar() {
        when {
            index < 4 -> index = 0
            index < 10 -> index -= 3
            else -> index -= 6
        }
    }

    fun updateMatch() { index = if (index < 7) 7 else 10 }

    fun updateRep() { index = if (index < 7) 8 else 11 }

    fun updateShortRep() { index = if (index < 7) 9 else 11 }

    fun isCharState(): Boolean = index < 7
}
