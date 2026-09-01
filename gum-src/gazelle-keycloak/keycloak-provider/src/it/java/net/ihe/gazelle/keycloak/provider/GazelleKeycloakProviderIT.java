package net.ihe.gazelle.keycloak.provider;

import net.ihe.gazelle.keycloak.provider.utils.KeycloakAdminOperator;
import net.ihe.gazelle.keycloak.provider.utils.KeycloakITUserDAO;
import net.ihe.gazelle.keycloak.provider.utils.SystemPropertyDBConfig;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.resource.*;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.Arrays;
import java.util.List;

import static net.ihe.gazelle.user.management.api.domain.user.GazelleDefaultGroup.GAZELLE_ADMIN;
import static net.ihe.gazelle.user.management.api.domain.user.GazelleDefaultGroup.MONITOR;
import static org.junit.jupiter.api.Assertions.*;

class GazelleKeycloakProviderIT {

    private static RealmResource gazelleRealm;
    private static RealmResource masterRealm;

    @BeforeAll
    public static void setUp() {
        KeycloakAdminOperator keycloakAuthentication = new KeycloakAdminOperator();
        gazelleRealm = keycloakAuthentication.getRealmGazelle();
        masterRealm = keycloakAuthentication.getKeycloakInstance().realm("master");
    }

    @Test
    void verifyUser() {
        // Get users
        UsersResource usersResource = gazelleRealm.users();
        List<UserRepresentation> usersRepresentation = usersResource.searchByEmail("user1@gazelle.com",true);

        assertFalse(usersRepresentation.isEmpty());
        UserRepresentation userRepresentation = usersRepresentation.getFirst();
        assertEquals("user1", userRepresentation.getUsername());
        assertEquals("user1@gazelle.com", userRepresentation.getEmail());
        assertEquals("user1 fn", userRepresentation.getFirstName());
        assertEquals("user1 ln", userRepresentation.getLastName());
        assertEquals(true, userRepresentation.isEnabled());
    }

    @Test
    void verifyInactiveUser() {
        // Get users
        UsersResource usersResource = gazelleRealm.users();
        UserRepresentation userRepresentation = usersResource.searchByEmail("inactive1@gazelle.com", true).get(0);
        assertNotNull(userRepresentation);
        assertEquals("inactiveUser", userRepresentation.getUsername());
        assertEquals("inactive1@gazelle.com", userRepresentation.getEmail());
        assertEquals("inactive1_firstname", userRepresentation.getFirstName());
        assertEquals("inactive1_lastname", userRepresentation.getLastName());
        assertEquals(false, userRepresentation.isEnabled());
    }

    @Test
    void verifyOldRolesAreNotPresent() {
        // Get gazelle roles names from realm
        RolesResource rolesResource = gazelleRealm.roles();
        List<RoleRepresentation> gazelleRoles = rolesResource.list().stream()
                .filter(roleRepresentation -> roleRepresentation.getName().contains("_role")).toList();
        List<String> gazelleRolesNames = gazelleRoles.stream().map(RoleRepresentation::getName).toList();

        assertFalse(gazelleRolesNames.contains("admin_role"));
        assertFalse(gazelleRolesNames.contains("vendor_role"));
        assertFalse(gazelleRolesNames.contains("monitor_role"));
        assertFalse(gazelleRolesNames.contains("vendor_admin_role"));
        assertFalse(gazelleRolesNames.contains("user_role"));
        assertFalse(gazelleRolesNames.contains("testing_session_admin_role"));
        assertFalse(gazelleRolesNames.contains("tests_editor_role"));
    }

    @Test
    void verifyNewRolesArePresent() {
        RolesResource rolesResource = gazelleRealm.roles();
        List<RoleRepresentation> gazelleRoles = rolesResource.list();
        List<String> gazelleRolesNames = gazelleRoles.stream().map(RoleRepresentation::getName).toList();

        assertTrue(gazelleRolesNames.contains(GAZELLE_ADMIN.getName()));
        assertTrue(gazelleRolesNames.contains(MONITOR.getName()));
    }

    @Test
    void verifyGroups() {
        // Search all users to import groups
        gazelleRealm.users().search("");

        // Get groups names from realm
        GroupsResource groupsResource = gazelleRealm.groups();
        List<GroupRepresentation> gazelleGroups = groupsResource.groups().stream().toList();
        List<String> gazelleGroupsNames = gazelleGroups.stream().map(GroupRepresentation::getName).toList();

        // Check presence of some groups
        assertTrue(gazelleGroupsNames.contains("INSTITution1"));
        assertTrue(gazelleGroupsNames.contains("institution2"));
        assertTrue(gazelleGroupsNames.contains("institution3"));
        assertTrue(gazelleGroupsNames.contains("institution4"));
    }

