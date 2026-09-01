package net.ihe.gazelle.user.management.commons.application.user.edit;

import net.ihe.gazelle.user.management.api.domain.user.Credentials;
import net.ihe.gazelle.user.management.api.domain.user.User;
import net.ihe.gazelle.user.management.api.application.ConflictException;
import net.ihe.gazelle.user.management.commons.application.exception.GazelleDAOException;

import java.util.NoSuchElementException;

public interface UserEditDAO {

    /**
     * Update the credentials of a user, throws an exception if the update failed
     *
     * @param userId           the id of the user
     * @param inputCredentials the future credentials of the user
     * @throws NoSuchElementException if the user was not found
     * @throws GazelleDAOException    if no update was performed
     */
    void updateCredentialsForUserId(String userId, Credentials inputCredentials);

    /**
     * Update the attributes of a user (firstname, lastname, email)
     *
     * @param userId the id of the user to update
     * @param user   the user attributes to update
     * @throws NoSuchElementException if the user was not found
     * @throws ConflictException      if the email already exist
     * @throws GazelleDAOException    if update error occurred
     */
    User updateAttributes(String userId, User user);

    /**
     * Update the attributes of a user (firstname, lastname, email)
     *
     * @param userId the id of the user
     * @param orgaId the id of the organization
     * @throws NoSuchElementException if the user was not found
     * @throws GazelleDAOException    if update error occurred
     */
    void updateUserOrganization(String userId, String orgaId);

    /**
     * Update the activation status of a user
     *
     * @param userId   the id of the user
     * @param activate the new activation status
     * @throws NoSuchElementException if the user was not found
     * @throws GazelleDAOException    if update error occurred
     */
    void updateActivatedStatusOfUser(String userId, Boolean activate);

    /**
     * Clear the activation code of a user
     *
     * @param userId the id of the user
     */
    void clearActivationCode(String userId);

    /**
     * Delete the user
     *
     * @param userId the id of the user to delete
     * @throws NoSuchElementException if the user was not found
     * @throws GazelleDAOException    if update error occurred
     */
    void deleteUser(String userId);

    /**
     * Archive the organization if it has no members
     * @param orgaId the id of the organization
     */
    void archiveOrgaIfNoMembers(String orgaId);

    /**
     * Get the user from the user id (used for authz)
     *
     * @param userId the id of the user
     * @return the organizationId
     */
    User getUserFromUserId(String userId);
}
