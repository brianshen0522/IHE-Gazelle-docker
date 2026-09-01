package net.ihe.gazelle.user.management.api.application.user.login;

import net.ihe.gazelle.user.management.api.application.user.edit.UserEditException;

import java.sql.Timestamp;
import java.util.NoSuchElementException;

/**
 * Component used to perform user log in action
 */
public interface UserLoginService {
    /**
     * Check if the password is valid for a user id
     *
     * @param userId   the id of the user
     * @param password the password to check
     * @return true if the password is valid, else return false
     * @throws IllegalArgumentException if userId or password is null
     * @throws NoSuchElementException if the user credentials are not found
     */
    boolean validatePassword(String userId, String password);

    /**
     * Update the last-login timestamp of a user
     *
     * @param userId    the id of the user
     * @param timestamp the new last-login timestamp
     * @throws IllegalArgumentException if userId or timestamp is null
     * @throws UserEditException        if the update fail
     */
    void updateLastLoginTimestampForUserId(String userId, Timestamp timestamp);

    /**
     * Check if the user need to change his password
     *
     * @param userId the id of the user
     * @return true if the user need to change his password, else return false
     * @throws IllegalArgumentException if userId is null
     */
    boolean needToChangePassword(String userId);
}