    @Test
    void getGroupForUser() {
        // Get user
        UsersResource usersResource = gazelleRealm.users();
        String userResourceId = usersResource.searchByEmail("user1@gazelle.com", true).getFirst().getId();
        UserResource userResource = usersResource.get(userResourceId);
        assertNotNull(userResource);

        // Get all organizations for user
        List<String> organizations = userResource.groups().stream().map(GroupRepresentation::getName).toList();
        assertTrue(organizations.contains("INSTITution1"));
    }


    @Test
    void verifyUpdateRoleKeycloakForUser() {
        // Get user
        UsersResource usersResource = gazelleRealm.users();
        String userResourceId = usersResource.searchByEmail("user1@gazelle.com", true).getFirst().getId();
        UserResource userResource = usersResource.get(userResourceId);

        assertNotNull(userResource);

        // Check roles in user realm roles
        List<String> roles = userResource.roles().realmLevel().listEffective().stream().map(RoleRepresentation::getName).toList();
        userResource.toRepresentation().getClientRoles();
        assertTrue(roles.contains(GAZELLE_ADMIN.getName()));

        // Add group in external DB
        KeycloakITUserDAO keycloakITDAO = getKeycloakITDAO();
        String userId = keycloakITDAO.getUserByEmail(userResource.toRepresentation().getEmail()).getId();
        assertEquals(1, keycloakITDAO.addRoleForUserId(userId,MONITOR.getName()));

        // Check roles in user realm roles
        roles = userResource.roles().realmLevel().listEffective().stream().map(RoleRepresentation::getName).toList();
        assertTrue(roles.containsAll(Arrays.asList(GAZELLE_ADMIN.getName(), MONITOR.getName())));

        // Remove group in external DB
        assertEquals(1,keycloakITDAO.removeRoleForUserId(userId,MONITOR.getName()));

        // Check roles in user realm roles
        roles = userResource.roles().realmLevel().listEffective().stream().map(RoleRepresentation::getName).toList();
        assertTrue(roles.contains(GAZELLE_ADMIN.getName()));
        assertFalse(roles.contains(MONITOR.getName()));
    }

    @Test
    void verifyUpdateGroupForUser() {
        // Get user
        UsersResource usersResource = gazelleRealm.users();
        String userResourceId = usersResource.searchByEmail("user1@gazelle.com", true).getFirst().getId();
        UserResource userResource = usersResource.get(userResourceId);
        assertNotNull(userResource);

        // Check that user has only one group
        assertEquals(1,userResource.groups().size());
        assertTrue(userResource.groups().stream().map(GroupRepresentation::getName)
                .toList().contains("INSTITution1"));

        // Update user group
        KeycloakITUserDAO keycloakITDAO = getKeycloakITDAO();
        String userId = keycloakITDAO.getUserByEmail(userResource.toRepresentation().getEmail()).getId();
        assertEquals(1,keycloakITDAO.updateUserOrganization("institution2", userId));

        assertEquals(1,userResource.groups().size());
        assertTrue(userResource.groups().stream().map(GroupRepresentation::getName)
                .toList().contains("institution2"));

        assertEquals(1,keycloakITDAO.updateUserOrganization("INSTITution1", userId));

    }

    private KeycloakITUserDAO getKeycloakITDAO() {
        return new KeycloakITUserDAO(new SystemPropertyDBConfig());
    }

    @Test
    void gazelleClientsAdminCreatedTest(){
        // Flaky test, sometimes the client is not created in time
        waitStillConditionIsTrue(masterRealm.users().searchByEmail("clientadmin@test.com",true).isEmpty());

        // Search for gazelle-client-admin user in master realm
        List<UserRepresentation> userRepresentations = masterRealm.users().searchByEmail("clientadmin@test.com",true);
        assertFalse(userRepresentations.isEmpty());
        UserRepresentation gazelleClientsAdmin = userRepresentations.getFirst();
        assertEquals("gazelle-clients-admin",gazelleClientsAdmin.getUsername());
        assertTrue(gazelleClientsAdmin.isEnabled());


        ClientRepresentation gazelleManagementClient = masterRealm.clients().findByClientId("gazelle-realm").getFirst();
        // Flaky test, sometimes the client is not created in time
        waitStillConditionIsTrue(masterRealm.users().get(gazelleClientsAdmin.getId()).roles().clientLevel(gazelleManagementClient.getId()).listAll().isEmpty());

        // Verify that the user has the manage-client group
        List<RoleRepresentation> roleRepresentations = masterRealm.users().get(gazelleClientsAdmin.getId()).roles().clientLevel(gazelleManagementClient.getId()).listAll();
        List<String> roles = roleRepresentations.stream().map(RoleRepresentation::getName).toList();
        assertTrue(roles.contains("manage-clients"));
    }

    private static void waitStillConditionIsTrue(boolean condition) {
        for (int tryCount = 0; tryCount < 10; tryCount++) {
            if (condition) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                break;
            }
        }
    }
}
