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

import net.ihe.gazelle.oidc.common.business.accesstoken.AccessTokenService;
import net.ihe.gazelle.security.mocks.MockAccessTokenService;
import net.ihe.gazelle.servicemetadata.api.business.Service;
import net.ihe.gazelle.serviceregistry.client.business.ServiceRegistrationClient;
import net.ihe.gazelle.serviceregistry.client.business.ServiceRegistrationClient.RegistrationSession;
import net.ihe.gazelle.serviceregistry.client.technical.websocket.ServiceRegistrationWebSocketClient;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;

public class ITServiceConnector implements AutoCloseable {

   private static final int WAIT_BETWEEN_EACH_EVENTS_MILLISECONDS = 600;

   private final LinkedBlockingDeque<ServiceRegistrationClient.Event> raisedEvents = new LinkedBlockingDeque<>();
   private final List<RegistrationSession> sessions = new ArrayList<>();

   public ITServiceConnector(final String serviceRegistryUrl, final List<Service> services)
         throws InterruptedException {
      registerServices(serviceRegistryUrl, services);
   }

   private void registerServices(String serviceRegistryUrl, List<Service> services) throws InterruptedException {
      // Create a mock AccessTokenService that returns a valid JWT
      AccessTokenService mockAccessTokenService = new MockAccessTokenService();
      ServiceRegistrationClient client = new ServiceRegistrationWebSocketClient(serviceRegistryUrl, mockAccessTokenService);
      for (Service service : services) {
         sessions.add(registerService(client, service));
      }
   }

   private RegistrationSession registerService(ServiceRegistrationClient client, final Service service)
         throws InterruptedException {
      RegistrationSession session = client.connectAndRegister(service, raisedEvents::add);
      assertThat(
            pollAllEvents(WAIT_BETWEEN_EACH_EVENTS_MILLISECONDS, TimeUnit.MILLISECONDS).stream()
                  .map(ServiceRegistrationClient.Event::message)
                  .toList(),
            hasItem(containsString(
                  "Service " + service.getInstanceId() + "/" + service.getReplicaId() + " registered successfully."))
      );
      return session;
   }

   private void disconnectServices() throws InterruptedException {
      for (RegistrationSession session : sessions) {
         disconnectService(session);
      }
      sessions.clear();
   }

   private void disconnectService(RegistrationSession session) throws InterruptedException {
      session.close();
      assertThat(
            pollAllEvents(WAIT_BETWEEN_EACH_EVENTS_MILLISECONDS, TimeUnit.MILLISECONDS).stream()
                  .map(ServiceRegistrationClient.Event::message)
                  .toList(),
            hasItem(containsString("closed"))
      );
   }

   private List<ServiceRegistrationClient.Event> pollAllEvents(long wait, TimeUnit unit) throws InterruptedException {
      List<ServiceRegistrationClient.Event> events = new ArrayList<>();
      ServiceRegistrationClient.Event event = null;
      while ((event = raisedEvents.poll(wait, unit)) != null) {
         events.add(event);
      }
      return events;
   }

   @Override
   public void close() {
      try {
         disconnectServices();
      } catch (InterruptedException e) {
         Thread.currentThread().interrupt();
         throw new RuntimeException(e);
      }
   }
}
