package net.ihe.gazelle.user.management.core.interlay.transactional;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Default;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import net.ihe.gazelle.security.business.Authz;
import net.ihe.gazelle.security.business.GazelleIdentity;
import net.ihe.gazelle.user.management.api.application.organization.OrganizationLookupService;
import net.ihe.gazelle.user.management.api.application.user.delegation.UserDelegationService;
import net.ihe.gazelle.user.management.api.application.user.edit.UserEditService;
import net.ihe.gazelle.user.management.api.application.user.lookup.UserLookupService;
import net.ihe.gazelle.user.management.api.domain.user.User;
import net.ihe.gazelle.user.management.commons.application.user.edit.UserEditDAO;
import net.ihe.gazelle.user.management.commons.application.user.edit.UserEditEmailManager;
import net.ihe.gazelle.user.management.commons.application.user.edit.UserEditServiceImpl;
import net.ihe.gazelle.user.management.commons.application.user.login.HashPasswordServiceProvider;

import java.util.Locale;

/**
 * Transactional implementation of UserEditService, which delegates the calls to a non-transactional implementation
 */
@RequestScoped
@Default
public class UserEditServiceTransactional implements UserEditService {

    private final UserEditService userEditService;

    /**
     * Constructor for UserEditServiceTransactional, which initializes the non-transactional UserEditServiceImpl with the provided dependencies.
     * @param userEditDAO the DAO for user editing operations
     * @param hashPasswordServiceProvider the provider for hashing passwords
     * @param authz the authorization service for checking permissions
     * @param userEditEmailManager the manager for sending emails related to user editing operations
     * @param userLookupService the service for looking up user information
     * @param userDelegationService the service for managing user delegations
     * @param organizationLookupService the service for looking up organization information
     */
    @Inject
    public UserEditServiceTransactional(UserEditDAO userEditDAO, HashPasswordServiceProvider hashPasswordServiceProvider, Authz authz, UserEditEmailManager userEditEmailManager,
                                        UserLookupService userLookupService, UserDelegationService userDelegationService, OrganizationLookupService organizationLookupService) {
        this.userEditService = new UserEditServiceImpl(userEditDAO, hashPasswordServiceProvider, authz,
                userEditEmailManager, userLookupService, userDelegationService, organizationLookupService);
    }


    @Override
    public void checkPasswordIsValid(String password, String passwordConfirmation) {
        userEditService.checkPasswordIsValid(password, passwordConfirmation);
    }

    @Override
    @Transactional
    public void updatePasswordForUserId(String userId, String newPassword, String newPasswordConfirmation) {
        userEditService.updatePasswordForUserId(userId, newPassword, newPasswordConfirmation);
    }

    @Override
    @Transactional
    public User updateAttributes(String userId, User user, GazelleIdentity identity, Locale locale) {
        return userEditService.updateAttributes(userId, user, identity, locale);
    }

    @Override
    @Transactional
    public void activateUser(String userId, GazelleIdentity identity) {
        userEditService.activateUser(userId, identity);
    }

    @Override
    @Transactional
    public void deactivateUser(String userId, GazelleIdentity identity) {
        userEditService.deactivateUser(userId, identity);
    }

    @Override
    @Transactional
    public void deleteUser(String userId, GazelleIdentity identity, Locale locale) {
        userEditService.deleteUser(userId, identity, locale);
    }
}
