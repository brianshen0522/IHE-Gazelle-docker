package net.ihe.gazelle.user.management.core.interlay.factory;

import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import net.ihe.gazelle.user.management.api.application.user.delegation.UserDelegationService;
import net.ihe.gazelle.user.management.commons.application.user.delegation.UserDelegationDAO;
import net.ihe.gazelle.user.management.commons.application.user.delegation.UserDelegationServiceImpl;
import net.ihe.gazelle.user.management.commons.application.user.edit.UserEditDAO;
import net.ihe.gazelle.user.management.commons.application.user.lookup.UserLookupDAO;

/**
 * Factory class for creating instances of UserDelegationService.
 */
public class UserDelegationServiceFactory {

    private final UserDelegationDAO userDelegationDAO;
    private final UserLookupDAO userLookupDAO;
    private final UserEditDAO userEditDAO;

    /**
     * Constructs a UserDelegationServiceFactory with the specified DAOs.
     * @param userDelegationDAO the UserDelegationDAO to be used by the service
     * @param userLookupDAO the UserLookupDAO to be used by the service
     * @param userEditDAO the UserEditDAO to be used by the service
     */
    @Inject
    public UserDelegationServiceFactory(UserDelegationDAO userDelegationDAO, UserLookupDAO userLookupDAO, UserEditDAO userEditDAO) {
        this.userDelegationDAO = userDelegationDAO;
        this.userLookupDAO = userLookupDAO;
        this.userEditDAO = userEditDAO;
    }

    /**
     * Produces an instance of UserDelegationService.
     * @return a new instance of UserDelegationService
     */
    @Produces
    public UserDelegationService getUserDelegationService() {
        return new UserDelegationServiceImpl(userDelegationDAO, userLookupDAO, userEditDAO);
    }
}
