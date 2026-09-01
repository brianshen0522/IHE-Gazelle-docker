package net.ihe.gazelle.keycloak.provider.interlay;

import jakarta.ws.rs.core.Response;
import net.ihe.gazelle.keycloak.core.interlay.identity.KeycloakIdentity;
import net.ihe.gazelle.user.management.api.application.group.GroupService;
import net.ihe.gazelle.user.management.api.application.organization.OrganizationManagementService;
import net.ihe.gazelle.user.management.api.application.user.delegation.UserDelegationService;
import net.ihe.gazelle.user.management.api.application.user.edit.UserEditService;
import net.ihe.gazelle.user.management.api.application.user.login.UserLoginService;
import net.ihe.gazelle.user.management.api.application.user.lookup.UserLookupService;
import net.ihe.gazelle.user.management.api.application.user.lookup.UserQueryParams;
import net.ihe.gazelle.user.management.api.application.user.registration.UserRegistrationException;
import net.ihe.gazelle.user.management.api.application.user.registration.UserRegistrationService;
import net.ihe.gazelle.user.management.api.domain.user.User;
import net.ihe.gazelle.user.management.commons.interlay.utils.CustomSHA1HashService;
import org.keycloak.component.ComponentModel;
import org.keycloak.credential.CredentialInput;
import org.keycloak.credential.CredentialInputUpdater;
import org.keycloak.credential.CredentialInputValidator;
import org.keycloak.models.*;
import org.keycloak.models.credential.PasswordCredentialModel;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.services.ErrorResponseException;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.UserStorageProvider;
import org.keycloak.storage.user.UserLookupProvider;
import org.keycloak.storage.user.UserQueryProvider;
import org.keycloak.storage.user.UserRegistrationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static net.ihe.gazelle.user.management.api.application.user.lookup.UserQueryParams.*;

