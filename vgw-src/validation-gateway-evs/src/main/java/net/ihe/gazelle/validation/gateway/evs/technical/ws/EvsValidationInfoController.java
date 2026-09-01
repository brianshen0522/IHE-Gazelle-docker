package net.ihe.gazelle.validation.gateway.evs.technical.ws;

import com.kereval.gazelle.datahouse.api.business.search.ItemNotFoundException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import net.ihe.gazelle.oidc.rest.business.ProtectedResource;
import net.ihe.gazelle.security.business.GazelleIdentity;
import net.ihe.gazelle.validation.gateway.evs.business.exception.ForbiddenException;
import net.ihe.gazelle.validation.gateway.evs.business.exception.UnauthorizedException;
import net.ihe.gazelle.validation.gateway.evs.business.service.ItemTransformationService;
import net.ihe.gazelle.validation.gateway.evs.business.service.ValidationAccessPolicy;
import net.ihe.gazelle.validation.gateway.evs.business.service.ValidationLookupService;
import net.ihe.gazelle.validation.gateway.evs.business.service.ValidationPresentation;
import net.ihe.gazelle.validation.gateway.evs.technical.dto.ValidationStatus;
import net.ihe.gazelle.validation.gateway.evs.technical.service.GazelleIdentityService;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;
import java.util.Optional;

@ApplicationScoped
@ProtectedResource
@Path("/evs/rest")
public class EvsValidationInfoController {

    private static final Logger LOG = LoggerFactory.getLogger(EvsValidationInfoController.class);
    private static final String UNAUTHORIZED_MESSAGE = "Unauthorized.";
    private static final String FORBIDDEN_MESSAGE = "Forbidden.";
    private final ValidationLookupService lookupService;
    private final ItemTransformationService itemTransformationService;
    private final ValidationPresentation presentation;
    private final GazelleIdentityService identityService;
    private final JsonWebToken jwt;
    private final ValidationAccessPolicy accessPolicy;

    @Inject
    public EvsValidationInfoController(ValidationLookupService lookupService,
                                       ItemTransformationService itemTransformationService,
                                       ValidationPresentation presentation,
                                       JsonWebToken jwt,
                                       ValidationAccessPolicy accessPolicy) {
        this.lookupService = lookupService;
        this.itemTransformationService = itemTransformationService;
        this.presentation = presentation;
        this.identityService = new GazelleIdentityService();
        this.jwt = jwt;
        this.accessPolicy = accessPolicy;
    }

    @GET
    @Path("/GetValidationDate")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getValidationDate(@QueryParam("oid") String oid,
                                    @DefaultValue("") @QueryParam("cache") String cache,
                                    @jakarta.ws.rs.core.Context UriInfo uriInfo,
                                    @HeaderParam("Authorization") String authorization) {
        try {
            GazelleIdentity identity = identityService.resolveIdentity(authorization, jwt);
            var item = lookupService.readReportItem(oid);
            accessPolicy.assertCanAccess(item.getAccessControlList(), null, identity);
            var report = itemTransformationService.readReport(item);
            return Optional.ofNullable(presentation.toOffsetDateTime(report.getDateTime()))
                    .map(OffsetDateTime::toString)
                    .map(Response::ok)
                    .orElse(Response.status(Response.Status.NOT_FOUND).entity("Validation date not found.")).build();
        } catch (ItemNotFoundException e) {
            LOG.error("Validation report item not found for oid '{}'.", oid, e);
            return Response.status(Response.Status.NOT_FOUND).entity("Validation not found.").build();
        } catch (UnauthorizedException e) {
            LOG.error("Unauthorized access to validation report oid '{}'.", oid, e);
            return Response.status(Response.Status.UNAUTHORIZED).entity(UNAUTHORIZED_MESSAGE).build();
        } catch (ForbiddenException e) {
            LOG.error("Forbidden access to validation report oid '{}'.", oid, e);
            return Response.status(Response.Status.FORBIDDEN).entity(FORBIDDEN_MESSAGE).build();
        }
    }

