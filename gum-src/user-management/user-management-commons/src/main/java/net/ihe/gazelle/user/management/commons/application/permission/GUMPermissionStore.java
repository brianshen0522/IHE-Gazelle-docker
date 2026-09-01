/*
 * Copyright 2024 IHE International.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.ihe.gazelle.user.management.commons.application.permission;

import net.ihe.gazelle.security.business.*;
import net.ihe.gazelle.user.management.api.domain.user.User;

import java.util.Objects;
import java.util.Set;

import static net.ihe.gazelle.user.management.api.domain.user.GazelleDefaultGroup.*;
import static net.ihe.gazelle.user.management.commons.application.user.registration.GUMIdentity.GUM_ADMIN;

public class GUMPermissionStore implements PermissionStore {

    private static final String KEYCLOAK_ADMIN = "keycloak_admin";

    private static final String ALL_USERS_TARGET = "all-users";
    private static final String USERS_TARGET = "users";
    private static final String USERS_PREFERENCES_TARGET = "users-preferences";
    private static final String ORGA_USERS_TARGET = "group-users";
    private static final String GROUP_TARGET = "groups";
    private static final String ORGANIZATION_TARGET = "organization";
    private static final String CREATE_ACTION = "create";
    private static final String DELETE_ACTION = "delete";
    private static final String UPDATE_ACTION = "update";
    private static final String READ_ACTION = "read";

    public static final String ALL_USER_READ = ALL_USERS_TARGET + "." + READ_ACTION;
    public static final String ORGA_USER_READ = ORGA_USERS_TARGET + "." + READ_ACTION;
    public static final String USER_CREATE = USERS_TARGET + "." + CREATE_ACTION;
    public static final String USER_PREFERENCES_DELETE = USERS_PREFERENCES_TARGET + "." + DELETE_ACTION;
    public static final String USER_DELETE = USERS_TARGET + "." + DELETE_ACTION;
    public static final String USER_UPDATE = USERS_TARGET + "." + UPDATE_ACTION;
    public static final String USER_GROUP_UPDATE = USERS_TARGET + "_" + GROUP_TARGET + "." + UPDATE_ACTION;
    public static final String USER_ORGANIZATION_UPDATE = USERS_TARGET + "_" + ORGANIZATION_TARGET + "." + UPDATE_ACTION;
    public static final String GROUP_READ = GROUP_TARGET + "." + READ_ACTION;
    public static final String GROUP_UPDATE = GROUP_TARGET + "." + UPDATE_ACTION;
    public static final String GROUP_CREATE = GROUP_TARGET + "." + CREATE_ACTION;
    public static final String GROUP_DELETE = GROUP_TARGET + "." + DELETE_ACTION;
    public static final String ORGANIZATION_CREATE = ORGANIZATION_TARGET + "." + CREATE_ACTION;
    public static final String ORGANIZATION_UPDATE = ORGANIZATION_TARGET + "." + UPDATE_ACTION;
    public static final String ORGANIZATION_READ = ORGANIZATION_TARGET + "." + READ_ACTION;
    public static final String ORGANIZATION_ARCHIVE = ORGANIZATION_TARGET + "." + DELETE_ACTION;

    @Override
    public Set<Permission> getPermissions() {
        return Set.of(
                new Permission(ALL_USER_READ, Policies.or(Policies.or(Policies.or(
                                        hasProjectAdminRightsPolicy(), hasTSMRolePolicy()),
                                checkIdentityIdPolicy()),
                        Policies.and(isOrgaAdministrator(), checkIdentityGroupPolicy()))),
                new Permission(ORGA_USER_READ, isOrgaAdministrator()),
                new Permission(USER_CREATE, Policies.or(Policies.or(
                                hasProjectAdminRightsPolicy(), hasTSMRolePolicy()),
                        Policies.and(isOrgaAdministrator(), checkIdentityGroupPolicy()))),
                new Permission(USER_UPDATE, Policies.or(Policies.or(Policies.or(
                                        hasProjectAdminRightsPolicy(), hasTSMRolePolicy()),
                                checkIdentityIdPolicy()),
                        Policies.and(isOrgaAdministrator(), checkIdentityGroupPolicy()))),
                new Permission(USER_GROUP_UPDATE, canUpdateGroup()),
                new Permission(USER_PREFERENCES_DELETE, Policies.or(Policies.or(Policies.or(
                                        hasProjectAdminRightsPolicy(), hasTSMRolePolicy()),
                                checkIdentityIdPolicy()),
                        Policies.and(isOrgaAdministrator(), checkIdentityGroupPolicy()))),
                new Permission(USER_DELETE, Policies.or(hasGazelleAdministratorRolePolicy(), checkIdentityIdPolicy())),
                new Permission(GROUP_CREATE, hasProjectAdminRightsPolicy()),
                new Permission(GROUP_READ, isAuthenticated()),
                new Permission(GROUP_UPDATE, hasProjectAdminRightsPolicy()),
                new Permission(GROUP_DELETE, hasProjectAdminRightsPolicy()),
                new Permission(ORGANIZATION_UPDATE, Policies.or(Policies.or(hasProjectAdminRightsPolicy(), hasTSMRolePolicy()),
                        Policies.and(isOrgaAdministrator(), checkIdentityGroupPolicy()))),
                new Permission(ORGANIZATION_READ, isAuthenticated()),
                new Permission(ORGANIZATION_ARCHIVE, Policies.or(hasProjectAdminRightsPolicy(), Policies.or(hasTSMRolePolicy(), hasGUMAdminGroupPolicy()))),
                new Permission(ORGANIZATION_CREATE, Policies.or(hasProjectAdminRightsPolicy(), Policies.or(hasTSMRolePolicy(), hasGUMAdminGroupPolicy()))),
                new Permission(USER_ORGANIZATION_UPDATE, isKeycloakPolicy())
        );
    }


    /**
     * This policy is responsible to check that the provided userId in context is equals to the id of the current identity
     * This happens when users want to perform actions on themselves.
     */
    private static Policy checkIdentityIdPolicy() {
        return (identity, context) -> {
            if (context == null)
                throw new MissingContextException("Missing user id in context");
            return isAuthenticated().evaluate(identity, context) && context.length > 0 && identity.getId().equals(context[0]);
        };
    }

    /**
     * This policy is responsible to check that the provided organization id in context is equals to the organization id of the current identity
     * This appends when users want to perform actions on users of same organization (with organization admin role)
     */
    private static Policy checkIdentityGroupPolicy() {
        return (identity, context) -> {
            if (context == null)
                throw new MissingContextException("Missing group id in context");
            return isAuthenticated().evaluate(identity, context)
                    && context.length > 1
                    && context[1] != null
                    && context[1].equals(identity.getOrganizationId());
        };
    }

    private static Policy hasProjectAdministratorRolePolicy() {
        return (identity, context) -> isAuthenticated().evaluate(identity, context)
                && (identity.hasGroup(PROJECT_ADMIN.getName()));
    }

    private static Policy hasGUMAdminGroupPolicy() {
        return (identity, context) -> (identity.hasGroup(GUM_ADMIN));
    }

    /**
     * This policy is responsible to check that the identity is a super administrator or a project administrator,
     * so he can do what he wants to do
     */
    private static Policy hasProjectAdminRightsPolicy() {
        return (identity, context) -> isAuthenticated().evaluate(identity, context)
                && (hasGazelleAdministratorRolePolicy().evaluate(identity, context)
                || hasProjectAdministratorRolePolicy().evaluate(identity, context));
    }

    /**
     * This policy is responsible to check that the identity is authenticated
     */
    private static Policy isAuthenticated() {
        return (identity, context) -> identity != null && identity.isAuthenticated();
    }

    /**
     * This policy is responsible to check that the identity is a Testing Session Manager, so he can do what he wants to do
     */
    private static Policy hasTSMRolePolicy() {
        return (identity, context) -> isAuthenticated().evaluate(identity, context) && (identity.hasGroup(TESTING_SESSION_MANAGER.getName()));
    }

    /**
     * This policy is responsible to check that the identity is an organization administrator
     */
    private static Policy isOrgaAdministrator() {
        return (identity, context) -> isAuthenticated().evaluate(identity, context) && identity.getGroups().stream().anyMatch(group -> group.startsWith(PREFIX_ORGANIZATION_ADMIN.getName()));
    }

    /**
     * This policy is responsible to check that the identity is a Gazelle administrator
     */
    private static Policy hasGazelleAdministratorRolePolicy() {
        return (identity, context) -> isAuthenticated().evaluate(identity, context) && (identity.hasGroup(GAZELLE_ADMIN.getName()) || identity.hasGroup(KEYCLOAK_ADMIN));
    }

    /**
     * This policy is responsible to check that the identity is a Gazelle administrator
     */
    private static Policy isKeycloakPolicy() {
        return (identity, context) -> isAuthenticated().evaluate(identity, context)
                && identity.hasGroup(KEYCLOAK_ADMIN);
    }

    /**
     * This policy checks if the curren identity is not the user as the one in the context
     */
    private static Policy isNotHimself() {
        return (identity, context) -> {
            if (context == null)
                throw new MissingContextException("Missing user id in context");
            return isAuthenticated().evaluate(identity, context) && context.length > 0 && !identity.getId().equals(context[0]);
        };
    }

    private static Policy isHimself() {
        return (identity, context) -> {
            if (context == null)
                throw new MissingContextException("Missing user id in context");
            return isAuthenticated().evaluate(identity, context) && context.length > 0 && identity.getId().equals(context[0]);
        };
    }

    /**
     * This policy is responsible for checking the user group can be added/removed
     */
    private static Policy canUpdateGroup() {
        return (GUMPermissionStore::assertAuthorizedToUpdateGroup);
    }

    /**
     * That the identity can update a particular group to a user
     *
     * @param identity the logged identity
     * @param context  the context required
     * @return true if authorized, false otherwise
     * @throws MissingContextException if context is not complete
     */
    private static boolean assertAuthorizedToUpdateGroup(GazelleIdentity identity, Object[] context) {
        if (context == null || context.length < 2)
            throw new MissingContextException("Context is missing at least one those: groupId or user");

        String groupId = (String) context[0];
        User user = (User) context[1];

        if (identity == null)
            return false;
        // If identity is keycloak admin, OK
        if (identity.hasGroup(KEYCLOAK_ADMIN))
            return true;
        // If identity is gazelle admin, he can edit all roles except its own gazelle admin role
        if (identity.hasGroup(GAZELLE_ADMIN.getName()))
            return !(Objects.equals(groupId, GAZELLE_ADMIN.getName()) && isHimself().evaluate(identity, user.getId()));
        // If identity is project admin, he can edit all roles except gazelle admin and project admin ones
        if (identity.hasGroup(PROJECT_ADMIN.getName()))
            return !Objects.equals(groupId, GAZELLE_ADMIN.getName()) && !Objects.equals(groupId, PROJECT_ADMIN.getName());
        // If identity is tsm, he can edit all roles except gazelle admin, project admin and testing session manager ones
        if (identity.hasGroup(TESTING_SESSION_MANAGER.getName()))
            return !Objects.equals(groupId, TESTING_SESSION_MANAGER.getName())
                  && !Objects.equals(groupId, GAZELLE_ADMIN.getName())
                  && !Objects.equals(groupId, PROJECT_ADMIN.getName());
        // If identity is orga admin, he can edit sut operator role
        if (identity.getGroups().stream().anyMatch(group -> group.startsWith(PREFIX_ORGANIZATION_ADMIN.getName())))
            return isNotHimself().evaluate(identity, user.getId())
                  && Objects.equals(identity.getOrganizationId(), user.getOrganizationId())
                  && (Objects.equals(groupId, SUT_OPERATOR.getName())
                     || groupId.startsWith(PREFIX_ORGANIZATION_ADMIN.getName())
                     || groupId.startsWith(PREFIX_ORGANIZATION_MEMBER.getName())
                  );

        return false;
    }
}
