package net.ihe.gazelle.keycloak.core.interlay.configuration;

import net.ihe.gazelle.user.management.api.application.configuration.ApplicationConfig;
import net.ihe.gazelle.user.management.api.application.configuration.DatabaseConfig;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConfigurationTest {

    @Test
    void databaseConfigTest() {
        DatabaseConfig databaseConfig = new DatabaseConfigImpl();

        assertEquals("org.postgresql.Driver", databaseConfig.getDriverClass());
        assertEquals("jdbc:postgresql://localhost:5432/gum", databaseConfig.getGumDBUrl());
        assertEquals(10, databaseConfig.getDefaultPoolSize());
        assertEquals(50, databaseConfig.getMaxPoolSize());
        assertEquals(60, databaseConfig.getReapConnectionTimeout());
        assertEquals("gazelle",databaseConfig.getUsername());
        assertEquals("gazelle",databaseConfig.getPassword());
    }

    @Test
    void applicationConfigTest() {

        ApplicationConfig applicationConfig = new ApplicationConfigImpl();

        assertEquals("https://fakeurl.fr/gazelle", applicationConfig.getGazelleTMUrl());
        assertEquals("http://localhost:8081", applicationConfig.getRootTestBedUrl());
        assertEquals("http://localhost:8081/gum", applicationConfig.getGUMBaseUrl());
        assertEquals("http://localhost:28080", applicationConfig.getSSOBaseUrl());
        assertNull(applicationConfig.getGUMUIBaseUrl());
        assertTrue(applicationConfig.isOrganizationCreationEnabled());
        assertTrue(applicationConfig.isUserRegistrationEnabled());
        assertEquals(31,applicationConfig.getPurgeInactivatedUsersDaysLimit());
    }
    @Test
    void equalsHashcode() {
        EqualsVerifier.simple().forClass(DatabaseConfig.class).verify();
    }

}
