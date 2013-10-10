package com.myzone.archivemanager.services;

import com.google.common.base.Objects;
import com.myzone.archivemanager.core.Core;
import com.myzone.archivemanager.core.DataService;
import com.myzone.archivemanager.data.DataAccessor;
import com.myzone.archivemanager.data.InMemoryDataAccessor;
import com.myzone.archivemanager.model.Document;
import com.myzone.archivemanager.model.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Clock;
import java.util.SortedSet;
import java.util.function.Consumer;

/**
 * @author myzone
 * @date 9/6/13 10:36 AM
 */
public class UserRegistrationService implements DataService<UserRegistrationService.UserRegistrationRequest, UserRegistrationService.UserRegistrationResponse, Core.DataProvider<User, Core.DataProvider<Document, Core.DataProvider.DataProviderEnd>>> {

    @Override
    public void process(UserRegistrationRequest request, @NotNull Consumer<? super UserRegistrationResponse> callback, @NotNull Core.ApplicationDataContext<? extends Core.DataProvider<User, Core.DataProvider<Document, Core.DataProvider.DataProviderEnd>>> dataContext) {
        try {
            DataAccessor<User> usersUserDataAccessor = dataContext.getDataProvider().get();
            DataAccessor.Transaction<User> transaction = usersUserDataAccessor.beginTransaction();
            try {
                if (transaction.getAll().filter((user) -> request.getPreferredUsername().equals(user.getUsername())).count() > 0) {
                    throw new Exception();
                }

                User newUser = new SimpleUser(request.getPreferredUsername(), request.getPreferredPassword());
                transaction.save(newUser);
                transaction.commit();

                callback.accept((UserRegistrationResponse) () -> newUser);
            } finally {
                transaction.rollback();
            }
        } catch (Exception e) {
            callback.accept((UserRegistrationResponse) () -> null);
        }
    }

    @Override
    public void onLoad(@NotNull Core<?, ? extends Core.DataProvider<User, Core.DataProvider<Document, Core.DataProvider.DataProviderEnd>>> core) {

    }

    @Override
    public void onUnload(@NotNull Core<?, ? extends Core.DataProvider<User, Core.DataProvider<Document, Core.DataProvider.DataProviderEnd>>> core) {

    }

    public static interface UserRegistrationRequest {

        String getPreferredUsername();

        String getPreferredPassword();

    }

    public static interface UserRegistrationResponse {

        User getRegisteredUser();

    }

    private static class SimpleUser extends InMemoryDataAccessor.DataObject implements User {

        private final String userName;
        private final String password;

        private SimpleUser(String userName, String password) {
            this.userName = userName;
            this.password = password;
        }

        @Override
        public String getUsername() {
            return userName;
        }

        @Override
        public boolean isRightPassword(String password) {
            return password.equals(this.password);
        }

        @Override
        public void modifyPassword(AuthorizedSession authorizedSession, String newPassword) {
            throw new UnsupportedOperationException();
        }

        @Override
        public SortedSet<Session> getSessions() {
            throw new UnsupportedOperationException();
        }

        @Override
        public CloseableSession startSession(String password) throws SessionStartFailedException {
            if (!isRightPassword(password)) {
                throw new LoginFailedException();
            }

            return new CloseableSession() {

                @Override
                public void close() {
                }

                @NotNull
                @Override
                public User getOwner() {
                    return SimpleUser.this;
                }

                @NotNull
                @Override
                public Clock getBegin() {
                    throw new UnsupportedOperationException();
                }

                @Nullable
                @Override
                public Clock getEnd() {
                    throw new UnsupportedOperationException();
                }
            };
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            SimpleUser that = (SimpleUser) o;

            if (password != null ? !password.equals(that.password) : that.password != null) return false;
            if (userName != null ? !userName.equals(that.userName) : that.userName != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = userName != null ? userName.hashCode() : 0;
            result = 31 * result + (password != null ? password.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return Objects
                    .toStringHelper(this)
                    .add("userName", userName)
                    .add("password", password)
                    .toString();
        }
    }

}
