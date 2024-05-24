package io.github.sheikah45.fx2j.processor.testcontroller;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.input.ContextMenuEvent;

import java.io.FileNotFoundException;
import java.io.IOException;

public class EventHandlerMethodController {

    public Button throwingNoEventButton;
    public Button throwingEventButton;
    public Button withEventButton;
    public Button withoutEventButton;
    public Button contextButton;

    public int eventActionCount = 0;
    public int noEventActionCount = 0;
    public int throwsEventCount = 0;
    public int throwsNoEventCount = 0;
    public int contextRequestCount = 0;

    public void onActionWithEventThrow(ActionEvent event) throws IOException, FileNotFoundException {
        throwsEventCount++;
    }

    public void onActionWithNoEventThrow() throws Exception {
        throwsNoEventCount++;
    }

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
