/*
 * Copyright 2025-2026 IHE International.
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

package net.ihe.gazelle.simulation.technical.config;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import net.ihe.gazelle.simulation.business.ApplicationConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class ApplicationConfigTest {

    @Inject
    ApplicationConfig config;

    @Test
    void should_get_application_config() {
        assertTrue(config.getServiceRegistryUrl().contains("http://localhost:"));
        assertEquals(1, config.getServicesCacheTimeoutMinutes());
        assertEquals(10, config.getSequencesCacheMaxTimeoutMinutes());
        assertTrue(config.getSvsUrl().contains("/SVSSimulator/rest"));
    }
}
