package net.ihe.gazelle.user.management.commons.application.user.registration;

import net.ihe.gazelle.security.business.Authz;
import net.ihe.gazelle.security.business.GazelleIdentity;
import net.ihe.gazelle.user.management.api.application.configuration.ApplicationConfig;
import net.ihe.gazelle.user.management.api.application.organization.OrganizationLookupService;
import net.ihe.gazelle.user.management.api.application.organization.OrganizationManagementService;
import net.ihe.gazelle.user.management.api.application.user.edit.UserEditException;
import net.ihe.gazelle.user.management.api.application.user.edit.UserEditService;
import net.ihe.gazelle.user.management.api.application.user.registration.ConsentService;
import net.ihe.gazelle.user.management.api.application.user.registration.UserRegistrationException;
import net.ihe.gazelle.user.management.api.application.user.registration.UserRegistrationService;
import net.ihe.gazelle.user.management.api.domain.organization.DelegatedOrganization;
import net.ihe.gazelle.user.management.api.domain.organization.Organization;
import net.ihe.gazelle.user.management.api.domain.user.GazelleDefaultGroup;
import net.ihe.gazelle.user.management.api.domain.user.User;
import net.ihe.gazelle.user.management.commons.application.exception.ErrorMessage;
import net.ihe.gazelle.user.management.commons.application.exception.GazelleDAOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static net.ihe.gazelle.user.management.api.domain.user.GazelleDefaultGroup.PREFIX_ORGANIZATION_ADMIN;
import static net.ihe.gazelle.user.management.api.domain.user.GazelleDefaultGroup.PREFIX_ORGANIZATION_MEMBER;
import static net.ihe.gazelle.user.management.commons.application.exception.ErrorMessage.*;
import static net.ihe.gazelle.user.management.commons.application.permission.GUMPermissionStore.USER_CREATE;
import static net.ihe.gazelle.user.management.commons.application.permission.GUMPermissionStore.USER_GROUP_UPDATE;
import static net.ihe.gazelle.user.management.commons.application.user.edit.UserEditServiceImpl.KEYCLOAK_ADMIN;

//TODO ceoche review: Why this class is in user-management-common ? It annoys me that this part is embedded in Keycloak.
// If we take a look back it means every feature of gum is also embedded in keycloak, so why do we even bother having a
// separate micro-service ? My suggestion would be to keep it in user-management-core only, and keycloak would call a
// RestClient to perform the user registration.
public class UserRegistrationServiceImpl implements UserRegistrationService {

    private static final Logger log = LoggerFactory.getLogger(UserRegistrationServiceImpl.class);
    private static final String THE_EMAIL_FOR_USER_REGISTRATION_COULD_NOT_BE_SENT = "The email for user registration could not be sent";

    private final UserRegistrationEmailManager userRegistrationEmailManager;
    private final UserRegistrationDAO userRegistrationDAO;
    private final ConsentService consentService;
    private final ApplicationConfig applicationConfig;
    private final UserEditService userEditService;
    private final OrganizationManagementService organizationManagementService;
    private final GazelleIdentity gumIdentity = new GUMIdentity();
    private final OrganizationLookupService organizationLookupService;
    private final Authz authzService;

    public static final String EMAIL_REGEX = "^[a-zA-Z0-9.!#$%&'*+\\-/=?^_`{|}~]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
    // VLD: keep old regex for email here for a moment "^(?=.{1,}@)[\\p{L}0-9_+-]+(\\.[\\p{L}0-9_+-]+)*@([\\p{L}0-9][\\p{L}0-9-]*)(\\.[\\p{L}0-9-]+)*(\\.[\\p{L}]{2,})$";
    public static final String NAME_REGEX = "^[^\\^0-9_!¡?÷?¿\\/\\\\\\^+=@#$%ˆ&*(){}|~<>;:\\[\\]]*$";

