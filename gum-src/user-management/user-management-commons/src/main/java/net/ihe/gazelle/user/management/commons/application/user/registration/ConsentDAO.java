package net.ihe.gazelle.user.management.commons.application.user.registration;

import net.ihe.gazelle.user.management.commons.application.exception.GazelleDAOException;

/**
 * DAO interface for managing user consent.
 */
public interface ConsentDAO {

    /**
     * Accept the consent of a user
     * @param userId the id of the user
     * @throws GazelleDAOException if no update was performed
     */
    void acceptUserConsent(String userId);


    /**
     * Check if a user has given his consent
     * @param userId the id of the user
     * @return true if the user has given his consent, false otherwise
     */
    boolean needToGiveConsent(String userId);
}
