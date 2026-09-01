package net.ihe.gazelle.keycloak.provider.interlay;

import jakarta.ws.rs.core.Response;
import net.ihe.gazelle.security.business.GazelleIdentity;
import net.ihe.gazelle.user.management.api.application.ConflictException;
import net.ihe.gazelle.user.management.api.application.group.GroupService;
import net.ihe.gazelle.user.management.api.application.organization.OrganizationManagementService;
import net.ihe.gazelle.user.management.api.application.user.edit.UserEditService;
import net.ihe.gazelle.user.management.api.domain.group.Group;
import net.ihe.gazelle.user.management.api.domain.user.DelegatedUser;
import net.ihe.gazelle.user.management.api.domain.user.User;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.*;
import org.keycloak.services.ErrorResponseException;
import org.keycloak.storage.adapter.AbstractUserAdapterFederatedStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Stream;

/**
 * Adapter that exposes Gazelle user data to Keycloak.
 * Read operations (getGroupsStream, getRoleMappingsStream, hasRole) are pure reads from gazelleUser.
 * Write operations (grantRole, joinGroup, etc.) update both Gazelle and Keycloak federated storage.
 */
public class GazelleUserModelAdapter extends AbstractUserAdapterFederatedStorage {

    private static final Logger log = LoggerFactory.getLogger(GazelleUserModelAdapter.class);
    private static final String ORGA_PREFIX = "org:";
    private final User gazelleUser;
    private final UserEditService userEditService;
    private final GroupService groupService;
    private final OrganizationManagementService organizationManagementService;
    private final GazelleIdentity identity;

    public GazelleUserModelAdapter(KeycloakSession session, RealmModel realm,
                                   ComponentModel storageProviderModel, User gazelleUser,
                                   UserEditService userEditService, GroupService groupService,
                                   OrganizationManagementService organizationManagementService,
                                   GazelleIdentity identity) {
        super(session, realm, storageProviderModel);
        this.gazelleUser = new User(gazelleUser);
        this.userEditService = userEditService;
        this.groupService = groupService;
        this.organizationManagementService = organizationManagementService;
        this.identity = identity;
    }

    public User getUser() {
        return this.gazelleUser;
    }

    public String getGazelleId() {
        return this.gazelleUser.getId();
    }

    @Override
    public String getUsername() {
        return this.gazelleUser.getId();
    }

    @Override
    public void setUsername(String s) {
        this.gazelleUser.setId(s);
    }

    @Override
    public long getGroupsCount() {
        return this.gazelleUser.getOrganizationId() == null ? 0 : 1;
    }

    @Override
    public boolean hasDirectRole(RoleModel role) {
        return hasRole(role);
    }

    @Override
    public void grantRole(RoleModel role) {
        if (role != null && !hasGazelleGroup(role)) {
            try {
                this.groupService.createGroup(new Group(role.getName()), identity);
            } catch (ConflictException _) {
                log.info("Group {} already exists in Gazelle, skipping creation", role.getName());
            }
            try {
                this.groupService.joinGroup(this.getGazelleId(), role.getName(), identity);
            } catch (Exception e) {
                log.info("Unable to join user {} to group {} in Gazelle: {}", this.getGazelleId(), role.getName(), e.getMessage());
            }
        }
        if (role != null && !super.hasRole(role)) {
            try {
                super.grantRole(role);
            } catch (Exception e) {
                // Ignore duplicate role mapping errors
                log.info("Failed to grant role {} to user {} (likely already granted): {}",
                        role.getName(), this.getGazelleId(), e.getMessage());
            }
        }
    }

    @Override
    public void deleteRoleMapping(RoleModel role) {
        if (role != null && hasGazelleGroup(role))
            this.groupService.leaveGroup(this.getGazelleId(), role.getName(), identity);
        if (role != null && super.hasRole(role))
            super.deleteRoleMapping(role);
    }

    @Override
    public void setLastName(String lastName) {
        User user = new User();
        user.setLastName(lastName);
        this.userEditService.updateAttributes(this.gazelleUser.getId(), user, identity, Locale.ENGLISH);
        super.setLastName(lastName);
    }

    @Override
    public void setFirstName(String firstName) {
        User user = new User();
        user.setFirstName(firstName);
        this.userEditService.updateAttributes(this.gazelleUser.getId(), user, identity, Locale.ENGLISH);
        super.setFirstName(firstName);
    }

    @Override
    public void setEmail(String email) {
        User user = new User();
        user.setEmail(email);
        this.userEditService.updateAttributes(this.gazelleUser.getId(), user, identity, Locale.ENGLISH);
        super.setEmail(email);
    }

    @Override
    public void joinGroup(GroupModel group) {
        try {
            organizationManagementService.joinOrganization(this.getGazelleId(), group.getName());
        } catch (Exception e) {
            log.info("Unable to join user {} to organization {} in Gazelle: {}", this.getGazelleId(), group.getName(), e.getMessage());
        }
        super.getGroupsStream().forEach(this::leaveGroup);
        try {
            super.joinGroup(group);
        } catch (Exception e) {
            // Ignore duplicate group membership errors
            log.info("Failed to join group {} for user {} (likely already member): {}",
                    group.getName(), this.getGazelleId(), e.getMessage());
        }
    }

