package net.ihe.gazelle.validation.gateway.quarkus.ws;

import net.ihe.gazelle.oidc.rest.business.ProtectedResource;
import net.ihe.gazelle.search.api.PresentationException;
import net.ihe.gazelle.search.api.ReadException;
import net.ihe.gazelle.search.api.ReadService;
import net.ihe.gazelle.search.jaxrs.api.AbstractSearchServiceRest;
import net.ihe.gazelle.search.jaxrs.api.SearchServiceRest;
import net.ihe.gazelle.validation.gateway.business.ProfileReadId;
import net.ihe.gazelle.validation.gateway.business.ProfileSearchCriteria;
import net.ihe.gazelle.validation.gateway.business.SearchProfileService;
import net.ihe.gazelle.validation.gateway.business.ValidationProfileWithService;
import net.ihe.gazelle.validation.gateway.quarkus.service.ValidationProfileIndexService;
import net.ihe.gazelle.validation.gateway.technical.ProfilePresenterService;
import net.ihe.gazelle.validation.v2.api.business.profile.ValidationProfile;
import net.ihe.gazelle.validation.v2.api.technical.dto.profile.ValidationProfileDTO;
import net.ihe.gazelle.security.business.GazelleIdentity;
import net.ihe.gazelle.security.business.UnauthorizedException;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.headers.Header;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.ArrayList;
import java.util.List;

import static jakarta.ws.rs.core.Response.Status.BAD_REQUEST;
import static jakarta.ws.rs.core.Response.Status.NOT_FOUND;
import static jakarta.ws.rs.core.Response.Status.OK;

