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

import jakarta.websocket.*;
import net.ihe.gazelle.modelmarshaller.technical.jackson.JacksonSerDes;
import net.ihe.gazelle.modelmarshaller.technical.serialization.SerializationException;
import net.ihe.gazelle.modelmarshaller.technical.serialization.TextSerDes;
import net.ihe.gazelle.oidc.common.business.accesstoken.AccessTokenService;
import net.ihe.gazelle.servicemetadata.api.business.Service;
import net.ihe.gazelle.servicemetadata.api.business.ServiceValidator;
import net.ihe.gazelle.servicemetadata.technical.jaxrs.ServiceDTO;
import net.ihe.gazelle.serviceregistry.api.business.ServiceId;
import net.ihe.gazelle.serviceregistry.api.technical.websocket.Outcome;
import net.ihe.gazelle.serviceregistry.client.business.ServiceRegistrationClient;
import net.ihe.gazelle.serviceregistry.client.business.ServiceRegistrationClient.Event;
import net.ihe.gazelle.serviceregistry.client.business.ServiceRegistrationClient.Event.Level;
import net.ihe.gazelle.serviceregistry.client.technical.dto.SecuredServiceDTO;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

/**
 * Jakarta WebSocket implementation of a Service Registration Session. Most of the methods are package-private as they
 * are intented to be called by the {@link ServiceRegistrationWebSocketClient}.
 */
final class RegistrationWebSocketSession extends Endpoint implements ServiceRegistrationClient.RegistrationSession {

   private static final TextSerDes SERDES = new JacksonSerDes();
   private static final Consumer<Event> INTO_THE_VOID = e -> {};
   private static final int NUMBER_OF_LATCH = 1;

   private final Service service;
   private final ServiceId serviceId;
   private final Consumer<Event> eventConsumer;
   private final AccessTokenService accessTokenService;
   private CountDownLatch sessionLatch = new CountDownLatch(NUMBER_OF_LATCH);
   private Session session = null;

   /**
    * Constructor to create a WebSocket session for service registration. It will serialize the service into a
    * {@link ServiceDTO} and use it to create the service message to send over the WebSocket.
    *
    * @param service            the service to register
    * @param eventConsumer      the consumer to handle events raised by this session
    * @param accessTokenService optional service to retrieve access tokens for authentication
    *
    * @throws IllegalArgumentException if the service is not valid
    */
   RegistrationWebSocketSession(Service service, Consumer<Event> eventConsumer, AccessTokenService accessTokenService) {
      this(
            assertValidAndGetId(service),
            service,
            eventConsumer != null ? eventConsumer : INTO_THE_VOID,
            accessTokenService
      );
   }

   private RegistrationWebSocketSession(ServiceId serviceId, Service service, Consumer<Event> eventConsumer,
                                        AccessTokenService accessTokenService) {
      this.service = service;
      this.serviceId = serviceId;
      this.eventConsumer = eventConsumer;
      this.accessTokenService = accessTokenService;
   }

   /**
    * onOpen is called by the container when the WebSocket connection is opened. It will release any code waiting for
    * the connection to be established on {@link #awaitOpen()} and register the service with the service registry.
    * <p>
    * DO NOT CALL THIS METHOD DIRECTLY.
    *
    * @param session the WebSocket session
    */
   @Override
   public void onOpen(Session session, EndpointConfig endpointConfig) {
      session.addMessageHandler(String.class, (MessageHandler.Whole<String>) this::handleIncomingMessage);
      this.session = session;
      sessionLatch.countDown();
      doRegister();
   }

   /**
    * onClose is called by the container when the WebSocket connection is closed. It will reset the session and
    * openLatch to allow for a new connection to be established.
    * <p>
    * DO NOT CALL THIS METHOD DIRECTLY.
    *
    * @param session the WebSocket session that was closed
    */
   @Override
   public synchronized void onClose(Session session, CloseReason closeReason) {
      super.onClose(session, closeReason);
      this.session = null;
      this.sessionLatch = new CountDownLatch(NUMBER_OF_LATCH);
      eventConsumer.accept(
            new Event(Level.INFO,
                  MessageFormat.format("Registration WebSocket session closed: {0} - {1}", closeReason.getCloseCode(),
                        closeReason.getReasonPhrase()))
      );
   }

