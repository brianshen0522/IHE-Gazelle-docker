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

import jakarta.inject.Inject;
import jakarta.websocket.CloseReason;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import net.ihe.gazelle.errorhandling.business.UnexpectedError;
import net.ihe.gazelle.modelmarshaller.technical.serialization.DeserializationException;
import net.ihe.gazelle.modelmarshaller.technical.serialization.TextSerDes;
import net.ihe.gazelle.oidc.websocket.technical.ProtectedWebSocket;
import net.ihe.gazelle.security.business.GazelleIdentity;
import net.ihe.gazelle.security.business.UnauthorizedException;
import net.ihe.gazelle.servicemetadata.api.business.Service;
import net.ihe.gazelle.serviceregistry.api.business.ServiceId;
import net.ihe.gazelle.serviceregistry.api.technical.websocket.Outcome;
import net.ihe.gazelle.serviceregistry.business.registration.ServiceRegistration;
import net.ihe.gazelle.serviceregistry.technical.dto.SecuredRegistrationDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.NoSuchElementException;

/**
 * WebSocket endpoint for service registration. This endpoint allows services to register themselves by sending a
 * message containing their metadata. Registered services are considered available as long as they keep the connection
 * open. And they will be marked unreachable upon deconnection.
 */
@ServerEndpoint("/service-registration/{instanceId}/{replicaId}")
public class RegistrationWebSocketImpl {

     private static final Logger LOG = LoggerFactory.getLogger(RegistrationWebSocketImpl.class);

     private final ServiceRegistration serviceRegistration;
     private final GazelleIdentity identity;
     private final TextSerDes serdes;

     /**
      * Constructor for the RegistrationWebSocketImpl.
      *
      * @param serviceRegistration the service registration business logic
      * @param identity           the GazelleIdentity of the current user
      * @param serdes             the TextSerDes for serialization/deserialization (managed singleton)
      */
     @Inject
     public RegistrationWebSocketImpl(ServiceRegistration serviceRegistration, GazelleIdentity identity, TextSerDes serdes) {
         this.serviceRegistration = serviceRegistration;
         this.identity = identity;
         this.serdes = serdes;
     }

    /**
     * Handles incoming messages from the WebSocket client. This method is called when a client sends a message to the
     * WebSocket endpoint.
     * <p>
     * This method will respond Outcome.success() to the client if the service is registered successfully, or
     * Outcome.failure() if there is an error (parsing error, bad request or other unexpected error).
     *
     * @param session    the WebSocket session
     * @param instanceId the instance ID of the service
     * @param replicaId  the replica ID of the service
     * @param message    the message sent by the client, containing service metadata.
     */
    @OnMessage
    @ProtectedWebSocket
    public void onMessage(Session session,
                          @PathParam("instanceId") String instanceId,
                          @PathParam("replicaId") String replicaId,
                          String message) {
         try {
             LOG.debug("Received message: {}", message);
             SecuredRegistrationDTO<Service> serviceDTO = serdes.deserialize(message, SecuredRegistrationDTO.class);
             serviceDTO.setInstanceId(instanceId).setReplicaId(replicaId);
             serviceRegistration.connectService(serviceDTO.getBusinessObject(),identity);
             session.getAsyncRemote().sendText(
                     serdes.serializeAsString(
                             Outcome.success("Service " + instanceId + "/" + replicaId + " registered successfully."))
             );
         } catch (DeserializationException e) {
             // client side errors are logged at debug level
             LOG.debug("Error parsing registration websocket message: ", e);
             session.getAsyncRemote().sendText(
                     serdes.serializeAsString(Outcome.failure("Invalid registration message: " + new UnexpectedError(e)))
             );
         } catch (IllegalArgumentException e) {
             // client side errors are logged at debug level
             LOG.debug("Invalid service registration attempt: ", e);
             session.getAsyncRemote().sendText(
                     serdes.serializeAsString(
                             Outcome.failure("Invalid service registration attempt: " + new UnexpectedError(e)))
             );
         } catch (UnauthorizedException e) {
             LOG.error("Unauthorized registration attempt", e);
            try {
                session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, "Unauthenticated"));
            } catch (IOException ex) {
                LOG.error("Error closing WebSocket session after unauthorized registration attempt", ex);
            }
         } catch (Exception e) {
             LOG.error("Unexpected error during service registration for {}:{}", instanceId, replicaId, e);
             session.getAsyncRemote().sendText(
                     serdes.serializeAsString(Outcome.failure(new UnexpectedError(e).toString()))
             );
         }
    }

    /**
     * Handles the closing of the WebSocket session. This method is called when the client disconnects from the WebSocket
     * endpoint and marks the service as unreachable.
     * <p>
     * In case of error, this method will log the error, but will not send a message back to the client.
     *
     * @param session    the WebSocket session
     * @param instanceId the instance ID of the service
     * @param replicaId  the replica ID of the service
     */
    @OnClose
    public void onClose(Session session,
                        @PathParam("instanceId") String instanceId,
                        @PathParam("replicaId") String replicaId) {
        try {
            serviceRegistration.disconnectService(new ServiceId(instanceId, replicaId));
        } catch (NoSuchElementException e) {
            // client side errors are logged at debug level
            LOG.debug("Unable to disconnect unknown service", e);
        } catch (Exception e) {
            LOG.error("Unexpected error during disconnect of service {}:{}", instanceId, replicaId, e);
        }
    }

}
