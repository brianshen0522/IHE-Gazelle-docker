package net.ihe.gazelle.validation.gateway.quarkus.security;

import net.ihe.gazelle.security.business.Permission;
import net.ihe.gazelle.security.business.PermissionStore;
import net.ihe.gazelle.security.business.acl.DefaultACLPermissionStore;
import net.ihe.gazelle.security.business.rbac.DefaultRBACPermissionStore;
import net.ihe.gazelle.security.technical.rbac.RBACMatrixProviderImpl;

import java.util.HashSet;
import java.util.Set;

public class ValidationGatewayPermissionStore implements PermissionStore {

    private final PermissionStore delegate = new DefaultRBACPermissionStore(new RBACMatrixProviderImpl());
    private final PermissionStore itemAclPermissionStore = new DefaultACLPermissionStore("item", new RBACMatrixProviderImpl());

    @Override
    public Set<Permission> getPermissions() {
        Set<Permission> permissions = new HashSet<>(delegate.getPermissions());
        permissions.addAll(itemAclPermissionStore.getPermissions());
        return permissions;
    }
}
