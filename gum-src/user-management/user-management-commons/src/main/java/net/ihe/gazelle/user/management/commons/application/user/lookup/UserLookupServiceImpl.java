package net.ihe.gazelle.user.management.commons.application.user.lookup;

import net.ihe.gazelle.security.business.Authz;
import net.ihe.gazelle.security.business.GazelleIdentity;
import net.ihe.gazelle.security.business.UnauthorizedException;
import net.ihe.gazelle.user.management.api.application.user.lookup.SortOrder;
import net.ihe.gazelle.user.management.api.application.user.lookup.UserLookupService;
import net.ihe.gazelle.user.management.api.application.user.lookup.UserQueryParams;
import net.ihe.gazelle.user.management.api.application.user.lookup.UserSearchResult;
import net.ihe.gazelle.user.management.api.domain.user.User;
import net.ihe.gazelle.user.management.commons.application.exception.ErrorMessage;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import static net.ihe.gazelle.user.management.commons.application.permission.GUMPermissionStore.ALL_USER_READ;
import static net.ihe.gazelle.user.management.commons.application.permission.GUMPermissionStore.ORGA_USER_READ;

public class UserLookupServiceImpl implements UserLookupService {

    private static final String FIRST_NAME = "firstName";
    protected UserLookupDAO userLookupDAO;
    private final Authz authz;

    public UserLookupServiceImpl(UserLookupDAO userLookupDAO, Authz authz) {
        this.userLookupDAO = userLookupDAO;
        this.authz = authz;
    }

    @Override
    public List<User> searchAndFilterUsers(UserQueryParams userQueryParams, Integer offset, Integer limit, GazelleIdentity identity) {
        return searchAndFilterUsersWithCount(userQueryParams, offset, limit, null, null, identity).users();
    }

    @Override
    public UserSearchResult searchAndFilterUsersWithCount(UserQueryParams userQueryParams, Integer offset, Integer limit, String sortBy, SortOrder sortOrder,
                                                          GazelleIdentity identity) {
        if (identity==null || !identity.isAuthenticated())
            throw new UnauthorizedException();

        if (userQueryParams == null)
            userQueryParams = UserQueryParams.nullQuery();
        if (offset == null)
            offset = 0;
        if (limit == null)
            limit = 100;
        if (sortBy == null)
            sortBy = FIRST_NAME;
        if (sortOrder == null)
            sortOrder = SortOrder.ASC;
        if (userQueryParams.search() == null || userQueryParams.search().equals("*"))
            userQueryParams = UserQueryParams.clone(userQueryParams).setSearch("");

        userQueryParams = updateQueryParamDependingOnAuthorization(userQueryParams, identity);
        return new UserSearchResult(userLookupDAO.searchForUsers(userQueryParams,
                offset, limit, sortBy, sortOrder), offset, limit, userLookupDAO.countFilteredUsers(userQueryParams));
    }

    @Override
    public UserSearchResult searchAndFilterUsersSummary(UserQueryParams userQueryParams, Integer offset, Integer limit, String sortBy, SortOrder sortOrder,
                                                                 GazelleIdentity identity) {
        if (identity==null || !identity.isAuthenticated())
            throw new UnauthorizedException();

        if (userQueryParams == null)
            userQueryParams = UserQueryParams.nullQuery();
        if (offset == null)
            offset = 0;
        if (limit == null)
            limit = 100;
        if (sortBy == null)
            sortBy = FIRST_NAME;
        if (sortOrder == null)
            sortOrder = SortOrder.ASC;
        if (userQueryParams.search() == null || userQueryParams.search().equals("*"))
            userQueryParams = UserQueryParams.clone(userQueryParams).setSearch("");

        return new UserSearchResult(userLookupDAO.searchForUsersSummary(userQueryParams,
                offset, limit, sortBy, sortOrder), offset, limit, userLookupDAO.countFilteredUsers(userQueryParams));
    }

    private UserQueryParams updateQueryParamDependingOnAuthorization(UserQueryParams userQueryParams, GazelleIdentity identity) {
        if (authz.isAuthorized(identity, ALL_USER_READ)) {
            return userQueryParams;
        } else if (authz.isAuthorized(identity, ORGA_USER_READ)) {
            return UserQueryParams.clone(userQueryParams).setAttribute("organizationId", identity.getOrganizationId());
        } else {
            return UserQueryParams.clone(userQueryParams).setSearch(identity.getId());
        }

    }

    @Override
    public Map<String, Long> getValueCount(String propertyName, UserQueryParams userQueryParams, GazelleIdentity identity) {
        authz.assertAuthorized(identity, ALL_USER_READ);

        if (propertyName != null && Arrays.asList(FIRST_NAME, "lastName", "organizationId", "roles", "activated")
                .contains(propertyName)) {
            return userLookupDAO.getValueCount(propertyName, userQueryParams);
        } else {
            throw new IllegalArgumentException(
                    "propertyName must be define and with value: [firstName, lastName, organizationId, roles, activated]");
        }
    }

    @Override
    public User getUserById(String userId, GazelleIdentity identity) {
        if (userId == null)
            throw new IllegalArgumentException(ErrorMessage.USER_ID_IS_NULL.getMessage());

        User user = userLookupDAO.getUserById(userId);
        if (!authz.isAuthorized(identity, ALL_USER_READ, userId, user != null ? user.getOrganizationId():  null))
            throw new UnauthorizedException();
        if (user == null)
            throw new NoSuchElementException();

        return user;
    }

    @Override
    public User getUserSummaryById(String userId, GazelleIdentity identity) {
        if (userId == null)
            throw new IllegalArgumentException(ErrorMessage.USER_ID_IS_NULL.getMessage());

        if (identity == null || !identity.isAuthenticated())
            throw new UnauthorizedException();

        User user = userLookupDAO.getUserSummaryById(userId);
        if (user == null)
            throw new NoSuchElementException();

        return user;
    }

    @Override
    public User getUserByEmail(String email, GazelleIdentity identity) {
        if (email == null)
            throw new IllegalArgumentException("email is null");
        User user = userLookupDAO.getUserByEmail(email);
        authz.assertAuthorized(identity, ALL_USER_READ, user.getId(), user.getOrganizationId());

        return user;
    }

    @Override
    public String getActivationCodeForUserId(String userId) {
        if (userId == null) throw new IllegalArgumentException(ErrorMessage.USER_ID_IS_NULL.getMessage());
        return userLookupDAO.getActivationCodeForUserId(userId);
    }
}
