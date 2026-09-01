/*
 * Copyright 2025 IHE International.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.ihe.gazelle.serviceregistry.technical;

import io.quarkus.test.junit.QuarkusIntegrationTest;
import org.eclipse.microprofile.config.ConfigProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Test to verify that configuration properties are correctly loaded in integration tests.
 */
@QuarkusIntegrationTest
class ConfigurationIT {

    @Test
    void testConfigurationPropertiesAreLoaded() {
        // Verify SSO configuration
        String ssoUrl = ConfigProvider.getConfig().getValue("gzl.sso.url", String.class);
        assertNotNull(ssoUrl, "gzl.sso.url should be loaded");
        assertEquals("http://localhost:12345", ssoUrl, "gzl.sso.url should have correct value");

        String ssoAdminUser = ConfigProvider.getConfig().getValue("gzl.sso.admin.user", String.class);
        assertNotNull(ssoAdminUser, "gzl.sso.admin.user should be loaded");
        assertEquals("admin", ssoAdminUser, "gzl.sso.admin.user should have correct value");

        // Verify service name
        String serviceName = ConfigProvider.getConfig().getValue("gzl.service.name", String.class);
        assertNotNull(serviceName, "gzl.service.name should be loaded");
        assertEquals("test", serviceName, "gzl.service.name should have correct value");
    }
}

