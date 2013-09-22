package com.myzone.archivemanager.model;

import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * @author myzone
 * @date 9/18/13 12:05 AM
 */
public interface Domain {

    @NotNull
    Set<Document<?>> getDocuments();

}
