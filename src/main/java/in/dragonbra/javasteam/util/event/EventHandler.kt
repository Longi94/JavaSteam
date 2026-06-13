package `in`.dragonbra.javasteam.util.event

fun interface EventHandler<T : EventArgs> {
    fun handleEvent(sender: Any, e: T)
}