@ApplicationScoped
@Path("/rest/v1/profiles")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Search Validation Profiles")
public class SearchProfileControllerImpl extends AbstractSearchServiceRest<
        ValidationProfileWithService,
        ValidationProfileWithServiceDTO,
        ValidationProfileQueryBeanParam,
        ProfileSearchCriteria>
{
    private static final Logger LOG = LoggerFactory.getLogger(SearchProfileControllerImpl.class);

    private final ReadService<ProfileReadId, ValidationProfile> readProfileService;
    private final ProfilePresenterService profilePresenterService;
    private final GazelleIdentity identity;

    @Inject
    public SearchProfileControllerImpl(
            SearchProfileService searchProfileService,
            ReadService<ProfileReadId, ValidationProfile> readProfileService,
            ValidationProfileIndexService indexService,
            ProfilePresenterService profilePresenterService,
            GazelleIdentity identity) {
        super(
                indexService,
                new ValidationProfileSuggestionService(searchProfileService, indexService),
                new ValidationProfileQueryMapper(indexService),
                searchProfileService,
                identity
        );
        this.readProfileService = Objects.requireNonNull(readProfileService, "readProfileService must not be null");
        this.profilePresenterService = Objects.requireNonNull(profilePresenterService, "profilePresenterService must not be null");
        this.identity = Objects.requireNonNull(identity, "identity must not be null");
    }

    @Override
    @GET
    @Path("/indexes")
    @Operation(summary = "Get indexed fields",
            description = "Returns the list of indexed fields available for validation profile search.")
    @APIResponse(responseCode = "200", description = "List of indexed fields",
            content = @Content(mediaType = MediaType.APPLICATION_JSON))
    public Response getIndexes() {
        return super.getIndexes();
    }

    @Override
    @GET
    @Path("/indexes/{fieldName}/values")
    @Operation(summary = "Get possible values",
            description = "Returns possible values for the requested indexed field.")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "List of possible values",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON)),
            @APIResponse(responseCode = "404", description = "Unknown search parameter"),
            @APIResponse(responseCode = "401", description = "Unauthorized"),
            @APIResponse(responseCode = "403", description = "Forbidden"),
            @APIResponse(responseCode = "500", description = "Unexpected error")
    })
    public Response getPossibleValues(
            String fieldName,
            @BeanParam ValidationProfileQueryBeanParam query) {
        return super.getPossibleValues(fieldName, query);
    }

    @Override
    @Operation(summary = "Search profiles",
            description = "Search validation profiles using indexed query parameters.")
    @SecurityRequirement(name = "Keycloak")
    @ProtectedResource
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Search result (paged)",
                    headers = @Header(name = "Content-Range",
                            description = "profiles [start]-[end]/[total]",
                            schema = @Schema(type = SchemaType.STRING)),
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ValidationProfileWithServiceDTO[].class))),
            @APIResponse(responseCode = "400", description = "Malformed search parameter"),
            @APIResponse(responseCode = "401", description = "Unauthorized"),
            @APIResponse(responseCode = "403", description = "Forbidden"),
            @APIResponse(responseCode = "500", description = "Unexpected error")
    })
    public Response search(
            @BeanParam ValidationProfileQueryBeanParam query,
            @jakarta.ws.rs.QueryParam(SearchServiceRest.PRESENTATION_FIELDS) java.util.List<String> attributePaths) {
        return super.search(query, attributePaths);
    }

    @GET
    @Path("/{serviceName}/{profileId}")
    @Operation(summary = "Read a profile",
            description = "Returns a single validation profile identified by serviceName and profileId.")
    @SecurityRequirement(name = "Keycloak")
    @ProtectedResource
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Profile found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ValidationProfileDTO.class))),
            @APIResponse(responseCode = "400", description = "Missing parameter"),
            @APIResponse(responseCode = "404", description = "Profile not found"),
            @APIResponse(responseCode = "401", description = "Unauthorized"),
            @APIResponse(responseCode = "403", description = "Forbidden"),
            @APIResponse(responseCode = "500", description = "Unexpected error")
    })
    public Response readProfile(
            @PathParam("serviceName") @Parameter(required = true) String serviceName,
            @PathParam("profileId") @Parameter(required = true) String profileId,
            @jakarta.ws.rs.QueryParam(SearchServiceRest.PRESENTATION_FIELDS) java.util.List<String> attributePaths) {
        if (serviceName == null || serviceName.isBlank() || profileId == null || profileId.isBlank()) {
            return Response.status(BAD_REQUEST).entity("Missing parameter serviceName and/or profileId.").build();
        }
        try {
            ValidationProfile profile = readProfileService.readObject(
                    new ProfileReadId(profileId, serviceName),
                    identity
            );
            List<String> normalizedPaths = normalizePresentationPaths(attributePaths);
            ValidationProfile presented = applyPresentation(profile, normalizedPaths);
            return Response.status(OK).entity(new ValidationProfileDTO(presented)).build();
        } catch (UnauthorizedException e) {
            LOG.error("Error while reading profile", e);

            if (!identity.isAuthenticated()) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("Unauthorized access to read profile")
                        .build();
            }

            return Response.status(Response.Status.FORBIDDEN)
                    .entity("Forbidden access to read profile")
                    .build();
        }
        catch (IllegalArgumentException e) {
            return Response.status(BAD_REQUEST).entity(e.getMessage()).build();
        } catch (NoSuchElementException e) {
            return Response.status(NOT_FOUND).entity("Profile not found.").build();
        } catch (ReadException e) {
            return Response.serverError().entity("Unexpected error").build();
        } catch (PresentationException e) {
            return Response.status(BAD_REQUEST)
                  .entity(SearchServiceRest.INVALID_PRESENTATION_PARAMETERS)
                  .build();
        }
    }

    @Override
    protected String getContentRangeType() {
        return "profiles";
    }

    @Override
    protected ValidationProfileWithServiceDTO mapToDTO(ValidationProfileWithService object) {
        return ValidationProfileWithServiceDTO.from(object);
    }

    private ValidationProfile applyPresentation(ValidationProfile profile, List<String> attributePaths) {
        if (profile == null || attributePaths == null || attributePaths.isEmpty()) {
            return profile;
        }
        return profilePresenterService.getPresentedObject(profile, attributePaths);
    }

    private List<String> normalizePresentationPaths(List<String> attributePaths) {
        if (attributePaths == null || attributePaths.isEmpty()) {
            return List.of();
        }
        List<String> normalized = new ArrayList<>();
        for (String path : attributePaths) {
            if (path == null || path.isBlank()) {
                continue;
            }
            String trimmed = path.trim();
            if (trimmed.startsWith("profile.")) {
                trimmed = trimmed.substring("profile.".length());
            }
            if ("validationService".equalsIgnoreCase(trimmed) || "profile".equalsIgnoreCase(trimmed)) {
                continue;
            }
            if (!trimmed.isEmpty()) {
                normalized.add(trimmed);
            }
        }
        return normalized;
    }
}
