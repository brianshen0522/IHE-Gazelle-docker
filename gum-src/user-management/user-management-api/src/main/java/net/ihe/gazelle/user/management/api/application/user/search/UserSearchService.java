package net.ihe.gazelle.user.management.api.application.user.search;

import net.ihe.gazelle.search.api.SearchService;
import net.ihe.gazelle.user.management.api.domain.user.User;

/**
 * Service for searching users.
 * <br>
 * See {@link SearchService}
 */
public interface UserSearchService extends SearchService<User, UserSearchCriteria> {
}
