package in.dragonbra.javasteam.util.event;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class EventTest {

    static class TestEventHandler implements EventHandler<EventArgs> {
        boolean eventHandled = false;
        Object sender = null;
        EventArgs eventArgs = null;

        @Override
        public void handleEvent(Object sender, EventArgs e) {
            this.eventHandled = true;
            this.sender = sender;
            this.eventArgs = e;
        }
    }

    @Test
    void addEventHandlerAndHandleEvent() {
        var event = new Event<>();
        var handler = new TestEventHandler();

        event.addEventHandler(handler);

        event.handleEvent(this, EventArgs.EMPTY);

        Assertions.assertTrue(handler.eventHandled);
        Assertions.assertEquals(this, handler.sender);
        Assertions.assertEquals(EventArgs.EMPTY, handler.eventArgs);
    }

    @Test
    void removeEventHandler() {
        var event = new Event<>();
        var handler = new TestEventHandler();

        event.addEventHandler(handler);
        event.removeEventHandler(handler);

        event.handleEvent(this, EventArgs.EMPTY);

        Assertions.assertFalse(handler.eventHandled);
    }
}
