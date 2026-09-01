package net.ihe.gazelle.user.management.commons.application.user.delegation;

import net.ihe.gazelle.user.management.api.domain.user.DelegatedUser;
import net.ihe.gazelle.user.management.api.domain.user.User;
import net.ihe.gazelle.user.management.commons.application.exception.GazelleDAOException;

import java.util.NoSuchElementException;

public interface UserDelegationDAO {

    /**
     * Retrieve the corresponding delegated user
     * @param externalId the external id of the delegated user
     * @param idpId the idpId of the delegated user
     * @return the user corresponding to the delegated user, else null
     * @throws GazelleDAOException if the retrieval failed
     * @throws NoSuchElementException if the delegated user is not found
     */
    DelegatedUser getDelegatedUser(String externalId, String idpId);

    /**
     * Retrieve the corresponding delegated user by its Gazelle id
     * @param userId the external id of the delegated user
     * @return the user corresponding to the delegated user, else null
     * @throws GazelleDAOException if the retrieval failed
     * @throws NoSuchElementException if the delegated user is not found
     */
    DelegatedUser getDelegatedUserById(String userId);

    /**
     * Create a delegated user
     *
     * @param user the user to register
     * @param externalId the external id of the user
     * @param idpId the idpId of the user
     * @throws GazelleDAOException if registration failed
     */
    DelegatedUser createDelegatedUser(User user, String externalId, String idpId);

    /**
     * Transform a local user to a delegated user
     *
     * @param userEmail                 the user to register
     * @param externalId           the external id of the user
     * @param idpId                  the idpId of the user
     * @return the registered user
     * @throws IllegalArgumentException if the user or one of the parameters is null
     */
    DelegatedUser transformUserIntoDelegatedUser(String userEmail, String externalId, String idpId);

    /**
     * Check if a delegated user exists
     * @param externalId the external id of the user
     * @param idpId the idpId of the user
     * @return true if it exists, otherwise false
     */
    boolean isDelegatedUserExisting(String externalId, String idpId);
}
