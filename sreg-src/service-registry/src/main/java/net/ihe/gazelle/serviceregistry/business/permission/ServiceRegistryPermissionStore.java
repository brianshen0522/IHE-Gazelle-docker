package net.ihe.gazelle.serviceregistry.business.permission;

import net.ihe.gazelle.security.business.Permission;
import net.ihe.gazelle.security.business.PermissionStore;
import net.ihe.gazelle.security.business.rbac.DefaultRBACPermissionStore;
import net.ihe.gazelle.security.business.rbac.RBACMatrixProvider;

import java.util.Set;

/**
 * This class is responsible to provide permissions for the Service Registry application.
 */
public class ServiceRegistryPermissionStore implements PermissionStore {

    /**
     * Permission to read services
     */
    public static final String PERMISSION_SERVICE_READ = "service:read";
    /**
     * Permission to register a service
     */
    public static final String PERMISSION_SERVICE_REGISTER = "service:register";

    private final PermissionStore delegate;

    /**
     * Default constructor
     *
     * @param rbacMatrixProvider Provider of the RBAC matrix
     */
    public ServiceRegistryPermissionStore(RBACMatrixProvider rbacMatrixProvider) {
        this.delegate = new DefaultRBACPermissionStore(rbacMatrixProvider);
    }

    @Override
    public Set<Permission> getPermissions() {
        return delegate.getPermissions();
    }

}
