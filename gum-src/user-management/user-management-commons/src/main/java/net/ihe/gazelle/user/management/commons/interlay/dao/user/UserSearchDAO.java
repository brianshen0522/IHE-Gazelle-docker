package net.ihe.gazelle.user.management.commons.interlay.dao.user;

import net.ihe.gazelle.search.api.Range;
import net.ihe.gazelle.search.api.SearchResult;
import net.ihe.gazelle.search.api.Sort;
import net.ihe.gazelle.user.management.api.application.user.search.UserSearchCriteria;
import net.ihe.gazelle.user.management.api.domain.user.User;

import java.util.List;

/**
 * DAO interface for all data object user search.
 */
public interface UserSearchDAO {

    /**
     * Get suggestions for a field.
     *
     * @param field    field to search
     * @param criteria search criteria
     * @return list of suggestions
     */
    List<String> getSuggestions(String field, UserSearchCriteria criteria);

    /**
     * Search for users.
     *
     * @param criteria       the search criteria
     * @param range          the pagination range
     * @param sortParameters the sorting parameters
     * @return the search result
     */
    SearchResult<User> search(UserSearchCriteria criteria, Range range, List<Sort> sortParameters);

    /**
     * Get a user by its id.
     *
     * @param userId the id of the user
     * @return the search result
     */
    SearchResult<User> getUserById(String userId);
}
