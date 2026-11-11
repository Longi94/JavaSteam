package `in`.dragonbra.javasteam.util.event

import java.util.concurrent.CopyOnWriteArrayList

class Event<T : EventArgs> {
    private val handlers = CopyOnWriteArrayList<EventHandler<T>>()

    fun addEventHandler(handler: EventHandler<T>) {
        handlers.add(handler)
    }

    fun removeEventHandler(handler: EventHandler<T>) {
        handlers.remove(handler)
    }

    operator fun plusAssign(handler: EventHandler<T>) = addEventHandler(handler)

    operator fun minusAssign(handler: EventHandler<T>) = removeEventHandler(handler)

    operator fun invoke(sender: Any, e: T) = handleEvent(sender, e)

    fun handleEvent(sender: Any, e: T) {
        handlers.forEach { it.handleEvent(sender, e) }
    }
}
