package net.ihe.gazelle.serviceregistry.technical.security;

import net.ihe.gazelle.security.business.rbac.RBACPropertiesProvider;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


class RBACPropertiesProviderTest {

    RBACPropertiesProvider rbacPropertiesProvider = new RBACPropertiesProviderImpl();

    @Test
    void testGetRBACProperties() {
        Properties properties = rbacPropertiesProvider.getRBACProperties();
        assertTrue(properties.containsKey("service:read"));
        assertEquals("role:gazelle_admin,machine,role:test_service,role:project_admin", properties.getProperty("service:read"));
    }
}
