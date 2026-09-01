package net.ihe.gazelle.keycloak.provider.interlay.model;

import org.keycloak.models.ClientModel;
import org.keycloak.models.GroupModel;
import org.keycloak.models.OrganizationModel;
import org.keycloak.models.RoleModel;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class BasicGroupModel implements GroupModel {

    private final String id;
    private String name;
    private String description;

    public BasicGroupModel(String id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public void setSingleAttribute(String name, String value) {

    }

    @Override
    public void setAttribute(String name, List<String> values) {

    }

    @Override
    public void removeAttribute(String name) {

    }

    @Override
    public String getFirstAttribute(String name) {
        return "";
    }

    @Override
    public Stream<String> getAttributeStream(String name) {
        return Stream.empty();
    }

    @Override
    public Map<String, List<String>> getAttributes() {
        return Map.of();
    }

    @Override
    public GroupModel getParent() {
        return null;
    }

    @Override
    public String getParentId() {
        return "";
    }

    @Override
    public Stream<GroupModel> getSubGroupsStream() {
        return Stream.empty();
    }

    @Override
    public void setParent(GroupModel group) {

    }

    @Override
    public void addChild(GroupModel subGroup) {

    }

    @Override
    public void removeChild(GroupModel subGroup) {

    }

    @Override
    public OrganizationModel getOrganization() {
        return null;
    }

    @Override
    public Stream<RoleModel> getRealmRoleMappingsStream() {
        return Stream.empty();
    }

    @Override
    public Stream<RoleModel> getClientRoleMappingsStream(ClientModel app) {
        return Stream.empty();
    }

    @Override
    public boolean hasRole(RoleModel role) {
        return false;
    }

    @Override
    public void grantRole(RoleModel role) {

    }

    @Override
    public Stream<RoleModel> getRoleMappingsStream() {
        return Stream.empty();
    }

    @Override
    public void deleteRoleMapping(RoleModel role) {

    }
}