    public UserRegistrationServiceImpl(UserEditService userEditService, OrganizationLookupService organizationLookupService,
                                       OrganizationManagementService organizationManagementService, ConsentService consentService,
                                       UserRegistrationDAO userRegistrationDAO, ApplicationConfig applicationConfig,
                                       UserRegistrationEmailManager userRegistrationEmailManager, Authz authzService) {
        this.userRegistrationDAO = userRegistrationDAO;
        this.userRegistrationEmailManager = userRegistrationEmailManager;
        this.applicationConfig = applicationConfig;
        this.userEditService = userEditService;
        this.organizationManagementService = organizationManagementService;
        this.organizationLookupService = organizationLookupService;
        this.consentService = consentService;
        this.authzService = authzService;
    }

    @Override
    public User registerUserWithNewOrganization(User user, Organization organization, boolean consent, String password, String passwordConfirmation, Locale locale) {
        if (!applicationConfig.isUserRegistrationEnabled())
            throw new UserRegistrationException(ErrorMessage.USER_REGISTRATION_DISABLED.getMessage());
        if (!applicationConfig.isOrganizationCreationEnabled())
            throw new UserRegistrationException(ErrorMessage.ORGANIZATION_CREATION_DISABLED.getMessage());
        assertNewUserIsValid(user);
        assertOrganizationIsValid(organization);

        // Check if the password is valid
        userEditService.checkPasswordIsValid(password, passwordConfirmation);

        // Prepare user attributes
        User newUser = generateUser(user);

        // If it's the first user, it will be admin
        if (userRegistrationDAO.getAllUsersCount() == 0)
            newUser.addGroupId(GazelleDefaultGroup.GAZELLE_ADMIN.getName());

        try {
            // Create the organization
            Organization createdOrga = performOrganizationCreation(organization);

            addOrganizationAdminAndMemberGroups(newUser, createdOrga.getId());
            newUser.setOrganizationId(createdOrga.getId());

            // Register the user
            userRegistrationDAO.registerUser(newUser);

            // Persist the user consent if given
            // We created the user even if not consent is given because administrators
            // needs to be able to create new users without their consents
            if (consent)
                consentService.acceptUserConsent(newUser.getId());

            // Update the password of the user
            userEditService.updatePasswordForUserId(newUser.getId(), password, passwordConfirmation);

            // Send email to all vendor admin to activate the new user
            userRegistrationEmailManager.sendMailToActivateUserItself(newUser, organization.getName(), locale);

            return newUser;
        } catch (GazelleDAOException e) {
            // Rollback the user registration
            userRegistrationDAO.rollbackUserRegistration(newUser.getId()); //TODO wrap of the method in a transaction for auto rollback
            log.error("Failed to register user, rollback registration...", e);
            throw new UserRegistrationException("Failed to register user due to persistence problem.", e);
        } catch (ActivationEmailManagerException e) {
            log.error(THE_EMAIL_FOR_USER_REGISTRATION_COULD_NOT_BE_SENT, e);
            throw new UserRegistrationException(THE_EMAIL_FOR_USER_REGISTRATION_COULD_NOT_BE_SENT, e);
        }
    }

