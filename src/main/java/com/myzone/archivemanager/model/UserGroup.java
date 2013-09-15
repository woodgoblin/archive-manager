package com.myzone.archivemanager.model;

import java.util.Set;

/**
 * @author myzone
 * @date 9/15/13 1:45 PM
 */
public interface UserGroup {

    Set<User> getUsers();

    void rank(User modifier, User targetUser);

    void disrank(User modifier, User targetUser);

}
