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

package net.ihe.gazelle.maestro.quarkus.websocket;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import net.ihe.gazelle.maestro.api.business.Maestro;
import net.ihe.gazelle.maestro.quarkus.websocket.dto.MessageDTO;
import net.ihe.gazelle.maestro.quarkus.websocket.dto.StartTestRunDTO;
import net.ihe.gazelle.maestro.quarkus.websocket.dto.StartTestSuiteRunDTO;
import net.ihe.gazelle.maestro.quarkus.websocket.dto.UserInteractionCompletedDTO;
import net.ihe.gazelle.modelmarshaller.technical.jackson.JacksonSerDes;
import net.ihe.gazelle.modelmarshaller.technical.serialization.DeserializationException;
import net.ihe.gazelle.modelmarshaller.technical.serialization.TextSerDes;
import net.ihe.gazelle.oidc.websocket.technical.ProtectedWebSocket;
import net.ihe.gazelle.security.business.GazelleIdentity;
import net.ihe.gazelle.security.business.UnauthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * WebSocket controller for handling test execution streams.
 * <p>
 * This endpoint allows clients to start test runs or test suite runs and to receive updates
 * asynchronously over a WebSocket connection. It also supports user interactions during
 * test execution.
 * </p>
 * <p>
 * The endpoint is located at <code>/v1/test-stream/run</code>.
 * </p>
 */
@RequestScoped
@ServerEndpoint("/v1/test-stream/run")
public class TestStreamSocketController {

   /**
    * Name of the interface.
    */
   public static final String INTERFACE_NAME = "Test Run Stream API";

   /**
    * Version of the interface.
    */
   public static final String INTERFACE_VERSION = "1.0";

   private static final Logger LOG = LoggerFactory.getLogger(TestStreamSocketController.class.getName());

   private final GazelleIdentity identity;
   private final WebSocketSessionService sessionService;
   private final TextSerDes serDes = new JacksonSerDes();
   private final Maestro maestro;

   /**
    * Creates a new TestStreamSocketController instance.
    *
    * @param identity the current Gazelle identity used to authenticate incoming WebSocket messages
    * @param maestro  the Maestro engine used to execute tests and test suites
    * @param sessionService the WebSocket session service used to manage WebSocket sessions
    */
   @Inject
   public TestStreamSocketController(GazelleIdentity identity, Maestro maestro, WebSocketSessionService sessionService) {
      this.identity = identity;
      this.maestro = maestro;
      this.sessionService = sessionService; // NOSONAR - CDI-managed service reference
   }

   /**
    * Called when a new WebSocket connection is opened.
    *
    * @param session the newly opened WebSocket session
    */
   @OnOpen
   public void onOpen(Session session) {
      LOG.info("Test Stream Socket Connected: {}", session.getId());
      WebSocketSession maestroCaller = new WebSocketSession(session);
      sessionService.addSession(session.getId(), maestroCaller);
   }

   /**
    * Called when a WebSocket connection is closed.
    *
    * @param session the WebSocket session being closed
    */
   @OnClose
   public void onClose(Session session) {
      LOG.info("Test Stream Socket Disconnected: {}", session.getId());
      sessionService.removeSession(session.getId());
   }

   /**
    * Called when a message is received from a WebSocket client.
    * <p>
    * Depending on the type of the message, it may start a test run, start a test suite run,
    * or complete a user interaction.
    * </p>
    *
    * @param session       the WebSocket session from which the message was received
    * @param messageString the raw message payload as a JSON string
    * @throws IOException if an error occurs when closing the session due to invalid input or authentication failure
    * @throws DeserializationException if unable to deserialize the received message
    */
   @OnMessage
   @ProtectedWebSocket
   public void onMessage(Session session, String messageString) throws IOException {
      LOG.debug("Received message: {}", messageString);
      try {
         if (!identity.isAuthenticated()) {
            throw new UnauthorizedException("Identity is not authenticated");
         }
         MessageDTO<?> message = serDes.deserialize(messageString, MessageDTO.class);
         WebSocketSession maestroCaller = sessionService.getSession(session.getId());
         switch (message) {
            case StartTestRunDTO testRun ->
                  maestro.executeTest(testRun.getBusinessObject(), testRun.isPersist(), maestroCaller);
            case StartTestSuiteRunDTO testSuiteRun ->
                  maestro.executeTestSuite(testSuiteRun.getBusinessObject(), testSuiteRun.isPersist(), maestroCaller);
            case UserInteractionCompletedDTO userInteractionCompleted ->
                  maestroCaller.completeInteraction(userInteractionCompleted.getBusinessObject());
            default -> {
               sessionService.removeSession(session.getId());
               String error = "Unexpected message type: " + message.getClass().getName();
               session.close(new CloseReason(CloseReason.CloseCodes.UNEXPECTED_CONDITION, error));
            }
         }
      } catch (DeserializationException e) {
         LOG.error("Unable to parse test run", e);
         session.close(new CloseReason(CloseReason.CloseCodes.UNEXPECTED_CONDITION, "Unable to parse test run, please contact an administrator."));
      } catch (UnauthorizedException e) {
         session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, "Unauthenticated"));
      } catch (Exception e) {
         LOG.error("Unexpected error during websocket test execution", e);
         session.close(new CloseReason(CloseReason.CloseCodes.CLOSED_ABNORMALLY, "Unexpected error, please contact an administrator."));
      }
   }
}