    @Override
    public User registerUser(User user, boolean consent, String password, String passwordConfirmation, Locale locale) {
        if (!applicationConfig.isUserRegistrationEnabled())
            throw new UserRegistrationException(ErrorMessage.USER_REGISTRATION_DISABLED.getMessage());
        assertNewUserIsValid(user);
        Organization organization = checkThatOrganizationIdExistsAndIsAValidOrganization(user.getOrganizationId());

        userEditService.checkPasswordIsValid(password, passwordConfirmation);

        // Generate the user
        User newUser = generateUser(user);
        newUser.addGroupId(PREFIX_ORGANIZATION_MEMBER.getName() + user.getOrganizationId());
        newUser.setOrganizationId(user.getOrganizationId());

        try {
            userRegistrationDAO.registerUser(newUser);

            if (consent)
                consentService.acceptUserConsent(newUser.getId());

            userEditService.updatePasswordForUserId(newUser.getId(), password, passwordConfirmation);

            // Send email to user to activate its account
            List<String> vendorAdminEmails = userRegistrationDAO.getActiveAdminsOfOrganization(organization.getId()).stream().map(User::getEmail).toList();
            userRegistrationEmailManager.sendMailActiveUserToAllVendorAdmin(newUser, organization.getName(), vendorAdminEmails, locale);

            return newUser;
        } catch (GazelleDAOException e) {
            // Rollback the user registration
            userRegistrationDAO.rollbackUserRegistration(newUser.getId());
            throw new UserRegistrationException(e.getMessage(), e);
        } catch (ActivationEmailManagerException e) {
            throw new UserRegistrationException(THE_EMAIL_FOR_USER_REGISTRATION_COULD_NOT_BE_SENT, e);
        }
    }

    @Override
    public User createUser(User user, GazelleIdentity identity, Locale locale) {
        authzService.assertAuthorized(identity, USER_CREATE, null, user != null ? user.getOrganizationId() : null);

        assertNewUserIsValid(user);
        if (!identity.hasGroup(KEYCLOAK_ADMIN)) // In Keyckloak, we need to be able to create user without orga
            checkThatOrganizationIdExistsAndIsAValidOrganization(user.getOrganizationId());
        User newUser = generateUser(user);
        newUser.setOrganizationId(user.getOrganizationId());
        newUser.addGroupId(PREFIX_ORGANIZATION_MEMBER.getName() + user.getOrganizationId());
        addProvidedGroups(user, newUser);

        // Check authorization for each given role to the newUser
        verifyRoleAuthorization(identity, newUser);

        newUser.setActivated(true);

        try {
            userRegistrationDAO.registerUser(newUser);
            if (applicationConfig.isUserCreationEmailNotificationEnabled())
                userRegistrationEmailManager.sendMailToNewUserCreatedByAdmin(newUser, locale);
            return newUser;
        } catch (GazelleDAOException e) {
            userRegistrationDAO.rollbackUserRegistration(newUser.getId());
            throw new UserRegistrationException(e.getMessage(), e);
        }
    }

    @Override
    public User createUserWithNewOrganization(User user, Organization organization, GazelleIdentity identity, Locale locale) {
        authzService.assertAuthorized(identity, USER_CREATE, null, user != null ? user.getOrganizationId() : null);
        assertNewUserIsValid(user);
        assertOrganizationIsValid(organization);

        User newUser = generateUser(user);

        try {
            Organization createdOrga = performOrganizationCreation(organization);

            newUser.setOrganizationId(createdOrga.getId());
            addOrganizationAdminAndMemberGroups(newUser, createdOrga.getId());
            addProvidedGroups(user, newUser);

            // Check authorization for each given role to the newUser
            verifyRoleAuthorization(identity, newUser);

            newUser.setActivated(true);

            userRegistrationDAO.registerUser(newUser);
            if (applicationConfig.isUserCreationEmailNotificationEnabled())
                userRegistrationEmailManager.sendMailToNewUserCreatedByAdmin(newUser, locale);
            return newUser;
        } catch (GazelleDAOException e) {
            userRegistrationDAO.rollbackUserRegistration(newUser.getId());
            throw new UserRegistrationException(e.getMessage(), e);
        }
    }

    private void verifyRoleAuthorization(GazelleIdentity identity, User newUser) {
        for (String groupId : newUser.getGroupIds()) {
            authzService.assertAuthorized(identity, USER_GROUP_UPDATE, groupId, newUser, false);
        }
    }

    @Override
    public User activateUserWithActivationCode(String activationCode) {
        if (activationCode == null)
            throw new IllegalArgumentException("activationCode is null");

        try {
            return userRegistrationDAO.activateUserWithActivationCode(activationCode);
        } catch (GazelleDAOException e) {
            throw new UserEditException("Unable to activate user", e);
        }
    }

