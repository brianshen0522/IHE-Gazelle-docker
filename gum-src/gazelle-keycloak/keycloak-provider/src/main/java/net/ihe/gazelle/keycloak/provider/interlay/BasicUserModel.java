package net.ihe.gazelle.keycloak.provider.interlay;

import org.keycloak.models.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Basic implementation of UserModel for handling user data in Keycloak providers.
 *
 * This class provides a minimal implementation of the Keycloak UserModel interface,
 * focusing on essential user properties like email, first name, last name, and
 * enabled status. It's primarily used for JSON deserialization and basic user
 * operations within the Gazelle Keycloak provider ecosystem.
 *
 */
public class BasicUserModel implements UserModel {

    /**
     * The user's email address.
     */
    private String userEmail;

    /**
     * The user's first name.
     */
    private String firstName;

    /**
     * The user's last name.
     */
    private String lastName;

    /**
     * Flag indicating whether the user account is enabled.
     */
    private boolean isEnabled;

    /**
     * Default constructor for JSON deserialization.
     *
     * This constructor is required by Jackson for JSON deserialization.
     */
    public BasicUserModel() {
        // For Jackson
    }

    @Override
    public String getId() {
        return null;
    }

    @Override
    public String getUsername() {
        return null;
    }

    @Override
    public void setUsername(String s) {
        // Nothing to do here
    }

    @Override
    public Long getCreatedTimestamp() {
        return null;
    }

    @Override
    public void setCreatedTimestamp(Long aLong) {
        // Nothing to do here
    }

    @Override
    public boolean isEnabled() {
        return isEnabled;
    }

    @Override
    public void setEnabled(boolean b) {
        this.isEnabled = b;
    }

    @Override
    public void setSingleAttribute(String s, String s1) {
        // Nothing to do here
    }

    @Override
    public void setAttribute(String s, List<String> list) {
        // Nothing to do here
    }

    @Override
    public void removeAttribute(String s) {
        // Nothing to do here
    }

    @Override
    public String getFirstAttribute(String s) {
        return null;
    }

    @Override
    public Stream<String> getAttributeStream(String s) {
        return null;
    }

    @Override
    public Map<String, List<String>> getAttributes() {
        return null;
    }

    @Override
    public Stream<String> getRequiredActionsStream() {
        return null;
    }

    @Override
    public void addRequiredAction(String s) {
        // Nothing to do here
    }

    @Override
    public void removeRequiredAction(String s) {
        // Nothing to do here
    }

    @Override
    public String getFirstName() {
        return firstName;
    }

    @Override
    public void setFirstName(String s) {
        firstName = s;
    }

    @Override
    public String getLastName() {
        return lastName;
    }

    @Override
    public void setLastName(String s) {
        lastName = s;
    }

    @Override
    public String getEmail() {
        return userEmail;
    }

    @Override
    public void setEmail(String s) {
        userEmail = s;
    }

    @Override
    public boolean isEmailVerified() {
        return false;
    }

    @Override
    public void setEmailVerified(boolean b) {
        // Nothing to do here
    }

    @Override
    public Stream<GroupModel> getGroupsStream() {
        return null;
    }

    @Override
    public void joinGroup(GroupModel groupModel) {
        // Nothing to do here
    }

    @Override
    public void leaveGroup(GroupModel groupModel) {
        // Nothing to do here
    }

    @Override
    public boolean isMemberOf(GroupModel groupModel) {
        return false;
    }

    @Override
    public String getFederationLink() {
        return null;
    }

    @Override
    public void setFederationLink(String s) {
        // Nothing to do here
    }

    @Override
    public String getServiceAccountClientLink() {
        return null;
    }

    @Override
    public void setServiceAccountClientLink(String s) {
        // Nothing to do here
    }

    @Override
    public SubjectCredentialManager credentialManager() {
        return null;
    }

    @Override
    public Stream<RoleModel> getRealmRoleMappingsStream() {
        return null;
    }

    @Override
    public Stream<RoleModel> getClientRoleMappingsStream(ClientModel clientModel) {
        return null;
    }

    @Override
    public boolean hasRole(RoleModel roleModel) {
        return false;
    }

    @Override
    public void grantRole(RoleModel roleModel) {
        // Nothing to do here
    }

    @Override
    public Stream<RoleModel> getRoleMappingsStream() {
        return null;
    }

    @Override
    public void deleteRoleMapping(RoleModel roleModel) {
        // Nothing to do here
    }
}
