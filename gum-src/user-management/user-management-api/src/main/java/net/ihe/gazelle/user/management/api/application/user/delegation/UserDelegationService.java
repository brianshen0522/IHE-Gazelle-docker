package net.ihe.gazelle.user.management.api.application.user.delegation;

import net.ihe.gazelle.user.management.api.domain.user.DelegatedUser;
import net.ihe.gazelle.user.management.api.domain.user.User;

import java.util.NoSuchElementException;

/**
 * Service for managing delegated users.
 */
public interface UserDelegationService {

    /**
     * Create a delegated user
     *
     * @param user       the user to register
     * @param externalId the external id of the user
     * @param idpId      the idpId of the user
     * @return the registered user
     * @throws IllegalArgumentException if the user or the organization is null
     */
    DelegatedUser createDelegatedUser(User user, String externalId, String idpId);

    /**
     * Transform a local user to a delegated user
     *
     * @param userEmail  the user to register
     * @param externalId the external id of the user
     * @param idpId      the idpId of the user
     * @return the registered user
     * @throws IllegalArgumentException if the user or one of the parameters is null
     */
    DelegatedUser transformUserIntoDelegatedUser(String userEmail, String externalId, String idpId);

    /**
     * Get delegatedUser
     *
     * @param externalId the externalId of the user
     * @param idpId      the idp id of the user
     * @return the delegated user
     * @throws IllegalArgumentException if one of the parameters is null
     * @throws NoSuchElementException   if the user is not found
     */
    DelegatedUser getDelegatedUser(String externalId, String idpId);

    /**
     * Get a delegatedUser from its Gazelle id
     *
     * @param userId the id of the user
     * @return the delegated user
     * @throws IllegalArgumentException if one of the parameters is null
     * @throws NoSuchElementException   if the user is not found
     */
    DelegatedUser getDelegatedUserById(String userId);

    /**
     * Check if a user is delegated using the user id.
     *
     * @param userId the user id
     * @return true if the user is delegated, false otherwise
     */
    boolean isUserDelegatedFromId(String userId);

    /**
     * Check if a user is delegated using the user email.
     *
     * @param email the user email
     * @return true if the user is delegated, false otherwise
     */
    boolean isUserDelegatedFromEmail(String email);

    /**
     * Activate the delegated user
     *
     * @param userId The id of the user to be activated
     */
    void activateDelegatedUser(String userId);

    /**
     * Check if a delegated user exists
     *
     * @param externalId the external id of the user
     * @param idpId      the idpId of the user
     * @return true if it exists, otherwise false
     */
    boolean isDelegatedUserExisting(String externalId, String idpId);
}
