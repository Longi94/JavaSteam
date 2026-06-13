package `in`.dragonbra.javasteam.util.event

import java.util.Timer
import java.util.TimerTask

class ScheduledFunction(private val func: Runnable, var delay: Long) {
    private var timer: Timer? = null
    private var started = false

    @Synchronized
    fun start() {
        if (!started) {
            timer = Timer().also {
                it.scheduleAtFixedRate(object : TimerTask() {
                    override fun run() = func.run()
                }, 0L, delay)
            }
            started = true
        }
    }

    @Synchronized
    fun stop() {
        if (started) {
            timer?.cancel()
            timer = null
            started = false
        }
    }
}