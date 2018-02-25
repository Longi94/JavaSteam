package in.dragonbra.javasteam.util.event;

import java.util.Timer;
import java.util.TimerTask;

/**
 * @author lngtr
 * @since 2018-02-20
 */
public class ScheduledFunction {

    private long delay;

    private Runnable func;

    private Timer timer = new Timer();

    private boolean bStarted = false;

    private TimerTask tick = new TimerTask() {
        @Override
        public void run() {
            if (func != null) {
                func.run();
            }
        }
    };

    public ScheduledFunction(Runnable func, long delay) {
        this.delay = delay;
        this.func = func;
    }

    public void start() {
        if (!bStarted) {
            timer.scheduleAtFixedRate(tick, 0, delay);
            bStarted = true;
        }
    }

    public void stop() {
        if (bStarted) {
            timer.cancel();
            timer.purge();
            bStarted = false;
        }
    }

    public long getDelay() {
        return delay;
    }

    public void setDelay(long delay) {
        this.delay = delay;
    }
}
