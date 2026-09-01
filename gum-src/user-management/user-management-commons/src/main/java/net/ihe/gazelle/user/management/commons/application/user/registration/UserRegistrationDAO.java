package net.ihe.gazelle.user.management.commons.application.user.registration;

import net.ihe.gazelle.user.management.api.domain.user.User;
import net.ihe.gazelle.user.management.commons.application.exception.GazelleDAOException;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * Component used to register a user
 */
public interface UserRegistrationDAO {

    /**
     * Register a user
     *
     * @param user the user to register
     * @return the registered user
     * @throws GazelleDAOException if registration failed
     */
    User registerUser(User user);

    /**
     *
     * @param activationCode the activation code given for the activation
     * @throws GazelleDAOException if no activation was performed
     * @return the activated user
     */
    User activateUserWithActivationCode(String activationCode);

    /**
     * Check if an email is already taken by a user
     *
     * @param email the email to check
     * @return true if the email is already registered, else false
     */
    boolean isEmailAlreadyExist(String email);

    /**
     * Get all active admins of an organization
     *
     * @param organizationId         the id of the organization
     * @return the list of active users in the organization
     * @throws GazelleDAOException if the retrieval failed
     * @throws NoSuchElementException if the organization is not found
     */
    List<User> getActiveAdminsOfOrganization(String organizationId);

    /**
     * Count the number of users
     * @return the number of users
     */
    int getAllUsersCount();

    /**
     * Rollback the registration of a user (delete the user and its consent)
     * @param userId the id of the user
     */
    void rollbackUserRegistration(String userId);
}
