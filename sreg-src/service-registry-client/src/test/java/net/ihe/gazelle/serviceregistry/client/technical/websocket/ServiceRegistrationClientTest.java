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

package net.ihe.gazelle.serviceregistry.client.technical.websocket;

import net.ihe.gazelle.modelmarshaller.technical.jackson.JacksonSerDes;
import net.ihe.gazelle.modelmarshaller.technical.jackson.ObjectMapperBuilder;
import net.ihe.gazelle.modelmarshaller.technical.serialization.TextSerDes;
import net.ihe.gazelle.servicemetadata.api.business.Service;
import net.ihe.gazelle.servicemetadata.api.business.ServiceBuilder;
import net.ihe.gazelle.servicemetadata.technical.jaxrs.ServiceDTO;
import net.ihe.gazelle.serviceregistry.client.business.ServiceRegistrationClient;
import net.ihe.gazelle.serviceregistry.client.business.ServiceRegistrationClient.Event;
import net.ihe.gazelle.serviceregistry.client.business.ServiceRegistrationClient.RegistrationSession;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.QueueDispatcher;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.*;

class ServiceRegistrationClientTest {

   private static final TextSerDes SERDES = new JacksonSerDes(new ObjectMapperBuilder().build());
   private static final LinkedBlockingQueue<String> RECEIVED_MESSAGES = new LinkedBlockingQueue<>();
   private static final LinkedBlockingQueue<Event> RAISED_EVENTS = new LinkedBlockingQueue<>();
   private static final String SRV_REG_ROOT_PATH = "service-registry";

   private MockWebServer mockWebServer;
   private QueueDispatcher dispatcher;

   @BeforeEach
   void setUp() throws IOException {
      mockWebServer = new MockWebServer();
      dispatcher = new QueueDispatcher();
      mockWebServer.setDispatcher(dispatcher);
      mockWebServer.start();
   }

   @AfterEach
   void tearDown() throws Exception {
      RECEIVED_MESSAGES.clear();
      RAISED_EVENTS.clear();
      dispatcher.shutdown();
      mockWebServer.shutdown();
   }

   @Test
   void testInvalidService() {
      mockWebServer.enqueue(getServerSuccessResponse()); // at least one response to accept websocket connection.
      Service invalidService = new Service().setInstanceId("aaa111").setReplicaId("1");

      ServiceRegistrationClient client = new ServiceRegistrationWebSocketClient(
            mockWebServer.url(SRV_REG_ROOT_PATH).toString(), null);
      assertThrows(IllegalArgumentException.class, () -> {
         try (var _ = client.connectAndRegister(invalidService, RAISED_EVENTS::add)) {
            // should not reach this point
         }
      });
   }

   @Test
   void testServerError() throws InterruptedException {
      ServiceRegistrationClient client = new ServiceRegistrationWebSocketClient(
            mockWebServer.url(SRV_REG_ROOT_PATH).toString(), null);
      Service service = getValidService();
      mockWebServer.enqueue(getServerErrorResponse());

      try (var _ = client.connectAndRegister(service, RAISED_EVENTS::add)) {
         // wait until no more events are raised
         List<Event> events = pollAllEvents(1, TimeUnit.SECONDS);
         assertEquals(4, events.size());
         assertThat(
               events.stream().map(Event::message).toList(),
               hasItem(containsString("Received error from service-registry: Unexpected Error"))
         );
      }

   }

   @Test
   void testRegistrationClient() throws InterruptedException {
      Service service = getValidService();
      mockWebServer.enqueue(getServerSuccessResponse());

      ServiceRegistrationClient client = new ServiceRegistrationWebSocketClient(
            mockWebServer.url(SRV_REG_ROOT_PATH).toString(), null);

      try (RegistrationSession session = client.connectAndRegister(service, RAISED_EVENTS::add)) {
         assertTrue(session.isOpen());
         assertEquals(getExpected(service),
               RECEIVED_MESSAGES.poll(3, java.util.concurrent.TimeUnit.SECONDS));
         assertEquals("/service-registry/service-registration/123abc/1", mockWebServer.takeRequest().getPath());
      }
   }

   @Test
   void testSessionClose() {
      Service service = getValidService();
      mockWebServer.enqueue(getServerSuccessResponse());
      ServiceRegistrationClient client = new ServiceRegistrationWebSocketClient(
            mockWebServer.url(SRV_REG_ROOT_PATH).toString(), null);

      RegistrationSession session = null;
      try {
         session = client.connectAndRegister(service, RAISED_EVENTS::add);
         assertTrue(session.isOpen());
      } catch (Exception e) {
         fail("connectAndRegister should not throw an throwable", e);
      } finally {
         session.close();
      }
      assertFalse(session.isOpen());
   }