   /**
    * onError is called by the container when an error occurs in the WebSocket session. It will log an error event with
    * the error message.
    * <p>
    * DO NOT CALL THIS METHOD DIRECTLY.
    *
    * @param session   the WebSocket session where the error occurred
    * @param throwable the throwable that caused the error
    */
   @Override
   public void onError(Session session, Throwable throwable) {
      super.onError(session, throwable);
      eventConsumer.accept(
            new Event(Level.ERROR,
                  MessageFormat.format("Error in websocket for service-registration: {0}", throwable.getMessage()),
                  throwable)
      );
   }

   /**
    * Checks if the WebSocket session is open (aka, the {@link #onOpen(Session, EndpointConfig)} method has been completed). Because
    * websockets are inherently asynchronous, calling this method right after the creation of the session may return
    * false. To ensure the session is open, you should call {@link #awaitOpen()} first.
    *
    * @return true if the session is open, false otherwise
    */
   @Override
   public boolean isOpen() {
      return session != null && session.isOpen();
   }

   /**
    * Closes the WebSocket session. This method will close the session and reset the session and openLatch to allow for
    * a new connection to be established.
    * <p>
    * If an error occurs while closing the session, it will log an error message and close the session quietly.
    */
   @Override
   public synchronized void close() {
      try {
         if (isOpen()) {
            session.close();
         }
      } catch (IOException e) {
         eventConsumer.accept(
               new Event(Level.ERROR,
                     "Error closing websocket for service-registration. Will force closing now...", e)
         );
         IOUtils.closeQuietly(session);
      } finally {
         session = null;
         sessionLatch = new CountDownLatch(NUMBER_OF_LATCH);
      }
   }


   @Override
   public ServiceId getServiceId() {
      return serviceId;
   }

   /**
    * Waits for the WebSocket session to be opened. This method will block until the {@link #onOpen(Session, EndpointConfig)} method has
    * been completed, indicating that the session is ready for use.
    *
    * @throws InterruptedException if the thread is interrupted while waiting
    */
   void awaitOpen() throws InterruptedException {
      sessionLatch.await();
   }

   /**
    * Sends the service registration message to the service registry. This method will send the service message over the
    * WebSocket connection and log an event indicating that the registration has been sent. The session must be open to
    * call this method.
    * <p>
    * If an error occurs while sending the message, it will raise an error event.
    */
   void doRegister() {
      try {
         String message = SERDES.serializeAsString(injectToken(service));
         session.getAsyncRemote().sendText(message);
         eventConsumer.accept(
               new Event(
                     Level.DEBUG,
                     MessageFormat.format("Registration of Service `{0} {1}` sent.",
                           serviceId.instanceId(), serviceId.replicaId())
               )
         );
      } catch (SerializationException e) {
         eventConsumer.accept(
               new Event(Level.ERROR,
                     MessageFormat.format("Error while serializing service `{0} {1}`: {2}",
                           serviceId.instanceId(), serviceId.replicaId(), e.getMessage()), e)
         );
      }
   }

   private ServiceDTO<Service> injectToken(Service service) {
      if (accessTokenService != null) {
         try {
            String jwt = accessTokenService.getAccessToken();
            if (jwt != null) {
               eventConsumer.accept(
                     new Event(Level.DEBUG, "Added authentication token to WebSocket message")
               );
               return new SecuredServiceDTO(service, jwt);
            }
         } catch (Exception e) {
            eventConsumer.accept(
                  new Event(Level.WARN, "Failed to add authentication token to WebSocket message: " + e.getMessage())
            );
         }
      }
      return new ServiceDTO<>(service);
   }

   /**
    * Pushes an event to the event consumer associated with this session. Used by the websocket client.
    *
    * @param event the event to push
    */
   void pushEvent(Event event) {
      eventConsumer.accept(event);
   }

   /**
    * Socket listener method called when a message is received from the WebSocket. It will raise an event according to
    * the message received.
    *
    * @param message the message received from the WebSocket
    */
   private void handleIncomingMessage(String message) {
      Outcome outcome = SERDES.deserialize(message, Outcome.class);
      if (Outcome.Status.FAILURE.equals(outcome.getStatus())) {
         eventConsumer.accept(
               new Event(Level.ERROR,
                     MessageFormat.format("Received error from service-registry: {0}", outcome.getMessage()))
         );
      } else {
         eventConsumer.accept(
               new Event(Level.DEBUG,
                     MessageFormat.format("Incoming message from service-registry: {0}", outcome.getMessage()))
         );
      }
   }

   private static ServiceId assertValidAndGetId(Service service) {
      new ServiceValidator().assertValid(service);
      return new ServiceId(service);
   }
}
