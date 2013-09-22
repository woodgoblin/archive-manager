package com.myzone.archivemanager.model;

import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * @author myzone
 * @date 9/15/13 1:45 PM
 */
public interface UserGroup {

    void rank(@NotNull User.AuthorizedSession session, @NotNull User targetUser);

    void disrank(@NotNull User.AuthorizedSession session, @NotNull User targetUser);

    @NotNull
    Set<User> getUsers();

}
