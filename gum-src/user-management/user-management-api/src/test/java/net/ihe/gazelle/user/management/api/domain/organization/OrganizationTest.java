package net.ihe.gazelle.user.management.api.domain.organization;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class OrganizationTest {

    @Test
    void testBasicConstructor() {
        Organization organization = new Organization("organizationId");

        assertEquals("organizationId", organization.getId());
        assertNull(organization.getName());

        organization = new Organization();
        assertNull(organization.getId());
        assertNull(organization.getName());
    }

    @Test
    void testCompleteConstructor() {
        Organization organization = new Organization("organizationId","orgaShortname", "organization");
        assertEquals("organizationId", organization.getId());
        assertEquals("organization", organization.getName());
    }

    @Test
    void testCopyConstructor() {
        Organization organization = new Organization("organizationId","orgaShortname", "organization");
        Organization organizationCopy = new Organization(organization);
        assertEquals(organization.getId(), organizationCopy.getId());
        assertEquals(organization.getName(), organizationCopy.getName());
    }

    @Test
    void testOrganizationSetters() {
        Organization organization = new Organization("organizationId");

        organization.setId("mockId");
        organization.setName("organizationName");
        assertEquals("mockId", organization.getId());
        assertEquals("organizationName", organization.getName());
    }

    @Test
    void testOrganizationEquals() {
        EqualsVerifier.simple().forClass(Organization.class).verify();
    }
}
