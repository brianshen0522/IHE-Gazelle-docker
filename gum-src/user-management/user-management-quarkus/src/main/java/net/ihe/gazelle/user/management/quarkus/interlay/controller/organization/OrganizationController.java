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

package net.ihe.gazelle.user.management.quarkus.interlay.controller.organization;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import net.ihe.gazelle.oidc.rest.business.ProtectedResource;
import net.ihe.gazelle.user.management.api.application.configuration.ApplicationConfig;
import net.ihe.gazelle.user.management.api.interlay.organization.OrganizationDto;
import net.ihe.gazelle.user.management.api.interlay.organization.OrganizationCreationRequest;
import net.ihe.gazelle.user.management.commons.interlay.exceptions.ErrorResponseBody;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import static net.ihe.gazelle.user.management.quarkus.interlay.controller.user.UserController.*;

/**
 * REST controller interface for managing organizations in the Gazelle User Management system.
 */
@Path(OrganizationController.ORGANIZATION_REST_PATH)
@Tag(name = "Organization management", description = "Organization management API.")
public interface OrganizationController {

    String ORGANIZATION_REST_PATH = "/rest/organizations";

    /**
     * Retrieve a unique organization based on its id (keyword).
     * @param organizationId the id of the organization to retrieve
     * @return a Response containing the organization matching the given id, along with appropriate HTTP status codes.
     */
    @GET
    @Path("{organizationId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Retrieve unique organization", description = "Get an unique organization from the id")
    @APIResponse(responseCode = "200", description = "Matching organization",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = OrganizationDto.class))
    )
    @APIResponse(responseCode = "404", description = "No organization found with the given id.",
            content = @Content(schema = @Schema(implementation = ErrorResponseBody.class),
                    mediaType = MediaType.APPLICATION_JSON))
    Response getOrganizationsById(@PathParam("organizationId") String organizationId);


    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @ProtectedResource
    @SecurityRequirement(name = ApplicationConfig.SECURITY_SCHEME)
    @Operation(summary = "Create a new organization", description = "Create a new organization in the system based on the provided organization information.")
    @APIResponse(responseCode = "201", description = "Organization created",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = OrganizationDto.class))
    )
    @APIResponse(responseCode = "400", description = BAD_REQUEST_DESCRIPTION,
            content = @Content(schema = @Schema(implementation = ErrorResponseBody.class), mediaType = MediaType.APPLICATION_JSON))
    Response createOrganization(OrganizationCreationRequest organizationCreationRequest);

    @PATCH
    @Path("{organizationId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ProtectedResource
    @SecurityRequirement(name = ApplicationConfig.SECURITY_SCHEME)
    @Operation(summary = "Patch an organization", description = "Patch an organization in the system based on the provided organization information.")
    @APIResponse(responseCode = "200", description = "Organization updated",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = OrganizationDto.class))
    )
    @APIResponse(responseCode = "400", description = BAD_REQUEST_DESCRIPTION,
            content = @Content(schema = @Schema(implementation = ErrorResponseBody.class), mediaType = MediaType.APPLICATION_JSON))
    Response patchOrganization(@PathParam("organizationId") String organizationId, OrganizationCreationRequest organizationCreationRequest);

    @DELETE
    @Path("{organizationId}")
    @ProtectedResource
    @SecurityRequirement(name = ApplicationConfig.SECURITY_SCHEME)
    @Operation(summary = "Archive an organization", description = "Archive an organization in the system based on the provided organization id.")
    @APIResponse(responseCode = "200", description = "Organization archived", content = @Content)
    @APIResponse(responseCode = "404", description = NOT_FOUND_DESCRIPTION, content = @Content(schema = @Schema(implementation = ErrorResponseBody.class), mediaType = MediaType.APPLICATION_JSON))
    @APIResponse(responseCode = "401", description = UNAUTHORIZED_DESCRIPTION,
            content = @Content(schema = @Schema(implementation = ErrorResponseBody.class), mediaType = MediaType.APPLICATION_JSON))
    @APIResponse(responseCode = "403", description = FORBIDDEN_DESCRIPTION,
            content = @Content(schema = @Schema(implementation = ErrorResponseBody.class), mediaType = MediaType.APPLICATION_JSON))
    Response archiveOrganization(@PathParam("organizationId") String organizationId);

}