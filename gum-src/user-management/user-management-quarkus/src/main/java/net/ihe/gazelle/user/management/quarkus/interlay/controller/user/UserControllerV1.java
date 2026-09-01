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

package net.ihe.gazelle.user.management.quarkus.interlay.controller.user;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import net.ihe.gazelle.oidc.rest.business.ProtectedResource;
import net.ihe.gazelle.user.management.api.application.configuration.ApplicationConfig;
import net.ihe.gazelle.user.management.api.application.user.lookup.SortOrder;
import net.ihe.gazelle.user.management.api.domain.user.UserPreference;
import net.ihe.gazelle.user.management.api.interlay.user.*;
import net.ihe.gazelle.user.management.commons.interlay.exceptions.ErrorResponseBody;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.ParameterStyle;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import static net.ihe.gazelle.user.management.quarkus.interlay.controller.openapi.OpenApiExamples.*;

/**
 * REST controller exposing the user management endpoints.
 * @deprecated Use {@link UserController} instead.
 */
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "User management V1", description = "Warning Deprecated, User management API.")
@Path("/rest/users")
@Deprecated(since = "5.0.0")
public interface UserControllerV1 {

    /**
     * Common description for 404 Not Found responses.
     */
    String NOT_FOUND_DESCRIPTION = "Not found - The user was not found";
    /**
     * Common description for 403 Forbidden responses.
     */
    String FORBIDDEN_DESCRIPTION = "Forbidden - Insufficient permissions";
    /**
     * Common description for 401 Unauthorized responses.
     */
    String UNAUTHORIZED_DESCRIPTION = "Unauthorized - Please provide a valid bearer token";
    /**
     * Common description for 400 Bad Request responses.
     */
    String BAD_REQUEST_DESCRIPTION = "Bad request - The provided JSON body is not valid";

