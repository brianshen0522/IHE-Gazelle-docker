package net.ihe.gazelle.user.management.commons.application.user.lookup;

import net.ihe.gazelle.user.management.api.application.user.lookup.SortOrder;
import net.ihe.gazelle.user.management.api.application.user.lookup.UserQueryParams;
import net.ihe.gazelle.user.management.api.domain.user.User;
import net.ihe.gazelle.user.management.commons.application.exception.GazelleDAOException;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

public interface UserLookupDAO {

    /**
     * Retrieve the activation code for a user
     *
     * @param userId the id of the user
     * @return the activation code
     * @throws java.util.NoSuchElementException if no activation code is found
     */
    String getActivationCodeForUserId(String userId);

    /**
     * Search for users
     *
     * @param userQueryParam   the search pattern
     * @param offset          the offset of the first result (if null, start at the beginning)
     * @param limit the number of results to return (if null, return all results)
     * @return the list of users matching the search pattern
     * @throws GazelleDAOException if the search failed
     */
    List<User> searchForUsers(UserQueryParams userQueryParam, Integer offset, Integer limit, String sortBy, SortOrder sortOrder);

    /**
     * Search for users summary
     *
     * @param userQueryParams   the search pattern
     * @param offset          the offset of the first result (if null, start at the beginning)
     * @param limit the number of results to return (if null, return all results)
     * @return the list of users summary matching the search pattern
     * @throws GazelleDAOException if the search failed
     */
    List<User> searchForUsersSummary(UserQueryParams userQueryParams, Integer offset, Integer limit, String sortBy, SortOrder sortOrder);

    /**
     * Count the number of users matching the search pattern
     * @param query the search pattern
     * @return the number of users matching the search pattern
     */
    Long countFilteredUsers(UserQueryParams query);

    /**
     * Get a user by his id
     *
     * @param userId the id of the user
     * @return the user
     * @throws GazelleDAOException if the retrieval failed
     * @throws NoSuchElementException if the user is not found
     */
    User getUserById(String userId);

    /**
     * Get a user summary by his id
     *
     * @param userId the id of the user
     * @return the user summary
     * @throws GazelleDAOException if the retrieval failed
     * @throws NoSuchElementException if the user is not found
     */
    User getUserSummaryById(String userId);


    /**
     * Get a user by his email
     *
     * @param email the email of the user
     * @return the user
     * @throws GazelleDAOException if the retrieval failed
     * @throws NoSuchElementException if the user is not found
     */
    User getUserByEmail(String email);

    /**
     * Get a User by its activation code
     * @param activationCode activation code of the User
     * @return the found User
     * @throws GazelleDAOException if the retrieval failed
     */
    User getUserByActivationCode(String activationCode);

    /**
     * @param propertyName The name of the property to count
     * @param userQueryParams the User parameters to match with
     * @return the map of possible values user count.
     */
    Map<String, Long> getValueCount(String propertyName, UserQueryParams userQueryParams);

}
