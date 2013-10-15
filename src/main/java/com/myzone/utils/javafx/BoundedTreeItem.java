package com.myzone.utils.javafx;

import javafx.beans.InvalidationListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TreeItem;

/**
 * @author myzone
 * @date 10/15/13 2:10 AM
 */
public class BoundedTreeItem<T> extends TreeItem<T> {

    public BoundedTreeItem(ObservableValue<T> observableValue) {
        super(observableValue.getValue());

        observableValue.addListener((InvalidationListener) observable -> {
            setValue(observableValue.getValue());
        });
    }

}
