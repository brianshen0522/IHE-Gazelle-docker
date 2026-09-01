package net.ihe.gazelle.validation.gateway.evs.technical.ws;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.kereval.gazelle.datahouse.api.business.record.Item;
import com.kereval.gazelle.datahouse.api.business.search.ItemNotFoundException;
import jakarta.activation.MimeType;
import jakarta.activation.MimeTypeParseException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import net.ihe.gazelle.oidc.rest.business.ProtectedResource;
import net.ihe.gazelle.security.business.GazelleIdentity;
import net.ihe.gazelle.validation.gateway.evs.business.exception.*;
import net.ihe.gazelle.validation.gateway.evs.business.exception.ForbiddenException;
import net.ihe.gazelle.validation.gateway.evs.business.model.ValidationCreationResult;
import net.ihe.gazelle.validation.gateway.evs.business.service.*;
import net.ihe.gazelle.validation.gateway.evs.technical.dto.ValidationDTO;
import net.ihe.gazelle.validation.gateway.evs.technical.dto.ValidationServiceProfileDTO;
import net.ihe.gazelle.validation.gateway.evs.technical.service.GazelleIdentityService;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.ParameterIn;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.List;
import java.util.Set;

@ApplicationScoped
@Path("/evs/rest/validations")
@Tag(name = "EVS Validations API")
@OpenAPIDefinition(info = @Info(title = "EVS Validation APIs", version = "1.0",
      description = "REST APIs exposed by Validation Gateway to proxy EVS validation scenarios. "
            + "Authentication is controlled by `evs.api.user-need-to-be-logged-in`: when true, "
            + "Bearer JWT is required; when false, endpoints are publicly callable."))
@ProtectedResource
public class EvsValidationController {

    private static final String RESPOND_ASYNC = "respond-async";
    private static final String REPORT_XML_MEDIA_TYPE = "application/gzl.validation.report+xml";
    private static final String REPORT_JSON_MEDIA_TYPE = "application/gzl.validation.report+json";
    private static final String UNAUTHORIZED_MESSAGE = "Unauthorized.";
    private static final String FORBIDDEN_MESSAGE = "Forbidden.";
    private static final Logger LOG = LoggerFactory.getLogger(EvsValidationController.class);
    private static final Set<String> VALIDATION_MEDIA_TYPES = Set.of(MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON);
    private static final Set<String> REPORT_MEDIA_TYPES = Set.of(
          MediaType.APPLICATION_XML,
          REPORT_XML_MEDIA_TYPE,
          MediaType.APPLICATION_JSON,
          REPORT_JSON_MEDIA_TYPE
    );

    private final EvsValidationService validationService;
    private final ValidationLookupService lookupService;
    private final ValidationAccessPolicy accessPolicy;
    private final ItemTransformationService itemTransformationService;
    private final ValidationPresentation presentation;
    private final GazelleIdentityService identityService;
    private final JsonWebToken jwt;
    private final ObjectMapper objectMapper;
    private final XmlMapper xmlMapper;
    @ConfigProperty(name = "evs.api.user-need-to-be-logged-in", defaultValue = "false")
    boolean userNeedToBeLoggedIn;

