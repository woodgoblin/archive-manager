package com.myzone.archivemanager.services;

import com.myzone.archivemanager.core.Core;
import com.myzone.archivemanager.core.DataService;
import com.myzone.archivemanager.data.DataAccessor;
import com.myzone.archivemanager.model.Document;
import com.myzone.archivemanager.model.User;
import com.myzone.archivemanager.model.simple.SimpleUser;
import org.jetbrains.annotations.NotNull;

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

}
