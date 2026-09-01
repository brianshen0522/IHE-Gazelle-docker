/*
 * Copyright 2022-2025 IHE International.
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

package net.ihe.gazelle.serviceregistry.technical.config;

import io.quarkus.test.junit.QuarkusTest;
import net.ihe.gazelle.servicemetadata.api.business.Binding;
import net.ihe.gazelle.servicemetadata.api.business.HttpRestBinding;
import net.ihe.gazelle.servicemetadata.api.business.ProvidedInterface;
import net.ihe.gazelle.servicemetadata.api.business.Service;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
class MetadataTest {

    @ConfigProperty(name = "quarkus.http.port")
    int port;
    private Service service;

    @BeforeEach
    void setUp() {
        service = new ServiceRegistryMetadata().getMetadata();
    }

    @Test
    void testMetadata() {
        assertEquals("Service Registry", service.getName());
        assertEquals("Registry of deployed services in Gazelle Test Bed.", service.getDescription());
        assertEquals("unknown", service.getVersion(), "In quarkus dev or test mode, the version cannot be extracted, because the MANIFEST does not exist or is incomplete.");
        assertEquals("00001", service.getInstanceId());
        assertEquals("001", service.getReplicaId());
        List<ProvidedInterface> providedInterfaces = service.getProvidedInterfaces();
        assertEquals(3, providedInterfaces.size());
    }

    @Test
    void testLookupUpInterface() {
        ProvidedInterface lookupInterface = getInterface(service.getProvidedInterfaces(), "Service Lookup API");
        assertEquals("Service Lookup API", lookupInterface.getInterfaceName());
        assertEquals("1.0.0", lookupInterface.getInterfaceVersion());
        assertEquals(1, lookupInterface.getBindings().size());
        assertEquals(
                "http://localhost:" + port + "/service-registry",
                ((HttpRestBinding) lookupInterface.getBindings().getFirst()).getServiceUrl()
        );
    }

    @Test
    void testRegistrationInterface() {
        ProvidedInterface registrationInterface = getInterface(service.getProvidedInterfaces(), "Service Registration API");
        assertEquals("Service Registration API", registrationInterface.getInterfaceName());
        assertEquals("2.0.0", registrationInterface.getInterfaceVersion());
        assertEquals(2, registrationInterface.getBindings().size());
        List<Binding> bindings = registrationInterface.getBindings();
        assertThat(
                bindings,
                hasItem(
                        hasProperty("webSocketUrl", equalTo("ws://localhost:" + port + "/service-registry"))
                )
        );
        assertThat(
                bindings,
                hasItem(
                        hasProperty("serviceUrl", equalTo("http://localhost:" + port + "/service-registry"))
                )
        );
    }

    @Test
    void testHealthcheckInterface() {
        ProvidedInterface healthcheckInterface = getInterface(service.getProvidedInterfaces(), "Microprofile Health");
        assertEquals("Microprofile Health", healthcheckInterface.getInterfaceName());
        assertEquals("4.0", healthcheckInterface.getInterfaceVersion());
        assertEquals(1, healthcheckInterface.getBindings().size());
        assertEquals(
                "http://localhost:" + port + "/service-registry/health",
                ((HttpRestBinding) healthcheckInterface.getBindings().getFirst()).getServiceUrl()
        );
    }

    private static @NonNull ProvidedInterface getInterface(List<ProvidedInterface> providedInterfaces, String interfaceName) {
        return providedInterfaces.stream()
                .filter(i -> i.getInterfaceName().equals(interfaceName))
                .findFirst()
                .orElseThrow();
    }

}
