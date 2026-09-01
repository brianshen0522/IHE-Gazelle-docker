package net.ihe.gazelle.user.management.quarkus.interlay.controller;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import net.ihe.gazelle.oidc.rest.business.ProtectedResource;
import net.ihe.gazelle.user.management.api.application.configuration.ApplicationConfig;
import net.ihe.gazelle.user.management.api.interlay.group.GroupResource;
import net.ihe.gazelle.user.management.commons.interlay.exceptions.ErrorResponseBody;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import static net.ihe.gazelle.user.management.quarkus.interlay.controller.user.UserController.*;

/**
 * REST controller interface for managing groups in the Gazelle User Management system.
 */
@Path("/rest/groups")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Group management", description = "Group management API.")
public interface GroupController {

    /**
     * Create a new group based on the provided group resource.
     * @param groupResource the resource containing the details of the group to create
     * @return 201 Created if the group was successfully created
     */
    @POST
    @ProtectedResource
    @SecurityRequirement(name = ApplicationConfig.SECURITY_SCHEME)
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Create a new group", description = "Create a new group.")
    @APIResponse(responseCode = "201", description = "Created - The group was successfully created")
    @APIResponse(responseCode = "400", description = "Bad request - The provided group resource is not valid",
            content = @Content(schema = @Schema(implementation = ErrorResponseBody.class),
                    mediaType = MediaType.APPLICATION_JSON))
    @Parameter(name = "groupResource", description = "The group resource")
    Response createGroup(GroupResource groupResource);

    /**
     * Search for groups in the GUM system.
     * @param search the search pattern (id or name)
     * @param type the type of group to search for
     * @param offset the offset of the search
     * @param limit the limit of the search
     * @return 200 OK with a list of matching groups
     */
    @GET
    @ProtectedResource
    @SecurityRequirement(name = ApplicationConfig.SECURITY_SCHEME)
    @Operation(summary = "Search for GUM groups", description = "Search for all the groups corresponding to the search parameters")
    @APIResponse(responseCode = "200", description = "OK - List of matching groups",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = GroupResource[].class))
    )
    @APIResponse(responseCode = "400", description = "Bad request",
            content = @Content(schema = @Schema(implementation = ErrorResponseBody.class),
                    mediaType = MediaType.APPLICATION_JSON))
    @Parameter(name = "search", description = "Search pattern (id or name)")
    @Parameter(name = "type", description = "The type of group", examples = {
            @ExampleObject(name = "All", value = ""),
            @ExampleObject(name = "ORGANIZATION", value = "org"),
            @ExampleObject(name = "ROLE", value = "role"),
            @ExampleObject(name = "ORGANIZATION_ADMIN", value = "org-adm")})
    @Parameter(name = "offset", description = "The offset of the search")
    @Parameter(name = "limit", description = "The limit of the search", example = "10")
    Response searchForGroups(@QueryParam("search") String search, @QueryParam("type") String type,
                             @QueryParam("offset") Integer offset, @QueryParam("limit") Integer limit);

    /**
     * Retrieve a group by its ID.
     * @param groupId the ID of the group to retrieve
     * @return 200 OK with the group resource
     */
    @GET
    @Path("{groupId}")
    @ProtectedResource
    @SecurityRequirement(name = ApplicationConfig.SECURITY_SCHEME)
    @Operation(summary = "Retrieve an group by its id", description = "Retrieve an group by its id.")
    @APIResponse(responseCode = "200", description = "OK - The corresponding group",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = GroupResource.class)))
    @APIResponse(responseCode = "401", description = UNAUTHORIZED_DESCRIPTION,
            content = @Content(schema = @Schema(implementation = ErrorResponseBody.class), mediaType = MediaType.APPLICATION_JSON))
    @APIResponse(responseCode = "403", description = FORBIDDEN_DESCRIPTION,
            content = @Content(schema = @Schema(implementation = ErrorResponseBody.class), mediaType = MediaType.APPLICATION_JSON))
    @APIResponse(responseCode = "404", description = NOT_FOUND_DESCRIPTION,
            content = @Content(schema = @Schema(implementation = ErrorResponseBody.class), mediaType = MediaType.APPLICATION_JSON))
    Response getGroupById(@PathParam("groupId") String groupId);

    /**
     * Update a Gazelle group.
     * @param groupId the ID of the group to update
     * @param groupResource the updated group resource
     * @return 200 OK with the updated group resource
     */
    @PATCH
    @Path("/{groupId}")
    @ProtectedResource
    @SecurityRequirement(name = ApplicationConfig.SECURITY_SCHEME)
    @Operation(summary = "Edit a Gazelle group", description = "Edit name or group membership of a Gazelle group")
    @APIResponse(responseCode = "200", description = "OK - Successful edited",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = GroupResource.class)))
    @APIResponse(responseCode = "400", description = "Bad request",
            content = @Content(schema = @Schema(implementation = ErrorResponseBody.class), mediaType = MediaType.APPLICATION_JSON))
    @APIResponse(responseCode = "404", description = NOT_FOUND_DESCRIPTION,
            content = @Content(schema = @Schema(implementation = ErrorResponseBody.class), mediaType = MediaType.APPLICATION_JSON))
    @Parameter(name = "groupResource", description = "The group resource")
    Response updateGroup(@PathParam("groupId") String groupId, GroupResource groupResource);

    /**
     * Delete a Gazelle group.
     * @param groupId the ID of the group to delete
     * @return 200 OK with the deleted group resource
     */
    @DELETE
    @Path("/{groupId}")
    @ProtectedResource
    @SecurityRequirement(name = ApplicationConfig.SECURITY_SCHEME)
    @Operation(summary = "Delete a Gazelle group", description = "Delete a Gazelle group.")
    @APIResponse(responseCode = "200", description = "OK - Successful deleted",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = GroupResource.class)))
    @APIResponse(responseCode = "404", description = "Group not found",
            content = @Content(schema = @Schema(implementation = ErrorResponseBody.class),
                    mediaType = MediaType.APPLICATION_JSON))
    Response deleteGroup(@PathParam("groupId") String groupId);

}
