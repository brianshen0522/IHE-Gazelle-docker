package net.ihe.gazelle.user.management.api.application.user.edit;


import net.ihe.gazelle.security.business.GazelleIdentity;
import net.ihe.gazelle.user.management.api.domain.user.User;

import java.util.Locale;

/**
 * Service for editing user information.
 */
public interface UserEditService {

    /**
     * Check if a password is enough secure
     *
     * @param password the password to check
     * @param passwordConfirmation the confirmation of the password to check
     * @throws IllegalArgumentException if the password is not valid
     */
    void checkPasswordIsValid(String password, String passwordConfirmation);

    /**
     * Update the password of a user
     *
     * @param userId                  the id of the user
     * @param newPassword             the new password
     * @param newPasswordConfirmation the new password confirmation
     * @throws IllegalArgumentException if userId or newPassword is null
     * @throws UserEditException        if the update fail
     */
    void updatePasswordForUserId(String userId, String newPassword, String newPasswordConfirmation);

    /**
     * Update attributes of a user (firstname, lastname, email)
     *
     * @param  userId                   the id of the user to update attributes
     * @param  providedUserAttributes   the attributes of the user to update
     * @param  identity                 the identity of the user who update attributes
     * @param  locale                   the locale used to send the email
     * @return the updated user
     * @throws IllegalArgumentException if one of the arguments is null
     * @throws UserEditException        if the update fail
     */
    User updateAttributes(String userId, User providedUserAttributes, GazelleIdentity identity, Locale locale);

    /**
     * Activate a Gazelle user
     *
     * @param userId   the id of the user
     * @param identity the identity of the user who deactivate the user
     * @throws IllegalArgumentException if userId is null
     * @throws UserEditException        if the update fail
     */
    void activateUser(String userId, GazelleIdentity identity);

    /**
     * Deactivate a Gazelle user
     *
     * @param userId   the id of the user
     * @param identity the identity of the user who deactivate the user
     * @throws IllegalArgumentException if userId is null
     * @throws UserEditException        if the update fail
     */
    void deactivateUser(String userId, GazelleIdentity identity);

    /**
     * Delete a Gazelle user
     *
     * @param userId   the id of the user
     * @param identity the identity of the user who deactivate the user
     * @param  locale                   the locale used to send the email
     * @throws IllegalArgumentException if userId is null
     * @throws UserEditException        if the operation fail
     */
    void deleteUser(String userId, GazelleIdentity identity, Locale locale);
}
