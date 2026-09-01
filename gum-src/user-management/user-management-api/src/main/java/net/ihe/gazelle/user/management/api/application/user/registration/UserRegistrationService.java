package net.ihe.gazelle.user.management.api.application.user.registration;

import net.ihe.gazelle.security.business.GazelleIdentity;
import net.ihe.gazelle.user.management.api.application.user.edit.UserEditException;
import net.ihe.gazelle.user.management.api.domain.organization.Organization;
import net.ihe.gazelle.user.management.api.domain.user.User;

import java.util.Locale;

/**
 * Service interface for user registration operations in Gazelle User Management.
 * <p>
 * This interface defines methods for registering users, activating accounts, and managing registration-related logic.
 * </p>
 */
public interface UserRegistrationService {

    /**
     * Register a user and an organization
     *
     * @param user                 the user to register
     * @param organization                the organization to add the user to
     * @param consent              the consent of the user
     * @param password             the password of the user
     * @param passwordConfirmation the confirmation of the password
     * @param locale               the locale used to send emails
     * @return the registered user
     * @throws IllegalArgumentException if the user or the organization is null
     */
    User registerUserWithNewOrganization(User user, Organization organization, boolean consent,
                                         String password, String passwordConfirmation, Locale locale);

    /**
     * Register a user
     *
     * @param user                 the user to register
     * @param consent              the consent of the user
     * @param password             the password of the user
     * @param passwordConfirmation the confirmation of the password
     * @param locale               the locale used to send emails
     * @return the registered user
     * @throws IllegalArgumentException if one of the parameters is illegal
     */
    User registerUser(User user, boolean consent, String password, String passwordConfirmation, Locale locale);

    /**
     * Create a user by joining an existing organization
     *
     * @param user                 the user to create
     * @param identity             the identity of the caller
     * @param locale               the locale used to send emails
     * @return the created user
     * @throws IllegalArgumentException if one of the parameters is illegal
     */
    User createUser(User user, GazelleIdentity identity, Locale locale);

    /**
     * Create a user with a new organization, user is automatically organization admin of this organization
     *
     * @param user                 the user to create
     * @param organization         the organization to add the user to
     * @param identity             the identity of the caller
     * @param locale               the locale used to send emails
     * @return the created user
     * @throws IllegalArgumentException if one of the parameters is illegal
     */
    User createUserWithNewOrganization(User user, Organization organization, GazelleIdentity identity, Locale locale);

    /**
     * Activate a user with an activation code
     *
     * @param activationCode    the activation code given for the activation
     * @throws IllegalArgumentException if activationCode is null
     * @throws UserEditException        if the update fail
     * @return the activated user
     */
    User activateUserWithActivationCode(String activationCode);
}
