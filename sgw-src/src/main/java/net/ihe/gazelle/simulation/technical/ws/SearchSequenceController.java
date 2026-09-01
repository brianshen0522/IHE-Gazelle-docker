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

package net.ihe.gazelle.simulation.technical.ws;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import net.ihe.gazelle.oidc.rest.business.ProtectedResource;
import net.ihe.gazelle.search.api.ReadException;
import net.ihe.gazelle.search.api.ReadService;
import net.ihe.gazelle.search.api.SearchService;
import net.ihe.gazelle.search.api.SuggestionService;
import net.ihe.gazelle.search.jaxrs.api.AbstractSearchServiceRest;
import net.ihe.gazelle.search.jaxrs.api.QueryMapper;
import net.ihe.gazelle.security.business.BaseGazelleIdentity;
import net.ihe.gazelle.security.business.GazelleIdentity;
import net.ihe.gazelle.simulation.business.model.ResolvedSimulationSequence;
import net.ihe.gazelle.simulation.business.search.SequenceIndexService;
import net.ihe.gazelle.simulation.business.search.SequenceSearchCriteria;
import net.ihe.gazelle.simulation.technical.dto.ResolvedSimulationSequenceDTO;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.ParameterIn;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * Controller for the search of supported sequences.
 */
@RequestScoped
@Path("/simulation/v1/sequences")
public class SearchSequenceController extends
      AbstractSearchServiceRest<ResolvedSimulationSequence, ResolvedSimulationSequenceDTO, SequenceQueryBeanParam, SequenceSearchCriteria> {

   /**
    * Content range type
    */
   public static final String CONTENT_RANGE_TYPE = "SimulationSequence";

   private final GazelleIdentity identity;
   private final ReadService<String, ResolvedSimulationSequence> readService;

   /**
    * Constructs a new instance of the SearchSequenceController, initializing its dependencies
    * for handling simulation sequence search operations.
    *
    * @param identity          The {@link GazelleIdentity} object representing the authenticated user context.
    * @param indexService      The {@link SequenceIndexService} used for defining index fields
    *                          and for facilitating searching and filtering of sequences.
    * @param suggestionService The {@link SuggestionService} for providing suggestions based
    *                          on {@link SequenceSearchCriteria}.
    * @param queryMapper       The {@link QueryMapper} responsible for mapping query parameters
    *                          from {@link SequenceQueryBeanParam} to {@link SequenceSearchCriteria}.
    * @param searchService     The {@link SearchService} for performing search operations
    *                          on {@link ResolvedSimulationSequence} objects based on
    *                          {@link SequenceSearchCriteria}.
    * @param readService       The {@link ReadService} for retrieving specific {@link ResolvedSimulationSequence}
    *                          instances by their unique identifiers.
    */
   @Inject
   public SearchSequenceController(
         GazelleIdentity identity,
         SequenceIndexService indexService,
         SuggestionService<SequenceSearchCriteria> suggestionService,
         QueryMapper<SequenceQueryBeanParam, SequenceSearchCriteria> queryMapper,
         SearchService<ResolvedSimulationSequence, SequenceSearchCriteria> searchService,
         ReadService<String, ResolvedSimulationSequence> readService) {
      super(indexService, suggestionService, queryMapper, searchService);
      this.identity = identity;
      this.readService = readService;
   }

   @Override
   @GET
   @ProtectedResource
   @Path(GET_INDEXES_PATH)
   @Tag(name = "Sequence Search API")
   @SecurityRequirement(name = "Keycloak")
   public Response getIndexes() {
      if (!identity.isAuthenticated()) {
         return Response.status(Response.Status.UNAUTHORIZED).build();
      }
      return super.getIndexes();
   }

   @Override
   @GET
   @ProtectedResource
   @Path(POSSIBLE_VALUES_PATH)
   @Tag(name = "Sequence Search API")
   @SecurityRequirement(name = "Keycloak")
   public Response getPossibleValues(
         @PathParam(POSSIBLE_VALUES_FIELD) String field,
         @BeanParam SequenceQueryBeanParam searchParamsBean
   ) {
      if (!identity.isAuthenticated()) {
         return Response.status(Response.Status.UNAUTHORIZED).build();
      }
      return super.getPossibleValues(field, searchParamsBean);
   }

   @Override
   @GET
   @ProtectedResource
   @Tag(name = "Sequence Search API")
   @SecurityRequirement(name = "Keycloak")
   @Operation(
         summary = "Search supported sequences",
         description = "Search supported sequences across all available simulation services, with filter and sort parameters."
   )
   @APIResponse(
         responseCode = "200",
         description =
               """
                     The List of supported sequences by the simulation service where this request was sent.
                     The result is by default sorted by id in alphabetical order but can be sorted by one of the following parameters : serviceName, transaction, standard, role.
                     The result can be sorted either in ascending or descending order.
                     A limit and offset can be used to paginate result.
                     """,
         content = @Content(
               mediaType = MediaType.APPLICATION_JSON,
               schema = @Schema(implementation = ResolvedSimulationSequenceDTO[].class)
         )
   )
   public Response search(
         @BeanParam SequenceQueryBeanParam searchParamsBean,
         @QueryParam(PRESENTATION_FIELDS) List<String> attributePaths) {
      if (!identity.isAuthenticated()) {
         return Response.status(Response.Status.UNAUTHORIZED).build();
      }
      return super.search(searchParamsBean, attributePaths);
   }

   /**
    * Retrieves a Simulation Sequence by providing its unique identifier.
    *
    * @param id The unique identifier of the Simulation Sequence to be retrieved.
    * @return A Response object containing the retrieved Simulation Sequence in JSON format if
    * the request is successful. Possible response statuses:
    * - 200: The Simulation Sequence identified by the provided id.
    * - 401: If the id is not a string or the user is not authenticated.
    * - 404: If no Simulation Sequence exists for the provided id.
    * - 500: If an unexpected error occurs during retrieval.
    */
   @GET
   @Path("{id}")
   @ProtectedResource
   @Produces(MediaType.APPLICATION_JSON)
   @Tag(name = "Sequence Search API")
   @SecurityRequirement(name = "Keycloak")
   @Operation(
         summary = "Get Simulation Sequence by id",
         description = "Retrieve a Simulation Sequence by providing its unique identifier."
   )
   @APIResponse(
         responseCode = "200",
         description = "The Simulation Sequence that is identified by the provided id.",
         content = @Content(
               mediaType = MediaType.APPLICATION_JSON,
               schema = @Schema(implementation = ResolvedSimulationSequenceDTO.class)
         )
   )
   @APIResponse(
         responseCode = "401",
         description = "If the id is not a string."
   )
   @APIResponse(
         responseCode = "404",
         description = "If the id does not belong to any Simulation sequence."
   )
   @APIResponse(
         responseCode = "500",
         description = "If any unexpected error occurs."
   )
   public Response getSimulationSequenceById(
         @Parameter(
               in = ParameterIn.PATH,
               name = "id",
               description = "The unique identifier of the Simulation sequence to apply the retrieving."
         )
         @PathParam("id") String id
   ) {
      try {
         if (!identity.isAuthenticated()) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
         }
         ResolvedSimulationSequence resolvedSimulationSequence = readService.readObject(id, BaseGazelleIdentity.unauthenticatedIdentity());
         return Response.ok(new ResolvedSimulationSequenceDTO(resolvedSimulationSequence)).build();
      } catch (ReadException e) {
         LOG.debug(e.getMessage(), e);
         return Response
               .status(Response.Status.BAD_REQUEST)
               .entity(e.getMessage())
               .build();
      } catch (NoSuchElementException e) {
         LOG.debug(e.getMessage(), e);
         return Response
               .status(Response.Status.NOT_FOUND)
               .entity(e.getMessage())
               .build();
      } catch (Exception e) {
         LOG.error("Error while reading Simulation sequence.", e);
         return Response
               .status(Response.Status.INTERNAL_SERVER_ERROR)
               .entity("Error while reading Simulation sequence.")
               .build();
      }
   }

   @Override
   public String getContentRangeType() {
      return CONTENT_RANGE_TYPE;
   }

   @Override
   public ResolvedSimulationSequenceDTO mapToDTO(ResolvedSimulationSequence object) {
      return new ResolvedSimulationSequenceDTO(object);
   }

}
