package net.ihe.gazelle.validation.gateway.evs.technical.service;

import com.kereval.gazelle.datahouse.api.business.record.Item;
import net.ihe.gazelle.security.business.Authz;
import net.ihe.gazelle.security.business.BaseGazelleIdentity;
import net.ihe.gazelle.security.business.GazelleIdentity;
import net.ihe.gazelle.security.business.Groups;
import net.ihe.gazelle.security.business.acl.AccessControlList;
import net.ihe.gazelle.security.technical.acl.ReadAccessKeyGeneratorImpl;
import net.ihe.gazelle.validation.gateway.evs.business.model.ValidationRequestIdentity;
import net.ihe.gazelle.validation.gateway.evs.business.exception.ForbiddenException;
import net.ihe.gazelle.validation.gateway.evs.business.exception.UnauthorizedException;
import net.ihe.gazelle.validation.gateway.evs.business.model.ValidationServiceValidationResult;
import net.ihe.gazelle.validation.gateway.evs.business.service.ValidationAccessPolicy;
import net.ihe.gazelle.validation.gateway.evs.business.service.ValidationExecutionService;
import net.ihe.gazelle.validation.gateway.evs.business.service.ValidationPresentation;
import net.ihe.gazelle.validation.gateway.evs.business.service.ItemTransformationService;
import net.ihe.gazelle.validation.gateway.evs.business.service.TestRunMapper;
import net.ihe.gazelle.validation.gateway.evs.technical.dto.EntryPoint;
import net.ihe.gazelle.validation.gateway.evs.technical.dto.HandledObjectDTO;
import net.ihe.gazelle.validation.gateway.evs.technical.dto.ValidationDTO;
import net.ihe.gazelle.validation.gateway.evs.technical.dto.ValidationServiceDTO;
import net.ihe.gazelle.validation.gateway.evs.technical.dto.ValidationStatus;
import net.ihe.gazelle.validation.v2.api.business.Input;
import net.ihe.gazelle.validation.v2.api.business.report.ValidationMethod;
import net.ihe.gazelle.validation.v2.api.business.report.ValidationReport;
import net.ihe.gazelle.validation.v2.api.business.report.ValidationTestResult;
import net.ihe.gazelle.maestro.api.business.property.ByteArrayItemProperty;
import net.ihe.gazelle.maestro.api.business.testreport.StepRunReport;
import net.ihe.gazelle.maestro.api.business.testreport.TestReport;
import net.ihe.gazelle.maestro.api.business.testreport.TestRunReport;
import net.ihe.gazelle.errorhandling.business.UnexpectedError;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EvsValidationServiceTest {

    @Test
    @DisplayName("VAL-022 exposes validated file content references for EVS client")
    void val022_exposesValidatedFileReferences() {
        LegacyCompatibilityFacade service = newService("https://datahouse.example");
        ValidationReport report = new ValidationReport();
        report.setInputs(List.of(
              input("file-1", "alpha".getBytes(), "/items/123", null),
              input("file-2", "beta".getBytes(), "https://external.example/file", null),
              input("file-3", "gamma".getBytes(), null, "att-7")
        ));

        ValidationDTO response = service.toValidationResponse("oid-22", report, publicAcl(), "https://gateway.example/api/", null);

        assertThat(response.getObjects(), hasSize(3));
        assertThat(response.getObjects().get(0).getRole(), is("file-1"));
        assertThat(response.getObjects().get(0).getRef(), is("https://datahouse.example/items/123"));
        assertArrayEquals("alpha".getBytes(), response.getObjects().get(0).getContent());
        assertThat(response.getObjects().get(1).getRef(), is("https://external.example/file"));
        assertThat(response.getObjects().get(2).getRef(), is("https://datahouse.example/attachments/att-7"));
    }

    @Test
    @DisplayName("VAL-037 maps validation service metadata onto the EVS response")
    void val037_mapsValidationServiceMetadata() {
        LegacyCompatibilityFacade service = newService("https://datahouse.example");
        ValidationMethod method = new ValidationMethod();
        method.setValidationServiceName("evs-service");
        method.setValidationProfileID("profile-42");
        ValidationReport report = new ValidationReport();
        report.setValidationMethod(method);

        ValidationDTO response = service.toValidationResponse("oid-37", report, publicAcl(), "https://gateway.example/api/", null);

        ValidationServiceDTO validationService = response.getValidationService();
        assertThat(validationService, notNullValue());
        assertThat(validationService.getName(), is("evs-service"));
        assertThat(validationService.getValidator(), is("profile-42"));
    }

    @Test
    @DisplayName("VAL-014 preserves summary status mapping for EVS compatibility")
    void val014_mapsSummaryStatus() {
        LegacyCompatibilityFacade service = newService("https://datahouse.example");
        ValidationReport report = new ValidationReport();
        report.setOverallResult(ValidationTestResult.PASSED);
        assertThat(service.toValidationStatus(report), is(ValidationStatus.DONE_PASSED));

        report.setOverallResult(ValidationTestResult.FAILED);
        assertThat(service.toValidationStatus(report), is(ValidationStatus.DONE_FAILED));

        report.setOverallResult(null);
        assertThat(service.toValidationStatus(report), is(ValidationStatus.DONE_UNDEFINED));
    }

    @Test
    @DisplayName("VAL-049 exposes a report reference URL for overview rendering")
    void val049_exposesReportReference() {
        LegacyCompatibilityFacade service = newService("https://datahouse.example");
        ValidationReport report = new ValidationReport();
        ValidationDTO response = service.toValidationResponse("oid-49", report, publicAcl(), "https://gateway.example/api/", null);

        assertThat(response.getValidationReportRef(), notNullValue());
        assertThat(response.getValidationReportRef().getLocation(),
              is("https://gateway.example/api/evs/rest/validations/oid-49/report"));
    }

    @Test
    @DisplayName("VAL-036 enforces ACL rules for validation reports")
    void val036_appliesAclRules() {
        LegacyCompatibilityFacade service = newService("https://datahouse.example");
        AccessControlList acl = new AccessControlList();
        acl.setPublic(false);
        acl.setReadAccessKey("secret");
        acl.setOwners(Set.of("owner-1"));
        acl.setEditors(Set.of());
        acl.setReaders(Set.of());

        BaseGazelleIdentity unauthenticated = BaseGazelleIdentity.unauthenticatedIdentity();
        assertDoesNotThrow(() -> service.assertCanAccess(acl, "secret", unauthenticated));
        assertDoesNotThrow(() -> service.assertCanAccess(acl, null, authenticatedIdentity("owner-1")));

        assertThrows(UnauthorizedException.class,
              () -> service.assertCanAccess(acl, null, unauthenticated));
        GazelleIdentity other = authenticatedIdentity("other");
        assertThrows(ForbiddenException.class,
              () -> service.assertCanAccess(acl, null, other));
    }

    @Test
    @DisplayName("VAL-014 keeps report timestamp in UTC for summary display")
    void val014_keepsReportDateInUtc() {
        LegacyCompatibilityFacade service = newService("https://datahouse.example");
        Date reportDate = new Date(0);
        ValidationReport report = new ValidationReport();
        report.setDateTime(reportDate);

        ValidationDTO response = service.toValidationResponse("oid-14", report, publicAcl(), "https://gateway.example/api/", null);

        assertThat(response.getDate(), equalTo(OffsetDateTime.ofInstant(reportDate.toInstant(), ZoneOffset.UTC)));
    }

    @Test
    @DisplayName("VAL-049 extracts report locations from Maestro outputs")
    void val049_extractsReportLocationFromOutputs() {
        LegacyCompatibilityFacade service = newService("https://datahouse.example");
        ByteArrayItemProperty output = new ByteArrayItemProperty();
        output.setName("VALIDATION_REPORT");
        output.setReference("https://gateway.example/report/oid-49");

        StepRunReport stepRunReport = new StepRunReport();
        stepRunReport.addOutput(output);

        TestRunReport runReport = new TestRunReport();
        runReport.addStepRunReport(stepRunReport);

        TestReport report = new TestReport();
        report.addTestRunReport(runReport);

        String location = service.extractValidationReportLocation(report);
        assertThat(location, is("https://gateway.example/report/oid-49"));
        assertThat(service.extractValidationReportLocation(new TestReport()), nullValue());
    }

    @Test
    @DisplayName("VAL-049 extracts report locations when Maestro returns plural output key")
    void val049_extractsReportLocationFromPluralOutputName() {
        LegacyCompatibilityFacade service = newService("https://datahouse.example");
        ByteArrayItemProperty output = new ByteArrayItemProperty();
        output.setName("VALIDATION_REPORTS");
        output.setReference("https://gateway.example/report/oid-49-bis");

        StepRunReport stepRunReport = new StepRunReport();
        stepRunReport.addOutput(output);

        TestRunReport runReport = new TestRunReport();
        runReport.addStepRunReport(stepRunReport);

        TestReport report = new TestReport();
        report.addTestRunReport(runReport);

        assertThat(service.extractValidationReportLocation(report), is("https://gateway.example/report/oid-49-bis"));
    }

    @Test
    @DisplayName("VAL-049 derives the report identifier from different locations")
    void val049_extractsReportIdFromLocation() {
        LegacyCompatibilityFacade service = newService("https://datahouse.example");
        assertThat(service.extractReportId("https://gateway.example/report/oid-49"), is("oid-49"));
        assertThat(service.extractReportId("raw-id"), is("raw-id"));
        assertThat(service.extractReportId("invalid uri/with-space"), is("with-space"));
    }

    @Test
    @DisplayName("VAL-049 reports step unexpected error when validation output is missing")
    void val049_usesStepUnexpectedErrorWhenReportOutputMissing() {
        LegacyCompatibilityFacade service = newService("https://datahouse.example");

        UnexpectedError unexpectedError = new UnexpectedError();
        unexpectedError.setName("NoSuchElementException");
        unexpectedError.setMessage("Unexpected error while running step: java.util.NoSuchElementException: No service with name string");

        StepRunReport stepRunReport = new StepRunReport();
        stepRunReport.addUnexpectedError(unexpectedError);

        TestRunReport runReport = new TestRunReport();
        runReport.addStepRunReport(stepRunReport);

        TestReport report = new TestReport();
        report.addTestRunReport(runReport);

        assertThat(service.resolveExecutionFailureMessage(report),
              is("Unexpected error while running step: java.util.NoSuchElementException: No service with name string"));
    }

    @Test
    @DisplayName("VAL-036 builds ACLs private-by-default for authenticated identities")
    void val036_buildsAccessControlLists() {
        LegacyCompatibilityFacade service = newService("https://datahouse.example");

        // anonymous -> public, no key
        AccessControlList publicAnon = service.buildAccessControlList(BaseGazelleIdentity.unauthenticatedIdentity());
        assertThat(publicAnon.isPublic(), is(true));
        assertThat(publicAnon.getReadAccessKey(), nullValue());
        assertThat(publicAnon.getOwners(), equalTo(Set.of(Groups.ROLE_ADMIN)));

        // authenticated individual -> private, generated key, monitor + TSM + org viewers
        AccessControlList privateAuth = service.buildAccessControlList(authenticatedIdentity("user-42",
              Set.of("role:user", Groups.PREFIX_ORGANIZATION + "acme")));
        assertThat(privateAuth.isPublic(), is(false));
        assertThat(privateAuth.getOwners(), equalTo(Set.of("user-42")));
        assertThat(privateAuth.getReadAccessKey(), notNullValue());
        assertThat(privateAuth.getReadAccessKey().length(), is(8));
        assertThat(privateAuth.getReaders(), equalTo(Set.of(Groups.ROLE_MONITOR, Groups.ROLE_PROJECT_ADMIN, Groups.ROLE_TESTING_SESSION_MANAGER,
              Groups.PREFIX_ORGANIZATION + "acme")));
    }

    @Test
    @DisplayName("VAL-091 assigns Gazelle administrator as owner for m2m validations")
    void val091_buildsAccessControlListForM2mCaller() {
        LegacyCompatibilityFacade service = newService("https://datahouse.example");

        AccessControlList acl = service.buildAccessControlList(machineToMachineIdentity("m2m-client",
              Set.of(Groups.ROLE_TESTING_SESSION_MANAGER, Groups.PREFIX_ORGANIZATION + "ignored")));

        assertThat(acl.isPublic(), is(false));
        assertThat(acl.getOwners(), equalTo(Set.of(Groups.ROLE_ADMIN)));
        assertThat(acl.getReadAccessKey(), notNullValue());
        assertThat(acl.getReaders(), equalTo(Set.of(Groups.ROLE_MONITOR, Groups.ROLE_PROJECT_ADMIN, Groups.ROLE_TESTING_SESSION_MANAGER)));
    }

    @Test
    @DisplayName("VAL-037 enriches validation metadata with sharing, owner and caller")
    void val037_enrichesValidationMetadata() {
        LegacyCompatibilityFacade service = newService("https://datahouse.example");
        ValidationDTO request = new ValidationDTO();
        AccessControlList acl = new AccessControlList();
        acl.setPublic(false);
        acl.setOwners(Set.of("user-37"));
        ValidationDTO enriched = service.enrichValidationMetadata(request, acl, authenticatedIdentity("user-37"));

        assertThat(enriched.getSharing().getPrivate(), is(true));
        assertThat(enriched.getOwner().getUsername(), is("user-37"));
        assertThat(enriched.getCaller().getEntryPoint(), is(EntryPoint.WS));
        assertThat(enriched.getDate(), notNullValue());
    }

    @Test
    @DisplayName("VAL-091 exposes Gazelle administrator as owner for m2m metadata")
    void val091_enrichesValidationMetadataForM2m() {
        LegacyCompatibilityFacade service = newService("https://datahouse.example");
        ValidationDTO request = new ValidationDTO();
        AccessControlList acl = new AccessControlList();
        acl.setPublic(false);
        acl.setOwners(Set.of(Groups.ROLE_ADMIN));

        ValidationDTO enriched = service.enrichValidationMetadata(request, acl,
              machineToMachineIdentity("m2m-client", Set.of(Groups.ROLE_TESTING_SESSION_MANAGER)));

        assertThat(enriched.getOwner().getUsername(), is(Groups.ROLE_ADMIN));
        assertThat(enriched.getOwner().getOrganization(), nullValue());
    }

    @Test
    @DisplayName("VAL-037 rejects invalid validation requests before execution")
    void val037_validatesRequests() {
        LegacyCompatibilityFacade service = newService("https://datahouse.example");

        ValidationServiceValidationResult missingRequest = service.validateRequest(null);
        assertThat(missingRequest.isValid(), is(false));
        assertThat(missingRequest.getMessage(), is("missing-validation-request"));

        ValidationDTO missingService = new ValidationDTO();
        missingService.setObjects(List.of(new HandledObjectDTO()));
        ValidationServiceValidationResult missingServiceResult = service.validateRequest(missingService);
        assertThat(missingServiceResult.getMessage(), is("missing-validation-service"));

        ValidationDTO missingObjects = new ValidationDTO();
        ValidationServiceDTO svcDto = new ValidationServiceDTO();
        svcDto.setName("svc");
        svcDto.setValidator("validator");
        missingObjects.setValidationService(svcDto);
        ValidationServiceValidationResult missingObjectsResult = service.validateRequest(missingObjects);
        assertThat(missingObjectsResult.getMessage(), is("missing-validation-object"));

        ValidationDTO missingContent = new ValidationDTO();
        missingContent.setValidationService(svcDto);
        HandledObjectDTO emptyObj = new HandledObjectDTO();
        emptyObj.setContent(new byte[0]);
        missingContent.setObjects(List.of(emptyObj));
        ValidationServiceValidationResult missingContentResult = service.validateRequest(missingContent);
        assertThat(missingContentResult.getMessage(), is("missing-validation-object-content"));

        ValidationDTO valid = new ValidationDTO();
        valid.setValidationService(svcDto);
        HandledObjectDTO filled = new HandledObjectDTO();
        filled.setContent("byte".getBytes());
        valid.setObjects(List.of(filled));
        ValidationServiceValidationResult validResult = service.validateRequest(valid);
        assertThat(validResult.isValid(), is(true));
    }

    @Test
    @DisplayName("VAL-049 serializes XML report payload when requested")
    void val049_serializesXmlReportPayload() {
        LegacyCompatibilityFacade service = newService("https://datahouse.example");
        String payload = service.toReportPayload(itemWithMinimalReport(), "application/xml");

        assertThat(payload, containsString("validationOverview"));
        assertThat(payload, containsString("validationOverallResult"));
    }

    @Test
    @DisplayName("VAL-049 serializes JSON report payload using EVSClient report shape")
    void val049_serializesJsonReportPayloadUsingLegacyShape() {
        LegacyCompatibilityFacade service = newService("https://datahouse.example");
        String payload = service.toReportPayload(itemWithMinimalReport(), "application/json");

        assertThat(payload, containsString("\"validationOverview\""));
        assertThat(payload, containsString("\"validationOverallResult\""));
    }

    private static LegacyCompatibilityFacade newService(String baseUrl) {
        return new LegacyCompatibilityFacade(baseUrl);
    }

    private static AccessControlList publicAcl() {
        AccessControlList acl = new AccessControlList();
        acl.setPublic(true);
        acl.setOwners(Set.of(Groups.ROLE_ADMIN));
        acl.setEditors(Set.of());
        acl.setReaders(Set.of());
        return acl;
    }

    private static Input input(String id, byte[] content, String location, String itemId) {
        Input input = new Input();
        input.setId(id);
        input.setContent(content);
        input.setLocation(location);
        input.setItemId(itemId);
        return input;
    }

    private static GazelleIdentity authenticatedIdentity(String id) {
        return authenticatedIdentity(id, Set.of("org"));
    }

    private static GazelleIdentity authenticatedIdentity(String id, Set<String> groups) {
        return new BaseGazelleIdentity(() -> id)
              .setId(id)
              .setName(id)
              .setGroups(groups);
    }

    private static GazelleIdentity machineToMachineIdentity(String id, Set<String> groups) {
        return new TestMachineToMachineIdentity(id, groups);
    }

    private static Item itemWithMinimalReport() {
        ValidationReport report = new ValidationReport();
        report.setOverallResult(ValidationTestResult.PASSED);
        String content;
        try {
            content = new com.fasterxml.jackson.databind.ObjectMapper()
                  .writeValueAsString(new net.ihe.gazelle.validation.v2.api.technical.dto.report.ValidationReportDTO(report));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        Item item = new Item();
        item.setContent(content);
        return item;
    }

    private static class LegacyCompatibilityFacade {

        private final ValidationPresentation presentation;
        private final ValidationAccessPolicy accessPolicy;
        private final ValidationReportServiceImpl reportService;
        private final ItemTransformationService itemTransformationService;
        private final ValidationExecutionService executionService;

        LegacyCompatibilityFacade(String baseUrl) {
            this.presentation = new ValidationPresentation(baseUrl, "");
            this.accessPolicy = new ValidationAccessPolicy(8, new ReadAccessKeyGeneratorImpl(), new DefaultTestAuthz());
            this.reportService = new ValidationReportServiceImpl(null);
            this.itemTransformationService = new ItemTransformationService();
            this.executionService = new ValidationExecutionService(
                  accessPolicy,
                  presentation,
                  new TestRunMapper(),
                  null,
                  reportService,
                  null,
                  null
            );
        }

        ValidationDTO toValidationResponse(String oid,
                                           ValidationReport report,
                                           AccessControlList acl,
                                           String baseUri,
                                           String privacyKey) {
            return presentation.toValidationResponse(oid, report, acl, baseUri, privacyKey);
        }

        ValidationStatus toValidationStatus(ValidationReport report) {
            return presentation.toValidationStatus(report);
        }

        void assertCanAccess(AccessControlList acl, String privacyKey, GazelleIdentity identity) {
            accessPolicy.assertCanAccess(acl, privacyKey, identity);
        }

        String extractValidationReportLocation(TestReport report) {
            return reportService.extractValidationReportLocation(report);
        }

        String extractReportId(String reportLocation) {
            return reportService.extractReportId(reportLocation);
        }

        String resolveExecutionFailureMessage(TestReport report) {
            return reportService.resolveExecutionFailureMessage(report);
        }

        AccessControlList buildAccessControlList(GazelleIdentity identity) {
            return accessPolicy.buildAccessControlList(identity);
        }

        ValidationDTO enrichValidationMetadata(ValidationDTO validation, AccessControlList acl, GazelleIdentity identity) {
            return presentation.enrichValidationMetadata(validation, acl, identity);
        }

        ValidationServiceValidationResult validateRequest(ValidationDTO validationRequest) {
            return executionService.validateRequest(validationRequest);
        }

        String toReportPayload(Item item, String accept) {
            return itemTransformationService.toReportPayload(item, accept);
        }
    }

    private static class DefaultTestAuthz implements Authz {
        @Override
        public boolean isAuthorized(GazelleIdentity identity, String action, Object... context) {
            if (context.length == 0 || !(context[0] instanceof net.ihe.gazelle.security.business.ProtectedResource resource)) {
                return false;
            }
            AccessControlList acl = resource.getAccessControlList();
            return identity != null && identity.isAuthenticated() && (
                  acl.isAmongstTheOwners(identity) ||
                        acl.isAmongstTheReaders(identity) ||
                        acl.isAmongstTheEditors(identity));
        }

        @Override
        public <C extends java.util.Collection<? extends Object>> C filterOutUnauthorized(GazelleIdentity identity,
                                                                                           String action,
                                                                                           C collection) {
            return collection;
        }
    }

    private static class TestMachineToMachineIdentity extends BaseGazelleIdentity implements ValidationRequestIdentity {

        TestMachineToMachineIdentity(String id, Set<String> groups) {
            super(() -> id);
            setId(id);
            setName(id);
            setGroups(new LinkedHashSet<>(groups));
        }

        @Override
        public boolean isMachineToMachine() {
            return true;
        }
    }
}
