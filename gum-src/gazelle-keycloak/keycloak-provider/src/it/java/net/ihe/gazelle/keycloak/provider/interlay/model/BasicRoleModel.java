package net.ihe.gazelle.keycloak.provider.interlay.model;

import org.keycloak.models.RoleContainerModel;
import org.keycloak.models.RoleModel;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * This class is an empty shell used for unit test, to be able to call Keycloak mocked method
 * Only id and name are currently used
 */
public class BasicRoleModel implements RoleModel {

    private final String id;
    private String name;

    public BasicRoleModel(String id, String name) {
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
    public String getDescription() {
        return "";
    }

    @Override
    public void setDescription(String description) {
        raiseUnsupportedOperationException();
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean isComposite() {
        return false;
    }

    @Override
    public void addCompositeRole(RoleModel role) { raiseUnsupportedOperationException(); }

    @Override
    public void removeCompositeRole(RoleModel role) { raiseUnsupportedOperationException(); }

    @Override
    public Stream<RoleModel> getCompositesStream(String search, Integer first, Integer max) {
        return Stream.empty();
    }

    @Override
    public boolean isClientRole() {
        return false;
    }

    @Override
    public String getContainerId() {
        return "";
    }

    @Override
    public RoleContainerModel getContainer() {
        return null;
    }

    @Override
    public boolean hasRole(RoleModel role) {
        return false;
    }

    @Override
    public void setSingleAttribute(String name, String value) {
        raiseUnsupportedOperationException();
    }

    @Override
    public void setAttribute(String name, List<String> values) {
        raiseUnsupportedOperationException();
    }

    @Override
    public void removeAttribute(String name) {
        raiseUnsupportedOperationException();
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

    private void raiseUnsupportedOperationException() {
        throw new UnsupportedOperationException("Empty shell");
    }
}
