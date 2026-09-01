package net.ihe.gazelle.user.management.commons.application.user.search;

import net.ihe.gazelle.search.api.Range;
import net.ihe.gazelle.search.api.SearchQuery;
import net.ihe.gazelle.search.api.SearchResult;
import net.ihe.gazelle.search.api.Sort;
import net.ihe.gazelle.security.business.Authz;
import net.ihe.gazelle.security.business.GazelleIdentity;
import net.ihe.gazelle.security.business.UnauthorizedException;
import net.ihe.gazelle.user.management.api.application.user.search.UserSearchCriteria;
import net.ihe.gazelle.user.management.api.application.user.search.UserSearchService;
import net.ihe.gazelle.user.management.api.domain.user.User;
import net.ihe.gazelle.user.management.commons.application.permission.GUMPermissionStore;
import net.ihe.gazelle.user.management.commons.interlay.dao.user.UserSearchDAO;

import java.util.List;

/**
 * Implementation of User Search service
 */
public class UserSearchServiceImpl implements UserSearchService {

    private final UserSearchDAO userSearchDAO;
    private final Authz authz;

    public UserSearchServiceImpl(UserSearchDAO userSearchDAO, Authz authz) {
        this.userSearchDAO = userSearchDAO;
        this.authz = authz;
    }

    @Override
    public SearchResult<User> search(SearchQuery<UserSearchCriteria> query, GazelleIdentity identity) {
        if (identity == null || !identity.isAuthenticated()) {
            throw new UnauthorizedException("User must be authenticated to perform this action");
        }

        if (query == null) {
            query = new SearchQuery<>(new UserSearchCriteria(), getDefaultRange(), getDefaultSort());
        } else {
            List<Sort> sorts = query.sorts() != null && !query.sorts().isEmpty() ? query.sorts() : getDefaultSort();
            Range range = query.range() != null ? query.range() : getDefaultRange();
            UserSearchCriteria criteria = query.searchCriteria() != null ? query.searchCriteria() : new UserSearchCriteria();
            query = new SearchQuery<>(criteria, range, sorts);
        }
        Range.validateRange(query.range());

        if (authz.isAuthorized(identity, GUMPermissionStore.ALL_USER_READ)) {
            return userSearchDAO.search(query.searchCriteria(), query.range(), query.sorts());
        } else if (authz.isAuthorized(identity, GUMPermissionStore.ORGA_USER_READ)) {
            query.searchCriteria().setOrganizationIdParam(identity.getOrganizationId());
            return userSearchDAO.search(query.searchCriteria(), query.range(), query.sorts());
        } else {
            return userSearchDAO.getUserById(identity.getId());
        }
    }

    @Override
    public SearchResult<User> search(SearchQuery<UserSearchCriteria> query, List<String> attributePaths, GazelleIdentity identity) {
        throw new UnsupportedOperationException("Presentation schema search not implemented");
    }

    private static List<Sort> getDefaultSort() {
        return List.of(new Sort("lastName", Sort.Order.ASCENDING));
    }

    private static Range getDefaultRange() {
        return new Range(Range.DEFAULT_OFFSET, Range.DEFAULT_LIMIT);
    }
}