    /**
     * Register a new user. This endpoint is public to allows anyone to register its own account. By default,
     * new users are disabled. Possibility to create a new organization during the registration or join an existing one.
     *
     * @param userRegisterRequest The user registration request containing the user information and the organization information
     * @return A Response with status 201 and the created user in body if the registration is successful,
     * or an error response with status 400 if the request is invalid, or 409 if the email already exist
     */
    @POST
    @Path("register")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Register a new user", description = "Register a new user. This endpoint is public to allows" +
            " anyone to register its own account. By default, new users are disabled. Possibility to create a new organization" +
            " during the registration or join an existing one.", deprecated = true)
    @RequestBody(content = @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
            @ExampleObject(name = "Register user with new organization", description = "Example of body to register an user" +
                    " and create its organization in same time.",
                    value = REGISTER_USER_NEW_ORGA_EXAMPLE),
            @ExampleObject(name = "Register user with existing organization", description = "Example of body to register an" +
                    " user and join an existing organization.",
                    value = REGISTER_USER_JOIN_ORGA_EXAMPLE)
    }))
    @APIResponse(responseCode = "201", description = "Created - The user was successfully created",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = UserResource.class)))
    @APIResponse(responseCode = "400", description = BAD_REQUEST_DESCRIPTION,
            content = @Content(schema = @Schema(implementation = ErrorResponseBody.class), mediaType = MediaType.APPLICATION_JSON))
    @APIResponse(responseCode = "409", description = "Conflict - The email already exist",
            content = @Content(schema = @Schema(implementation = ErrorResponseBody.class), mediaType = MediaType.APPLICATION_JSON))
    Response registerUserV1(UserRegisterRequest userRegisterRequest);

    /**
     * Create a new user. This endpoint is protected and only accessible by admin users. The new user is activated by
     * default and must join an existing organization.
     *
     * @param userCreationRequest The user resource containing the user information. The organizationId field is mandatory
     *                            and must correspond to an existing organization.
     * @return A Response with status 201 and the created user in body if the creation is successful, or an error response
     * with status 400 if the request is invalid, or 401 if the authentication token is missing or invalid, or 403 if
     * the user does not have the required permissions, or 409 if the email already exist
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @ProtectedResource
    @SecurityRequirement(name = ApplicationConfig.SECURITY_SCHEME)
    @Operation(summary = "Create a new user", description = "Create a new user. The new user is activated by default and" +
            " can join an existing organization or create a new organization", deprecated = true)
    @RequestBody(content = @Content(mediaType = MediaType.APPLICATION_JSON, examples = {
            @ExampleObject(name = "Create a new user and create an orga", value = CREATE_USER_NEW_ORGA_EXAMPLE),
            @ExampleObject(name = "Create a new user and join an orga", value = CREATE_USER_JOIN_ORGA_EXAMPLE)
    }))
    @APIResponse(responseCode = "201", description = "Created - The user was successfully created",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = UserResource.class)))
    @APIResponse(responseCode = "400", description = BAD_REQUEST_DESCRIPTION,
            content = @Content(schema = @Schema(implementation = ErrorResponseBody.class), mediaType = MediaType.APPLICATION_JSON))
    @APIResponse(responseCode = "401", description = UNAUTHORIZED_DESCRIPTION,
            content = @Content(schema = @Schema(implementation = ErrorResponseBody.class), mediaType = MediaType.APPLICATION_JSON))
    @APIResponse(responseCode = "403", description = FORBIDDEN_DESCRIPTION,
            content = @Content(schema = @Schema(implementation = ErrorResponseBody.class), mediaType = MediaType.APPLICATION_JSON))
    @APIResponse(responseCode = "409", description = "Conflict - The email already exist",
            content = @Content(schema = @Schema(implementation = ErrorResponseBody.class), mediaType = MediaType.APPLICATION_JSON))
    Response createUserV1(UserCreationRequest userCreationRequest);

    /**
     * Update the user attributes. This endpoint is protected and only accessible by admin users.
     *
     * @param userId       The id of the user to update
     * @param userResource The user resource containing the updated user information. The organizationId field cannot be
     *                     updated with this endpoint, to change the organization of a user, it must be done through the organization management endpoints.
     * @return A Response with status 200 and the updated user in body if the update is successful
     */
    @PATCH
    @Path("{userId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @ProtectedResource
    @SecurityRequirement(name = ApplicationConfig.SECURITY_SCHEME)
    @Operation(summary = "Update the user attributes", description = "Update the user attributes.", deprecated = true)
    @APIResponse(responseCode = "200", description = "OK - The updated user",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = UserResource.class)))
    @APIResponse(responseCode = "400", description = BAD_REQUEST_DESCRIPTION,
            content = @Content(schema = @Schema(implementation = ErrorResponseBody.class), mediaType = MediaType.APPLICATION_JSON))
    @APIResponse(responseCode = "401", description = UNAUTHORIZED_DESCRIPTION,
            content = @Content(schema = @Schema(implementation = ErrorResponseBody.class), mediaType = MediaType.APPLICATION_JSON))
    @APIResponse(responseCode = "403", description = FORBIDDEN_DESCRIPTION,
            content = @Content(schema = @Schema(implementation = ErrorResponseBody.class), mediaType = MediaType.APPLICATION_JSON))
    @APIResponse(responseCode = "404", description = NOT_FOUND_DESCRIPTION,
            content = @Content(schema = @Schema(implementation = ErrorResponseBody.class), mediaType = MediaType.APPLICATION_JSON))
    @APIResponse(responseCode = "409", description = "Conflict - The email already exist",
            content = @Content(schema = @Schema(implementation = ErrorResponseBody.class), mediaType = MediaType.APPLICATION_JSON))
    Response patchV1(@PathParam("userId") String userId, UserEditionResource userResource);

    /**
     * Search for users corresponding to the search parameters.
     *
     * @param search         A free text search on the user attributes (first name, last name, email) and on the organization name
     * @param firstName      Strict search on the user first name
     * @param lastName       Strict search on the user last name
     * @param email          Strict search on the user email
     * @param organizationId Strict search on the user organization id
     * @param group          Search for users belonging to a specific group
     * @param activated      Filter users based on their activation state
     * @param delegated      Filter users based on their delegation state
     * @param externalId     Strict search on the user external id
     * @param idpId          Strict search on the user identity provider id
     * @param offset         The offset of the search, used for pagination
     * @param limit          The limit of the search, used for pagination
     * @param sortBy         The field to sort the results by. The sorting is ascending by default, to sort in descending order, the sortOrder parameter must be used.
     * @param sortOrder      The sorting order of the results. The sorting is ascending by default, to sort in descending order, this parameter must be set to DESC.
     * @return A Response with status 200 and the list of matching users in body if the search is successful
     * @deprecated Please use the search from {@link net.ihe.gazelle.user.management.quarkus.interlay.controller.user.search.UserSearchController} instead.
     */
    @GET
    @ProtectedResource
    @SecurityRequirement(name = ApplicationConfig.SECURITY_SCHEME)
    @Operation(summary = "Search for GUM users",
            description = "Deprecated, please use the /rest/v2/users/ endpoint." +
                    "Search for all the users corresponding to the search parameters", deprecated = true)
    @APIResponse(responseCode = "200", description = "OK - List of matching users",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = UserResource[].class)))
    @APIResponse(responseCode = "400", description = "Bad request",
            content = @Content(schema = @Schema(implementation = ErrorResponseBody.class), mediaType = MediaType.APPLICATION_JSON))
    @APIResponse(responseCode = "401", description = UNAUTHORIZED_DESCRIPTION,
            content = @Content(schema = @Schema(implementation = ErrorResponseBody.class), mediaType = MediaType.APPLICATION_JSON))
    @APIResponse(responseCode = "403", description = FORBIDDEN_DESCRIPTION,
            content = @Content(schema = @Schema(implementation = ErrorResponseBody.class), mediaType = MediaType.APPLICATION_JSON))
    @Parameter(name = "search", description = "Search pattern", example = "kereval.com")
    @Parameter(name = "activated", description = "The activation state of the user")
    @Parameter(name = "delegated", description = "The delegation state of the user")
    @Parameter(name = "externalId", description = "The external identifier of the user")
    @Parameter(name = "idpId", description = "The identity provider identifier of the user")
    @Parameter(name = "offset", description = "The offset of the search")
    @Parameter(name = "limit", description = "The limit of the search")
    @Parameter(name = "sortBy", description = "The sorting field")
    @Parameter(name = "sortOrder", description = "The sorting order")
    @Parameter(name = "organizationId", description = "Strict search user on it organization Id")
    @Parameter(name = "group", description = "Search users based on group")
    @Parameter(name = "firstName", description = "Strict search user on it first name")
    @Parameter(name = "lastName", description = "Strict search user on it last name")
    @Parameter(name = "email", description = "Strict search user on it email")
    @Deprecated(since = "5.0.0")
    Response searchAndFilter(
            @QueryParam("search") String search,
            @QueryParam("firstName") String firstName,
            @QueryParam("lastName") String lastName,
            @QueryParam("email") String email,
            @QueryParam("organizationId") String organizationId,
            @QueryParam("group") String group,
            @QueryParam("activated") Boolean activated,
            @QueryParam("delegated") Boolean delegated,
            @QueryParam("externalId") String externalId,
            @QueryParam("idpId") String idpId,
            @QueryParam("offset") Integer offset,
            @QueryParam("limit") Integer limit,
            @QueryParam("sortBy") String sortBy,
            @QueryParam("sortOrder") SortOrder sortOrder
    );

    /**
     * Search for users corresponding to the search parameters and return a limited set of information for each user.
     *
     * @param search         A free text search on the user attributes (first name, last name, email) and on the organization name
     * @param firstName      Strict search on the user first name
     * @param lastName       Strict search on the user last name
     * @param email          Strict search on the user email
     * @param organizationId Strict search on the user organization id
     * @param group          Search for users belonging to a specific group
     * @param activated      Filter users based on their activation state
     * @param delegated      Filter users based on their delegation state
     * @param externalId     Strict search on the user external id
     * @param idpId          Strict search on the user identity provider id
     * @param offset         The offset of the search, used for pagination
     * @param limit          The limit of the search, used for pagination
     * @param sortBy         The field to sort the results by. The sorting is ascending by default, to sort in descending order, the sortOrder parameter must be used.
     * @param sortOrder      The sorting order of the results. The sorting is ascending by default, to sort in descending order, this parameter must be set to DESC.
     * @return A Response with status 200 and the list of matching users summary in body if the search is successful
     */
    @GET
    @Path("summary")
    @ProtectedResource
    @SecurityRequirement(name = ApplicationConfig.SECURITY_SCHEME)
    @Operation(summary = "Search for GUM users summary",
            description = "Search for all the users corresponding to the search parameters. Returns only a limited set of information",
            deprecated = true)
    @APIResponse(responseCode = "200", description = "OK - List of matching users",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = UserSummaryResource[].class)))
    @APIResponse(responseCode = "400", description = "Bad request",
            content = @Content(schema = @Schema(implementation = ErrorResponseBody.class), mediaType = MediaType.APPLICATION_JSON))
    @APIResponse(responseCode = "401", description = UNAUTHORIZED_DESCRIPTION,
            content = @Content(schema = @Schema(implementation = ErrorResponseBody.class), mediaType = MediaType.APPLICATION_JSON))
    @Parameter(name = "search", description = "Search pattern", example = "kereval.com")
    @Parameter(name = "activated", description = "The activation state of the user")
    @Parameter(name = "delegated", description = "The delegation state of the user")
    @Parameter(name = "externalId", description = "The external identifier of the user")
    @Parameter(name = "idpId", description = "The identity provider identifier of the user")
    @Parameter(name = "offset", description = "The offset of the search")
    @Parameter(name = "limit", description = "The limit of the search")
    @Parameter(name = "sortBy", description = "The sorting field")
    @Parameter(name = "sortOrder", description = "The sorting order")
    @Parameter(name = "organizationId", description = "Strict search user on it organization Id")
    @Parameter(name = "group", description = "Search users based on group")
    @Parameter(name = "firstName", description = "Strict search user on it first name")
    @Parameter(name = "lastName", description = "Strict search user on it last name")
    @Parameter(name = "email", description = "Strict search user on it email")
    Response searchAndFilterSummaryV1(
            @QueryParam("search") String search,
            @QueryParam("firstName") String firstName,
            @QueryParam("lastName") String lastName,
            @QueryParam("email") String email,
            @QueryParam("organizationId") String organizationId,
            @QueryParam("group") String group,
            @QueryParam("activated") Boolean activated,
            @QueryParam("delegated") Boolean delegated,
            @QueryParam("externalId") String externalId,
            @QueryParam("idpId") String idpId,
            @QueryParam("offset") Integer offset,
            @QueryParam("limit") Integer limit,
            @QueryParam("sortBy") String sortBy,
            @QueryParam("sortOrder") SortOrder sortOrder
    );

    /**
     * Count the number of users corresponding to the search parameters. This endpoint is useful to avoid doing a search
     * with a limit set to 1 just to get the total count of users corresponding to the search parameters.
     *
     * @param propertyName   The name of the property to count. This property must be one of the following:
     *                       "search", "firstName", "lastName", "email", "organizationId", "group", "activated", "delegated", "externalId" or "idpId".
     *                       The count will be done on the users matching the search parameters and for which the property is not null.
     * @param search         A free text search on the user attributes (first name, last name, email) and on the organization name
     * @param firstName      Strict search on the user first name
     * @param lastName       Strict search on the user last name
     * @param organizationId Strict search on the user organization id
     * @param group          Search for users belonging to a specific group
     * @param activated      Filter users based on their activation state
     * @param delegated      Filter users based on their delegation state
     * @return A Response with status 200 and the number of users corresponding to the search parameters and for which the property is not null in body if the count is successful
     * @deprecated Please use the search from {@link net.ihe.gazelle.user.management.quarkus.interlay.controller.user.search.UserSearchController} instead as it returns the count in the headers.
     */
    @GET
    @Path("{propertyName}/count")
    @ProtectedResource
    @SecurityRequirement(name = ApplicationConfig.SECURITY_SCHEME)
    @Operation(summary = "Count the number of user based on a propertyName",
            description = "Deprecated, please use the /rest/v2/users/ endpoint.\n" +
                    "Count the number of user based on a propertyName.", deprecated = true)
    @APIResponse(responseCode = "200", description = "OK - The number of users corresponding to the search")
    @Deprecated(since = "5.0.0")
    Response getValueCount(
            @PathParam("propertyName") String propertyName,
            @QueryParam("search") String search,
            @QueryParam("firstName") String firstName,
            @QueryParam("lastName") String lastName,
            @QueryParam("organizationId") String organizationId,
            @QueryParam("group") String group,
            @QueryParam("activated") Boolean activated,
            @QueryParam("delegated") Boolean delegated
    );


    /**
     * Get an user by its id.
     *
     * @param userId The id of the user to retrieve
     * @return A Response with status 200 and the corresponding user in body if the user is found
     */
    @GET
    @Path("{userId}")
    @ProtectedResource
    @SecurityRequirement(name = ApplicationConfig.SECURITY_SCHEME)
    @Operation(summary = "Retrieve an user by its id", description = "Retrieve an user by its id.", deprecated = true)
    @APIResponse(responseCode = "200", description = "OK - The corresponding user",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = UserResource.class)))
    @APIResponse(responseCode = "401", description = UNAUTHORIZED_DESCRIPTION,
            content = @Content(schema = @Schema(implementation = ErrorResponseBody.class), mediaType = MediaType.APPLICATION_JSON))
    @APIResponse(responseCode = "403", description = FORBIDDEN_DESCRIPTION,
            content = @Content(schema = @Schema(implementation = ErrorResponseBody.class), mediaType = MediaType.APPLICATION_JSON))
    @APIResponse(responseCode = "404", description = NOT_FOUND_DESCRIPTION,
            content = @Content(schema = @Schema(implementation = ErrorResponseBody.class), mediaType = MediaType.APPLICATION_JSON))
    Response getUserByIdV1(@PathParam("userId") String userId);

    /**
     * Get an user summary by its id. The user summary contains a limited set of information about the user, and does not contain any sensitive information.
     *
     * @param userId The id of the user to retrieve
     * @return A Response with status 200 and the corresponding user summary in body if the user is found
     */
    @GET
    @Path("{userId}/summary")
    @ProtectedResource
    @SecurityRequirement(name = ApplicationConfig.SECURITY_SCHEME)
    @Operation(summary = "Retrieve an user summary by its id", description = "Retrieve a limited set of user information by its id.",
            deprecated = true)
    @APIResponse(responseCode = "200", description = "OK - The corresponding user summary",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = UserSummaryResource.class)))
    @APIResponse(responseCode = "401", description = UNAUTHORIZED_DESCRIPTION,
            content = @Content(schema = @Schema(implementation = ErrorResponseBody.class), mediaType = MediaType.APPLICATION_JSON))
    @APIResponse(responseCode = "404", description = NOT_FOUND_DESCRIPTION,
            content = @Content(schema = @Schema(implementation = ErrorResponseBody.class), mediaType = MediaType.APPLICATION_JSON))
    Response getUserSummaryByIdV1(@PathParam("userId") String userId);

    /**
     * Activate a user from an activation code. This endpoint is public and does not require authentication
     *
     * @param activationCode The activation code required to activate the user.
     * @return A Response with status 200 if the activation is successful
     */
    @POST
    @Path("activate/{activationCode}")
    @Operation(summary = "Activate a user from an activation code", description = "Activate a user from an activation code.",
            deprecated = true)
    @APIResponse(responseCode = "200", description = "OK - Successfully activated",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = UserResource.class)))
    @APIResponse(responseCode = "404", description = "Not found - No user corresponding to the activation code",
            content = @Content(schema = @Schema(implementation = ErrorResponseBody.class), mediaType = MediaType.APPLICATION_JSON))
    Response activateFromActivationCodeV1(@PathParam("activationCode") String activationCode);

    /**
     * Activate a user from its id.
     *
     * @param activationResource The activation resource containing the id of the user to activate
     * @return A Response with status 200 if the activation is successful
     */
    @POST
    @Path("activate")
    @Consumes(MediaType.APPLICATION_JSON)
    @ProtectedResource
    @SecurityRequirement(name = ApplicationConfig.SECURITY_SCHEME)
    @Operation(summary = "Activate a user from its id", description = "Activate a user from its id.", deprecated = true)
    @APIResponse(responseCode = "200", description = "OK - Successfully activated", content = @Content)
    @APIResponse(responseCode = "401", description = UNAUTHORIZED_DESCRIPTION,
            content = @Content(schema = @Schema(implementation = ErrorResponseBody.class), mediaType = MediaType.APPLICATION_JSON))
    @APIResponse(responseCode = "403", description = FORBIDDEN_DESCRIPTION,
            content = @Content(schema = @Schema(implementation = ErrorResponseBody.class), mediaType = MediaType.APPLICATION_JSON))
    @APIResponse(responseCode = "404", description = NOT_FOUND_DESCRIPTION,
            content = @Content(schema = @Schema(implementation = ErrorResponseBody.class), mediaType = MediaType.APPLICATION_JSON))
    Response activateV1(ActivationResource activationResource);

    /**
     * Deactivate a user from its id. This endpoint is protected and only accessible by admin users.
     *
     * @param activationResource The activation resource containing the id of the user to deactivate
     * @return A Response with status 200 if the deactivation is successful
     */
    @POST
    @Path("deactivate")
    @Consumes(MediaType.APPLICATION_JSON)
    @ProtectedResource
    @SecurityRequirement(name = ApplicationConfig.SECURITY_SCHEME)
    @Operation(summary = "Deactivate a user from its id", description = "Deactivate a user from its id.", deprecated = true)
    @APIResponse(responseCode = "200", description = "OK - Successfully deactivated", content = @Content)
    @APIResponse(responseCode = "401", description = UNAUTHORIZED_DESCRIPTION,
            content = @Content(schema = @Schema(implementation = ErrorResponseBody.class), mediaType = MediaType.APPLICATION_JSON))
    @APIResponse(responseCode = "403", description = FORBIDDEN_DESCRIPTION,
            content = @Content(schema = @Schema(implementation = ErrorResponseBody.class), mediaType = MediaType.APPLICATION_JSON))
    @APIResponse(responseCode = "404", description = NOT_FOUND_DESCRIPTION,
            content = @Content(schema = @Schema(implementation = ErrorResponseBody.class), mediaType = MediaType.APPLICATION_JSON))
    Response deactivateV1(ActivationResource activationResource);

    /**
     * Delete a user from its id. This endpoint is protected and only accessible by admin users. The deletion is irreversible.
     *
     * @param userId The id of the user to delete
     * @return A Response with status 200 if the deletion is successful
     */
    @DELETE
    @Path("{userId}")
    @ProtectedResource
    @SecurityRequirement(name = ApplicationConfig.SECURITY_SCHEME)
    @Operation(summary = "Delete the user associated to the given id",
            description = "Delete the user associated to the given id.", deprecated = true)
    @APIResponse(responseCode = "200", description = "OK - Successfully deleted", content = @Content)
    @APIResponse(responseCode = "401", description = UNAUTHORIZED_DESCRIPTION,
            content = @Content(schema = @Schema(implementation = ErrorResponseBody.class), mediaType = MediaType.APPLICATION_JSON))
    @APIResponse(responseCode = "403", description = FORBIDDEN_DESCRIPTION,
            content = @Content(schema = @Schema(implementation = ErrorResponseBody.class), mediaType = MediaType.APPLICATION_JSON))
    @APIResponse(responseCode = "404", description = NOT_FOUND_DESCRIPTION,
            content = @Content(schema = @Schema(implementation = ErrorResponseBody.class), mediaType = MediaType.APPLICATION_JSON))
    @Parameter(name = "userId", description = "User id", example = "9ec7105a-be72-4a37-955d-b5a665d69da4")
    Response deleteUserV1(@PathParam("userId") String userId);

    /**
     * Get the user preferences from the user id. This endpoint is protected and only accessible by the user itself or by admin users.
     *
     * @param userId The id of the user to retrieve the preferences from
     * @return A Response with status 200 and the user preferences in body if the user is found
     */
    @GET
    @Path("{userId}/preferences")
    @Tag(name = "User preferences management V1", description = "Warning deprecated, User preferences API.")
    @SecurityRequirement(name = ApplicationConfig.SECURITY_SCHEME)
    @ProtectedResource
    @Operation(summary = "Get user preferences by user id",
            description = "Returns all preferences as per the user id", deprecated = true)
    @APIResponse(responseCode = "200", description = "OK - Successfully retrieved",
            content = @Content(schema = @Schema(implementation = UserPreference.class), mediaType = MediaType.APPLICATION_JSON))
    @APIResponse(responseCode = "401", description = UNAUTHORIZED_DESCRIPTION,
            content = @Content(schema = @Schema(implementation = ErrorResponseBody.class), mediaType = MediaType.APPLICATION_JSON))
    @APIResponse(responseCode = "403", description = FORBIDDEN_DESCRIPTION,
            content = @Content(schema = @Schema(implementation = ErrorResponseBody.class), mediaType = MediaType.APPLICATION_JSON))
    @APIResponse(responseCode = "404", description = NOT_FOUND_DESCRIPTION,
            content = @Content(schema = @Schema(implementation = ErrorResponseBody.class), mediaType = MediaType.APPLICATION_JSON))
    @Parameter(name = "userId", description = "User id", example = "9ec7105a-be72-4a37-955d-b5a665d69da4")
    Response getUserPreferencesV1(@PathParam("userId") String userId);

    /**
     * Get a single user preference from the user id and the preference name. This endpoint is protected and only accessible by the user itself or by admin users.
     *
     * @param userId       The id of the user to retrieve the preference from
     * @param preferenceId The name of the preference to retrieve
     * @return A Response with status 200 and the user preference in body if the user and the preference are found
     */
    @GET
    @Path("{userId}/preferences/{preferenceId}")
    @ProtectedResource
    @Tag(name = "User preferences management V1", description = "Warning deprecated, User preferences API.")
    @SecurityRequirement(name = ApplicationConfig.SECURITY_SCHEME)
    @Operation(summary = "Get a single user preferences by user id and the preference name",
            description = "Returns the wanted preference as per the user id", deprecated = true)
    @APIResponse(responseCode = "200", description = "OK - Successfully retrieved")
    @APIResponse(responseCode = "401", description = UNAUTHORIZED_DESCRIPTION,
            content = @Content(schema = @Schema(implementation = ErrorResponseBody.class), mediaType = MediaType.APPLICATION_JSON))
    @APIResponse(responseCode = "403", description = FORBIDDEN_DESCRIPTION,
            content = @Content(schema = @Schema(implementation = ErrorResponseBody.class), mediaType = MediaType.APPLICATION_JSON))
    @APIResponse(responseCode = "404", description = "Not found - The user or the preference was not found",
            content = @Content(schema = @Schema(implementation = ErrorResponseBody.class),
                    mediaType = MediaType.APPLICATION_JSON))
    @Parameter(name = "userId", description = "User id", example = "9ec7105a-be72-4a37-955d-b5a665d69da4")
    @Parameter(name = "preferenceId", description = "The name of the preference wanted", example = "languagesSpoken")
    Response getSingleUserPreferenceV1(@PathParam("userId") String userId, @PathParam("preferenceId") String preferenceId);

    /**
     * Update the user preferences from the user id. This endpoint is protected and only accessible by the user itself or by admin users.
     *
     * @param userId                 The id of the user to update the preferences from
     * @param userPreferenceResource The user preference resource containing the preferences to update. The preference
     *                               name must be specified in the name field of each preference, and the preference value
     *                               must be specified in the value field.
     * @return A Response with status 200 and the updated user preferences in body if the user is found and the preferences are successfully updated
     */
    @PUT
    @Path("{userId}/preferences")
    @Consumes(MediaType.APPLICATION_JSON)
    @ProtectedResource
    @Tag(name = "User preferences management V1", description = "Warning deprecated, User preferences API.")
    @SecurityRequirement(name = ApplicationConfig.SECURITY_SCHEME)
    @Operation(summary = "Update the preferences of user",
            description = "Returns the updated user preferences", deprecated = true)
    @APIResponse(responseCode = "200", description = "OK - Successfully updated",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = UserPreference.class)))
    @APIResponse(responseCode = "401", description = UNAUTHORIZED_DESCRIPTION,
            content = @Content(schema = @Schema(implementation = ErrorResponseBody.class), mediaType = MediaType.APPLICATION_JSON))
    @APIResponse(responseCode = "403", description = FORBIDDEN_DESCRIPTION,
            content = @Content(schema = @Schema(implementation = ErrorResponseBody.class), mediaType = MediaType.APPLICATION_JSON))
    @APIResponse(responseCode = "404", description = "Not found - The user or the preference was not found")
    @Parameter(name = "userId", description = "User id", example = "9ec7105a-be72-4a37-955d-b5a665d69da4")
    @RequestBody(description = "User preference(s) to update",
            content = @Content(schema = @Schema(implementation = UserPreferenceResource.class)))
    Response updateUserPreferenceV1(@PathParam("userId") String userId, UserPreferenceResource userPreferenceResource);

    /**
     * Get the user profile picture from the user id. This endpoint is protected and only accessible by the user itself or by admin users.
     *
     * @param userId The id of the user to retrieve the profile picture from
     * @param format The format of the profile picture to retrieve. The possible values are "normal" and "thumbnail".
     *               If the format is not specified, the normal profile picture will be returned by default.
     * @return A Response with status 200 and the user profile picture in body if the user is found
     */
    @GET
    @Path("{userId}/preferences/picture")
    @Produces("image/jpeg")
    @ProtectedResource
    @Tag(name = "User preferences management V1", description = "Warning deprecated, User preferences API.")
    @SecurityRequirement(name = ApplicationConfig.SECURITY_SCHEME)
    @Operation(summary = "Get the profile picture by user id",
            description = "Returns the profile picture as per the user id", deprecated = true)
    @APIResponse(responseCode = "200", description = "OK - Successfully retrieved")
    @APIResponse(responseCode = "400", description = "Bad request - Format is not supported",
            content = @Content(schema = @Schema(implementation = ErrorResponseBody.class), mediaType = MediaType.APPLICATION_JSON))
    @APIResponse(responseCode = "401", description = UNAUTHORIZED_DESCRIPTION,
            content = @Content(schema = @Schema(implementation = ErrorResponseBody.class), mediaType = MediaType.APPLICATION_JSON))
    @APIResponse(responseCode = "403", description = FORBIDDEN_DESCRIPTION,
            content = @Content(schema = @Schema(implementation = ErrorResponseBody.class), mediaType = MediaType.APPLICATION_JSON))
    @APIResponse(responseCode = "404", description = "Not found - The user was not found ",
            content = @Content(schema = @Schema(implementation = ErrorResponseBody.class), mediaType = MediaType.APPLICATION_JSON))
    @Parameter(name = "userId", description = "User id", example = "9ec7105a-be72-4a37-955d-b5a665d69da4")
    @Parameter(name = "format", description = "Query param to select ",
            examples = {@ExampleObject(name = "normal", value = "normal"),
                    @ExampleObject(name = "thumbnail", value = "thumbnail")},
            style = ParameterStyle.FORM, required = true)
    Response getProfilePictureV1(@PathParam("userId") String userId, @QueryParam("format") String format);

    /**
     * Update the user profile picture from the user id. This endpoint is protected and only accessible by the user itself or by admin users.
     *
     * @param userId         The id of the user to update the profile picture from
     * @param profilePicture The new profile picture in byte array format. The profile picture must be in jpeg format and must not exceed 5MB.
     * @return A Response with status 200 and the updated user profile picture in body if the user is found and the profile picture is successfully updated
     */
    @PUT
    @Path("{userId}/preferences/picture")
    @Consumes("image/jpeg")
    @Produces("image/jpeg")
    @ProtectedResource
    @Tag(name = "User preferences management V1", description = "Warning deprecated, User preferences API.")
    @SecurityRequirement(name = ApplicationConfig.SECURITY_SCHEME)
    @Operation(summary = "Update the profile picture by user id",
            description = "Update the profile picture as per the user id", deprecated = true)
    @APIResponse(responseCode = "200", description = "OK - Successfully retrieved", content = @Content(mediaType = "image/jpeg"))
    @APIResponse(responseCode = "401", description = UNAUTHORIZED_DESCRIPTION,
            content = @Content(schema = @Schema(implementation = ErrorResponseBody.class), mediaType = MediaType.APPLICATION_JSON))
    @APIResponse(responseCode = "403", description = FORBIDDEN_DESCRIPTION,
            content = @Content(schema = @Schema(implementation = ErrorResponseBody.class), mediaType = MediaType.APPLICATION_JSON))
    @APIResponse(responseCode = "404", description = NOT_FOUND_DESCRIPTION,
            content = @Content(schema = @Schema(implementation = ErrorResponseBody.class), mediaType = MediaType.APPLICATION_JSON))
    @Parameter(name = "userId", description = "User id", example = "9ec7105a-be72-4a37-955d-b5a665d69da4")
    Response updateProfilePictureV1(@PathParam("userId") String userId, byte[] profilePicture);

    /**
     * Delete the user profile picture from the user id, and revert to the default profile picture.
     *
     * @param userId The id of the user to delete the profile picture from
     * @return A Response with status 200 and the default user profile picture in body if the user is found and the profile picture is successfully deleted
     */
    @DELETE
    @Path("{userId}/preferences/picture")
    @Produces("image/jpeg")
    @ProtectedResource
    @Tag(name = "User preferences management V1", description = "Warning deprecated, User preferences API.")
    @SecurityRequirement(name = ApplicationConfig.SECURITY_SCHEME)
    @Operation(summary = "Reset the profile picture by user id to the default one",
            description = "Reset the profile picture as per the user id to the default one", deprecated = true)
    @APIResponse(responseCode = "200", description = "OK - Successfully reverted picture to default one", content = @Content)
    @APIResponse(responseCode = "401", description = UNAUTHORIZED_DESCRIPTION,
            content = @Content(schema = @Schema(implementation = ErrorResponseBody.class), mediaType = MediaType.APPLICATION_JSON))
    @APIResponse(responseCode = "403", description = FORBIDDEN_DESCRIPTION,
            content = @Content(schema = @Schema(implementation = ErrorResponseBody.class), mediaType = MediaType.APPLICATION_JSON))
    @APIResponse(responseCode = "404", description = NOT_FOUND_DESCRIPTION,
            content = @Content(schema = @Schema(implementation = ErrorResponseBody.class), mediaType = MediaType.APPLICATION_JSON))
    @Parameter(name = "userId", description = "User id", example = "9ec7105a-be72-4a37-955d-b5a665d69da4")
    Response deleteUserProfilePictureV1(@PathParam("userId") String userId);

    /**
     * Join a group from the user id and the group id. This endpoint is protected and only accessible by the user itself or by admin users.
     *
     * @param userId          The id of the user to join the group
     * @param groupIdResource The group id resource containing the id of the group to join
     * @return A Response with status 200 if the user successfully joined the group
     */
    @POST
    @Path("{userId}/groups/join")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ProtectedResource
    @SecurityRequirement(name = ApplicationConfig.SECURITY_SCHEME)
    @Operation(summary = "Join the group in the request body",
            description = "Join the group in the request body.", deprecated = true)
    @APIResponse(responseCode = "200", description = "OK - Successfully joined", content = @Content)
    @APIResponse(responseCode = "401", description = UNAUTHORIZED_DESCRIPTION,
            content = @Content(schema = @Schema(implementation = ErrorResponseBody.class), mediaType = MediaType.APPLICATION_JSON))
    @APIResponse(responseCode = "403", description = FORBIDDEN_DESCRIPTION,
            content = @Content(schema = @Schema(implementation = ErrorResponseBody.class), mediaType = MediaType.APPLICATION_JSON))
    @APIResponse(responseCode = "404", description = "Not found - The user or the group was not found",
            content = @Content(schema = @Schema(implementation = ErrorResponseBody.class), mediaType = MediaType.APPLICATION_JSON))
    @Parameter(name = "userId", description = "User id", example = "9ec7105a-be72-4a37-955d-b5a665d69da4")
    Response joinGroupV1(@PathParam("userId") String userId, GroupIdResource groupIdResource);

    /**
     * Leave a group from the user id and the group id. This endpoint is protected and only accessible by the user itself or by admin users.
     *
     * @param userId          The id of the user to leave the group
     * @param groupIdResource The group id resource containing the id of the group to leave
     * @return A Response with status 200 if the user successfully left the group
     */
    @POST
    @Path("{userId}/groups/leave")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ProtectedResource
    @SecurityRequirement(name = ApplicationConfig.SECURITY_SCHEME)
    @Operation(summary = "Leave the group in the request body",
            description = "Leave the group in the request body.", deprecated = true)
    @APIResponse(responseCode = "200", description = "OK - Successfully left", content = @Content)
    @APIResponse(responseCode = "401", description = UNAUTHORIZED_DESCRIPTION,
            content = @Content(schema = @Schema(implementation = ErrorResponseBody.class), mediaType = MediaType.APPLICATION_JSON))
    @APIResponse(responseCode = "403", description = FORBIDDEN_DESCRIPTION,
            content = @Content(schema = @Schema(implementation = ErrorResponseBody.class), mediaType = MediaType.APPLICATION_JSON))
    @APIResponse(responseCode = "404", description = "Not found - The user or the group was not found",
            content = @Content(schema = @Schema(implementation = ErrorResponseBody.class), mediaType = MediaType.APPLICATION_JSON))
    @Parameter(name = "userId", description = "User id", example = "9ec7105a-be72-4a37-955d-b5a665d69da4")
    Response leaveGroupV1(@PathParam("userId") String userId, GroupIdResource groupIdResource);


}
