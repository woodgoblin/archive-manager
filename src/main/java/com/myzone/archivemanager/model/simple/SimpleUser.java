package com.myzone.archivemanager.model.simple;

import com.google.common.base.Objects;
import com.myzone.archivemanager.model.User;
import org.jetbrains.annotations.NotNull;

import java.time.Clock;
import java.time.Instant;
import java.util.Collections;
import java.util.NavigableSet;

/**
 * @author myzone
 * @date 10/11/13 3:36 AM
 */
public class SimpleUser implements User {

    private final String username;
    private volatile String password;

    public SimpleUser(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @NotNull
    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isRightPassword(@NotNull String password) {
        return password.equals(this.password);
    }

    @Override
    public void modifyPassword(@NotNull AuthorizedSession authorizedSession, @NotNull String newPassword) {
        if (equals(authorizedSession.getOwner()))  {
            password = newPassword;
        }
    }

    @NotNull
    @Override
    public NavigableSet<Session> getSessions() {
        return Collections.emptyNavigableSet(); // temporary @fixme
    }

    @NotNull
    @Override
    public CloseableSession startSession(@NotNull String password) throws SessionStartFailedException {
        return new CloseableSession() {

            private final Instant startInstant = Clock.systemDefaultZone().instant();
            private volatile Instant endInstant = Instant.MAX;

            @Override
            public void close() {
                endInstant = Clock.systemDefaultZone().instant();
            }

            @NotNull
            @Override
            public User getOwner() {
                return SimpleUser.this;
            }

            @NotNull
            @Override
            public Instant getBegin() {
                return startInstant;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @NotNull
            @Override
            public Instant getEnd() {
                return endInstant;
            }

        };

    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("username", username)
                .add("password", password)
                .toString();
    }

}
