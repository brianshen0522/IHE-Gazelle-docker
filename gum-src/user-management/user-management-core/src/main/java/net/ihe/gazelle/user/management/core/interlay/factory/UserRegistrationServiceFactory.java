package net.ihe.gazelle.user.management.core.interlay.factory;

import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import net.ihe.gazelle.security.business.Authz;
import net.ihe.gazelle.user.management.api.application.configuration.ApplicationConfig;
import net.ihe.gazelle.user.management.api.application.organization.OrganizationLookupService;
import net.ihe.gazelle.user.management.api.application.organization.OrganizationManagementService;
import net.ihe.gazelle.user.management.api.application.user.edit.UserEditService;
import net.ihe.gazelle.user.management.api.application.user.registration.ConsentService;
import net.ihe.gazelle.user.management.api.application.user.registration.UserRegistrationService;
import net.ihe.gazelle.user.management.commons.application.user.registration.UserRegistrationDAO;
import net.ihe.gazelle.user.management.commons.application.user.registration.UserRegistrationEmailManager;
import net.ihe.gazelle.user.management.commons.application.user.registration.UserRegistrationServiceImpl;

/**
 * Factory class for creating instances of UserRegistrationService with all necessary dependencies injected.
 */
public class UserRegistrationServiceFactory {

    private final UserEditService userEditService;
    private final OrganizationManagementService organizationManagementService;
    private final OrganizationLookupService organizationLookupService;
    private final UserRegistrationEmailManager emailManager;
    private final UserRegistrationDAO userRegistrationDAO;
    private final ApplicationConfig applicationConfig;
    private final ConsentService consentService;
    private final Authz authz;

    /**
     * Constructs a new UserRegistrationServiceFactory with the specified dependencies.
     * @param userEditService the service for editing user information
     * @param organizationManagementService the service for registering organizations
     * @param organizationLookupService the service for looking up organization information
     * @param emailManager the manager for handling user registration emails
     * @param userRegistrationDAO the data access object for user registration operations
     * @param applicationConfig the application configuration containing necessary settings
     * @param consentService the service for managing user consents during registration
     * @param authz the authorization service for checking permissions during user registration
     */
    @Inject
    public UserRegistrationServiceFactory(UserEditService userEditService, OrganizationManagementService organizationManagementService, OrganizationLookupService organizationLookupService,
                                          UserRegistrationEmailManager emailManager, UserRegistrationDAO userRegistrationDAO, ApplicationConfig applicationConfig, ConsentService consentService, Authz authz) {
        this.userEditService = userEditService;
        this.organizationManagementService = organizationManagementService;
        this.organizationLookupService = organizationLookupService;
        this.emailManager = emailManager;
        this.userRegistrationDAO = userRegistrationDAO;
        this.applicationConfig = applicationConfig;
        this.consentService = consentService;
        this.authz = authz;
    }

    /**
     * Produces an instance of UserRegistrationService with all dependencies injected.
     * @return a fully initialized UserRegistrationService instance ready for use in the application
     */
    @Produces
    public UserRegistrationService getUserRegistrationService() {
        return new UserRegistrationServiceImpl(userEditService, organizationLookupService, organizationManagementService,
                consentService, userRegistrationDAO, applicationConfig, emailManager, authz);
    }
}
