package com.myzone.archivemanager.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.NavigableSet;

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
    NavigableSet<Session> getSessions();

    @NotNull
    CloseableSession startSession(@NotNull String password) throws SessionStartFailedException;

    interface Session {

        @NotNull
        Instant getBegin();

        @NotNull
        Instant getEnd();

    }

    interface AuthorizedSession extends Session {

        @NotNull
        User getOwner();

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
