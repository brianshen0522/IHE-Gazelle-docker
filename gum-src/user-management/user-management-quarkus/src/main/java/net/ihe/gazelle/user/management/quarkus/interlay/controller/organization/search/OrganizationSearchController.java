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

package net.ihe.gazelle.user.management.quarkus.interlay.controller.organization.search;

import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import net.ihe.gazelle.oidc.rest.business.ProtectedResource;
import net.ihe.gazelle.user.management.api.interlay.organization.OrganizationDto;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.headers.Header;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import static net.ihe.gazelle.search.jaxrs.api.SearchServiceRest.*;

/**
 * REST controller interface for organization searches.
 */
public interface OrganizationSearchController {

    /**
     * Open API interface name
     */
    String INTERFACE_NAME = "Organization management";
    /**
     * Security requirement for endpoint
     */
    String SECURITY_REQUIREMENT_KEYCLOAK = "Keycloak";

    /**
     * Endpoint to retrieve the list of available indexes for organizations, which can be used for search and filtering purposes.
     *
     * @return a Response containing the list of available indexes for organizations, or an appropriate error response if access is denied
     */
    @GET
    @Path(GET_INDEXES_PATH)
    @Tag(name = INTERFACE_NAME)
    @SecurityRequirement(name = SECURITY_REQUIREMENT_KEYCLOAK)
    @ProtectedResource
    Response getIndexes();

    /**
     * Endpoint to retrieve the possible values for a specific index field of organizations, which can be used for search and filtering purposes.
     *
     * @param field           the name of the index field for which to retrieve possible values
     * @param searchParamBean the query parameters for searching possible values, allowing filtering based on other criteria
     * @return a Response containing the list of possible values for the specified index field, or an appropriate error response if the field is invalid or access is denied
     */
    @GET
    @Path(POSSIBLE_VALUES_PATH)
    @Tag(name = INTERFACE_NAME)
    @SecurityRequirement(name = SECURITY_REQUIREMENT_KEYCLOAK)
    @ProtectedResource
    Response getPossibleValues(
            @PathParam(POSSIBLE_VALUES_FIELD) String field,
            @BeanParam OrganizationQueryBeanParam searchParamBean
    );

    /**
     * Endpoint to search for organizations based on the provided query parameters, allowing filtering and pagination of results.
     *
     * @param queryBeanParam the query parameters for searching organizations, including criteria such as shortname and name, as well as pagination parameters
     * @return a Response containing a paginated list of organizations matching the search criteria, along with information about the total number of matches and the range of results returned, or an appropriate error response if access is denied
     */
    @GET
    @Path("/")
    @Tag(name = INTERFACE_NAME)
    @Operation(
            summary = "Search for organizations",
            description = "Search for organizations based on the provided query parameters. The query parameters allow filtering the organizations based on various criteria such as shortname and name. " +
                    "The result is paginated and includes information about the total number of matches and the range of results returned."
    )
    @APIResponse(
            responseCode = "200",
            description = "The List of organizations. The result is by default sorted by name in alphabetical order.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = OrganizationDto[].class)
            ),
            headers = {
                    @Header(
                            name = "Content-Range",
                            description = "Information about the result sub-set returned, its start and finish indexes " +
                                    "over the number total of matches.",
                            schema = @Schema(type = SchemaType.STRING, examples = "Organizations 0-24/1420")
                    )
            }
    )
    @SecurityRequirement(name = SECURITY_REQUIREMENT_KEYCLOAK)
    @Tag(name = INTERFACE_NAME)
    @ProtectedResource
    Response search(@BeanParam OrganizationQueryBeanParam queryBeanParam);

}