    @GET
    @Path("/GetValidationPermanentLink")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getValidationPermanentLink(@QueryParam("oid") String oid,
                                             @DefaultValue("") @QueryParam("cache") String cache,
                                             @jakarta.ws.rs.core.Context UriInfo uriInfo,
                                             @HeaderParam("Authorization") String authorization) {
        try {
            GazelleIdentity identity = identityService.resolveIdentity(authorization, jwt);
            var item = lookupService.readReportItem(oid);
            accessPolicy.assertCanAccess(item.getAccessControlList(), null, identity);
            return Response.ok().entity(presentation.buildValidationPortalUrl(baseUri(uriInfo), oid)).build();
        } catch (ItemNotFoundException e) {
            LOG.error("Validation report item not found for oid '{}'.", oid, e);
            return Response.status(Response.Status.NOT_FOUND).entity("Validation not found.").build();
        } catch (UnauthorizedException e) {
            LOG.error("Unauthorized access to validation report oid '{}'.", oid, e);
            return Response.status(Response.Status.UNAUTHORIZED).entity(UNAUTHORIZED_MESSAGE).build();
        } catch (ForbiddenException e) {
            LOG.error("Forbidden access to validation report oid '{}'.", oid, e);
            return Response.status(Response.Status.FORBIDDEN).entity(FORBIDDEN_MESSAGE).build();
        }
    }

    @GET
    @Path("/GetValidationStatus")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getValidationStatus(@QueryParam("oid") String oid,
                                      @DefaultValue("") @QueryParam("cache") String cache,
                                      @HeaderParam("Authorization") String authorization) {
        try {
            GazelleIdentity identity = identityService.resolveIdentity(authorization, jwt);
            var item = lookupService.readReportItem(oid);
            accessPolicy.assertCanAccess(item.getAccessControlList(), null, identity);
            var report = itemTransformationService.readReport(item);
            return Response.ok().entity(mapStatus(presentation.toValidationStatus(report))).build();
        } catch (ItemNotFoundException e) {
            LOG.error("Validation report item not found for oid '{}'.", oid, e);
            return Response.status(Response.Status.NOT_FOUND).entity("Validation not found.").build();
        } catch (UnauthorizedException e) {
            LOG.error("Unauthorized access to validation report oid '{}'.", oid, e);
            return Response.status(Response.Status.UNAUTHORIZED).entity(UNAUTHORIZED_MESSAGE).build();
        } catch (ForbiddenException e) {
            LOG.error("Forbidden access to validation report oid '{}'.", oid, e);
            return Response.status(Response.Status.FORBIDDEN).entity(FORBIDDEN_MESSAGE).build();
        }
    }

    @GET
    @Path("/GetLastResultStatusByExternalId")
    @Produces(MediaType.TEXT_PLAIN)
    @Operation(hidden = true)
    public Response getLastResultStatusByExternalId(@QueryParam("externalId") String externalId,
                                                    @QueryParam("toolOid") String toolOid,
                                                    @DefaultValue("") @QueryParam("cache") String cache) {
        return Response.status(Response.Status.NOT_IMPLEMENTED)
                .entity("External ID lookup is not available in this deployment due to missing metadata.")
                .build();
    }

    @GET
    @Path("/GetValidationPermanentLinkByExternalId")
    @Produces(MediaType.TEXT_PLAIN)
    @Operation(hidden = true)
    public Response getValidationPermanentLinkByExternalId(@QueryParam("externalId") String externalId,
                                                           @QueryParam("toolOid") String toolOid,
                                                           @DefaultValue("") @QueryParam("cache") String cache,
                                                           @jakarta.ws.rs.core.Context UriInfo uriInfo) {
        return Response.status(Response.Status.NOT_IMPLEMENTED)
                .entity("External ID lookup is not available in this deployment due to missing metadata.")
                .build();
    }

    private String mapStatus(ValidationStatus status) {
        if (status == null) {
            return "";
        }
        return switch (status) {
            case DONE_PASSED -> "PASSED";
            case DONE_FAILED -> "FAILED";
            case DONE_UNDEFINED -> "ABORTED";
            case PENDING, IN_PROGRESS -> "NOT_PERFORMED";
        };
    }

    private String baseUri(UriInfo uriInfo) {
        return uriInfo.getBaseUri().toASCIIString();
    }
}
