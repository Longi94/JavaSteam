package in.dragonbra.javasteam.util.event;

import java.util.HashSet;

public class Event<T extends EventArgs> {
    protected final HashSet<EventHandler<T>> handlers = new HashSet<>();

    public void addEventHandler(EventHandler<T> handler) {
        synchronized (handlers) {
            handlers.add(handler);
        }
    }

    public void removeEventHandler(EventHandler<T> handler) {
        synchronized (handlers) {
            handlers.remove(handler);
        }
    }

    public void handleEvent(Object sender, T e) {
        synchronized (handlers) {
            for (final EventHandler<T> handler : handlers) {
                handler.handleEvent(sender, e);
            }
        }
    }
}