package net.ihe.gazelle.user.management.commons.application.user.search;

import net.ihe.gazelle.security.business.Authz;
import net.ihe.gazelle.security.business.GazelleIdentity;
import net.ihe.gazelle.security.business.UnauthorizedException;
import net.ihe.gazelle.user.management.api.application.user.search.UserSearchCriteria;
import net.ihe.gazelle.user.management.api.application.user.search.UserSearchIndexServiceImpl;
import net.ihe.gazelle.user.management.api.application.user.search.UserSuggestionService;
import net.ihe.gazelle.user.management.commons.application.permission.GUMPermissionStore;
import net.ihe.gazelle.user.management.commons.interlay.dao.user.UserSearchDAO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implementation of User Suggestion service
 */
public class UserSuggestionServiceImpl implements UserSuggestionService {

    private final UserSearchDAO userSearchDAO;
    private final UserSearchIndexServiceImpl userSearchIndexServiceImpl;
    private final Authz authz;

    public UserSuggestionServiceImpl(UserSearchDAO userSearchDAO, UserSearchIndexServiceImpl userSearchIndexServiceImpl, Authz authz) {
        this.userSearchDAO = userSearchDAO;
        this.userSearchIndexServiceImpl = userSearchIndexServiceImpl;
        this.authz = authz;
    }


    @Override
    public List<String> getSuggestions(String field, UserSearchCriteria criteria, GazelleIdentity identity) {
        if (identity == null || !identity.isAuthenticated()) {
            throw new UnauthorizedException("User must be authenticated to perform this action");
        }
        assertFieldExists(field);

        List<String> suggestions = new ArrayList<>();

        if (isFieldBoolean(field)) {
            return List.of("true", "false");
        }
        if (UserSearchIndexServiceImpl.GROUP.equals(field)) {
            suggestions.addAll(List.of("org", "org-adm"));
        }

        if (authz.isAuthorized(identity, GUMPermissionStore.ALL_USER_READ)) {
            suggestions.addAll(userSearchDAO.getSuggestions(field, criteria));
            return suggestions;
        } else if (authz.isAuthorized(identity, GUMPermissionStore.ORGA_USER_READ)) {
            criteria.setOrganizationIdParam(identity.getOrganizationId());
            suggestions.addAll(userSearchDAO.getSuggestions(field, criteria));
            return suggestions;
        }
        return Collections.emptyList();
    }

    private boolean isFieldBoolean(String field) {
        return UserSearchIndexServiceImpl.ACTIVATED.equals(field) || UserSearchIndexServiceImpl.DELEGATED.equals(field);
    }

    private void assertFieldExists(String field) {
        if (field == null)
            throw new IllegalArgumentException("Field is null");

        if (!userSearchIndexServiceImpl.isIndexedField(field)) {
            throw new IllegalArgumentException(field + " is not a valid index");
        }
    }


}