    /**
     * Check that the given organization Id is corresponding to a valid existing organization
     *
     * @param organizationId the id of the organization
     * @return the found organization
     */
    private Organization checkThatOrganizationIdExistsAndIsAValidOrganization(String organizationId) {
        if (organizationId == null)
            throw new IllegalArgumentException(ErrorMessage.ORGANIZATION_ID_IS_NULL.getMessage());
        Organization organization = organizationLookupService.getOrganizationById(organizationId);
        assertOrganizationIsValid(organization);
        return organization;
    }

    /**
     * Create a new organization
     *
     * @param organization the organization to create
     */
    private Organization performOrganizationCreation(Organization organization) {
        try {
            return organizationManagementService.createOrganization(organization, gumIdentity);
        } catch (GazelleDAOException e) {
            throw new UserRegistrationException("Failed to create organization", e);
        }
    }

    /**
     * Check if the provided organization is valid
     *
     * @param organization the organization to check
     */
    private void assertOrganizationIsValid(Organization organization) {
        if (organization == null) throw new IllegalArgumentException("Organization is null");
        if (organization instanceof DelegatedOrganization)
            throw new IllegalArgumentException("Organization is delegated");
        if (organization.getShortname() == null || organization.getName() == null)
            throw new IllegalArgumentException("Some required organization attributes are null");
        if (organization.getShortname().length() > 32)
            throw new IllegalArgumentException("Organization shortname is too long");
    }

    /**
     * Check if the provided user is valid
     *
     * @param user the user to check
     */
    private void assertNewUserIsValid(User user) {
        if (user == null) throw new IllegalArgumentException("User is null");
        if (user.getFirstName() == null) throw new IllegalArgumentException("Firstname is null");
        if (user.getLastName() == null) throw new IllegalArgumentException("Lastname is null");
        if (user.getEmail() == null) throw new IllegalArgumentException("Email is null");
        if (!user.getFirstName().matches(NAME_REGEX))
            throw new IllegalArgumentException(FIRSTNAME_NOT_VALID.getMessage());
        if (!user.getLastName().matches(NAME_REGEX))
            throw new IllegalArgumentException(LASTNAME_NOT_VALID.getMessage());
        if (!user.getEmail().toLowerCase().matches(EMAIL_REGEX))
            throw new IllegalArgumentException(EMAIL_NOT_VALID.getMessage());
        if (userRegistrationDAO.isEmailAlreadyExist(user.getEmail()))
            throw new UserRegistrationException(EMAIL_ALREADY_EXISTS.getMessage());
    }

    /**
     * Generate a new user from the provided user attributes
     *
     * @param user          the attributes of the new user
     * @return the new user
     */
    private User generateUser(User user) {
        // Generate a new user ID
        String userId = UUID.randomUUID().toString();
        User newUser = new User(userId);

        // Set dynamic attributes
        newUser.setFirstName(user.getFirstName());
        newUser.setLastName(user.getLastName());
        newUser.setEmail(user.getEmail().toLowerCase());

        // Set static attributes
        newUser.setActivated(false);

        // Generate activation code (UUID without dashes)
        String activationCode = UUID.randomUUID().toString().replace("-", "");
        newUser.setActivationCode(activationCode);

        // Add default groups
        newUser.addGroupId(GazelleDefaultGroup.SUT_OPERATOR.getName());

        return newUser;
    }

    private void addOrganizationAdminAndMemberGroups(User user, String organizationId) {
        user.addGroupId(PREFIX_ORGANIZATION_ADMIN.getName() + organizationId);
        user.addGroupId(PREFIX_ORGANIZATION_MEMBER.getName() + organizationId);
    }

    private void addProvidedGroups(User sourceUser, User targetUser) {
        if (sourceUser.getGroupIds() != null)
            sourceUser.getGroupIds().forEach(targetUser::addGroupId);
    }
}
