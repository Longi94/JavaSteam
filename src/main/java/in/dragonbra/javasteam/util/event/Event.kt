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

    fun handleEvent(sender: Any, e: T) {
        handlers.forEach { it.handleEvent(sender, e) }
    }
}