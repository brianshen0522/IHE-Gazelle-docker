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

package net.ihe.gazelle.simulation.technical.dao;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import net.ihe.gazelle.modelmarshaller.technical.jackson.JacksonSerDes;
import net.ihe.gazelle.modelmarshaller.technical.serialization.TextSerDes;
import net.ihe.gazelle.security.mocks.KeycloakMockResource;
import net.ihe.gazelle.servicemetadata.api.business.HttpRestBindingBuilder;
import net.ihe.gazelle.servicemetadata.api.business.ProvidedInterfaceBuilder;
import net.ihe.gazelle.servicemetadata.api.business.Service;
import net.ihe.gazelle.servicemetadata.api.business.ServiceBuilder;
import net.ihe.gazelle.servicemetadata.technical.jaxrs.ServiceDTO;
import net.ihe.gazelle.simulation.business.ApplicationConfig;
import net.ihe.gazelle.simulation.business.SequenceChecksumCache;
import net.ihe.gazelle.simulation.business.SequenceChecksumCacheImpl;
import net.ihe.gazelle.simulation.jaxrs.api.technical.ws.SimulationAPI;
import net.ihe.gazelle.simulation.technical.config.ApplicationConfigMock;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
@QuarkusTestResource(KeycloakMockResource.class)
class SequenceChecksumCacheTest {

    private static WireMockServer wireMockServer;
    private static ApplicationConfig config;

    @BeforeAll
    static void setup() {
        wireMockServer = new WireMockServer(0);
        wireMockServer.start();
        configureFor("localhost", wireMockServer.port());
        config = new ApplicationConfigMock(wireMockServer.baseUrl());
    }

    @AfterEach
    void tearDown() {
        wireMockServer.resetAll();
    }

    @AfterAll
    static void cleanUp() {
        wireMockServer.stop();
    }

    @Test
    void test_time_or_service_or_checksum_expiration() {
        UUID serviceStub = wireMockServer.stubFor(get("/services?_limit=all&_offset=0&providedInterface=Simulation+Service+API&status=AVAILABLE%2CUNKNOWN")
                .willReturn(okJson(getServiceAsString()).withHeader("Content-Range", "DeployedServices 1-1/1"))).getId();
        UUID stub = wireMockServer.stubFor(get("/simulation/v1/sequences/checksum")
                .willReturn(okJson("{\"checksum\": \"0x1234ABCD\"}"))
        ).getId();
        SequenceChecksumCache cache = new SequenceChecksumCacheImpl(
                new ServiceRegistryDAOImpl(config, Duration.ofMillis(300)),
                new SimulationSequenceDAOImpl(Duration.ofMillis(200))
        );

        assertFalse(cache.isOutDated());

        await().during(Duration.ofMillis(300)).until(() -> true);
        assertFalse(cache.isOutDated());

        wireMockServer.removeStub(stub);
        wireMockServer.stubFor(get("/simulation/v1/sequences/checksum")
                .willReturn(okJson("{\"checksum\": \"0x1234ABCF\"}"))
        );
        assertFalse(cache.isOutDated());

        await().during(Duration.ofMillis(300)).until(() -> true);
        assertTrue(cache.isOutDated());
        assertFalse(cache.isOutDated());

        wireMockServer.removeStub(serviceStub);
        wireMockServer.stubFor(get("/services?_limit=all&_offset=0&providedInterface=Simulation+Service+API&status=AVAILABLE%2CUNKNOWN")
                .willReturn(okJson("[]").withHeader("Content-Range", "DeployedServices 1-1/1")));
        await().during(Duration.ofMillis(350)).until(() -> true);
        assertTrue(cache.isOutDated());
        assertFalse(cache.isOutDated());
    }

    private static String getServiceAsString() {
        TextSerDes serDes = new JacksonSerDes();
        List<ServiceDTO<Service>> serviceDTOList = Stream.of(getService())
                .map(ServiceDTO::new)
                .toList();
        return serDes.serializeAsString(serviceDTOList);
    }

    private static Service getService() {
        return new ServiceBuilder()
                .setName("test")
                .setReplicaId("1234")
                .setInstanceId("1234")
                .setVersion("1.0")
                .addProvidedInterfaceBuilder(
                        new ProvidedInterfaceBuilder()
                                .setInterfaceName(SimulationAPI.INTERFACE_NAME)
                                .setInterfaceVersion("1.0")
                                .addBinding(
                                        new HttpRestBindingBuilder()
                                                .setServiceUrl(wireMockServer.baseUrl())
                                )
                )
                .build();
    }
}
