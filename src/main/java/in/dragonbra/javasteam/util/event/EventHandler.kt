package in.dragonbra.javasteam.util.event;

public interface EventHandler<T extends EventArgs> {
    void handleEvent(Object sender, T e);
}
