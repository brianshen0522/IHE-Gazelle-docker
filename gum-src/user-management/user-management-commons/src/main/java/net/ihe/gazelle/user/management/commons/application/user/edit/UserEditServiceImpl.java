package net.ihe.gazelle.user.management.commons.application.user.edit;

import net.ihe.gazelle.security.business.Authz;
import net.ihe.gazelle.security.business.GazelleIdentity;
import net.ihe.gazelle.user.management.api.application.organization.OrganizationLookupService;
import net.ihe.gazelle.user.management.api.application.user.delegation.UserDelegationService;
import net.ihe.gazelle.user.management.api.application.user.edit.UserEditException;
import net.ihe.gazelle.user.management.api.application.user.edit.UserEditService;
import net.ihe.gazelle.user.management.api.application.user.lookup.UserLookupService;
import net.ihe.gazelle.user.management.api.domain.user.Credentials;
import net.ihe.gazelle.user.management.api.domain.user.User;
import net.ihe.gazelle.user.management.commons.application.exception.ErrorMessage;
import net.ihe.gazelle.user.management.commons.application.exception.GazelleDAOException;
import net.ihe.gazelle.user.management.commons.application.user.login.HashPasswordService;
import net.ihe.gazelle.user.management.commons.application.user.login.HashPasswordServiceProvider;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static net.ihe.gazelle.user.management.api.domain.user.GazelleDefaultGroup.*;
import static net.ihe.gazelle.user.management.commons.application.exception.ErrorMessage.*;
import static net.ihe.gazelle.user.management.commons.application.permission.GUMPermissionStore.*;
import static net.ihe.gazelle.user.management.commons.application.user.registration.UserRegistrationServiceImpl.EMAIL_REGEX;
import static net.ihe.gazelle.user.management.commons.application.user.registration.UserRegistrationServiceImpl.NAME_REGEX;

public class UserEditServiceImpl implements UserEditService {

    public static final String REGEX_PASSWORD_PATTERN = "^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[.#?!@~^$%=&*\\-<>:;,+£'`\\[\\]\"|°)(}{]).{8,}$";
    public static final String DEFAULT_HASH_METHOD = "PBKDF2";
    public static final String KEYCLOAK_ADMIN = "keycloak_admin";
    private final UserEditDAO userEditDAO;
    private final HashPasswordService hashService;
    private final Authz authz;
    private final UserEditEmailManager editEmailManager;
    private final UserLookupService userLookupService;
    private final UserDelegationService userDelegationService;
    private final OrganizationLookupService organizationLookupService;

    // Rule for passwords : at least 8 characters, 1 uppercase, 1 lowercase, 1 number and 1 special character
    private final Pattern passwordPattern = Pattern.compile(REGEX_PASSWORD_PATTERN);

    public UserEditServiceImpl(UserEditDAO userEditDAO, HashPasswordServiceProvider hashPasswordServiceProvider,
                               Authz authz, UserEditEmailManager editEmailManager,
                               UserLookupService userLookupService, UserDelegationService userDelegationService,
                               OrganizationLookupService organizationLookupService) {
        this.userEditDAO = userEditDAO;
        this.hashService = hashPasswordServiceProvider.getHashPasswordService(DEFAULT_HASH_METHOD)
                .orElseThrow(() -> new UserEditException("PBKDF2 hash method not found"));
        this.authz = authz;
        this.editEmailManager = editEmailManager;
        this.userLookupService = userLookupService;
        this.userDelegationService = userDelegationService;
        this.organizationLookupService = organizationLookupService;
    }

    @Override
    public void checkPasswordIsValid(String password, String passwordConfirmation) {
        assertPasswordIsValid(password, passwordConfirmation);
    }

    @Override
    public void updatePasswordForUserId(String userId, String newPassword, String newPasswordConfirmation) {
        if (userId == null)
            throw new IllegalArgumentException(ErrorMessage.USER_ID_IS_NULL.getMessage());

        if (userDelegationService.isUserDelegatedFromId(userId)) {
            throw new UserEditException("Gazelle cannot change the password of a delegated account. Follow the procedure at your identity provider.");
        }
        assertPasswordIsValid(newPassword, newPasswordConfirmation);

        Credentials credentials = hashService.hash(newPassword);
        userEditDAO.updateCredentialsForUserId(userId, credentials);
    }

