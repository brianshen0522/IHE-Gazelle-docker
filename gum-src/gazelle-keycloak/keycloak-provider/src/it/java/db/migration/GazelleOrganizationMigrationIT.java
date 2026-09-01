package db.migration;

import net.ihe.gazelle.keycloak.provider.utils.QuarkusITOrganizationDAO;
import net.ihe.gazelle.keycloak.provider.utils.SystemPropertyDBConfig;
import net.ihe.gazelle.user.management.api.domain.organization.DelegatedOrganization;
import net.ihe.gazelle.user.management.api.domain.organization.Organization;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


public class GazelleOrganizationMigrationIT {


    private static QuarkusITOrganizationDAO quarkusITOrganizationDAO;

    @BeforeAll
    public static void setUp() {
        quarkusITOrganizationDAO = new QuarkusITOrganizationDAO(new SystemPropertyDBConfig());
    }

    @Test
    void testGetInjectedOrganization() {
        Organization orga = quarkusITOrganizationDAO.getOrganizationByShortname("myOrga1");
        assertNotNull(orga);
        assertEquals("1111-1111", orga.getId());
        assertEquals("myOrga1", orga.getShortname());
        assertEquals("myOrganization1", orga.getName());
        assertEquals(false, orga.isArchived());
        assertEquals(0, orga.getLastUpdateTimestamp());
    }

    @Test
    void testGetMigratedOrganization() {
        Organization orga = quarkusITOrganizationDAO.getOrganizationByShortname("INSTITution1");
        assertNotNull(orga);
        assertEquals("INSTITution1", orga.getId());
        assertEquals("INSTITution1", orga.getShortname());
        assertEquals("INSTITution1 name", orga.getName());
        assertEquals(false, orga.isArchived());
        assertEquals(1704463270000L, orga.getLastUpdateTimestamp());
    }

    @Test
    void testGetMigratedDelegatedOrganization() {
        DelegatedOrganization orga = (DelegatedOrganization) quarkusITOrganizationDAO.getDelegatedOrganizationByShortname("delegInsti5");
        assertNotNull(orga);
        assertEquals("delegInsti5", orga.getId());
        assertEquals("delegInsti5", orga.getShortname());
        assertEquals("delegated Institution5", orga.getName());
        assertEquals("insti5-external-id", orga.getExternalId());
        assertEquals("insti5-idp-id", orga.getIdpId());
        assertEquals(false, orga.isArchived());
        assertEquals(0L, orga.getLastUpdateTimestamp());
    }


    @Test
    void testNotMigratedOrganization() {
        Organization orga = quarkusITOrganizationDAO.getOrganizationByShortname("orgaNotMigrated");
        assertNull(orga);
    }
}
