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

package net.ihe.gazelle.serviceregistry.technical.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import net.ihe.gazelle.oidc.rest.business.ProtectedResource;
import net.ihe.gazelle.search.jaxrs.api.AbstractSearchServiceRest;
import net.ihe.gazelle.security.business.GazelleIdentity;
import net.ihe.gazelle.security.business.UnauthorizedException;
import net.ihe.gazelle.serviceregistry.api.business.DeployedService;
import net.ihe.gazelle.serviceregistry.api.business.ServiceId;
import net.ihe.gazelle.serviceregistry.api.business.lookup.ServiceSearchCriteria;
import net.ihe.gazelle.serviceregistry.api.technical.dto.DeployedServiceDTO;
import net.ihe.gazelle.serviceregistry.business.lookup.ServiceLookup;
import net.ihe.gazelle.serviceregistry.business.registration.ServiceRegistration;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.headers.Header;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.NoSuchElementException;

import static net.ihe.gazelle.serviceregistry.api.business.lookup.ServiceLookupIdentifier.INTERFACE_NAME;
import static net.ihe.gazelle.serviceregistry.technical.openapi.OpenApiExample.EXAMPLE_DEPLOYED_SERVICE;
import static net.ihe.gazelle.serviceregistry.technical.openapi.OpenApiExample.EXAMPLE_DEPLOYED_SERVICES;

/**
 * REST controller for looking up services. Provides endpoints to search and retrieve services.
 */
@Path("/")
public class ServiceLookupController extends
      AbstractSearchServiceRest<DeployedService, DeployedServiceDTO, ServiceQueryBeanParam, ServiceSearchCriteria> {

   private static final String CONTENT_TYPE = "Content-Type";
   private static final String SECURITY_REQUIREMENT_KEYCLOAK = "Keycloak";

   private final ServiceRegistration serviceRegistration;
   private final GazelleIdentity identity;

   /**
    * Constructor for the ServiceLookupController.
    *
    * @param serviceRegistration the service registration business logic
    * @param serviceLookup       the service lookup business logic
    * @param identity            the Gazelle identity
    */
   @Inject
   public ServiceLookupController(ServiceRegistration serviceRegistration, ServiceLookup serviceLookup, GazelleIdentity identity) {
      super(serviceLookup, serviceLookup, new ServiceQueryMapper(serviceLookup), serviceLookup, identity);
      this.identity = identity;
      this.serviceRegistration = serviceRegistration;
   }

   @Override
   @GET
   @Path("/services" + GET_INDEXES_PATH)
   @Tag(name = INTERFACE_NAME)
   @SecurityRequirement(name = SECURITY_REQUIREMENT_KEYCLOAK)
   @ProtectedResource
   public Response getIndexes() {
      return super.getIndexes();
   }

   @Override
   @GET
   @Path("/services" + POSSIBLE_VALUES_PATH)
   @SecurityRequirement(name = SECURITY_REQUIREMENT_KEYCLOAK)
   @Tag(name = INTERFACE_NAME)
   @ProtectedResource
   public Response getPossibleValues(
         @PathParam(POSSIBLE_VALUES_FIELD) String field,
         @BeanParam ServiceQueryBeanParam searchParamBean
   ) {
      return super.getPossibleValues(field, searchParamBean);
   }

   @Override
   @GET
   @Path("/services")
   @Tag(name = INTERFACE_NAME)
   @SecurityRequirement(name = SECURITY_REQUIREMENT_KEYCLOAK)
   @Operation(
         summary = "Search for deployed services",
         description = "Search deployed services in Gazelle Test Bed with filter and sort parameters. An offset and " +
               "limit can be used to paginate result."
   )
   @APIResponse(
         responseCode = "200",
         description =
               "The List of deployed services. The result is by default sorted by name in alphabetical order.",
         content = @Content(
               mediaType = MediaType.APPLICATION_JSON,
               schema = @Schema(implementation = DeployedServiceDTO[].class),
               example = EXAMPLE_DEPLOYED_SERVICES
         ),
         headers = {
               @Header(
                     name = "Content-Range",
                     description = "Information about the result sub-set returned, its start and finish indexes " +
                           "over the number total of matches.",
                     schema = @Schema(type = SchemaType.STRING, examples = "items 0-24/1420")
               )
         }
   )
   @ProtectedResource
   public Response search(@BeanParam ServiceQueryBeanParam queryBeanParam) {
      return super.search(queryBeanParam);
   }

   /**
    * Retrieves a deployed service by its instance and replica identifiers.
    *
    * @param instanceId the identifier of the service instance
    * @param replicaId  the identifier of the service replica
    * @return a 200-OK Response containing the deployed service details or an error message (400, 404, or 500) if there
    * is an issue.
    */
   @GET
   @Path("/services/{instanceId}/{replicaId}")
   @Produces(MediaType.APPLICATION_JSON)
   @Tag(name = INTERFACE_NAME)
   @Operation(
         summary = "Get a deployed service by instance and replica ID",
         description = "Get details of a deployed service using its instance and replica identifiers."
   )
   @APIResponse(
         responseCode = "200",
         description = "The requested service.",
         content = @Content(
               mediaType = MediaType.APPLICATION_JSON,
               schema = @Schema(implementation = DeployedServiceDTO.class),
               example = EXAMPLE_DEPLOYED_SERVICE
         )
   )
   @SecurityRequirement(name = SECURITY_REQUIREMENT_KEYCLOAK)
   @ProtectedResource
   public Response getService(@PathParam("instanceId") String instanceId,
                              @PathParam("replicaId") String replicaId) {
      try {
         DeployedService service = serviceRegistration.getService(new ServiceId(instanceId, replicaId), identity);
         return Response.ok(new DeployedServiceDTO(service))
               .build();
      } catch (IllegalArgumentException e) {
         return Response.status(Response.Status.BAD_REQUEST)
               .header(CONTENT_TYPE, MediaType.TEXT_PLAIN)
               .entity("Invalid service identifier: " + e.getMessage()).build();
      } catch (UnauthorizedException _) {
         return getUnauthorizedOrForbiddenResponse(identity);
      } catch (NoSuchElementException e) {
         return Response.status(Response.Status.NOT_FOUND)
               .header(CONTENT_TYPE, MediaType.TEXT_PLAIN)
               .entity("Service not found: " + e.getMessage()).build();
      } catch (Exception e) {
         LOG.atError().setCause(e).log("Unexpected error while retrieving service {}:{}", instanceId, replicaId);
         return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
               .header(CONTENT_TYPE, MediaType.TEXT_PLAIN)
               .entity("An error occurred while retrieving the service: " + e.getMessage()).build();
      }
   }

   @Override
   protected String getContentRangeType() {
      return "DeployedService";
   }

   @Override
   protected DeployedServiceDTO mapToDTO(DeployedService service) {
      return new DeployedServiceDTO(service);
   }

   /**
    * Get a 401 Unauthorized or 403 Forbidden response based on the identity status.
    */
   private static Response getUnauthorizedOrForbiddenResponse(GazelleIdentity identity) {
      if (identity == null || !identity.isAuthenticated()) {
         return Response.status(Response.Status.UNAUTHORIZED).entity("Unauthorized").type(MediaType.TEXT_PLAIN).build();
      } else {
         return Response.status(Response.Status.FORBIDDEN).entity("Forbidden").type(MediaType.TEXT_PLAIN).build();
      }
   }
}