    @Override
    public User updateAttributes(String userId, User providedUserAttributes, GazelleIdentity identity, Locale locale) {
        User existingUser = userEditDAO.getUserFromUserId(userId);
        authz.assertAuthorized(identity, USER_UPDATE, userId, existingUser != null ? existingUser.getOrganizationId() : null);

        if (userId == null)
            throw new IllegalArgumentException(ErrorMessage.USER_ID_IS_NULL.getMessage());
        if (providedUserAttributes == null)
            throw new IllegalArgumentException("Provided user is null");
        if (existingUser == null)
            throw new NoSuchElementException(USER_NOT_FOUND.getMessage());

        assertUserIsActivated(existingUser, providedUserAttributes.isActivated());
        assertCanUpdateGroups(existingUser, providedUserAttributes, identity);

        try {
            assertAttributesAreValid(providedUserAttributes);

            if (providedUserAttributes.getOrganizationId() != null  && !Objects.equals(providedUserAttributes.getOrganizationId(), existingUser.getOrganizationId())) {
                // If organization change for update user, check that organization exists
                // Only Keycloak can update user orga
                authz.assertAuthorized(identity, USER_ORGANIZATION_UPDATE);
                checkOrganizationExists(providedUserAttributes.getOrganizationId());

                // If organization change for update user, we remove all the org-adm: + org: groups
                providedUserAttributes.setGroupIds(
                        manageGroupsForOrganizationUpdate(providedUserAttributes.getOrganizationId(), existingUser.getGroupIds(), providedUserAttributes.getGroupIds())
                );
            }

            // If email change for update user, call the email edit user service for mailing workflow.
            // Except for admins and orga admins.
            if (providedUserAttributes.getEmail() != null) {
                providedUserAttributes.setEmail(providedUserAttributes.getEmail().toLowerCase());
                if (shouldTriggerEmailUpdateFlow(identity))
                    performEmailUpdateFlow(userId, providedUserAttributes, existingUser, identity, locale);
            }

            providedUserAttributes.setId(userId);
            providedUserAttributes = userEditDAO.updateAttributes(userId, providedUserAttributes);
        } catch (GazelleDAOException e) {
            throw new UserEditException("User not updated", e);
        }
        return providedUserAttributes;
    }

    private void assertCanUpdateGroups(User existingUser, User providedUserAttributes, GazelleIdentity identity) {
        if (providedUserAttributes.getGroupIds() != null) {
            Set<String> newGroupIds = new HashSet<>(providedUserAttributes.getGroupIds());
            Set<String> originalUserGroupIds = existingUser.getGroupIds();

            //Check if there is groups to remove
            Set<String> groupIdsToRemove = originalUserGroupIds.stream()
                    .filter(groupId -> !newGroupIds.contains(groupId))
                    .collect(Collectors.toSet());
            //Check if there is groups to add
            Set<String> differentGroupIds = newGroupIds.stream()
                    .filter(groupId -> !originalUserGroupIds.contains(groupId)).collect(Collectors.toSet());
            differentGroupIds.addAll(groupIdsToRemove);

            //Check if is authorized to update groups
            if (!differentGroupIds.isEmpty()) {
                for (String groupId : differentGroupIds) {
                    authz.assertAuthorized(identity, USER_GROUP_UPDATE, groupId, existingUser);
                }
            }
        }
    }

    /**
     * Check that the user attributes are valid (firstName, lastName, email)
     *
     * @param user the user to check
     */
    private void assertAttributesAreValid(User user) {
        if (user.getFirstName() != null && (user.getFirstName().isBlank() ||!user.getFirstName().matches(NAME_REGEX)))
            throw new IllegalArgumentException(FIRSTNAME_NOT_VALID.getMessageWithParameter(user.getFirstName()));
        if (user.getLastName() != null && (user.getLastName().isBlank() || !user.getLastName().matches(NAME_REGEX)))
            throw new IllegalArgumentException(LASTNAME_NOT_VALID.getMessageWithParameter(user.getLastName()));
        if (user.getEmail() != null && !user.getEmail().toLowerCase().matches(EMAIL_REGEX))
            throw new IllegalArgumentException(EMAIL_NOT_VALID.getMessageWithParameter(user.getEmail()));
    }

    /**
     * This method check if the email update flow should be triggered
     *
     * @param identity the identity of the
     * @return the email update flow must be triggered or not
     */
    private static boolean shouldTriggerEmailUpdateFlow(GazelleIdentity identity) {
        return !identity.hasGroup(KEYCLOAK_ADMIN);
    }

    /**
     * This method is responsible to trigger email update flow (disable user + send email)
     *
     * @param userId                 the id of the user
     * @param providedUserAttributes the attributes of the user that must be updated
     * @param existingUser           the current existing user
     * @param locale                 the locale to ues for the mails
     */
    private void performEmailUpdateFlow(String userId, User providedUserAttributes, User existingUser, GazelleIdentity identity, Locale locale) {
        if (!providedUserAttributes.getEmail().equals(existingUser.getEmail())) {
            // Check if email is already used by another providedUserAttributes
            try {
                userLookupService.getUserByEmail(providedUserAttributes.getEmail(), identity);
                throw new UserEditException(EMAIL_ALREADY_EXISTS.getMessage());
            } catch (NoSuchElementException _) {
                // Email not found, continue
            }

            if (providedUserAttributes.getFirstName() == null)
                providedUserAttributes.setFirstName(existingUser.getFirstName());
            if (providedUserAttributes.getLastName() == null)
                providedUserAttributes.setLastName(existingUser.getLastName());

            // Disable providedUserAttributes and generate new activation code
            userEditDAO.clearActivationCode(userId);
            providedUserAttributes.setActivated(false);
            providedUserAttributes.setActivationCode(UUID.randomUUID().toString().replace("-", ""));
            sendEmailsForEmailUpdate(providedUserAttributes, existingUser.getEmail(), locale);
        } else {
            providedUserAttributes.setEmail(null);
        }
    }