   @Test
   void testKeepAliveOnOpenConnection() throws InterruptedException {
      Service service = getValidService();
      mockWebServer.enqueue(getServerSuccessResponse());
      mockWebServer.enqueue(getServerSuccessResponse());
      ServiceRegistrationClient client = new ServiceRegistrationWebSocketClient(
            mockWebServer.url(SRV_REG_ROOT_PATH).toString(), null);

      try (RegistrationSession session = client.connectAndRegister(service, RAISED_EVENTS::add)) {
         assertTrue(session.isOpen());
         assertEquals(getExpected(service), RECEIVED_MESSAGES.poll(1, TimeUnit.SECONDS));

         client.keepAlive(session);
         assertEquals(getExpected(service), RECEIVED_MESSAGES.poll(1, TimeUnit.SECONDS));
         assertEquals("/service-registry/service-registration/123abc/1", mockWebServer.takeRequest().getPath());
      }
   }

   @Test
   void testKeepAliveOnClosedConnection() throws InterruptedException {
      Service service = getValidService();
      mockWebServer.enqueue(getServerSuccessResponse());
      mockWebServer.enqueue(getServerSuccessResponse());
      ServiceRegistrationClient client = new ServiceRegistrationWebSocketClient(
            mockWebServer.url(SRV_REG_ROOT_PATH).toString(), null);

      RegistrationSession session = client.connectAndRegister(service, RAISED_EVENTS::add);
      assertTrue(session.isOpen());
      assertEquals(getExpected(service), RECEIVED_MESSAGES.poll(1, TimeUnit.SECONDS));

      session.close();
      assertFalse(session.isOpen());

      // Now try to keep alive the closed session
      client.keepAlive(session);
      assertEquals(getExpected(service), RECEIVED_MESSAGES.poll(1, TimeUnit.SECONDS));
      assertTrue(session.isOpen());

      session.close();
   }

   @Test
   void testServiceRegistryUrlHttpToWs() throws Exception {
      ServiceRegistrationWebSocketClient client = new ServiceRegistrationWebSocketClient(
            "http://example.test/service-registry", null);
      assertEquals("ws://example.test/service-registry", getServiceRegistryUrl(client));
   }

   @Test
   void testServiceRegistryUrlHttpsToWss() throws Exception {
      ServiceRegistrationWebSocketClient client = new ServiceRegistrationWebSocketClient(
            "https://example.test/service-registry", null);
      assertEquals("wss://example.test/service-registry", getServiceRegistryUrl(client));
   }

   private List<Event> pollAllEvents(long wait, TimeUnit unit) throws InterruptedException {
      List<Event> events = new ArrayList<>();
      Event event = null;
      while ((event = RAISED_EVENTS.poll(wait, unit)) != null) {
         events.add(event);
      }
      return events;
   }

   private static @NotNull MockResponse getServerErrorResponse() {
      return getServerMockResponse("{\"status\":\"FAILURE\",\"message\":\"Unexpected Error\"}");
   }

   private static @NotNull MockResponse getServerSuccessResponse() {
      return getServerMockResponse("{\"status\":\"SUCCESS\",\"message\":\"Service registered successfully\"}");
   }

   private static @NotNull MockResponse getServerMockResponse(String response) {
      return new MockResponse().withWebSocketUpgrade(new WebSocketListener() {
         @Override
         public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
            RECEIVED_MESSAGES.add(text);
            webSocket.send(response);

         }

         @Override
         public void onClosing(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
            webSocket.close(1000, "Closing connection");
         }

      });
   }

   private static Service getValidService() {
      return new ServiceBuilder()
            .setName("Test Service")
            .setDescription("This is a test service")
            .setVersion("1.0")
            .setInstanceId("123abc")
            .setReplicaId("1")
            .build();
   }

   private static String getExpected(Service service) {
      return SERDES.serializeAsString(new ServiceDTO<>(service));
   }

   private static String getServiceRegistryUrl(ServiceRegistrationWebSocketClient client) throws Exception {
      Field urlField = ServiceRegistrationWebSocketClient.class.getDeclaredField("serviceRegistryUrl");
      urlField.setAccessible(true);
      return (String) urlField.get(client);
   }
}
