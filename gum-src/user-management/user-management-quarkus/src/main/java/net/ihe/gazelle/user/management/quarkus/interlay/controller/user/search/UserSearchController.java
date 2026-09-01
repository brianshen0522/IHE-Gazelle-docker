package net.ihe.gazelle.user.management.quarkus.interlay.controller.user.search;

import jakarta.inject.Inject;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import net.ihe.gazelle.oidc.rest.business.ProtectedResource;
import net.ihe.gazelle.search.jaxrs.api.AbstractSearchServiceRest;
import net.ihe.gazelle.security.business.GazelleIdentity;
import net.ihe.gazelle.user.management.api.application.user.search.UserSearchCriteria;
import net.ihe.gazelle.user.management.api.application.user.search.UserSearchIndexServiceImpl;
import net.ihe.gazelle.user.management.api.application.user.search.UserSearchService;
import net.ihe.gazelle.user.management.api.application.user.search.UserSuggestionService;
import net.ihe.gazelle.user.management.api.domain.user.User;
import net.ihe.gazelle.user.management.api.interlay.user.UserResource;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.headers.Header;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.NoSuchElementException;

/**
 * REST controller for user search.
 */
@Path("/rest/v2/users")
@Tag(name = "User management", description = "User management API.")
public class UserSearchController extends AbstractSearchServiceRest<User, UserResource, UserQueryBeanParam, UserSearchCriteria> {


    @Inject
    public UserSearchController(UserSearchIndexServiceImpl indexService,
                                UserSuggestionService testCaseSuggestionService,
                                UserSearchService testCaseSearchService,
                                GazelleIdentity identity) {
        super(indexService, testCaseSuggestionService, new UserQueryMapper(indexService), testCaseSearchService, identity);
    }

    @Override
    @GET
    @Path(POSSIBLE_VALUES_PATH)
    @ProtectedResource
    @SecurityRequirement(name = "Keycloak")
    public Response getPossibleValues(@PathParam(POSSIBLE_VALUES_FIELD) String field, @BeanParam UserQueryBeanParam searchParamBean) {
        try {
            return super.getPossibleValues(field, searchParamBean);
        } catch (NoSuchElementException _) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @Override
    @GET
    @ProtectedResource
    @SecurityRequirement(name = "Keycloak")
    @APIResponse(
            responseCode = "200",
            description = "The List of users matching the search. The result is by default sorted by last name in alphabetical order.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = UserResource[].class)
            ),
            headers = {
                    @Header(
                            name = "Content-Range",
                            description = "Information about the result sub-set returned, its start and finish indexes " +
                                    "over the number total of matches.",
                            schema = @Schema(type = SchemaType.STRING, examples = "Users 0-24/1420")
                    )
            }
    )
    public Response search(@BeanParam UserQueryBeanParam queryBeanParam) {
        return super.search(queryBeanParam);
    }

    @Override
    @GET
    @Path(GET_INDEXES_PATH)
    public Response getIndexes() {
        return super.getIndexes();
    }

    @Override
    protected String getContentRangeType() {
        return "User";
    }

    @Override
    protected UserResource mapToDTO(User object) {
        return new UserResource(object);
    }
}