    private void checkOrganizationExists(String organizationId) {
        try {
            organizationLookupService.getOrganizationById(organizationId);
        } catch (NoSuchElementException e) {
            throw new UserEditException(ORGANIZATION_DOES_NOT_EXIST.getMessage(), e);
        }
    }

    /**
     * Manage groups linked to the organization when we update the organization of a user
     *
     * @param wantedGroupIds the expected group ids
     * @return the set of group ids after organization update
     */
    private Set<String> manageGroupsForOrganizationUpdate(String organizationId, Set<String> currentUserGroupIds, Set<String> wantedGroupIds) {
        Set<String> resultGroupIds = new HashSet<>(currentUserGroupIds);
        // If there are wanted groups, use them as base
        if (wantedGroupIds != null && !wantedGroupIds.isEmpty())
            resultGroupIds = new HashSet<>(wantedGroupIds);

        resultGroupIds = resultGroupIds.stream()
                .filter(groupId -> !groupId.startsWith(PREFIX_ORGANIZATION_ADMIN.getName()))
                .filter(groupId -> !groupId.startsWith(PREFIX_ORGANIZATION_MEMBER.getName()))
                .collect(Collectors.toSet());

        // Add the organization member group linked to the new organization
        resultGroupIds.add(PREFIX_ORGANIZATION_MEMBER.getName() + organizationId);

        return resultGroupIds;
    }

    private void sendEmailsForEmailUpdate(User user, String oldEmail, Locale locale) {
        editEmailManager.sendMailToOldEmailAddress(user, oldEmail, locale);
        editEmailManager.sendMailToValidateNewEmail(user, locale);
    }

    @Override
    public void activateUser(String userId, GazelleIdentity identity) {
        User existingUser = userEditDAO.getUserFromUserId(userId);
        authz.assertAuthorized(identity, USER_UPDATE, null, existingUser != null ? existingUser.getOrganizationId() : null);

        if (userId == null)
            throw new IllegalArgumentException(ErrorMessage.USER_ID_IS_NULL.getMessage());
        if (existingUser == null)
            throw new NoSuchElementException(USER_NOT_FOUND.getMessage());

        userEditDAO.updateActivatedStatusOfUser(userId, true);
        // Clear the activation code because the user is activated
        userEditDAO.clearActivationCode(userId);
    }

    @Override
    public void deactivateUser(String userId, GazelleIdentity identity) {
        User existingUser = userEditDAO.getUserFromUserId(userId);
        authz.assertAuthorized(identity, USER_UPDATE, null, existingUser != null ? existingUser.getOrganizationId() : null);

        if (userId == null)
            throw new IllegalArgumentException(ErrorMessage.USER_ID_IS_NULL.getMessage());
        if (existingUser == null)
            throw new NoSuchElementException(USER_NOT_FOUND.getMessage());

        userEditDAO.updateActivatedStatusOfUser(userId, false);
    }

    @Override
    public void deleteUser(String userId, GazelleIdentity identity, Locale locale) {
        User existingUser = userEditDAO.getUserFromUserId(userId);
        authz.assertAuthorized(identity, USER_DELETE, userId, existingUser != null ? existingUser.getOrganizationId() : null);

        if (userId == null)
            throw new IllegalArgumentException(ErrorMessage.USER_ID_IS_NULL.getMessage());
        if (existingUser == null)
            throw new NoSuchElementException(USER_NOT_FOUND.getMessage());

        editEmailManager.sendMailToDeletedUser(existingUser, locale);
        userEditDAO.deleteUser(userId);
        userEditDAO.archiveOrgaIfNoMembers(existingUser.getOrganizationId());
    }

    /**
     * Assert that the provided passwords are valid
     *
     * @param newPassword             the new password to validate
     * @param newPasswordConfirmation the confirmation of the new password
     */
    private void assertPasswordIsValid(String newPassword, String newPasswordConfirmation) {
        if (newPassword == null || newPasswordConfirmation == null)
            throw new IllegalArgumentException("At least one of the passwords is null");
        if (!newPassword.equals(newPasswordConfirmation))
            throw new IllegalArgumentException(PASSWORDS_NOT_EQUAL.getMessage());
        if (!isPasswordSecure(newPassword))
            throw new IllegalArgumentException(PASSWORD_NOT_SECURE.getMessage());
    }

    /**
     * Check is the provided password is enough secure
     *
     * @param password the provided password
     * @return true if the password is secure, false otherwise
     */
    private boolean isPasswordSecure(String password) {
        Matcher m = passwordPattern.matcher(password);
        return m.matches();
    }

    /**
     * Assert that the provided user is activated
     *
     * @param user the user to check the activation status
     * @throws IllegalStateException is the user is not activated
     */
    private void assertUserIsActivated(User user, Boolean futureActivationStatus) {
        if (Boolean.FALSE.equals(user.isActivated()) && Boolean.FALSE.equals(futureActivationStatus))
            throw new IllegalStateException("User is not activated");
    }
}
