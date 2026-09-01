package net.ihe.gazelle.user.management.core.interlay.factory;

import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import net.ihe.gazelle.security.business.Authz;
import net.ihe.gazelle.user.management.api.application.user.search.UserSearchIndexServiceImpl;
import net.ihe.gazelle.user.management.api.application.user.search.UserSearchService;
import net.ihe.gazelle.user.management.api.application.user.search.UserSuggestionService;
import net.ihe.gazelle.user.management.commons.application.user.search.UserSearchServiceImpl;
import net.ihe.gazelle.user.management.commons.application.user.search.UserSuggestionServiceImpl;
import net.ihe.gazelle.user.management.commons.interlay.dao.user.UserSearchDAO;

/**
 * Factory class for creating instances of services related to user search.
 */
public class UserSearchFactory {

    private final UserSearchDAO userSearchDAO;
    private final Authz authz;


    @Inject
    public UserSearchFactory(UserSearchDAO userSearchDAO, Authz authz) {
        this.userSearchDAO = userSearchDAO;
        this.authz = authz;
    }

    @Produces
    @Default
    public UserSearchService getUserSearchService() {
        return new UserSearchServiceImpl(userSearchDAO, authz);
    }

    @Produces
    @Default
    public UserSuggestionService getUserSuggestionService() {
        return new UserSuggestionServiceImpl(userSearchDAO, getUserSearchIndexService(), authz);
    }

    @Produces
    @Default
    private UserSearchIndexServiceImpl getUserSearchIndexService() {
        return new UserSearchIndexServiceImpl();
    }
}
