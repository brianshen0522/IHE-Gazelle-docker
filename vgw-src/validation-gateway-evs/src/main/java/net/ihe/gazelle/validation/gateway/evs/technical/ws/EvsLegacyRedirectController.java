package net.ihe.gazelle.validation.gateway.evs.technical.ws;

import net.ihe.gazelle.validation.gateway.evs.business.service.ValidationLookupService;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

@ApplicationScoped
@Path("/evs")
public class EvsLegacyRedirectController {

    private static final Logger LOG = LoggerFactory.getLogger(EvsLegacyRedirectController.class);
    private final ValidationLookupService lookupService;
    private final String validationPortalBaseUrl;

    @Inject
    public EvsLegacyRedirectController(ValidationLookupService lookupService,
                                       @ConfigProperty(name = "validation.portal.base-url")
                                       String validationPortalBaseUrl) {
        this.lookupService = lookupService;
        this.validationPortalBaseUrl = normalizeBaseUrl(validationPortalBaseUrl);
    }

    @GET
    @Path("/report.seam")
    @Produces(MediaType.TEXT_PLAIN)
    public Response redirectLegacyReportLink(@QueryParam("oid") String oid,
                                             @QueryParam("privacyKey") String privacyKey,
                                             @QueryParam("readAccessKey") String readAccessKey,
                                             @Context UriInfo uriInfo) {
        if (oid == null || oid.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Missing validation OID.").build();
        }
        String effectiveReadAccessKey = resolveReadAccessKey(privacyKey, readAccessKey);
        try {
            var locatedReport = lookupService.findReportByLegacyOid(oid);
            if (locatedReport.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND).entity("Validation not found.").build();
            }
            String redirectUrl = buildValidationPortalUrl(uriInfo, locatedReport.get().itemId(), effectiveReadAccessKey);
            return Response.seeOther(URI.create(redirectUrl)).build();
        } catch (RuntimeException e) {
            LOG.error("Unexpected error while resolving legacy validation link for oid '{}'.", oid, e);
            return Response.serverError().entity("Unexpected error while resolving validation link.").build();
        }
    }

    private String buildValidationPortalUrl(UriInfo uriInfo, String itemId, String readAccessKey) {
        UriBuilder builder;
        if (validationPortalBaseUrl.isBlank()) {
            builder = uriInfo.getBaseUriBuilder()
                  .path("validation-portal")
                  .path("reports")
                  .path(itemId);
        } else {
            UriBuilder configuredBaseBuilder = UriBuilder.fromUri(validationPortalBaseUrl);
            builder = validationPortalBaseUrl.endsWith("/reports")
                  ? configuredBaseBuilder.path(itemId)
                  : configuredBaseBuilder.path("reports").path(itemId);
        }
        if (readAccessKey != null && !readAccessKey.isBlank()) {
            builder.queryParam("readAccessKey", readAccessKey);
        }
        return builder.build().toASCIIString();
    }

    private String resolveReadAccessKey(String privacyKey, String readAccessKey) {
        return (readAccessKey != null && !readAccessKey.isBlank()) ? readAccessKey : privacyKey;
    }

    private String normalizeBaseUrl(String baseUrl) {
        if (baseUrl == null) {
            return "";
        }
        String trimmed = baseUrl.trim();
        if (trimmed.endsWith("/")) {
            return trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }
}
