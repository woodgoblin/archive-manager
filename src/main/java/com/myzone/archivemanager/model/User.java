package com.myzone.archivemanager.model;

import java.time.Clock;
import java.util.SortedSet;

/**
 * @author myzone
 * @date 9/6/13 10:23 AM
 */
public interface User {

    String getUsername();

    boolean isRightPassword(String password);

    void modifyPassword(AuthorizedSession authorizedSession, String newPassword);

    SortedSet<Session> getSessions();

    ClosableSession startSession(String password) throws SessionStartFailedException;

    interface Session {

        Clock getBegin();

        Clock getEnd();

    }

    interface AuthorizedSession extends Session {

        User getSessionOwner();

    }

    interface ClosableSession extends AuthorizedSession, AutoCloseable {

    }

    class SessionStartFailedException extends Exception {

    }

    class LoginFailedException extends SessionStartFailedException {

    }

    class SessionAlreadyStartedException extends SessionStartFailedException {

    }

}
