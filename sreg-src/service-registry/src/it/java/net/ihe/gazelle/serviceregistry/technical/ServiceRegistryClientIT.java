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

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.ValidatableResponse;
import jakarta.ws.rs.core.MediaType;
import net.ihe.gazelle.oidc.common.business.accesstoken.AccessTokenService;
import net.ihe.gazelle.security.mocks.KeycloakMockResource;
import net.ihe.gazelle.security.mocks.MockAccessTokenService;
import net.ihe.gazelle.security.mocks.OIDCJWTGenerator;
import net.ihe.gazelle.servicemetadata.api.business.Service;
import net.ihe.gazelle.servicemetadata.api.business.ServiceBuilder;
import net.ihe.gazelle.serviceregistry.client.business.ServiceRegistrationClient;
import net.ihe.gazelle.serviceregistry.client.business.ServiceRegistrationClient.Event;
import net.ihe.gazelle.serviceregistry.client.business.ServiceRegistrationClient.RegistrationSession;
import net.ihe.gazelle.serviceregistry.client.technical.websocket.ServiceRegistrationWebSocketClient;
import org.apache.http.HttpHeaders;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.given;
import static net.ihe.gazelle.serviceregistry.api.business.DeployedService.Status.AVAILABLE;
import static net.ihe.gazelle.serviceregistry.api.business.DeployedService.Status.UNREACHABLE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test integration of service-registry-client with service-registry.
 */
@QuarkusTest
@Order(100)
@QuarkusTestResource(value = KeycloakMockResource.class, restrictToAnnotatedClass = true)
class ServiceRegistryClientIT {

   @TestHTTPResource
   URI registryUri;

   private LinkedBlockingDeque<Event> raisedEvents;

   @BeforeEach
   void setUp() {
      raisedEvents = new LinkedBlockingDeque<>();
   }

   @AfterEach
   void tearDown() {
      raisedEvents.clear();
   }

   @Test
   void testRegisterAndKeepAlive() throws InterruptedException {
      Service service = new ServiceBuilder()
            .setName("Test Service 2")
            .setVersion("1.0")
            .setInstanceId("def456")
            .setReplicaId("1")
            .build();

      // Create a mock AccessTokenService that returns a valid JWT
      AccessTokenService mockAccessTokenService = new MockAccessTokenService();

      List<Event> events;
      ServiceRegistrationClient client = new ServiceRegistrationWebSocketClient(registryUri.toString(), mockAccessTokenService);
      try(RegistrationSession session = client.connectAndRegister(service, raisedEvents::add)) {
         events = pollAllEvents(500, TimeUnit.MILLISECONDS);
         assertEquals(5, events.size(), "Expected 5 events: connecting, connected, token added, send registration, registration ack");
         assertThat(
               events.stream().map(Event::message).toList(),
               hasItem(containsString("Service def456/1 registered successfully."))
         );
         assertServiceConnected(service);

         client.keepAlive(session);

         events = pollAllEvents(500, TimeUnit.MILLISECONDS);
         assertEquals(3, events.size(), "Expected 3 events: token added, send registration, registration ack");
         assertThat(
               events.stream().map(Event::message).toList(),
               hasItem(containsString("Service def456/1 registered successfully."))
         );
         assertServiceConnected(service);
      }
      events = pollAllEvents(500, TimeUnit.MILLISECONDS);
      assertThat(
            events.stream().map(Event::message).toList(),
            hasItem(containsString("Registration WebSocket session closed"))
      );
      assertServiceDisconnected(service);
   }

   private List<Event> pollAllEvents(long wait, TimeUnit unit) throws InterruptedException {
      List<Event> events = new ArrayList<>();
      Event event = null;
      while ((event = raisedEvents.poll(wait, unit)) != null) {
         events.add(event);
      }
      return events;
   }

   private static void assertServiceConnected(final Service service) {
      assertServiceRegistered(service)
            .body("selfRegistered", is(true))
            .body("status", is(AVAILABLE.name()));
   }

   private static void assertServiceDisconnected(final Service service) {
      assertServiceRegistered(service)
            .body("selfRegistered", is(true))
            .body("status", is(UNREACHABLE.name()));
   }

   private static ValidatableResponse assertServiceRegistered(Service service) {
      String token = OIDCJWTGenerator.getValidJwt();
      return given().log().ifValidationFails()
            .when()
              .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
              .get("/services/" + service.getInstanceId() + "/" + service.getReplicaId())
            .then()
            .statusCode(200)
            .contentType(MediaType.APPLICATION_JSON)
            .body("name", is(service.getName()))
            .body("version", is(service.getVersion()))
            .body("instanceId", is(service.getInstanceId()))
            .body("replicaId", is(service.getReplicaId()));
   }

}
