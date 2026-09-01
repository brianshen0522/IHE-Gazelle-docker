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

package net.ihe.gazelle.serviceregistry.technical.websocket;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.websocket.*;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import net.ihe.gazelle.modelmarshaller.technical.jackson.JacksonSerDes;
import net.ihe.gazelle.modelmarshaller.technical.jackson.ObjectMapperBuilder;
import net.ihe.gazelle.modelmarshaller.technical.serialization.TextSerDes;
import net.ihe.gazelle.security.mocks.KeycloakMockResource;
import net.ihe.gazelle.security.mocks.OIDCJWTGenerator;
import net.ihe.gazelle.servicemetadata.api.business.Service;
import net.ihe.gazelle.servicemetadata.api.business.ServiceBuilder;
import net.ihe.gazelle.serviceregistry.api.business.ServiceId;
import net.ihe.gazelle.serviceregistry.technical.dto.SecuredRegistrationDTO;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.given;
import static net.ihe.gazelle.serviceregistry.api.business.DeployedService.Status.AVAILABLE;
import static net.ihe.gazelle.serviceregistry.api.business.DeployedService.Status.UNREACHABLE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
@QuarkusTestResource(KeycloakMockResource.class)
class RegistrationWebSocketTest {

    @TestHTTPResource("/service-registration")
    protected URI registrationEndpoint;


    @Test
    void testWebSocketRegistration() throws DeploymentException, IOException, InterruptedException {
        RegistrationClient registrationClient = new RegistrationClient();
        Service service = getTestService("abc123", "1");
        try (var _ = ContainerProvider.getWebSocketContainer()
                .connectToServer(registrationClient, appendServiceId(registrationEndpoint, new ServiceId(service)))) {
            registrationClient.register(service);
            assertEquals("{\"status\":\"SUCCESS\",\"message\":\"Service abc123/1 registered successfully.\"}", registrationClient.pollLastMessage());
            assertServiceConnected("abc123", "1");
        }
    }

    @Test
    void testDisconnect() throws DeploymentException, IOException, InterruptedException {
        RegistrationClient registrationClient = new RegistrationClient();
        Service service = getTestService("def456", "1");
        try (var _ = ContainerProvider.getWebSocketContainer()
                .connectToServer(registrationClient, appendServiceId(registrationEndpoint, new ServiceId(service)))) {
            registrationClient.register(service);
            assertEquals("{\"status\":\"SUCCESS\",\"message\":\"Service def456/1 registered successfully.\"}", registrationClient.pollLastMessage());
            assertServiceConnected("def456", "1");
        }
        assertServiceDisconnected("def456", "1");
    }

    @Test
    void testParsingError() throws DeploymentException, IOException, InterruptedException {
        String token = OIDCJWTGenerator.getValidJwt();
        RegistrationClient registrationClient = new RegistrationClient();
        try (var _ = ContainerProvider.getWebSocketContainer()
                .connectToServer(registrationClient, appendServiceId(registrationEndpoint, new ServiceId("aaa000", "1")))) {
            registrationClient.sendMessage("{ \"authorization\" : \""+token+"\", \"message\" : \"Invalid JSON message\"}");
            String response = registrationClient.pollLastMessage();
            assertThat(response, Matchers.containsString("\"status\":\"FAILURE\""));
            assertThat(response, Matchers.containsString("UnexpectedError: IllegalArgumentException"));
        }
    }

    @Test
    void testInvalidServiceError() throws DeploymentException, IOException, InterruptedException {
        RegistrationClient registrationClient = new RegistrationClient();
        Service service = getTestService("bbb111", "1").setName(null).setVersion(null); // Invalid service with null name
        try (var _ = ContainerProvider.getWebSocketContainer()
                .connectToServer(registrationClient, appendServiceId(registrationEndpoint, new ServiceId(service)))) {
            registrationClient.register(service);
            String response = registrationClient.pollLastMessage();
            assertThat(response, Matchers.containsString("\"status\":\"FAILURE\""));
            assertThat(response, Matchers.containsString("UnexpectedError: IllegalArgumentException"));
        }
    }

    private URI appendServiceId(final URI endpoint, ServiceId id) {
        return appendServiceId(endpoint, id.instanceId(), id.replicaId());
    }

    private URI appendServiceId(final URI endpoint, final String instanceId, final String replicaId) {
        return URI.create(endpoint.toString() + "/" + escapeURLParam(instanceId) + "/" + escapeURLParam(replicaId));
    }

    private String escapeURLParam(final String param) {
        return URLEncoder.encode(param, StandardCharsets.UTF_8);
    }

    private static void assertServiceConnected(final String instanceId, final String replicaId) {
        String token = OIDCJWTGenerator.getValidJwt();
        given().log().ifValidationFails()
                .when()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .get("/services/" + instanceId + "/" + replicaId)
                .then()
                .statusCode(200)
                .contentType(MediaType.APPLICATION_JSON)
                .body("name", is("Test Service"))
                .body("version", is("1.0.0"))
                .body("instanceId", is(instanceId))
                .body("replicaId", is(replicaId))
                .body("selfRegistered", is(true))
                .body("status", is(AVAILABLE.name()));
    }

    private static void assertServiceDisconnected(final String instanceId, final String replicaId) {
        String token = OIDCJWTGenerator.getValidJwt();
        given().log().ifValidationFails()
                .when()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .get("/services/" + instanceId + "/" + replicaId)
                .then()
                .statusCode(200)
                .contentType(MediaType.APPLICATION_JSON)
                .body("name", is("Test Service"))
                .body("version", is("1.0.0"))
                .body("instanceId", is(instanceId))
                .body("replicaId", is(replicaId))
                .body("selfRegistered", is(true))
                .body("status", is(UNREACHABLE.name()));
    }

    private static Service getTestService(final String instanceId, final String replicaId) {
        return new ServiceBuilder()
                .setName("Test Service")
                .setVersion("1.0.0")
                .setInstanceId(instanceId)
                .setReplicaId(replicaId)
                .build();
    }

    @ClientEndpoint
    public static class RegistrationClient {

        private final LinkedBlockingDeque<String> messages = new LinkedBlockingDeque<>();
        private final TextSerDes serDes = new JacksonSerDes(new ObjectMapperBuilder().build());
        private Session session;

        @OnOpen
        public void onOpen(Session session) {
            this.session = session;
        }

        @OnMessage
        public void onMessage(String message) {
            messages.add(message);
        }

        public void register(Service service) {
            if (session != null && session.isOpen()) {
                String token = OIDCJWTGenerator.getValidJwt();
                SecuredRegistrationDTO<Service> serviceDTO = new SecuredRegistrationDTO<>(service, token);
                try {
                    String message = serDes.serializeAsString(serviceDTO);
                    session.getBasicRemote().sendText(message);
                } catch (IOException e) {
                    throw new RuntimeException("Failed to send registration message", e);
                }
            } else {
                throw new IllegalStateException("WebSocket session with service registry is not open");
            }
        }

        public void sendMessage(String message) {
            if (session != null && session.isOpen()) {
                try {
                    session.getBasicRemote().sendText(message);
                } catch (IOException e) {
                    throw new RuntimeException("Failed to send message", e);
                }
            } else {
                throw new IllegalStateException("WebSocket session with service registry is not open");
            }
        }

        public String pollLastMessage() throws InterruptedException {
            return messages.poll(10, TimeUnit.SECONDS);
        }

        public void destroy() {
            if (session != null && session.isOpen()) {
                try {
                    session.close();
                } catch (IOException e) {
                    throw new RuntimeException("Failed to close WebSocket session", e);
                }
            }
        }
    }
}
