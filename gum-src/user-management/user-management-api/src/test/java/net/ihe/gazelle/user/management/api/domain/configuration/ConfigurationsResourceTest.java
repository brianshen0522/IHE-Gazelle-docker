package net.ihe.gazelle.user.management.api.domain.configuration;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConfigurationsResourceTest {

    @Test
    void constructorConfigurationsQueryTest() {
        ConfigurationsResource configurationsQuery = new ConfigurationsResource();
        assertFalse(configurationsQuery.isUserRegistrationEnabled());
        assertFalse(configurationsQuery.isOrganizationCreationEnabled());
        assertNull(configurationsQuery.getTermsOfServiceUrl());
        assertNull(configurationsQuery.getPrivacyPolicyUrl());
        assertFalse(configurationsQuery.isUserCreationEmailNotificationEnabled());
    }

    @Test
    void getterAndSettersConfigurationsQueryTest() {
        ConfigurationsResource configurationsQuery = new ConfigurationsResource();
        configurationsQuery.setUserRegistrationEnabled(true);
        configurationsQuery.setOrganizationCreationEnabled(true);
        configurationsQuery.setTermsOfServiceUrl("https://termOfUseUrl.com");
        configurationsQuery.setPrivacyPolicyUrl("https://privacyPolicyUrl.com");
        configurationsQuery.setUserCreationEmailNotificationEnabled(true);
        assertTrue(configurationsQuery.isUserRegistrationEnabled());
        assertTrue(configurationsQuery.isOrganizationCreationEnabled());
        assertEquals("https://termOfUseUrl.com", configurationsQuery.getTermsOfServiceUrl());
        assertEquals("https://privacyPolicyUrl.com", configurationsQuery.getPrivacyPolicyUrl());
        assertTrue(configurationsQuery.isUserCreationEmailNotificationEnabled());
    }

    @Test
    void configurationQueryEqualsTest() {
        EqualsVerifier.simple().forClass(ConfigurationsResource.class).verify();
    }
}
