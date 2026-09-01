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

import jakarta.websocket.ContainerProvider;
import net.ihe.gazelle.oidc.common.business.accesstoken.AccessTokenService;
import net.ihe.gazelle.servicemetadata.api.business.Service;
import net.ihe.gazelle.serviceregistry.api.business.ServiceId;
import net.ihe.gazelle.serviceregistry.client.business.ServiceRegistrationClient;
import net.ihe.gazelle.serviceregistry.client.business.ServiceRegistrationException;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.function.Consumer;

/**
 * Implementation of the ServiceRegistrationClient using Jakarta WebSocket for service registration. This client
 * connects to a service registry via WebSocket and allows for service registration and event handling.
 */
public class ServiceRegistrationWebSocketClient implements ServiceRegistrationClient {

   private final String serviceRegistryUrl;
   private final AccessTokenService accessTokenService;

   /**
    * Constructor for ServiceRegistrationWebSocketClient with optional AccessTokenService.
    *
    * @param serviceRegistryUrl the URL of the service registry to connect to.
    * @param accessTokenService optional service to retrieve access tokens for authentication
    */
   public ServiceRegistrationWebSocketClient(String serviceRegistryUrl, AccessTokenService accessTokenService) {
      this.serviceRegistryUrl = serviceRegistryUrl.replaceAll("^http", "ws");
      this.accessTokenService = accessTokenService;
   }


   @Override
   public RegistrationSession connectAndRegister(Service service, Consumer<Event> eventConsumer) {
      RegistrationWebSocketSession newSession = new RegistrationWebSocketSession(service, eventConsumer, accessTokenService);
      doConnect(newSession);
      try {
         newSession.awaitOpen();
         return newSession;
      } catch (InterruptedException e) {
         newSession.close();
         Thread.currentThread().interrupt();
         throw new ServiceRegistrationException(
               "Interrupted while waiting for the WebSocket session to open. Terminating socket now.", e);
      }
   }

   @Override
   public synchronized void keepAlive(RegistrationSession session) {
      if (session instanceof RegistrationWebSocketSession webSocketSession) {
         if (!webSocketSession.isOpen()) {
            webSocketSession.pushEvent(new Event(Event.Level.INFO, "Reconnect attempt to service-registry..."));
            doConnect(webSocketSession);
            webSocketSession.pushEvent(new Event(Event.Level.INFO, "Reconnected to service-registry."));
         } else {
            webSocketSession.doRegister();
         }
      } else {
         throw new IllegalArgumentException(
               "Only sessions of type RegistrationWebSocketSession are supported by the " +
               "ServiceRegistrationWebSocketClient.");
      }
   }

   private void doConnect(RegistrationWebSocketSession registrationSession) {
      try {
         registrationSession.pushEvent(
               new Event(Event.Level.DEBUG,
                     MessageFormat.format("Connecting to service-registry at {0}", serviceRegistryUrl))
         );
         ContainerProvider.getWebSocketContainer()
               .connectToServer(registrationSession, null,
                     buildSocketURI(serviceRegistryUrl, registrationSession.getServiceId()));
         registrationSession.pushEvent(
               new Event(Event.Level.DEBUG, "Connected to service-registry.")
         );
      } catch (Exception e) {
         registrationSession.pushEvent(new Event(Event.Level.ERROR,
               MessageFormat.format("Error while connecting to the service-registry at {0}", serviceRegistryUrl), e)
         );
         registrationSession.close();
      }
   }

   private static URI buildSocketURI(final String baseUrl, ServiceId serviceId) {
      return URI.create(
            baseUrl + "/service-registration"
            + "/" + escapeURLParam(serviceId.instanceId())
            + "/" + escapeURLParam(serviceId.replicaId())
      );
   }

   private static String escapeURLParam(final String param) {
      return URLEncoder.encode(param, StandardCharsets.UTF_8);
   }

}
