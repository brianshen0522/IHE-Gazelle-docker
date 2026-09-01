package net.ihe.gazelle.validation.gateway.evs.business.service;

import net.ihe.gazelle.security.business.Authz;
import net.ihe.gazelle.security.business.GazelleIdentity;
import net.ihe.gazelle.security.business.Groups;
import net.ihe.gazelle.security.business.ProtectedResource;
import net.ihe.gazelle.security.business.acl.AccessControlList;
import net.ihe.gazelle.security.business.acl.ReadAccessKeyGenerator;
import net.ihe.gazelle.validation.gateway.evs.business.exception.ForbiddenException;
import net.ihe.gazelle.validation.gateway.evs.business.exception.UnauthorizedException;
import net.ihe.gazelle.validation.gateway.evs.business.model.ValidationRequestIdentity;

import java.util.LinkedHashSet;
import java.util.Set;

public class ValidationAccessPolicy {
    private static final String ITEM_READ_ACTION = "item:read";

    private final int readAccessKeyLength;
    private final ReadAccessKeyGenerator readAccessKeyGenerator;
    private final Authz authz;

    public ValidationAccessPolicy(int readAccessKeyLength,
                                  ReadAccessKeyGenerator readAccessKeyGenerator,
                                  Authz authz) {
        this.readAccessKeyLength = readAccessKeyLength;
        this.readAccessKeyGenerator = readAccessKeyGenerator;
        this.authz = authz;
    }

    public void assertCanAccess(AccessControlList acl, String privacyKey, GazelleIdentity identity) {
        if (acl == null || acl.isPublic()) {
            return;
        }
        if (privacyKey != null && privacyKey.equals(acl.getReadAccessKey())) {
            return;
        }
        if (identity == null || !identity.isAuthenticated()) {
            throw new UnauthorizedException("missing-authorization");
        }
        if (isIdentityAuthorizedThroughAcl(identity, acl)) {
            return;
        }
        throw new ForbiddenException("forbidden");
    }

    private boolean isIdentityAuthorizedThroughAcl(GazelleIdentity identity, AccessControlList acl) {
        ProtectedResource resource = () -> acl;
        return authz.isAuthorized(identity, ITEM_READ_ACTION, resource);
    }

    public AccessControlList buildAccessControlList(GazelleIdentity identity) {
        AccessControlList acl = new AccessControlList();
        boolean authenticated = identity != null && identity.isAuthenticated();
        acl.setPublic(!authenticated);
        if (authenticated) {
            acl.setOwners(resolveOwners(identity));
            acl.setReaders(resolveReaders(identity));
            acl.setReadAccessKey(readAccessKeyGenerator.generateReadAccessKey(readAccessKeyLength));
        } else {
            acl.setOwners(Set.of(Groups.ROLE_ADMIN));
            acl.setReaders(Set.of());
        }
        acl.setEditors(Set.of());
        return acl;
    }

    private Set<String> resolveOwners(GazelleIdentity identity) {
        if (isMachineToMachine(identity) || identity == null || identity.getId() == null || identity.getId().isBlank()) {
            return Set.of(Groups.ROLE_ADMIN);
        }
        return Set.of(identity.getId());
    }

    private Set<String> resolveReaders(GazelleIdentity identity) {
        LinkedHashSet<String> readers = new LinkedHashSet<>();
        readers.add(Groups.ROLE_MONITOR);
        readers.add(Groups.ROLE_TESTING_SESSION_MANAGER);
        readers.add(Groups.ROLE_PROJECT_ADMIN);
        addOrganizationGroup(readers, identity);
        return Set.copyOf(readers);
    }

    private void addOrganizationGroup(Set<String> readers, GazelleIdentity identity) {
        if (isMachineToMachine(identity)) {
            return;
        }
        String organizationGroup = identity != null ? identity.getOrganizationGroup() : null;
        if (organizationGroup != null && !organizationGroup.isBlank()) {
            readers.add(organizationGroup);
        }
    }

    private boolean isMachineToMachine(GazelleIdentity identity) {
        return identity instanceof ValidationRequestIdentity requestIdentity && requestIdentity.isMachineToMachine();
    }
}