    /**
     * Returns groups based on Gazelle organization membership (read-only, no DB writes).
     */
    @Override
    public Stream<GroupModel> getGroupsStream() {
        String organizationId = this.gazelleUser.getOrganizationId();
        if (organizationId == null) {
            return Stream.empty();
        }
        // Find existing group by name from Gazelle organization
        return this.realm.getGroupsStream()
                .filter(group -> organizationId.equals(group.getName()));
    }

    @Override
    public boolean isMemberOf(GroupModel group) {
        String organizationId = this.gazelleUser.getOrganizationId();
        return organizationId != null && organizationId.equals(group.getName());
    }

    /**
     * Returns roles based on Gazelle group membership (read-only, no DB writes).
     */
    @Override
    public Stream<RoleModel> getRoleMappingsStream() {
        Set<String> gazelleGroupIds = this.gazelleUser.getGroupIds();
        if (gazelleGroupIds == null || gazelleGroupIds.isEmpty()) {
            return getOrganizationRoleStream();
        }

        Stream<RoleModel> groupRoles = gazelleGroupIds.stream()
                .map(this.realm::getRole)
                .filter(Objects::nonNull);

        return Stream.concat(groupRoles, getOrganizationRoleStream()).distinct();
    }

    private Stream<RoleModel> getOrganizationRoleStream() {
        String organizationId = this.gazelleUser.getOrganizationId();
        if (organizationId == null) {
            return Stream.empty();
        }
        String orgRoleName = ORGA_PREFIX + organizationId;
        RoleModel orgRole = this.realm.getRole(orgRoleName);
        return orgRole != null ? Stream.of(orgRole) : Stream.empty();
    }

    @Override
    public String getFirstName() {
        return this.gazelleUser.getFirstName() != null ? this.gazelleUser.getFirstName() : "";
    }

    @Override
    public void setAttribute(String attributeName, List<String> attributeValues) {
        User user = new User();
        if (attributeName.equals(UserModel.LAST_NAME)) {
            user.setLastName(attributeValues.getFirst());
        }
        if (attributeName.equals(UserModel.FIRST_NAME)) {
            user.setFirstName(attributeValues.getFirst());
        }
        if (attributeName.equals(UserModel.EMAIL)) {
            user.setEmail(attributeValues.getFirst());
        }

        try {
            userEditService.updateAttributes(this.gazelleUser.getId(), user, identity, Locale.ENGLISH);
        } catch (Exception e) {
            throw new ErrorResponseException(e.getMessage(), e.getMessage(), Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public String getLastName() {
        return this.gazelleUser.getLastName() != null ? this.gazelleUser.getLastName() : "";
    }

    @Override
    public String getEmail() {
        return this.gazelleUser.getEmail();
    }

    /**
     * Checks role based on Gazelle membership (read-only, no DB writes).
     */
    @Override
    public boolean hasRole(RoleModel role) {
        if (role == null) {
            return false;
        }
        return hasGazelleGroup(role) || hasGazelleOrganizationRole(role);
    }

    @Override
    public boolean isEnabled() {
        return this.gazelleUser.isActivated() || isTrustedByIdentityProvider();
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.gazelleUser.setActivated(enabled);
        try {
            if (enabled) {
                this.userEditService.activateUser(this.gazelleUser.getId(), identity);
            } else {
                this.userEditService.deactivateUser(this.gazelleUser.getId(), identity);
            }
        } catch (Exception e) {
            throw new ErrorResponseException("Unable to update user", e.getMessage(), Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        GazelleUserModelAdapter that = (GazelleUserModelAdapter) o;
        return Objects.equals(gazelleUser, that.gazelleUser);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), gazelleUser);
    }

    /**
     * If the user is delegated and has been disabled, if he is trying to log in from his IDP,
     * he is considered to be trusted, so we consider he is activated (he is set to activated in the block login event listener)
     */
    private boolean isTrustedByIdentityProvider() {
        return this.gazelleUser instanceof DelegatedUser
                && Boolean.FALSE.equals(this.gazelleUser.isActivated())
                && session.getContext().getAuthenticationSession() != null;
    }

    /**
     * Check if the user has a specific Gazelle group
     *
     * @param role the group to check
     * @return true if the user has the group, false otherwise
     */
    private boolean hasGazelleGroup(RoleModel role) {
        Set<String> groupIds = this.gazelleUser.getGroupIds();
        return groupIds != null && groupIds.stream().anyMatch(group -> group.equals(role.getName()));
    }

    private boolean hasGazelleOrganizationRole(RoleModel role) {
        String organizationId = this.gazelleUser.getOrganizationId();
        return organizationId != null && role.getName().equals(ORGA_PREFIX + organizationId);
    }

    @Override
    public boolean isEmailVerified() {
        return true;
    } // We don't use the email verification feature of Keycloak

    @Override
    public Map<String, List<String>> getAttributes() {
        Map<String, List<String>> result = super.getAttributes();
        if (result.get(UserModel.EMAIL).isEmpty() || result.get(UserModel.EMAIL).getFirst() == null)
            result.put(UserModel.EMAIL, List.of(this.gazelleUser.getEmail()));
        if (result.get(UserModel.FIRST_NAME).isEmpty() || result.get(UserModel.FIRST_NAME).getFirst() == null)
            result.put(UserModel.FIRST_NAME, List.of(this.gazelleUser.getFirstName()));
        if (result.get(UserModel.LAST_NAME).isEmpty() || result.get(UserModel.LAST_NAME).getFirst() == null)
            result.put(UserModel.LAST_NAME, List.of(this.gazelleUser.getLastName()));
        return result;
    }
}