public class GazelleUserStorageProvider implements UserStorageProvider,
        UserLookupProvider, UserQueryProvider, CredentialInputUpdater, CredentialInputValidator,
        UserRegistrationProvider {

    private static final Logger log = LoggerFactory.getLogger(GazelleUserStorageProvider.class);
    private static final int DEFAULT_MAX_RESULT = 5000;
    private static final String ORG_PREFIX = "org:";

    private final UserLoginService userLoginService;
    private final UserLookupService userLookupService;
    private final UserEditService userEditService;
    private final GroupService groupService;
    private final UserDelegationService userDelegationService;
    private final OrganizationManagementService organizationManagementService;
    private final UserRegistrationService userRegistrationService;
    private final KeycloakIdentity keycloakIdentity;
    private final KeycloakSession session;
    private final ComponentModel model;
    private final CustomSHA1HashService sha1HashService;

    public GazelleUserStorageProvider(KeycloakSession session, ComponentModel model,
                                      UserLookupService userLookupService, UserLoginService userLoginService,
                                      UserEditService userEditService, UserRegistrationService userRegistrationService,
                                      GroupService groupService, OrganizationManagementService organizationManagementService,
                                      UserDelegationService userDelegationService) {
        this.session = session;
        this.model = model;
        this.userLookupService = userLookupService;
        this.userLoginService = userLoginService;
        this.userEditService = userEditService;
        this.userRegistrationService = userRegistrationService;
        this.groupService = groupService;
        this.organizationManagementService = organizationManagementService;
        this.sha1HashService = new CustomSHA1HashService();
        this.keycloakIdentity = new KeycloakIdentity();
        this.userDelegationService = userDelegationService;
    }

    @Override
    public boolean supportsCredentialType(String credentialType) {
        return PasswordCredentialModel.TYPE.equals(credentialType);
    }

    @Override
    public boolean isConfiguredFor(RealmModel realmModel, UserModel userModel, String credentialType) {
        return supportsCredentialType(credentialType);
    }

    @Override
    public boolean isValid(RealmModel realmModel, UserModel userModel, CredentialInput input) {
        if (!supportsCredentialType(input.getType()) || !(input instanceof UserCredentialModel cred)) {
            return false;
        }

        if (needToAddRequiredActionChangePassword(userModel)) {
            userModel.addRequiredAction(UserModel.RequiredAction.UPDATE_PASSWORD);
        }

        try {
            return userLoginService.validatePassword(userModel.getUsername(), cred.getChallengeResponse());
        } catch (NoSuchElementException _) {
            log.warn("Credentials not found for user : {}", userModel.getUsername());
            return false;
        }
    }

    @Override
    public boolean updateCredential(RealmModel realmModel, UserModel userModel, CredentialInput credentialInput) {
        try {
            if (PasswordCredentialModel.TYPE.equals(credentialInput.getType())) {
                this.userEditService.updatePasswordForUserId(userModel.getUsername(),
                        credentialInput.getChallengeResponse(), credentialInput.getChallengeResponse());
            }
        } catch (Exception e) {
            throw new ModelException(e.getMessage());
        }
        return true;
    }

    @Override
    public void disableCredentialType(RealmModel realmModel, UserModel userModel, String credentialType) {
        // Nothing to do here for the moment
    }

    @Override
    public Stream<String> getDisableableCredentialTypesStream(RealmModel realmModel, UserModel userModel) {
        return Stream.of();
    }

    @Override
    public void close() {
        // Nothing to do
    }

    @Override
    public UserModel getUserById(RealmModel realm, String userId) {
        StorageId sid = new StorageId(userId);
        return getEagerUserModelByService(
                () -> userLookupService.getUserById(sid.getExternalId(), keycloakIdentity),
                realm, userId);
    }

    @Override
    public UserModel getUserByUsername(RealmModel realm, String username) {
        StorageId sid = new StorageId(username);
        return getLazyUserModelByService(
                () -> userLookupService.getUserById(sid.getExternalId(), keycloakIdentity),
                realm, username);
    }

    @Override
    public UserModel getUserByEmail(RealmModel realm, String email) {
        return getLazyUserModelByService(
                () -> userLookupService.getUserByEmail(email, keycloakIdentity),
                realm, email);
    }

    private UserModel getEagerUserModelByService(Supplier<User> userSupplier, RealmModel realm, String identifier) {
        try {
            User user = userSupplier.get();
            return createEagerGazelleUserModelAdapter(realm, user);
        } catch (NoSuchElementException _) {
            log.info("User not found ({})", identifier);
            // Unable to throw exception here because logins will not fail properly (redirect to error page)
            return null;
        } catch (Exception e) {
            log.warn(String.format("Unable to get the user %s", identifier), e);
            // Unable to throw exception here because logins will not fail properly (redirect to error page)
            return null;
        }
    }

    private UserModel getLazyUserModelByService(Supplier<User> userSupplier, RealmModel realm, String identifier) {
        try {
            User user = userSupplier.get();
            return createLazyGazelleUserModelAdapter(realm, user);
        } catch (NoSuchElementException _) {
            log.info("User not found ({})", identifier);
            // Unable to throw exception here because logins will not fail properly (redirect to error page)
            return null;
        } catch (Exception e) {
            log.warn(String.format("Unable to get the user %s", identifier), e);
            // Unable to throw exception here because logins will not fail properly (redirect to error page)
            return null;
        }
    }

    @Override
    public Stream<UserModel> searchForUserStream(RealmModel realmModel, Map<String, String> params) {
        return searchForUserStream(realmModel, params, 0, DEFAULT_MAX_RESULT);
    }

    @Override
    public Stream<UserModel> searchForUserStream(RealmModel realmModel, Map<String, String> map, Integer firstResult,
                                                 Integer offset) {
        // Search for users using map values
        UserQueryParams userQueryParams = UserQueryParams.nullQuery()
                .setSearch(map.get("keycloak.session.realm.users.query.search"))
                .setAttribute(ATTR_FIRST_NAME, map.get("firstname"))
                .setAttribute(ATTR_LAST_NAME, map.get("lastname"))
                .setAttribute(ATTR_EMAIL, map.get("email"));
        return userLookupService.searchAndFilterUsers(userQueryParams, firstResult, offset, keycloakIdentity)
                .stream().map(user -> createLazyGazelleUserModelAdapter(realmModel, user));
    }

    @Override
    public Stream<UserModel> searchForUserByUserAttributeStream(RealmModel realm, String attributeName,
                                                                String attributeValue) {
        UserQueryParams userQueryParams = switch (attributeName) {
            case "email" -> UserQueryParams.nullQuery().setAttribute(ATTR_EMAIL, attributeValue);
            case "firstname" -> UserQueryParams.nullQuery().setAttribute(ATTR_FIRST_NAME, attributeValue);
            case "lastname" -> UserQueryParams.nullQuery().setAttribute(ATTR_LAST_NAME, attributeValue);
            default -> UserQueryParams.nullQuery().setSearch(attributeValue);
        };
        List<User> users = userLookupService.searchAndFilterUsers(userQueryParams, 0, DEFAULT_MAX_RESULT, keycloakIdentity);
        return users.stream().map(user -> createLazyGazelleUserModelAdapter(realm, user));
    }


    @Override
    public Stream<UserModel> getGroupMembersStream(RealmModel realmModel, GroupModel groupModel, Integer offset, Integer numberOfResults) {
        UserQueryParams userQueryParams = UserQueryParams.nullQuery().setAttribute(ATTR_ORGANIZATION_ID,groupModel.getName());
        return userLookupService.searchAndFilterUsers(userQueryParams, offset, numberOfResults, keycloakIdentity).stream().map(user ->
                createLazyGazelleUserModelAdapter(realmModel, user));
    }

    @Override
    public Stream<UserModel> getGroupMembersStream(RealmModel realm, GroupModel group) {
        return getGroupMembersStream(realm, group, 0, DEFAULT_MAX_RESULT);
    }


    @Override
    public UserModel addUser(RealmModel realm, String username) {
        // The API of Keycloak to create a user does not match the API of Gazelle for user registration.
        // So when we create a user in Keycloak we set default values because after we know that these values will be
        // updated by Keycloak just after
        String sanitizedSuffix = username.replaceAll("[0-9]", "");
        User user = new User(username, "tmp" + sanitizedSuffix, "tmp" + sanitizedSuffix, sanitizedSuffix + "@tmp.fr");

        // Users are activated by default because they are created from Keycloak by a Gazelle admin, a Keycloak admin or by delegation login
        user.setActivated(true);
        AuthenticationSessionModel context = session.getContext().getAuthenticationSession();
        try {
            if (context != null) {
                user = createIdpUser(user, context);
            } else {
                user = userRegistrationService.createUser(user, keycloakIdentity, Locale.ENGLISH);
            }
            return createEagerGazelleUserModelAdapter(realm, user);
        } catch (IllegalArgumentException | UserRegistrationException e) {
            throw new ErrorResponseException("Unable to add user", e.getMessage(), Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public boolean removeUser(RealmModel realm, UserModel user) {
        try {
            userEditService.deleteUser(user.getUsername(), keycloakIdentity, Locale.ENGLISH);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public Stream<UserModel> getRoleMembersStream(RealmModel realm, RoleModel role) {
        return getRoleMembersStream(realm, role, 0, DEFAULT_MAX_RESULT);
    }

    @Override
    public Stream<UserModel> getRoleMembersStream(RealmModel realm, RoleModel role, Integer firstResult,
                                                  Integer maxResults) {
        UserQueryParams query = new UserQueryParams("", null, null, null, role.getName(), null, null, null, null, null);
        Stream<User> users = userLookupService.searchAndFilterUsers(query, firstResult, maxResults, keycloakIdentity).stream();
        return users.map(user -> createLazyGazelleUserModelAdapter(realm, user));
    }

    private boolean needToAddRequiredActionChangePassword(UserModel userModel) {
        return userLoginService.needToChangePassword(userModel.getUsername())
                && !userDelegationService.isUserDelegatedFromId(userModel.getUsername());
    }

    private GazelleUserModelAdapter createEagerGazelleUserModelAdapter(RealmModel realm, User user) {
        GazelleUserModelAdapter gazelleUserModelAdapter = new GazelleUserModelAdapter(session, realm, model, user,
                userEditService, groupService, organizationManagementService, keycloakIdentity);
        Set<String> groupIds = user.getGroupIds();
        setGazelleOrganization(gazelleUserModelAdapter, user.getOrganizationId(), realm);
        setGazelleGroups(gazelleUserModelAdapter, groupIds);

        return gazelleUserModelAdapter;
    }

    private GazelleUserModelAdapter createLazyGazelleUserModelAdapter(RealmModel realm, User user) {
        return new GazelleUserModelAdapter(session, realm, model, user,
                userEditService, groupService, organizationManagementService, keycloakIdentity);
    }

    private void setGazelleGroups(UserModel usermodel, Set<String> gazelleGroups) {
        // Manual synchronization to ensure that the roles are correctly set in Keycloak and Gazelle
        usermodel.getRoleMappingsStream()
                .filter(role -> role != null && role.getName().contains(":") && !gazelleGroups.contains(role.getName()))
                .forEach(usermodel::deleteRoleMapping);

        gazelleGroups.stream()
                .filter(roleString -> usermodel.getRoleMappingsStream().noneMatch(role -> role != null && role.getName().equals(roleString)))
                .forEach(groupString -> grantAndCreateKeycloakRoleIfNecessary(usermodel, groupString));
    }

    private void grantAndCreateKeycloakRoleIfNecessary(UserModel usermodel, String groupString) {
        RealmModel realm = session.getContext().getRealm();

        RoleModel roleModel = realm.getRole(groupString);
        if (roleModel == null && !usermodel.getGroupsStream().toList().isEmpty()) {
            roleModel = getOrCreateRole(realm, groupString);
        }
        if (roleModel != null)
            usermodel.grantRole(roleModel);
    }

    private void setGazelleOrganization(UserModel userModel, String gazelleGroupId, RealmModel realm) {
        if (gazelleGroupId == null) {
            return;
        }

        String encodedGroupId = ORG_PREFIX + sha1HashService.encode(gazelleGroupId);
        String realmId = realm.getId();
        String username = userModel.getUsername();

        // 1. Ensure group exists
        ensureGroupExists(realmId, encodedGroupId, gazelleGroupId);

        // 2. Ensure user is in the group (in a separate transaction)
        ensureUserIsInGroup(realmId, username, encodedGroupId);
    }

    private void ensureUserIsInGroup(String realmId, String username, String encodedGroupId) {
        try {
            KeycloakModelUtils.runJobInTransaction(session.getKeycloakSessionFactory(), separateSession -> {
                RealmModel separateRealm = separateSession.realms().getRealm(realmId);
                UserModel separateUser = separateSession.users().getUserByUsername(separateRealm, username);
                if (separateUser != null) {
                    GroupModel group = separateRealm.getGroupById(encodedGroupId);
                    if (group != null && !separateUser.isMemberOf(group)) {
                        // Leave other groups first
                        separateUser.getGroupsStream()
                                .filter(g -> !g.getId().equals(group.getId()))
                                .forEach(separateUser::leaveGroup);
                        separateUser.joinGroup(group);
                    }
                }
            });
        } catch (Exception e) {
            log.debug("User group membership update failed (likely already member): {} - {}", username, e.getMessage());
        }
    }

    /**
     * Ensures a group exists in the realm, creating it if necessary.
     */
    private void ensureGroupExists(String realmId, String groupId, String groupName) {
        try {
            KeycloakModelUtils.runJobInTransaction(session.getKeycloakSessionFactory(), separateSession -> {
                RealmModel separateRealm = separateSession.realms().getRealm(realmId);
                GroupModel existingGroup = separateRealm.getGroupById(groupId);
                if (existingGroup == null) {
                    separateRealm.createGroup(groupId, groupName);
                }
            });
        } catch (Exception e) {
            log.debug("Group creation failed (likely already exists): {} - {}", groupId, e.getMessage());
        }
    }

    /**
     * Ensures a role exists in the realm, creating it if necessary.
     */
    private void ensureRoleExists(String realmId, String roleName) {
        try {
            KeycloakModelUtils.runJobInTransaction(session.getKeycloakSessionFactory(), separateSession -> {
                RealmModel separateRealm = separateSession.realms().getRealm(realmId);
                if (separateRealm.getRole(roleName) == null) {
                    separateRealm.addRole(roleName);
                }
            });
        } catch (Exception e) {
            log.debug("Role creation failed (likely already exists): {} - {}", roleName, e.getMessage());
        }
    }

    /**
     * Gets or creates a role in the realm.
     * Used by setGazelleGroups for role-based group management.
     */
    private RoleModel getOrCreateRole(RealmModel realm, String roleName) {
        RoleModel roleModel = realm.getRole(roleName);
        if (roleModel != null) {
            return roleModel;
        }

        ensureRoleExists(realm.getId(), roleName);
        return realm.getRole(roleName);
    }

    private User createIdpUser(User user, AuthenticationSessionModel context) {
        String idpName = context.getAuthNote("GZL_IDP_ID");
        String externalId = context.getAuthNote("GZL_IDP_USER_EXTERNAL_ID");
        return userDelegationService.createDelegatedUser(user, externalId, idpName);
    }
}
