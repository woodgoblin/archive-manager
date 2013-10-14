package com.myzone.utils.javafx;

import com.sun.javafx.collections.ObservableListWrapper;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.Pane;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static java.util.Collections.unmodifiableList;

/**
 * @author myzone
 * @date 9/21/13 9:13 PM
 */
public class LastStackFramePane extends Pane {

    protected final ObservableList<Node> layers;

    public LastStackFramePane() {
        layers = new ObservableListWrapper<>(new ArrayList<>());

        layers.addListener((ListChangeListener<Node>) change -> {
            while (change.next()) {
                if (change.getTo() >= layers.size()) {
                    switchTo(layers.get(layers.size() - 1));
                }
            }
        });
    }

    private void switchTo(@NotNull Node node) {
        super.getChildren().clear();
        super.getChildren().add(node);
    }

    @Override
    public ObservableList<Node> getChildren() {
        return layers;
    }

    @Override
    public ObservableList<Node> getChildrenUnmodifiable() {
        return new ObservableListWrapper<>(unmodifiableList(layers));
    }
}
