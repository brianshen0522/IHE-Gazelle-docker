package net.ihe.gazelle.keycloak.provider;

import net.ihe.gazelle.keycloak.provider.utils.KeycloakAdminOperator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.RoleRepresentation;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class GazelleKeycloakRoleIT {
    private static RealmResource gazelleRealm;
    private static Keycloak keycloak;

    @BeforeAll
    public static void setUp() {
        KeycloakAdminOperator keycloakAuthentication = new KeycloakAdminOperator();
        gazelleRealm = keycloakAuthentication.getRealmGazelle();
        keycloak = keycloakAuthentication.getKeycloakInstance();
    }

    @Test
    void checkUserDefaultRole() {
        Optional<RoleRepresentation> roleRepresentationForUserRole = gazelleRealm.roles().list().stream().filter(role -> role.getName().equals("user")).findFirst();
        assertTrue(roleRepresentationForUserRole.isPresent());

        Optional<RoleRepresentation> roleRepresentationForDefaultRoles = gazelleRealm.roles().list().stream().filter(role -> role.getName().equals("default-roles-gazelle")).findFirst();
        assertTrue(roleRepresentationForDefaultRoles.isPresent());

        RoleRepresentation defaultRoleRepresentation = roleRepresentationForDefaultRoles.get();
        assertTrue(defaultRoleRepresentation.isComposite(), "default-roles-gazelle should be a composite role");

        // Get the composite roles
        Set<RoleRepresentation> compositeRoles = gazelleRealm.roles().get("default-roles-gazelle").getRoleComposites();
        assertNotNull(compositeRoles, "Composite roles should not be null");
        assertTrue(compositeRoles.stream().anyMatch(role -> role.getName().equals("user")),
                   "user role should be in the composite roles of default-roles-gazelle");
    }
}
