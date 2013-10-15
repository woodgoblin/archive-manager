package com.myzone.archivemanager.model;

import javafx.collections.ObservableSet;
import org.jetbrains.annotations.NotNull;

/**
 * @author myzone
 * @date 9/15/13 1:45 PM
 */
public interface UserGroup {

    void rank(@NotNull User.AuthorizedSession session, @NotNull User targetUser) throws NoAccessExecption;

    void disrank(@NotNull User.AuthorizedSession session, @NotNull User targetUser) throws NoAccessExecption;

    @NotNull
    ObservableSet<User> getUsers();

}
