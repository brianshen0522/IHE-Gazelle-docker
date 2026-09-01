package net.ihe.gazelle.user.management.commons.application.user.login;

import net.ihe.gazelle.user.management.api.domain.user.Credentials;
import net.ihe.gazelle.user.management.commons.application.exception.GazelleDAOException;

import java.sql.Timestamp;
import java.util.NoSuchElementException;

public interface UserLoginDAO {

    /**
     * Get a user password by its id
     *
     * @param userId the id of the user
     * @return the password
     * @throws GazelleDAOException if the user credentials retrieval failed
     * @throws NoSuchElementException if the user credentials are not found
     */
    Credentials getCredentialsForUserId(String userId);

    /**
     * Update the last login time for a user, throws an exception if the update failed
     *
     * @param userId    the id of the user
     * @param timestamp the timestamp of the last login
     * @throws GazelleDAOException if no update was performed
     */
    void updateLoginMetricsForUserId(String userId, Timestamp timestamp);

    /**
     * Check if the user need to change his password
     *
     * @param userId the id of the user
     * @return true if the user need to change his password, else return false
     * @throws NoSuchElementException if the user credentials are not found
     */
    boolean needToChangePassword(String userId);

}
