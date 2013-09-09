package com.myzone.archivemanager.services;

import com.myzone.archivemanager.core.Core;
import com.myzone.archivemanager.core.DataService;
import com.myzone.archivemanager.data.DataAccessor;
import com.myzone.archivemanager.model.Document;
import com.myzone.archivemanager.model.User;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

/**
 * @author myzone
 * @date 9/6/13 10:36 AM
 */
public class UserRegistrationService implements DataService<UserRegistrationService.UserRegistrationRequest, UserRegistrationService.UserRegistrationResponse, Core.Type<User, Core.Type<Document, Core.Type.End>>> {

    @Override
    public void process(UserRegistrationRequest request, @NotNull Function<? super UserRegistrationResponse, Void> callback, @NotNull Core.ApplicationDataContext<? extends Core.Type<User, Core.Type<Document, Core.Type.End>>> dataContext) {
        try {
            DataAccessor<User> usersUserDataAccessor = dataContext.getDataAccessors().get();
            DataAccessor.Transaction<User> transaction = usersUserDataAccessor.beginTransaction();
            try {
                if (transaction.getAll().filter((user) -> request.getPreferredUsername().equals(user.getUsername())).count() > 0)
                    throw new Exception();

                User newUser = new User(request.getPreferredUsername(), request.getPreferredPassword());
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
    public void onLoad(@NotNull Core<?, ? extends Core.Type<User, Core.Type<Document, Core.Type.End>>> core) {

    }

    @Override
    public void onUnload(@NotNull Core<?, ? extends Core.Type<User, Core.Type<Document, Core.Type.End>>> core) {

    }

    public static interface UserRegistrationRequest {

        String getPreferredUsername();

        String getPreferredPassword();

    }

    public static interface UserRegistrationResponse {

        User getRegisteredUser();

    }

}
