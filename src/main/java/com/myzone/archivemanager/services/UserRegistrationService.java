package com.myzone.archivemanager.services;

import com.myzone.archivemanager.core.Core;
import com.myzone.archivemanager.core.DataService;
import com.myzone.archivemanager.data.DataAccessor;
import com.myzone.archivemanager.data.InMemoryDataAccessor;
import com.myzone.archivemanager.model.Document;
import com.myzone.archivemanager.model.User;
import org.jetbrains.annotations.NotNull;

import java.util.SortedSet;
import java.util.function.Function;

/**
 * @author myzone
 * @date 9/6/13 10:36 AM
 */
public class UserRegistrationService implements DataService<UserRegistrationService.UserRegistrationRequest, UserRegistrationService.UserRegistrationResponse, Core.DataProvider<User, Core.DataProvider<Document, Core.DataProvider.DataProviderEnd>>> {

    @Override
    public void process(UserRegistrationRequest request, @NotNull Function<? super UserRegistrationResponse, Void> callback, @NotNull Core.ApplicationDataContext<? extends Core.DataProvider<User, Core.DataProvider<Document, Core.DataProvider.DataProviderEnd>>> dataContext) {
        try {
            DataAccessor<User> usersUserDataAccessor = dataContext.getDataProvider().get();
            DataAccessor.Transaction<User> transaction = usersUserDataAccessor.beginTransaction();
            try {
                if (transaction.getAll().filter((user) -> request.getPreferredUsername().equals(user.getUsername())).count() > 0) {
                    throw new Exception();
                }

                User newUser = new SimpleUser(request.getPreferredUsername());
                transaction.save(newUser);
                transaction.commit();

                callback.apply((UserRegistrationResponse) () -> newUser);
            } finally {
                transaction.rollback();
            }
        } catch (Exception e) {
            callback.apply((UserRegistrationResponse) () -> null);
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

        private SimpleUser(String userName) {
            this.userName = userName;
        }

        @Override
        public String getUsername() {
            return transactional(this).userName;
        }

        @Override
        public boolean isRightPassword(String password) {
            throw new UnsupportedOperationException();
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
        public ClosableSession startSession(String password) throws SessionStartFailedException {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            SimpleUser that = (SimpleUser) o;

            if (transactional(this).userName != null ? !transactional(this).userName.equals(that.userName) : that.userName != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return transactional(this).userName != null ? userName.hashCode() : 0;
        }

    }

}
