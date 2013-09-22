package com.myzone.archivemanager.model;

import org.jetbrains.annotations.NotNull;

/**
 * @author myzone
 * @date 9/18/13 1:32 AM
 */
public interface NamedDomain extends Domain {

    @NotNull
    String getName();

    @NotNull
    String getDescription();

    void modifyName(@NotNull User.AuthorizedSession session, @NotNull String name);

    void modifyDescription(@NotNull User.AuthorizedSession session, @NotNull String description);

}
