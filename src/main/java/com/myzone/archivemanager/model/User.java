package com.myzone.archivemanager.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Clock;
import java.util.SortedSet;

/**
 * @author myzone
 * @date 9/6/13 10:23 AM
 */
public interface User {

    @NotNull
    String getUsername();

    boolean isRightPassword(@NotNull String password);

    void modifyPassword(@NotNull AuthorizedSession authorizedSession, @NotNull String newPassword);

    @NotNull
    SortedSet<Session> getSessions();

    @NotNull
    CloseableSession startSession(@NotNull String password) throws SessionStartFailedException;

    interface Session {

        @NotNull
        Clock getBegin();

        @Nullable
        Clock getEnd();

    }

    interface AuthorizedSession extends Session {

        @NotNull
        User getSessionOwner();

    }

    interface CloseableSession extends AuthorizedSession, AutoCloseable {

        @Override
        void close();

    }

    class SessionStartFailedException extends Exception {

    }

    class LoginFailedException extends SessionStartFailedException {

    }

    class SessionAlreadyStartedException extends SessionStartFailedException {

    }

}
