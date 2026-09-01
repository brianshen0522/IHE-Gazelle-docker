/*
 * Copyright 2026 IHE International.
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

package net.ihe.gazelle.serviceregistry.technical.rest;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import net.ihe.gazelle.oidc.rest.business.ProtectedResource;
import net.ihe.gazelle.security.business.GazelleIdentity;
import net.ihe.gazelle.security.business.UnauthorizedException;
import net.ihe.gazelle.servicemetadata.api.business.Service;
import net.ihe.gazelle.servicemetadata.technical.jaxrs.ServiceDTO;
import net.ihe.gazelle.serviceregistry.api.business.DeployedService;
import net.ihe.gazelle.serviceregistry.api.business.ServiceId;
import net.ihe.gazelle.serviceregistry.api.technical.dto.DeployedServiceDTO;
import net.ihe.gazelle.serviceregistry.business.registration.ServiceRegistration;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.ihe.gazelle.serviceregistry.technical.openapi.OpenApiExample.EXAMPLE_DEPLOYED_SERVICE;

/**
 * REST controller for service registration. Provides an endpoint for Legacy services to register themselves or send heartbeats.
 */
@Path("/")
public class ServiceRegistrationController {

   /** INTERFACE_NAME */
   public static final String INTERFACE_NAME = "Service Registration API";

   private static final Logger LOG = LoggerFactory.getLogger(ServiceRegistrationController.class);
   private static final String SECURITY_REQUIREMENT_KEYCLOAK = "Keycloak";
   private static final String CONTENT_TYPE = "Content-Type";

   private final ServiceRegistration serviceRegistration;
   private final GazelleIdentity identity;

   /**
    * Constructor for the ServiceRegistrationController class.
    *
    * @param serviceRegistration the service registration instance that manages the registration and tracking of services.
    * @param identity the identity object that provides authentication and authorization information for the current context.
    */
   public ServiceRegistrationController(ServiceRegistration serviceRegistration, GazelleIdentity identity) {
      this.serviceRegistration = serviceRegistration;
      this.identity = identity;
   }

   /**
    * Registers a Legacy service or sends a heartbeat for an already registered service.
    *
    * <p>This endpoint provides REST-based service registration for Legacy services that cannot
    * implement WebSocket connectivity. It serves dual purposes:</p>
    * <ul>
    *   <li>Initial registration: Registers a new service and marks it as AVAILABLE</li>
    *   <li>Heartbeat: Updates an existing service's heartbeat timestamp to maintain AVAILABLE status</li>
    * </ul>
    *
    * <p>The endpoint is idempotent - calling it multiple times with the same data has the same effect
    * as calling it once. Services must call this endpoint periodically (within the configured heartbeat
    * timeout) to prevent being marked as UNREACHABLE.</p>
    *
    * <p>Services registered via this endpoint are marked as self-registered and use the same lifecycle
    * management as WebSocket-registered services:</p>
    * <ul>
    *   <li>No heartbeat within timeout period → status becomes UNREACHABLE</li>
    *   <li>UNREACHABLE for 72 hours → service is removed from registry</li>
    * </ul>
    *
    * @param instanceId the unique identifier of the service instance (must match service metadata)
    * @param replicaId the unique identifier of the service replica (must match service metadata)
    * @param serviceDTO the service metadata containing registration information
    * @return 200 OK with the service details, or an appropriate error response if registration fails.
    */
   @PUT
   @Path("/services/{instanceId}/{replicaId}")
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   @Tag(name = INTERFACE_NAME)
   @Operation(
         summary = "Register or update Legacy service (heartbeat)",
         description = "Register a Legacy service that cannot use WebSocket-based registration, or send a heartbeat " +
               "for an already registered service. The service will be marked as AVAILABLE. " +
               "This endpoint is idempotent and should be called periodically (within the configured heartbeat " +
               "timeout) to maintain AVAILABLE status. Services that don't send heartbeats will be marked " +
               "UNREACHABLE after the timeout expires."
   )
   @APIResponse(
         responseCode = "200",
         description = "Service registered or updated successfully.",
         content = @Content(
               mediaType = MediaType.APPLICATION_JSON,
               schema = @Schema(implementation = DeployedServiceDTO.class),
               example = EXAMPLE_DEPLOYED_SERVICE
         )
   )
   @APIResponse(
         responseCode = "400",
         description = "Invalid service metadata or ID mismatch.",
         content = @Content(mediaType = MediaType.TEXT_PLAIN)
   )
   @APIResponse(
         responseCode = "401",
         description = "Not authenticated.",
         content = @Content(mediaType = MediaType.TEXT_PLAIN)
   )
   @APIResponse(
         responseCode = "403",
         description = "Authenticated but lacks required permissions.",
         content = @Content(mediaType = MediaType.TEXT_PLAIN)
   )
   @APIResponse(
         responseCode = "500",
         description = "Unexpected server error.",
         content = @Content(mediaType = MediaType.TEXT_PLAIN)
   )
   @SecurityRequirement(name = SECURITY_REQUIREMENT_KEYCLOAK)
   @ProtectedResource
   public Response registerOrUpdateService(
         @PathParam("instanceId") String instanceId,
         @PathParam("replicaId") String replicaId,
         @RequestBody(
               description = "Service metadata to register or update",
               required = true
         ) ServiceDTO<Service> serviceDTO) {
      try {
         serviceDTO.setInstanceId(instanceId).setReplicaId(replicaId);
         serviceRegistration.connectService(serviceDTO.getBusinessObject(), identity);

         DeployedService registered = serviceRegistration.getService(new ServiceId(instanceId, replicaId), identity);
         return Response.ok(new DeployedServiceDTO(registered))
               .build();
      } catch (IllegalArgumentException e) {
         return Response.status(Response.Status.BAD_REQUEST)
               .header(CONTENT_TYPE, MediaType.TEXT_PLAIN)
               .entity("Invalid service metadata: " + e.getMessage()).build();
      } catch (UnauthorizedException e) {
         return getUnauthorizedOrForbiddenResponse(identity, e);
      } catch (Exception e) {
         LOG.error("Error registering/updating service {}:{}", instanceId, replicaId, e);
         return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
               .header(CONTENT_TYPE, MediaType.TEXT_PLAIN)
               .entity("An error occurred while processing the service: " + e.getMessage()).build();
      }
   }

   /**
    * Get a 401 Unauthorized or 403 Forbidden response based on the identity status.
    */
   private static Response getUnauthorizedOrForbiddenResponse(GazelleIdentity identity, UnauthorizedException e) {
      if (identity == null || !identity.isAuthenticated()) {
         return Response.status(Response.Status.UNAUTHORIZED).entity("Unauthorized").type(MediaType.TEXT_PLAIN).build();
      } else {
         return Response.status(Response.Status.FORBIDDEN).entity("Forbidden").type(MediaType.TEXT_PLAIN).build();
      }
   }

}
