package net.ihe.gazelle.user.management.core.interlay.factory;

import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import net.ihe.gazelle.security.business.Authz;
import net.ihe.gazelle.user.management.api.application.user.lookup.UserLookupService;
import net.ihe.gazelle.user.management.commons.application.user.lookup.UserLookupDAO;
import net.ihe.gazelle.user.management.commons.application.user.lookup.UserLookupServiceImpl;

/**
 * Factory class for creating instances of UserLookupService.
 */
public class UserLookupServiceFactory {

    private final UserLookupDAO userLookupDAO;
    private final Authz authzService;

    /**
     * Constructs a new UserLookupServiceFactory with the specified dependencies.
     * @param userLookupDAO the UserLookupDAO to be used by the UserLookupService
     * @param authzService the Authz service to be used for authorization checks
     */
    @Inject
    public UserLookupServiceFactory(UserLookupDAO userLookupDAO, Authz authzService) {
        this.userLookupDAO = userLookupDAO;
        this.authzService = authzService;
    }

    /**
     * Produces an instance of UserLookupService.
     * @return a new instance of UserLookupService
     */
    @Produces
    public UserLookupService getUserLoginService() {
        return new UserLookupServiceImpl(userLookupDAO, authzService);
    }
}
