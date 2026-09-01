package net.ihe.gazelle.validation.gateway.evs.technical.ws;

import com.kereval.gazelle.datahouse.api.business.record.Item;
import jakarta.ws.rs.core.*;
import jakarta.ws.rs.ext.RuntimeDelegate;
import net.ihe.gazelle.security.business.Authz;
import net.ihe.gazelle.security.business.GazelleIdentity;
import net.ihe.gazelle.security.business.acl.AccessControlList;
import net.ihe.gazelle.security.technical.acl.ReadAccessKeyGeneratorImpl;
import net.ihe.gazelle.validation.gateway.evs.business.exception.ValidationExecutionException;
import net.ihe.gazelle.validation.gateway.evs.business.exception.ValidationPendingException;
import net.ihe.gazelle.validation.gateway.evs.business.model.LocatedReportItem;
import net.ihe.gazelle.validation.gateway.evs.business.model.ValidationCreationResult;
import net.ihe.gazelle.validation.gateway.evs.business.service.*;
import net.ihe.gazelle.validation.gateway.evs.technical.dto.ValidationDTO;
import net.ihe.gazelle.validation.gateway.evs.technical.dto.ValidationServiceProfileDTO;
import net.ihe.gazelle.validation.gateway.evs.technical.dto.ValidatorDTO;
import net.ihe.gazelle.validation.gateway.evs.technical.dto.ValidationStatus;
import net.ihe.gazelle.validation.v2.api.business.Input;
import net.ihe.gazelle.validation.v2.api.business.report.ValidationReport;
import net.ihe.gazelle.validation.v2.api.business.report.ValidationTestResult;
import org.glassfish.jersey.internal.RuntimeDelegateImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class EvsValidationControllerTest {

    static {
        RuntimeDelegate.setInstance(new RuntimeDelegateImpl());
    }

    @Test
    @DisplayName("VAL-014 returns 202 when async preference is set")
    void val014_returnsAcceptedWhenAsync() {
        EvsValidationController controller = controller(false, false);
        Response response = controller.createValidation(new ValidationDTO(), null, "respond-async", baseUriInfo("https://gateway.example/api/"));

        assertThat(response.getStatus(), is(Response.Status.ACCEPTED.getStatusCode()));
        assertThat(response.getHeaderString("Location"), is("https://gateway.example/evs/rest/validations/oid-ctrl"));
    }

    @Test
    @DisplayName("VAL-014 returns created payload when no async preference is provided")
    void val014_returnsCreatedWhenSync() {
        EvsValidationController controller = controller(false, false);
        Response response = controller.createValidation(new ValidationDTO(), null, null, baseUriInfo("https://gateway.example/api/"));

        assertThat(response.getStatus(), is(Response.Status.CREATED.getStatusCode()));
        assertThat(response.getHeaderString("Content-Location"), is("https://gateway.example/evs/rest/validations/oid-ctrl"));
        assertThat(response.getHeaderString("X-Validation-Report-Redirect"), is("https://gateway.example/reports/oid-ctrl"));
    }

    @Test
    @DisplayName("VAL-049 surfaces Maestro step errors when validation report is missing")
    void val049_surfacesStepErrorWhenReportMissing() {
        EvsValidationController controller = controller(true, false);
        Response response = controller.createValidation(new ValidationDTO(), null, null, baseUriInfo("https://gateway.example/api/"));

        assertThat(response.getStatus(), is(Response.Status.BAD_REQUEST.getStatusCode()));
        assertThat((String) response.getEntity(),
              is("Unexpected error while running step: java.util.NoSuchElementException: No service with name string"));
    }

    @Test
    @DisplayName("VAL-050 rejects unauthenticated validation creation when login is required")
    void val050_rejectsUnauthenticatedCreationWhenLoginIsRequired() {
        EvsValidationController controller = controller(false, false, true);
        Response response = controller.createValidation(new ValidationDTO(), null, null, baseUriInfo("https://gateway.example/api/"));

        assertThat(response.getStatus(), is(Response.Status.UNAUTHORIZED.getStatusCode()));
        assertThat(response.getEntity(), is("Unauthorized."));
    }

    @Test
    @DisplayName("VAL-090 returns bad request when Authorization header is malformed")
    void val090_rejectsMalformedAuthorizationHeader() {
        EvsValidationController controller = controller(false, false);
        Response response = controller.createValidation(
              "{}",
              MediaType.APPLICATION_JSON,
              "Basic abc",
              null,
              baseUriInfo("https://gateway.example/api/"));

        assertThat(response.getStatus(), is(Response.Status.BAD_REQUEST.getStatusCode()));
        assertThat(response.getEntity(), is("Invalid Authorization header."));
    }

    @Test
    @DisplayName("VAL-037 rejects unsupported accept headers when reading by OID")
    void val037_rejectsUnsupportedAcceptsForValidation() {
        EvsValidationController controller = controller(false, false);
        Response response = controller.getValidationByOid("oid", null, null, null, "text/plain", baseUriInfo("https://gateway.example/api/"));

        assertThat(response.getStatus(), is(Response.Status.NOT_ACCEPTABLE.getStatusCode()));
    }

    @Test
    @DisplayName("VAL-037 can return validation payloads by OID")
    void val037_returnsValidationPayloadWhenAcceptable() {
        EvsValidationController controller = controller(false, false);
        Response response = controller.getValidationByOid("oid", null, null, null, MediaType.APPLICATION_JSON, baseUriInfo("https://gateway.example/api/"));

        assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));
        assertThat(response.getEntity() instanceof ValidationDTO, is(true));
    }

    @Test
    @DisplayName("VAL-014 returns 202 when validation is still pending")
    void val014_returnsAcceptedWhenValidationPending() {
        EvsValidationController controller = controller(false, true);
        Response response = controller.getValidationByOid("oid", null, null, null, MediaType.APPLICATION_JSON, baseUriInfo("https://gateway.example/api/"));

        assertThat(response.getStatus(), is(Response.Status.ACCEPTED.getStatusCode()));
        assertThat(response.getHeaderString("X-Progress"), is("IN_PROGRESS"));
    }

    @Test
    @DisplayName("VAL-014 pending validation locations use readAccessKey parameter")
    void val014_pendingValidationLocationUsesReadAccessKey() {
        EvsValidationController controller = controller(false, true);
        Response response = controller.getValidationByOid("oid", "legacy-key", null, null, MediaType.APPLICATION_JSON, baseUriInfo("https://gateway.example/api/"));

        assertThat(response.getStatus(), is(Response.Status.ACCEPTED.getStatusCode()));
        assertThat(response.getHeaderString("Location"), is("https://gateway.example/api/evs/rest/validations/oid?readAccessKey=legacy-key"));
    }

    @Test
    @DisplayName("VAL-049 rejects report formats that are not implemented yet")
    void val049_rejectsUnsupportedReportFormats() {
        EvsValidationController controller = controller(false, false);
        Response response = controller.getValidationReportByOid("oid", null, null, null, null, "application/junit+xml", baseUriInfo("https://gateway.example/api/"));

        assertThat(response.getStatus(), is(Response.Status.NOT_IMPLEMENTED.getStatusCode()));
    }

    @Test
    @DisplayName("VAL-014 returns 404 when report is not ready yet")
    void val014_returnsNotFoundWhenReportPending() {
        EvsValidationController controller = controller(false, true);
        Response response = controller.getValidationReportByOid("oid", null, null, null, null, MediaType.APPLICATION_JSON, baseUriInfo("https://gateway.example/api/"));

        assertThat(response.getStatus(), is(Response.Status.NOT_FOUND.getStatusCode()));
    }

    @Test
    @DisplayName("VAL-049 returns report payloads with the proper media type")
    void val049_returnsReportPayloadWithRequestedMediaType() {
        EvsValidationController controller = controller(false, false);
        Response response = controller.getValidationReportByOid("oid", null, null, null, null, "application/gzl.validation.report+xml", baseUriInfo("https://gateway.example/api/"));

        assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));
        assertThat(response.getMediaType().toString(), is("application/gzl.validation.report+xml"));
    }

    @Test
    @DisplayName("VAL-037 returns profiles as JSON even when XML is requested")
    void val037_returnsProfilesAsJson() {
        EvsValidationController controller = controller(false, false);
        Response response = controller.getValidationProfiles("svc", MediaType.APPLICATION_XML, null);

        assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));
        assertThat(response.getMediaType().toString(), is(MediaType.APPLICATION_JSON));
        assertThat(response.getEntity() instanceof List<?>, is(true));
        @SuppressWarnings("unchecked")
        List<ValidationServiceProfileDTO> profiles = (List<ValidationServiceProfileDTO>) response.getEntity();
        assertThat(profiles.getFirst().getValidator().getKeyword(), is("profile-123"));
    }

    @Test
    @DisplayName("VAL-037 serializes validation reads as XML when requested")
    void val037_returnsValidationAsXml() {
        EvsValidationController controller = controller(false, false);

        Response response = controller.getValidationByOid(
              "oid",
              null,
              null,
              null,
              MediaType.APPLICATION_XML,
              baseUriInfo("https://gateway.example/api/"));

        assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));
        assertThat(response.getMediaType().toString(), is(MediaType.APPLICATION_XML));
        assertThat(response.getEntity() instanceof String, is(true));
        assertThat((String) response.getEntity(), org.hamcrest.Matchers.containsString("<validation"));
        assertThat((String) response.getEntity(),
              org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("ValidationDTO@")));
    }

    @Test
    @DisplayName("External id status endpoint is not implemented")
    void valLastResultStatusByExternalId() {
        EvsValidationInfoController controller = infoController(false);
        Response response = controller.getLastResultStatusByExternalId("external", "tool", "");

        assertThat(response.getStatus(), is(Response.Status.NOT_IMPLEMENTED.getStatusCode()));
    }

    @Test
    @DisplayName("External id permanent link endpoint is not implemented")
    void valPermanentLinkByExternalId() {
        EvsValidationInfoController controller = infoController(false);
        Response response = controller.getValidationPermanentLinkByExternalId("external", "tool", "", baseUriInfo("https://gateway.example/api/"));

        assertThat(response.getStatus(), is(Response.Status.NOT_IMPLEMENTED.getStatusCode()));
    }

    private static EvsValidationController controller(boolean failCreate, boolean pendingLookup) {
        return controller(failCreate, pendingLookup, false);
    }

    private static EvsValidationController controller(boolean failCreate,
                                                      boolean pendingLookup,
                                                      boolean userNeedToBeLoggedIn) {
        EvsValidationService service = new TestValidationService(failCreate);
        ValidationReportService reportService = new TestReportService(pendingLookup);
        ValidationLookupService lookupService = new ValidationLookupService(new AsyncReportState(), reportService);
        EvsValidationController controller = new EvsValidationController(
              service,
              lookupService,
              new PermitAllAccessPolicy(),
              new TestItemTransformationService(),
              new TestPresentation(),
              null,
              new com.fasterxml.jackson.databind.ObjectMapper().findAndRegisterModules()
        );
        setBooleanField(controller, "userNeedToBeLoggedIn", userNeedToBeLoggedIn);
        return controller;
    }

    private static void setBooleanField(Object target, String fieldName, boolean value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.setBoolean(target, value);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    private static EvsValidationInfoController infoController(boolean pendingLookup) {
        ValidationLookupService lookupService = new ValidationLookupService(new AsyncReportState(), new TestReportService(pendingLookup));
        return new EvsValidationInfoController(lookupService, new TestItemTransformationService(), new TestPresentation(), null, new PermitAllAccessPolicy());
    }

    private static UriInfo baseUriInfo(String baseUri) {
        URI uri = URI.create(baseUri);
        return new UriInfo() {
            @Override
            public String getPath() {
                return uri.getPath();
            }

            @Override
            public String getPath(boolean decode) {
                return uri.getPath();
            }

            @Override
            public List<PathSegment> getPathSegments() {
                return List.of();
            }

            @Override
            public List<PathSegment> getPathSegments(boolean decode) {
                return List.of();
            }

            @Override
            public URI getRequestUri() {
                return uri;
            }

            @Override
            public UriBuilder getRequestUriBuilder() {
                return UriBuilder.fromUri(uri);
            }

            @Override
            public URI getAbsolutePath() {
                return uri;
            }

            @Override
            public UriBuilder getAbsolutePathBuilder() {
                return UriBuilder.fromUri(uri);
            }

            @Override
            public URI getBaseUri() {
                return uri;
            }

            @Override
            public UriBuilder getBaseUriBuilder() {
                return UriBuilder.fromUri(uri);
            }

            @Override
            public MultivaluedHashMap<String, String> getPathParameters() {
                return new MultivaluedHashMap<>();
            }

            @Override
            public MultivaluedHashMap<String, String> getPathParameters(boolean decode) {
                return new MultivaluedHashMap<>();
            }

            @Override
            public MultivaluedHashMap<String, String> getQueryParameters() {
                return new MultivaluedHashMap<>();
            }

            @Override
            public MultivaluedHashMap<String, String> getQueryParameters(boolean decode) {
                return new MultivaluedHashMap<>();
            }

            @Override
            public List<String> getMatchedURIs() {
                return List.of();
            }

            @Override
            public List<String> getMatchedURIs(boolean decode) {
                return List.of();
            }

            @Override
            public List<Object> getMatchedResources() {
                return List.of();
            }

            @Override
            public URI resolve(URI uri) {
                return UriBuilder.fromUri(baseUri).build(uri);
            }

            @Override
            public URI relativize(URI uri) {
                return URI.create(baseUri).relativize(uri);
            }
        };
    }

    private static class TestValidationService extends EvsValidationService {

        private final boolean failCreate;

        TestValidationService(boolean failCreate) {
            super((ValidationExecutionService) null, (ValidationProfileService) null);
            this.failCreate = failCreate;
        }

        @Override
        public ValidationCreationResult createValidation(ValidationDTO validationRequest,
                                                         GazelleIdentity identity,
                                                         boolean async,
                                                         String baseUri) {
            if (failCreate) {
                throw new ValidationExecutionException(
                      "Unexpected error while running step: java.util.NoSuchElementException: No service with name string");
            }
            return new ValidationCreationResult(
                  "oid-ctrl",
                  "https://gateway.example/evs/rest/validations/oid-ctrl",
                  "https://gateway.example/reports/oid-ctrl",
                  "key",
                  async
            );
        }

        @Override
        public List<ValidationServiceProfileDTO> listProfiles(String serviceName, GazelleIdentity identity) {
            ValidationServiceProfileDTO dto = new ValidationServiceProfileDTO();
            dto.setServiceName(serviceName != null ? serviceName : "default");
            ValidatorDTO validator = new ValidatorDTO();
            validator.setKeyword("profile-123");
            validator.setName("Profile 123");
            validator.setDomain("ITI");
            dto.setValidator(validator);
            return List.of(dto);
        }
    }

    private static class TestReportService implements ValidationReportService {

        private final boolean pendingLookup;

        TestReportService(boolean pendingLookup) {
            this.pendingLookup = pendingLookup;
        }

        @Override
        public Item readReportItem(String oid) {
            if (pendingLookup) {
                throw new ValidationPendingException("validation-pending");
            }
            Item item = new Item();
            AccessControlList acl = new AccessControlList();
            acl.setPublic(true);
            item.setAccessControlList(acl);
            item.setContent("{\"ok\":true}");
            item.setId(oid);
            return item;
        }

        @Override
        public Optional<LocatedReportItem> findReportByLegacyOid(String legacyOid) {
            return Optional.empty();
        }

        @Override
        public String extractValidationReportLocation(net.ihe.gazelle.maestro.api.business.testreport.TestReport report) {
            return null;
        }

        @Override
        public String extractReportId(String reportLocation) {
            return null;
        }

        @Override
        public String resolveExecutionFailureMessage(net.ihe.gazelle.maestro.api.business.testreport.TestReport report) {
            return "";
        }
    }

    private static class PermitAllAccessPolicy extends ValidationAccessPolicy {
        PermitAllAccessPolicy() {
            super(8, new ReadAccessKeyGeneratorImpl(), new PermitAllAuthz());
        }

        @Override
        public void assertCanAccess(AccessControlList acl, String privacyKey, GazelleIdentity identity) {
            // no-op
        }

        @Override
        public AccessControlList buildAccessControlList(GazelleIdentity identity) {
            AccessControlList acl = new AccessControlList();
            acl.setPublic(true);
            return acl;
        }
    }

    private static class PermitAllAuthz implements Authz {
        @Override
        public boolean isAuthorized(GazelleIdentity identity, String action, Object... context) {
            return true;
        }

        @Override
        public <C extends Collection<? extends Object>> C filterOutUnauthorized(GazelleIdentity identity,
                                                                                 String action,
                                                                                 C collection) {
            return collection;
        }
    }

    private static class TestItemTransformationService extends ItemTransformationService {
        @Override
        public ValidationReport readReport(Item item) {
            ValidationReport report = new ValidationReport();
            report.setOverallResult(ValidationTestResult.PASSED);
            return report;
        }

        @Override
        public String toReportPayload(Item item, String accept) {
            return "{\"report\":\"ok\"}";
        }
    }

    private static class TestPresentation extends ValidationPresentation {
        TestPresentation() {
            super("", "");
        }

        @Override
        public ValidationDTO enrichValidationMetadata(ValidationDTO validation, AccessControlList acl, GazelleIdentity identity) {
            return validation;
        }

        @Override
        public ValidationDTO toValidationResponse(String oid,
                                                  ValidationReport report,
                                                  AccessControlList acl,
                                                  String baseUri,
                                                  String privacyKey) {
            ValidationDTO dto = new ValidationDTO();
            dto.setOid(oid);
            return dto;
        }

        @Override
        public ValidationStatus toValidationStatus(ValidationReport report) {
            return ValidationStatus.DONE_PASSED;
        }

        @Override
        public String buildValidationUrl(String baseUri, String oid, String privacyKey) {
            if (privacyKey == null || privacyKey.isBlank()) {
                return "https://gateway.example/api/evs/rest/validations/" + oid;
            }
            return "https://gateway.example/api/evs/rest/validations/" + oid + "?readAccessKey=" + privacyKey;
        }

        @Override
        public String buildValidationPortalUrl(String baseUri, String oid) {
            return "https://portal.example/gazelle/validation-portal/reports/" + oid;
        }

        @Override
        public String buildReportUrl(String baseUri, String oid, String privacyKey) {
            if (privacyKey == null || privacyKey.isBlank()) {
                return "https://gateway.example/reports/" + oid;
            }
            return "https://gateway.example/reports/" + oid + "?readAccessKey=" + privacyKey;
        }

        @Override
        public OffsetDateTime toOffsetDateTime(java.util.Date date) {
            return date == null ? null : OffsetDateTime.now();
        }

        @Override
        public String resolveInputReference(Input input) {
            return null;
        }
    }
}
