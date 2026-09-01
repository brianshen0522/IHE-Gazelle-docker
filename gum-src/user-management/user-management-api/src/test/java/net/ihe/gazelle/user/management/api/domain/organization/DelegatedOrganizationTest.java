package net.ihe.gazelle.user.management.api.domain.organization;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class DelegatedOrganizationTest {

    @Test
    void constructorTest() {
        DelegatedOrganization delegatedOrganization = new DelegatedOrganization();
        assertNull(delegatedOrganization.getId());

        delegatedOrganization = new DelegatedOrganization("externalId", "idp");

        assertEquals("externalId", delegatedOrganization.getExternalId());
        assertEquals("idp", delegatedOrganization.getIdpId());

        delegatedOrganization = new DelegatedOrganization("delegatedId", "shortname","name", "externalId", "idp");
        assertEquals("delegatedId", delegatedOrganization.getId());

        Organization organization = new Organization("id");
        delegatedOrganization = new DelegatedOrganization(organization, "externalId", "idp");
        assertEquals("id", delegatedOrganization.getId());
    }

    @Test
    void equalsTest() {
        DelegatedOrganization delegatedUser = new DelegatedOrganization()
                .setExternalId("externalId")
                .setIdpId("idp");

        DelegatedOrganization delegatedUser2 = new DelegatedOrganization()
                .setExternalId("externalId")
                .setIdpId("idp");

        assertEquals(delegatedUser, delegatedUser2);
    }
}
