package com.myzone.archive.services;

import com.myzone.archive.core.Core;
import com.myzone.archive.core.Service;
import com.myzone.archive.data.DataAccessor;
import com.myzone.archive.model.User;

/**
 * @author myzone
 * @dat 9/6/13 10:36 AM
 */
public class UserRegistrationService implements Service<UserRegistrationService.UserRegistrationRequest, UserRegistrationService.UserRegistrationResponse> {

    @Override
    public UserRegistrationResponse process(UserRegistrationRequest request, Core.ApplicationDataContext dataContext) {
        try {
            DataAccessor.Transaction<User> transaction = dataContext.getUserDataAccessor().beginTransaction();
            try {
                if (transaction.getAll().filter((user) -> request.getPreferredUsername().equals(user.getUsername())).count() > 0)
                    throw new Exception();

                User newUser = new User(request.getPreferredUsername(), request.getPreferredPassword());
                transaction.save(newUser);
                transaction.commit();

                return () -> newUser;
            } finally {
                transaction.rollback();
            }
        } catch (Exception e) {
            return () -> null;
        }
    }

    public static interface UserRegistrationRequest {

        String getPreferredUsername();

        String getPreferredPassword();

    }

    public static interface UserRegistrationResponse {

        User getRegisteredUser();

    }

}
