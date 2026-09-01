package net.ihe.gazelle.user.management.api.application.user.lookup;

import net.ihe.gazelle.security.business.GazelleIdentity;
import net.ihe.gazelle.security.business.UnauthorizedException;
import net.ihe.gazelle.user.management.api.domain.user.User;

import java.util.List;
import java.util.Map;

/**
 * Component for looking up for users
 */
public interface UserLookupService {

    /**
     * Retrieve users corresponding to a search pattern, then filter them with the UserResource filter
     * It will return all Users that matches the search and/or the filter parameters
     * @param query the User parameters to match with
     * @param offset the offset of the result
     * @param limit the number of results
     * @param identity the identity of the user who uses the method
     * @return the list of users corresponding to the search parameter and the filter
     * @throws IllegalArgumentException if the search, offset param is null
     * @throws UnauthorizedException if the user is not authorized to perform this action
     */
    List<User> searchAndFilterUsers(UserQueryParams query, Integer offset, Integer limit, GazelleIdentity identity);

    /**
     * Retrieve users corresponding to a search pattern, then filter them with the UserResource filter
     * It will return all Users that matches the search and/or the filter parameters
     * @param query the User parameters to match with
     * @param offset the offset of the result
     * @param limit the number of results
     * @param sortBy the sorting field
     * @param sortOrder the sorting order
     * @param identity the identity of the user who uses the method
     * @return the list of users corresponding to the search parameter and the filter
     * @throws IllegalArgumentException if the search, offset param is null
     * @throws UnauthorizedException if the user is not authorized to perform this action
     */
    UserSearchResult searchAndFilterUsersWithCount(UserQueryParams query, Integer offset, Integer limit, String sortBy, SortOrder sortOrder,
                                                   GazelleIdentity identity);

    /**
     * Retrieve users corresponding to a search pattern, then filter them with the UserResource filter
     * It will return all Users that matches the search and/or the filter parameters
     * The information returned for the users is limited. Only id, firstname, lastname, and organization is returned
     * @param query the User parameters to match with
     * @param offset the offset of the result
     * @param limit the number of results
     * @param sortBy the sorting field
     * @param sortOrder the sorting order
     * @param identity the identity of the user who uses the method
     * @return the list of users corresponding to the search parameter and the filter
     * @throws IllegalArgumentException if the search, offset param is null
     * @throws UnauthorizedException if the user is not authorized to perform this action
     */
    UserSearchResult searchAndFilterUsersSummary(UserQueryParams query, Integer offset, Integer limit, String sortBy, SortOrder sortOrder,
                                                          GazelleIdentity identity);
    /**
     * Count the number of users per value of a given property.
     *
     * @param propertyName the name of the property
     * @param userQueryParams the User parameters to match with
     * @param identity the identity of the user who uses the method
     * @return the map of possible values user count.
     * @throws IllegalArgumentException if the search, offset param is null
     * @throws UnauthorizedException if the user is not authorized to perform this action
     */
    Map<String, Long> getValueCount(String propertyName, UserQueryParams userQueryParams, GazelleIdentity identity);

    /**
     * Retrieve a user by its id
     * @param userId the id of the user
     * @param identity the identity of the user who uses the method
     * @return the user corresponding to the id, else null
     * @throws IllegalArgumentException if the userId is null
     * @throws java.util.NoSuchElementException if the user is not found
     */
    User getUserById(String userId, GazelleIdentity identity);

    /**
     * Retrieve a user summary by its id
     * @param userId the id of the user
     * @param identity the identity of the user who uses the method
     * @return the user summary corresponding to the id, else null
     * @throws IllegalArgumentException if the userId is null
     * @throws java.util.NoSuchElementException if the user is not found
     */
    User getUserSummaryById(String userId, GazelleIdentity identity);

    /**
     * Retrieve a user by its email
     * @param email the email of the user
     * @param identity the identity of the user who uses the method
     * @return the user corresponding to the email, else null
     * @throws IllegalArgumentException if the email is null
     * @throws java.util.NoSuchElementException if the user is not found
     */
    User getUserByEmail(String email, GazelleIdentity identity);

    /**
     * Return the code needed to activate the user account
     * @param userId the id of the user
     * @return the activation code
     * @throws IllegalArgumentException if the userId is null
     */
    String getActivationCodeForUserId(String userId);

}
