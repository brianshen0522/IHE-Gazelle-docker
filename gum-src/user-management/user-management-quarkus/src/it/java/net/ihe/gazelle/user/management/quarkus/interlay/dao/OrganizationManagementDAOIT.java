package net.ihe.gazelle.user.management.quarkus.interlay.dao;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.inject.Inject;
import net.ihe.gazelle.user.management.api.domain.organization.Organization;
import net.ihe.gazelle.user.management.commons.application.organization.management.OrganizationManagementDAO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.NoSuchElementException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActivateRequestContext
class OrganizationManagementDAOIT {

    @Inject
    OrganizationManagementDAO organizationManagementDAO;

    private static final String PREFIX = "ORGARegistrationDAO";
    private static final String TEST_GROUP_ID = "ORGADAO_ID";

    @Test
    @TestTransaction
    void testCreateOrganizations() {
        Organization organization = new Organization(TEST_GROUP_ID);
        organization.setShortname(PREFIX+"-ShortName");
        organization.setName(PREFIX+"-GroupName");
        assertDoesNotThrow(()-> organizationManagementDAO.createOrganization(organization));

        assertTrue(organizationManagementDAO.isOrganizationAlreadyExist(
                new Organization(TEST_GROUP_ID)));
        assertTrue(organizationManagementDAO.isOrganizationAlreadyExist(
                new Organization(TEST_GROUP_ID.toLowerCase())));
        assertTrue(organizationManagementDAO.isOrganizationAlreadyExist(
                new Organization().setShortname((PREFIX+"-ShortName").toLowerCase())));
        assertTrue(organizationManagementDAO.isOrganizationAlreadyExist(
                new Organization().setShortname(PREFIX+"-ShortName")));
        assertFalse(organizationManagementDAO.isOrganizationAlreadyExist(
                new Organization().setName("testGroup-groupname")));
        assertFalse(organizationManagementDAO.isOrganizationAlreadyExist(
                new Organization("testGroupNotExisting")));
        assertFalse(organizationManagementDAO.isOrganizationAlreadyExist(
                new Organization("testGroupNotExisting").setName("testGroupNotExisting")));
    }

    @Test
    @TestTransaction
    void testUpdateOrganization() {
        String organizationId = TEST_GROUP_ID + "-UPDATE-" + UUID.randomUUID();
        Organization organization = new Organization(organizationId);
        organization.setShortname(PREFIX + "UpdShortName");
        organization.setName(PREFIX + "-Update-Name");
        organizationManagementDAO.createOrganization(organization);

        Organization updateAttributes = new Organization();
        updateAttributes.setName(PREFIX + "-Updated-Name");

        Organization updated = organizationManagementDAO.updateOrganization(organizationId, updateAttributes);

        assertNotNull(updated);
        assertEquals(organizationId, updated.getId());
        assertEquals(PREFIX + "-Updated-Name", updated.getName());
        assertEquals(PREFIX + "UpdShortName", updated.getShortname());
    }

    @Test
    @TestTransaction
    void testUpdateOrganizationNotFound() {
        Organization updateAttributes = new Organization();
        updateAttributes.setName("updated-name");

        assertThrows(NoSuchElementException.class,
                () -> organizationManagementDAO.updateOrganization("unknown-organization-id", updateAttributes));
    }

    @Test
    @TestTransaction
    void testGetOrganization() {
        Organization organization = new Organization("deleted-shortname", "deleted-name");
        organization.setId("deleted-organization-id");
        assertThrows(NoSuchElementException.class,
                () -> organizationManagementDAO.getOrganizationFromId("unknown-organization-id"));

        assertDoesNotThrow(()-> organizationManagementDAO.createOrganization(organization));
        Organization organization1 = organizationManagementDAO.getOrganizationFromId("deleted-organization-id");
        assertEquals("deleted-shortname", organization1.getShortname());
    }

    @Test
    @TestTransaction
    void testArchiveOrganization() {
        Organization organization = new Organization("archived-shortname", "archived-name", "https://archived-url.fr");
        organization.setId("archived-organization-id");
        assertThrows(NoSuchElementException.class,
                () -> organizationManagementDAO.archiveOrganization("unknown-organization-id"));

        assertDoesNotThrow(()-> organizationManagementDAO.createOrganization(organization));
        Organization organization1 = organizationManagementDAO.getOrganizationFromId("archived-organization-id");
        assertFalse(organization1.isArchived());

        assertDoesNotThrow(()-> organizationManagementDAO.archiveOrganization("archived-organization-id"));

        Organization organization2 = organizationManagementDAO.getOrganizationFromId("archived-organization-id");
        assertTrue(organization2.isArchived());
    }
}
