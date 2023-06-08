package io.github.sheikah45.fx2j.compiler.testcontroller;

import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.MapChangeListener;
import javafx.collections.SetChangeListener;
import javafx.css.PseudoClass;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ChangeHandlerController {

    public Button textChangeButton;
    public AnchorPane root;
    public String textValue;
    public final List<String> styleClasses = new ArrayList<>();
    public final Set<PseudoClass> psudeoStyleClasses = new HashSet<>();
    public final Map<Object, Object> properties = new HashMap<>();

    public void onTextChange(ObservableValue<? extends String> observable, String oldValue, String newValue) {
        textValue = newValue;
    }

    public void onStyleClassChange(ListChangeListener.Change<? extends String> change) {
        while (change.next()) {
            styleClasses.addAll(change.getAddedSubList());
            styleClasses.removeAll(change.getRemoved());
        }
    }

    public void onPsuedoStyleClassChange(SetChangeListener.Change<? extends PseudoClass> change) {
        psudeoStyleClasses.add(change.getElementAdded());
        psudeoStyleClasses.remove(change.getElementRemoved());
    }

    public void onPropertiesChange(MapChangeListener.Change<?, ?> change) {
        properties.put(change.getKey(), change.getValueAdded());
        properties.remove(change.getKey(), change.getValueRemoved());
    }
}
