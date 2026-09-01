package db.migration;

import net.ihe.gazelle.keycloak.provider.utils.KeycloakITUserPrefDAO;
import net.ihe.gazelle.keycloak.provider.utils.SystemPropertyDBConfig;
import net.ihe.gazelle.user.management.api.domain.user.UserPreference;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


public class GazelleUserPreferencesMigrationIT {


    private static KeycloakITUserPrefDAO keycloakITDAO;

    @BeforeAll
    public static void setUp() {
        keycloakITDAO = new KeycloakITUserPrefDAO(new SystemPropertyDBConfig());
    }

    @Test
    void testGetMigratedUserPreferences() {
        UserPreference user = keycloakITDAO.getUserPrefById("migrated-user");

        assertEquals("migrated-user",user.getUserId());
        assertEquals("C4",user.getTableLabel());
        assertTrue(user.getLanguagesSpoken().contains("fr"));
        assertTrue(user.getLanguagesSpoken().contains("de"));
        assertTrue(user.isNotifiedByEmail());
        assertFalse(user.getProfileThumbnailUri().isEmpty());
        assertFalse(user.getProfilePictureUri().isEmpty());
    }

    @Test
    void testGetMigratedUserPreferencesNullFields() {
        UserPreference user = keycloakITDAO.getUserPrefById("conflictuser2");

        assertEquals("conflictuser2",user.getUserId());
        assertEquals("",user.getTableLabel());
        assertTrue(user.getLanguagesSpoken().isEmpty());
        assertFalse(user.isNotifiedByEmail());
        assertNull(user.getProfileThumbnailUri());
        assertNull(user.getProfilePictureUri());
    }
}
