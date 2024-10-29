package `in`.dragonbra.javasteam.util.lzma.compress.lzma

class Optimal {
    var state: State = State()

    var prev1IsChar: Boolean = false
    var prev2: Boolean = false

    var posPrev2: Int = 0
    var backPrev2: Int = 0

    var price: Int = 0
    var posPrev: Int = 0
    var backPrev: Int = 0

    var backs0: Int = 0
    var backs1: Int = 0
    var backs2: Int = 0
    var backs3: Int = 0

    fun makeAsChar() {
        backPrev = -1 // 0xFFFFFFFF in Kotlin Int
        prev1IsChar = false
    }

    fun makeAsShortRep() {
        backPrev = 0
        prev1IsChar = false
    }

    fun isShortRep(): Boolean = backPrev == 0
}
