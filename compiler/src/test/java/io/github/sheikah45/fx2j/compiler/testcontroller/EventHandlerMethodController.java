package io.github.sheikah45.fx2j.compiler.testcontroller;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.input.ContextMenuEvent;

public class EventHandlerMethodController {

    public Button withEventButton;
    public Button withoutEventButton;
    public Button contextButton;

    public int eventActionCount = 0;
    public int noEventActionCount = 0;
    public int contextRequestCount = 0;

    public void onActionWithEvent(ActionEvent event) {
        eventActionCount++;
    }

    public void onActionWithoutEvent() {
        noEventActionCount++;
    }

    public void onContextMenuRequested(ContextMenuEvent contextMenuEvent) {
        contextRequestCount++;
    }
}
