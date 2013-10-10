package com.myzone.archivemanager.services;

import com.myzone.archivemanager.core.Core;
import com.myzone.archivemanager.core.DataService;
import com.myzone.archivemanager.data.DataAccessor;
import com.myzone.archivemanager.model.Document;
import com.myzone.archivemanager.model.User;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Consumer;

import static java.util.Optional.empty;

/**
 * @author myzone
 * @date 9/6/13 10:36 AM
 */
public class UserAuthorizationService implements DataService<UserAuthorizationService.UserAuthorizationRequest, UserAuthorizationService.UserAuthorizationResponse, Core.DataProvider<User, Core.DataProvider<Document, Core.DataProvider.DataProviderEnd>>> {

    @Override
    public void process(UserAuthorizationRequest request, @NotNull Consumer<? super UserAuthorizationResponse> callback, @NotNull Core.ApplicationDataContext<? extends Core.DataProvider<User, Core.DataProvider<Document, Core.DataProvider.DataProviderEnd>>> dataContext) {
        try {
            DataAccessor<User> usersUserDataAccessor = dataContext.getDataProvider().get();
            DataAccessor.Transaction<User> transaction = usersUserDataAccessor.beginTransaction();
            Optional<User> currentUser = transaction
                    .getAll()
                    .filter((user) -> user.getUsername().equals(request.getUsername()))
                    .findFirst();

            if (currentUser.isPresent()) {
                User.CloseableSession closeableSession = currentUser.get().startSession(request.getPassword());

                callback.accept(new UserAuthorizationResponse() {

                    @Override
                    public Optional<User.CloseableSession> getSession() {
                        return Optional.of(closeableSession);
                    }

                    @Override
                    public Optional<User.SessionStartFailedException> getException() {
                        return empty();
                    }

                });
            } else {
                throw new User.LoginFailedException();
            }
            transaction.rollback();
        } catch (User.SessionStartFailedException e) {
            callback.accept(new UserAuthorizationResponse() {

                @Override
                public Optional<User.CloseableSession> getSession() {
                    return empty();
                }

                @Override
                public Optional<User.SessionStartFailedException> getException() {
                    return Optional.of(e);
                }

            });
        }
    }

    @Override
    public void onLoad(@NotNull Core<?, ? extends Core.DataProvider<User, Core.DataProvider<Document, Core.DataProvider.DataProviderEnd>>> core) {

    }

    @Override
    public void onUnload(@NotNull Core<?, ? extends Core.DataProvider<User, Core.DataProvider<Document, Core.DataProvider.DataProviderEnd>>> core) {

    }

    public static interface UserAuthorizationRequest {

        String getUsername();

        String getPassword();

    }

    public static interface UserAuthorizationResponse {

        Optional<User.CloseableSession> getSession();

        Optional<User.SessionStartFailedException> getException();

    }

}
