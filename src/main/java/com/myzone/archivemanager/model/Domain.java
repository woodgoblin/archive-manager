package com.myzone.archivemanager.model;

import javafx.collections.ObservableSet;
import org.jetbrains.annotations.NotNull;

/**
 * @author myzone
 * @date 9/18/13 12:05 AM
 */
public interface Domain {

    @NotNull
    ObservableSet<Document<?>> getDocuments();

}