    @Inject
    public EvsValidationController(EvsValidationService validationService,
                                   ValidationLookupService lookupService,
                                   ValidationAccessPolicy accessPolicy,
                                   ItemTransformationService itemTransformationService,
                                   ValidationPresentation presentation,
                                   JsonWebToken jwt,
                                   ObjectMapper objectMapper) {
        this.validationService = validationService;
        this.lookupService = lookupService;
        this.accessPolicy = accessPolicy;
        this.itemTransformationService = itemTransformationService;
        this.presentation = presentation;
        this.jwt = jwt;
        this.objectMapper = objectMapper;
        this.xmlMapper = XmlMapper.builder().findAndAddModules().disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS).build();
        this.identityService = new GazelleIdentityService();
    }

    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.TEXT_XML, MediaType.APPLICATION_JSON})
    @Operation(
          summary = "Create a validation",
          description = """
                Creates a new EVS validation request.

                Behavior:
                - `POST /evs/rest/validations` with the entity to validate.
                - Binary payloads must be Base64 encoded.
                - Provide either `validationService` + `validator`, or an EVS-supported `objectType`.
                - Bearer JWT controls caller identity and report privacy.
                - Use `Prefer: respond-async` to request asynchronous processing.

                Specification references: `EVS-41`, `EVS-45`, `EVS-46`, `EVS-50`, `EVS-55`.""")
    @APIResponse(responseCode = "201", description = "**HTTP 201 Created** resource created and validation finished.")
    @APIResponse(responseCode = "202", description = "**HTTP 202 Accepted** validation created asynchronously.")
    @APIResponse(responseCode = "401", description = "**HTTP 401 Unauthorized** returned when `evs.api.user-need-to-be-logged-in=true` and credentials are missing or invalid.")
    @APIResponse(responseCode = "400", description = "**HTTP 400 Bad Request** missing data.")
    @APIResponse(responseCode = "500", description = "**HTTP 500 Internal Server Error** unexpected failure.")
    public Response createValidation(
          @RequestBody(
                description = "Validation object with the payload, requested inputs and target validation service.",
                required = true,
                content = @Content(schema = @Schema(implementation = ValidationDTO.class)))
          String payload,
          @HeaderParam("Content-Type") String contentType,
          @Parameter(
                description = "Bearer JWT used to identify the caller and mark produced reports as private.",
                in = ParameterIn.HEADER,
                name = "Authorization")
          @HeaderParam("Authorization") String authorization,
          @Parameter(
                description = "Set to `respond-async` in the Prefer header to ask for an asynchronous creation.",
                in = ParameterIn.HEADER,
                name = "Prefer",
                schema = @Schema(pattern = "respond-async"))
          @HeaderParam("Prefer") String prefer,
          @Context UriInfo uriInfo) {
        if (isMalformedAuthorizationHeader(authorization)) {
            return Response.status(Response.Status.BAD_REQUEST)
                  .entity("Invalid Authorization header.")
                  .build();
        }
        try {
            ValidationDTO validation = parseValidationPayload(payload, contentType);
            return createValidation(validation, authorization, prefer, uriInfo);
        } catch (InvalidValidationRequestException | ValidationExecutionException e) {
            LOG.error("Validation request rejected while creating EVS validation.", e);
            return Response.status(Response.Status.BAD_REQUEST)
                  .entity(e.getMessage())
                  .build();
        } catch (JsonProcessingException e) {
            LOG.error("Invalid validation payload received while creating EVS validation.", e);
            return Response.status(Response.Status.BAD_REQUEST)
                  .entity("Invalid request payload.")
                  .build();
        } catch (RuntimeException e) {
            LOG.error("Unexpected error while creating EVS validation.", e);
            return Response.serverError()
                  .entity("Unexpected error while creating validation.")
                  .build();
        }
    }

    Response createValidation(ValidationDTO validation, String authorization, String prefer, UriInfo uriInfo) {
        try {
            if (isMissingAuthorizationHeader(authorization) && userNeedToBeLoggedIn) {
                return Response.status(Response.Status.UNAUTHORIZED)
                      .entity(UNAUTHORIZED_MESSAGE)
                      .build();
            }
            boolean async = prefer != null && prefer.contains(RESPOND_ASYNC);
            GazelleIdentity identity = identityService.resolveIdentity(authorization, jwt);
            ValidationCreationResult result =
                  validationService.createValidation(validation, identity, async, baseUri(uriInfo));
            String validationUrl = result.getValidationUrl();
            if (async) {
                return Response.status(Response.Status.ACCEPTED)
                      .header("Location", validationUrl)
                      .build();
            }
            Response.ResponseBuilder builder = Response.created(URI.create(validationUrl))
                  .header("Content-Location", validationUrl);
            String reportLocation = result.getReportLocation();
            String reportHeader = reportLocation != null ? reportLocation :
                  presentation.buildReportUrl(baseUri(uriInfo), result.getOid(), result.getPrivacyKey());
            builder.header("X-Validation-Report-Redirect", reportHeader);
            return builder.build();
        } catch (ValidationExecutionException e) {
            LOG.error("Validation execution failed while creating EVS validation.", e);
            return Response.status(Response.Status.BAD_REQUEST)
                  .entity(e.getMessage())
                  .build();
        }
    }

    @GET
    @Path("/{oid}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Operation(
          summary = "Get validation by OID",
          description = """
                Retrieves a validation resource by OID.

                Behavior:
                - `GET /evs/rest/validations/{oid}` returns the validation payload.
                - Returns `200` when validation is complete, `202` when still pending.
                - Bearer JWT can grant access to private resources when properly scoped.

                Specification references: `EVS-58`, `EVS-61`, `EVS-66`.""")
    @APIResponse(responseCode = "200", description = "**HTTP 200 OK** validation finished and returned.")
    @APIResponse(responseCode = "202", description = "**HTTP 202 Accepted** validation still running.")
    @APIResponse(responseCode = "401", description = "**HTTP 401 Unauthorized** private validation without credentials, or when `evs.api.user-need-to-be-logged-in=true` and credentials are missing/invalid.")
    @APIResponse(responseCode = "403", description = "**HTTP 403 Forbidden** credentials cannot access requested validation.")
    @APIResponse(responseCode = "404", description = "**HTTP 404 Not Found** unknown validation.")
    @APIResponse(responseCode = "500", description = "**HTTP 500 Internal Server Error** unexpected failure.")
    public Response getValidationByOid(@Parameter(description = "Validation OID to retrieve.", required = true,
          in = ParameterIn.PATH) @PathParam("oid") String oid,
                                       @Parameter(description = "Legacy read access share key when validation is private.",
                                             in = ParameterIn.QUERY) @QueryParam("privacyKey") String privacyKey,
                                       @Parameter(description = "Read access share key when validation is private.",
                                             in = ParameterIn.QUERY) @QueryParam("readAccessKey") String readAccessKey,
                                       @Parameter(description = "Bearer JWT used to grant access.",
                                             in = ParameterIn.HEADER, name = "Authorization")
                                       @HeaderParam("Authorization") String authorization,
                                       @Parameter(description = "Accept header indicating requested format (application/xml, application/json).",
                                             in = ParameterIn.HEADER, name = "Accept",
                                             schema = @Schema(pattern = "(application/(xml|json))", defaultValue = "application/xml"))
                                       @HeaderParam("Accept") String accept,
                                       @Context UriInfo uriInfo) {
        if (!isAcceptable(accept, VALIDATION_MEDIA_TYPES)) {
            return Response.status(Response.Status.NOT_ACCEPTABLE).entity("Unsupported Accept header.").build();
        }
        String effectiveReadAccessKey = resolveReadAccessKey(privacyKey, readAccessKey);
        try {
            GazelleIdentity identity = identityService.resolveIdentity(authorization, jwt);
            Item item = lookupService.readReportItem(oid);
            accessPolicy.assertCanAccess(item.getAccessControlList(), effectiveReadAccessKey, identity);
            ValidationDTO response = presentation.toValidationResponse(
                  oid,
                  itemTransformationService.readReport(item),
                  item.getAccessControlList(),
                  baseUri(uriInfo),
                  effectiveReadAccessKey);
            if (accept != null && accept.contains(MediaType.APPLICATION_XML)) {
                return Response.ok(toXml(response), MediaType.APPLICATION_XML).build();
            }
            return Response.ok(response).build();
        } catch (ValidationPendingException e) {
            LOG.error("Validation is still pending for oid '{}'.", oid, e);
            return Response.status(Response.Status.ACCEPTED)
                  .header("Location", presentation.buildValidationUrl(baseUri(uriInfo), oid, effectiveReadAccessKey))
                  .header("X-Progress", "IN_PROGRESS")
                  .build();
        } catch (ItemNotFoundException e) {
            LOG.error("Validation report item not found for oid '{}'.", oid, e);
            return Response.status(Response.Status.NOT_FOUND).entity("Validation not found.").build();
        } catch (UnauthorizedException e) {
            LOG.error("Unauthorized access to validation oid '{}'.", oid, e);
            return Response.status(Response.Status.UNAUTHORIZED).entity(UNAUTHORIZED_MESSAGE).build();
        } catch (ForbiddenException e) {
            LOG.error("Forbidden access to validation oid '{}'.", oid, e);
            return Response.status(Response.Status.FORBIDDEN).entity(FORBIDDEN_MESSAGE).build();
        } catch (RuntimeException e) {
            LOG.error("Unexpected error while reading EVS validation.", e);
            return Response.serverError().entity("Unexpected error while reading validation.").build();
        }
    }

    @GET
    @Path("/{oid}/report")
    @Produces({
          MediaType.APPLICATION_XML,
          REPORT_XML_MEDIA_TYPE,
          MediaType.APPLICATION_JSON,
          REPORT_JSON_MEDIA_TYPE
    })
    @Operation(
          summary = "Get validation report by OID",
          description = """
                Retrieves the validation report associated with an OID.

                Behavior:
                - `GET /evs/rest/validations/{oid}/report` returns the report payload.
                - Returns `200` when the report exists.
                - Returns `404` when validation is unknown or the report is not yet available.

                Specification references: `EVS-69`, `EVS-74`.""")
    @APIResponse(responseCode = "200", description = "**HTTP 200 OK** report available.")
    @APIResponse(responseCode = "401", description = "**HTTP 401 Unauthorized** private validation without credentials, or when `evs.api.user-need-to-be-logged-in=true` and credentials are missing/invalid.")
    @APIResponse(responseCode = "403", description = "**HTTP 403 Forbidden** credentials cannot access requested report.")
    @APIResponse(responseCode = "404", description = "**HTTP 404 Not Found** unknown validation or pending report.")
    @APIResponse(responseCode = "406", description = "**HTTP 406 Not Acceptable** requested report format not supported.")
    @APIResponse(responseCode = "500", description = "**HTTP 500 Internal Server Error** unexpected failure.")
    public Response getValidationReportByOid(@Parameter(description = "Validation OID holding the report.", required = true,
          in = ParameterIn.PATH) @PathParam("oid") String oid,
                                             @Parameter(description = "Legacy share key for private reports.", in = ParameterIn.QUERY)
                                             @QueryParam("privacyKey") String privacyKey,
                                             @Parameter(description = "Share key for private reports.", in = ParameterIn.QUERY)
                                             @QueryParam("readAccessKey") String readAccessKey,
                                             @Parameter(description = "Only include assertions above this severity (optional).",
                                                   in = ParameterIn.QUERY)
                                             @QueryParam("severityThreshold") String severityThreshold,
                                             @Parameter(description = "Bearer JWT used to grant access.",
                                                   in = ParameterIn.HEADER, name = "Authorization")
                                             @HeaderParam("Authorization") String authorization,
                                             @Parameter(description = "Accept header selecting the report media type.",
                                                   in = ParameterIn.HEADER, name = "Accept",
                                                   schema = @Schema(pattern = "application/(xml|json|gzl.validation.report\\+xml|gzl.validation.report\\+json)",
                                                         defaultValue = "application/xml"))
                                             @HeaderParam("Accept") String accept,
                                             @Context UriInfo uriInfo) {
        if (accept != null && (accept.contains("application/junit+xml") || accept.contains("application/svrl+xml"))) {
            return Response.status(Response.Status.NOT_IMPLEMENTED)
                  .entity("Requested report format is not supported yet.")
                  .build();
        }
        if (!isAcceptable(accept, REPORT_MEDIA_TYPES)) {
            return Response.status(Response.Status.NOT_ACCEPTABLE).entity("Unsupported Accept header.").build();
        }
        String effectiveReadAccessKey = resolveReadAccessKey(privacyKey, readAccessKey);
        try {
            GazelleIdentity identity = identityService.resolveIdentity(authorization, jwt);
            var item = lookupService.readReportItem(oid);
            accessPolicy.assertCanAccess(item.getAccessControlList(), effectiveReadAccessKey, identity);
            String payload = itemTransformationService.toReportPayload(item, accept);
            return Response.ok(payload, resolveReportMediaType(accept)).build();
        } catch (ValidationPendingException e) {
            LOG.error("Validation report is not available yet for oid '{}'.", oid, e);
            return Response.status(Response.Status.NOT_FOUND)
                  .entity("Validation report not available yet.")
                  .build();
        } catch (ItemNotFoundException e) {
            LOG.error("Validation report item not found for oid '{}'.", oid, e);
            return Response.status(Response.Status.NOT_FOUND).entity("Validation not found.").build();
        } catch (UnauthorizedException e) {
            LOG.error("Unauthorized access to validation report oid '{}'.", oid, e);
            return Response.status(Response.Status.UNAUTHORIZED).entity(UNAUTHORIZED_MESSAGE).build();
        } catch (ForbiddenException e) {
            LOG.error("Forbidden access to validation report oid '{}'.", oid, e);
            return Response.status(Response.Status.FORBIDDEN).entity(FORBIDDEN_MESSAGE).build();
        } catch (RuntimeException e) {
            LOG.error("Unexpected error while reading EVS validation report for oid '{}'.", oid, e);
            return Response.serverError().entity("Unexpected error while reading validation report.").build();
        }
    }

    @GET
    @Path("/profiles")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
          summary = "List validation profiles",
          description = """
                Lists available EVS validation profiles.

                Behavior:
                - Optionally filter profiles by `serviceName`.
                - Without filter, returns profiles for the full EVS instance.
                - Always returns the full matching profile set.

                Specification reference: `EVSCLT-1217`.""")
    @ProtectedResource
    @APIResponse(responseCode = "200", description = "**HTTP 200 OK** profiles returned.")
    @APIResponse(responseCode = "401", description = "**HTTP 401 Unauthorized** returned when credentials are required and missing or invalid.")
    @APIResponse(responseCode = "403", description = "**HTTP 403 Forbidden** access not permitted.")
    @APIResponse(responseCode = "500", description = "**HTTP 500 Internal Server Error** unexpected failure.")
    public Response getValidationProfiles(
          @Parameter(description = "Validation service name to filter profiles.", in = ParameterIn.QUERY)
          @QueryParam("serviceName") String serviceName,
          @Parameter(description = "Accept header to request JSON.", in = ParameterIn.HEADER, name = "Accept",
                schema = @Schema(pattern = "application/json", defaultValue = "application/json"))
          @HeaderParam("Accept") String accept,
          @Parameter(description = "Bearer JWT used to authorize profile listing.",
                in = ParameterIn.HEADER, name = "Authorization")
          @HeaderParam("Authorization") String authorization) {
        try {
            GazelleIdentity identity = identityService.resolveIdentity(authorization, jwt);
            List<ValidationServiceProfileDTO> profiles =
                  validationService.listProfiles(serviceName, identity);
            return Response.ok(profiles, MediaType.APPLICATION_JSON).build();
        } catch (net.ihe.gazelle.security.business.UnauthorizedException e) {
            LOG.error("Unauthorized profile listing request.", e);
            GazelleIdentity identity = identityService.resolveIdentity(authorization, jwt);
            if (identity.isAuthenticated()) {
                return Response.status(Response.Status.FORBIDDEN)
                      .entity(FORBIDDEN_MESSAGE)
                      .build();
            }
            return Response.status(Response.Status.UNAUTHORIZED)
                  .entity(UNAUTHORIZED_MESSAGE)
                  .build();
        } catch (RuntimeException e) {
            LOG.error("Unexpected error while listing EVS profiles.", e);
            return Response.serverError()
                  .entity("Unexpected error while listing profiles.")
                  .build();
        }
    }


    private boolean isAcceptable(String accept, Set<String> allowed) {
        if (accept == null || accept.isBlank() || "*/*".equals(accept)) {
            return true;
        }
        try {
            for (String requestedRaw : accept.split("[\\s,]+")) {
                MimeType requested = new MimeType(requestedRaw);
                for (String candidate : allowed) {
                    if (requested.match(new MimeType(candidate))) {
                        return true;
                    }
                }
            }
            return false;
        } catch (MimeTypeParseException e) {
            LOG.error("Invalid Accept header value '{}'.", accept, e);
            return false;
        }
    }

    private String resolveReportMediaType(String accept) {
        if (accept != null && accept.contains(REPORT_XML_MEDIA_TYPE)) {
            return REPORT_XML_MEDIA_TYPE;
        }
        if (accept != null && accept.contains(MediaType.APPLICATION_XML)) {
            return MediaType.APPLICATION_XML;
        }
        if (accept != null && accept.contains(REPORT_JSON_MEDIA_TYPE)) {
            return REPORT_JSON_MEDIA_TYPE;
        }
        return MediaType.APPLICATION_JSON;
    }

    private String baseUri(UriInfo uriInfo) {
        return uriInfo.getBaseUri().toASCIIString();
    }

    private String resolveReadAccessKey(String privacyKey, String readAccessKey) {
        return (readAccessKey != null && !readAccessKey.isBlank()) ? readAccessKey : privacyKey;
    }

    private boolean isMissingAuthorizationHeader(String authorizationHeader) {
        return authorizationHeader == null || authorizationHeader.isBlank();
    }

    private boolean isMalformedAuthorizationHeader(String authorizationHeader) {
        if (authorizationHeader == null) {
            return false;
        }
        if (authorizationHeader.isBlank()) {
            return true;
        }
        if (!authorizationHeader.startsWith("Bearer ")) {
            return true;
        }
        String token = authorizationHeader.substring("Bearer ".length());
        return token.isBlank();
    }

    private ValidationDTO parseValidationPayload(String payload, String contentType) {
        if (payload == null || payload.isBlank()) {
            throw new JsonProcessingException("Empty request payload.");
        }
        try{
            if (contentType != null && contentType.toLowerCase().contains("xml")) {
                return xmlMapper.readValue(payload, ValidationDTO.class);
            }
            return objectMapper.readValue(payload, ValidationDTO.class);
        }
        catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new JsonProcessingException("Invalid request payload: ", e);
        }
    }

    private String toXml(Object payload) {
        try {
            return xmlMapper.writeValueAsString(payload);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new JsonProcessingException("Unable to serialize XML payload.", e);
        }
    }
}
