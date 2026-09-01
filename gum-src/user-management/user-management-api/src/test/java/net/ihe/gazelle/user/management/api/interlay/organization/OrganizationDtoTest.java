package net.ihe.gazelle.user.management.api.interlay.organization;

import net.ihe.gazelle.user.management.api.domain.organization.Organization;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OrganizationDtoTest {

    @Test
    void testBasicConstructor() {
        OrganizationDto organizationResource = new OrganizationDto("organizationId");

        assertEquals("organizationId", organizationResource.getId());
        assertNull(organizationResource.getName());

        organizationResource = new OrganizationDto();
        assertNull(organizationResource.getId());
        assertNull(organizationResource.getName());
        assertFalse(organizationResource.isDelegated());
    }

    @Test
    void testCompleteConstructor() {
        OrganizationDto organizationResource = new OrganizationDto("organizationId", "organizationResource");
        assertEquals("organizationId", organizationResource.getId());
        assertEquals("organizationResource", organizationResource.getName());

        organizationResource = new OrganizationDto("organizationId","organizationShortname", "organizationResource", "externalId", "idpId");
        assertEquals("organizationId", organizationResource.getId());
        assertEquals("organizationShortname", organizationResource.getShortname());
        assertEquals("organizationResource", organizationResource.getName());
        assertEquals("externalId", organizationResource.getExternalId());
        assertEquals("idpId", organizationResource.getIdpId());
        assertTrue(organizationResource.isDelegated());
    }

    @Test
    void testCopyConstructor() {
        OrganizationDto organizationResource = new OrganizationDto("organizationId", "organizationResource");
        OrganizationDto organizationCopy = new OrganizationDto(organizationResource);
        assertEquals(organizationResource.getId(), organizationCopy.getId());
        assertEquals(organizationResource.getName(), organizationCopy.getName());
    }

    @Test
    void testOrganizationSetters() {
        OrganizationDto organizationResource = new OrganizationDto("organizationId");

        organizationResource.setId("mockId");
        organizationResource.setName("organizationName");
        assertEquals("mockId", organizationResource.getId());
        assertEquals("organizationName", organizationResource.getName());
    }

    @Test
    void testAsOrganization() {
        OrganizationDto organizationResource = new OrganizationDto("organizationId", "organizationShortname", "organizationResource");
        Organization organization = organizationResource.asOrganization();
        assertEquals("organizationId", organization.getId());
        assertEquals("organizationResource", organization.getName());
    }

    @Test
    void testOrganizationEquals() {
        EqualsVerifier.simple().forClass(OrganizationDto.class).verify();
    }
}
