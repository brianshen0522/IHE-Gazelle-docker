package db.migration;

import net.ihe.gazelle.keycloak.provider.utils.KeycloakAdminOperator;
import net.ihe.gazelle.keycloak.provider.utils.KeycloakITUserDAO;
import net.ihe.gazelle.keycloak.provider.utils.SystemPropertyDBConfig;
import net.ihe.gazelle.user.management.api.domain.user.User;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.UserRepresentation;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;

import static net.ihe.gazelle.user.management.api.domain.user.GazelleDefaultGroup.*;
import static org.junit.jupiter.api.Assertions.*;


public class GazelleUserMigrationIT {

    private static RealmResource realmGazelle;

    @BeforeAll
    public static void setUp() {
        KeycloakAdminOperator keycloakAdminOperator = new KeycloakAdminOperator();
        realmGazelle = keycloakAdminOperator.getRealmGazelle();
    }

    @Test
    void testGetMigratedUserInDatabase() throws ParseException {
        // Retrieve updated user
        KeycloakITUserDAO keycloakITDAO = new KeycloakITUserDAO(new SystemPropertyDBConfig());
        User user = keycloakITDAO.getUserByEmail("migrated-user@gazelle.com");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        // Check user attributes
        assertEquals("migrated-user",user.getId());
        assertEquals("migratedUser fn",user.getFirstName());
        assertEquals("migratedUser ln",user.getLastName());
        assertTrue(user.isActivated());
        assertEquals(0,user.getLoginCounter());
        assertEquals(sdf.parse("2024-01-05 14:01:10").getTime(),user.getRegistrationTimestamp());
        assertEquals(sdf.parse("2024-03-05 14:01:10").getTime(),user.getLastUpdateTimestamp());
        assertEquals(sdf.parse("2024-02-05 14:01:10").getTime(),user.getLastLoginTimestamp());

        // Check user groups
        assertTrue(user.getGroupIds().contains(GAZELLE_ADMIN.getName()));
        assertTrue(user.getGroupIds().contains(MONITOR.getName()));
        assertFalse(user.getGroupIds().contains(TEST_DESIGNER.getName()));
        assertTrue(user.getGroupIds().contains(PREFIX_ORGANIZATION_ADMIN.getName() + "KER"));
        assertTrue(user.getGroupIds().contains(PREFIX_ORGANIZATION_MEMBER.getName() + "KER"));

        // Check credentials of the user
        Map<String,String> userPasswordData = keycloakITDAO.getPasswordDataFromUserId(user.getId());
        assertTrue(userPasswordData.get("credentials").contains("505c1cf582bdcac5f9acd559b142858e"));
        assertEquals("t", userPasswordData.get("reset_password"));
    }

    @Test
    void testGetMigratedUserInKeycloak() {
        // Retrieve updated user
        List<UserRepresentation> userRepresentationList = realmGazelle.users().searchByEmail("migrated-user@gazelle.com",true);
        assertEquals(1,userRepresentationList.size());
        UserRepresentation userRepresentation = userRepresentationList.getFirst();

        // Check user attributes
        assertTrue(userRepresentation.getId().contains("migrated-user"));
        assertEquals("migratedUser fn",userRepresentation.getFirstName());
        assertEquals("migratedUser ln",userRepresentation.getLastName());

        // Check realm roles
        Optional<UserRepresentation> userRepresentationStream = realmGazelle.roles().get("role:gazelle_admin").getUserMembers().stream().filter(role -> role.getId().equals(userRepresentation.getId())).findFirst();
        assertTrue(userRepresentationStream.isPresent());
        assertEquals("migrated-user@gazelle.com",userRepresentationStream.get().getEmail());

         userRepresentationStream = realmGazelle.groups().group("org:705b321b02f32b414426").members().stream()
                .filter(member -> member.getId().equals(userRepresentation.getId())).findFirst();
         assertTrue(userRepresentationStream.isPresent());
         assertEquals("migrated-user@gazelle.com",userRepresentationStream.get().getEmail());
    }

    @Test
    void testGetConflictUserInDatabase() {
        // Retrieve updated user
        KeycloakITUserDAO keycloakITDAO = new KeycloakITUserDAO(new SystemPropertyDBConfig());
        User user = keycloakITDAO.getUserByEmail("conflictuser@gazelle.com");

        // Check user attributes
        assertEquals("conflictuser2",user.getId());
        assertEquals("conflictUser2 fn",user.getFirstName());
        assertEquals("conflictUser2 ln",user.getLastName());
        assertTrue(user.isActivated());
        assertEquals(7,user.getLoginCounter());
    }
}
