package net.ihe.gazelle.validation.gateway.evs.technical.service;

import net.ihe.gazelle.maestro.api.business.property.ByteArrayProperty;
import net.ihe.gazelle.maestro.api.business.test.Step;
import net.ihe.gazelle.maestro.api.business.testreport.TestReport;
import net.ihe.gazelle.maestro.api.business.testrun.TestRun;
import net.ihe.gazelle.search.api.ReadService;
import net.ihe.gazelle.security.business.Authz;
import net.ihe.gazelle.security.business.GazelleIdentity;
import net.ihe.gazelle.security.technical.acl.ReadAccessKeyGeneratorImpl;
import net.ihe.gazelle.validation.gateway.business.ProfileReadId;
import net.ihe.gazelle.validation.gateway.evs.business.model.ValidationCreationResult;
import net.ihe.gazelle.validation.gateway.evs.business.service.*;
import net.ihe.gazelle.validation.gateway.evs.technical.dto.HandledObjectDTO;
import net.ihe.gazelle.validation.gateway.evs.technical.dto.ValidationDTO;
import net.ihe.gazelle.validation.gateway.evs.technical.dto.ValidationServiceDTO;
import net.ihe.gazelle.validation.v2.api.business.profile.SupportedInput;
import net.ihe.gazelle.validation.v2.api.business.profile.ValidationProfile;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

class ValidationExecutionServiceTest {

    @Test
    @DisplayName("createValidation uses the profile input id when role is absent")
    void createValidationUsesProfileInputIdWhenRoleAbsent() {
        CapturingGateway gateway = new CapturingGateway();
        ValidationExecutionService service = new ValidationExecutionService(
              new ValidationAccessPolicy(8, new ReadAccessKeyGeneratorImpl(), allowAllAuthz()),
              new ValidationPresentation("https://gateway.example", ""),
              new TestRunMapper(),
              gateway,
              new StubValidationReportService(),
              new AsyncReportState(),
              readProfileService(profileWithInputs("document"))
        );

        ValidationCreationResult result = service.createValidation(requestWithoutRole(), null, false, "https://gateway.example");
        Step step = gateway.lastRun.getTest().getSteps().getFirst();

        assertThat(result.getOid(), equalTo("report-oid"));
        assertThat(((ByteArrayProperty) step.getProperty("document")).getValue(), is("${payload.txt}"));
    }

    @Test
    @DisplayName("createValidation falls back to contentToValidate when the profile has multiple inputs")
    void createValidationFallsBackToContentWhenProfileHasMultipleInputs() {
        CapturingGateway gateway = new CapturingGateway();
        ValidationExecutionService service = new ValidationExecutionService(
              new ValidationAccessPolicy(8, new ReadAccessKeyGeneratorImpl(), allowAllAuthz()),
              new ValidationPresentation("https://gateway.example", ""),
              new TestRunMapper(),
              gateway,
              new StubValidationReportService(),
              new AsyncReportState(),
              readProfileService(profileWithInputs("document", "metadata"))
        );

        service.createValidation(requestWithoutRole(), null, false, "https://gateway.example");
        Step step = gateway.lastRun.getTest().getSteps().getFirst();

        assertThat(((ByteArrayProperty) step.getProperty("contentToValidate")).getValue(), is("${payload.txt}"));
    }

    @Test
    @DisplayName("createValidation keeps a Maestro input when the request has no original filename")
    void createValidationKeepsInputWhenOriginalFilenameMissing() {
        CapturingGateway gateway = new CapturingGateway();
        ValidationExecutionService service = new ValidationExecutionService(
              new ValidationAccessPolicy(8, new ReadAccessKeyGeneratorImpl(), allowAllAuthz()),
              new ValidationPresentation("https://gateway.example", ""),
              new TestRunMapper(),
              gateway,
              new StubValidationReportService(),
              new AsyncReportState(),
              readProfileService(profileWithInputs("document"))
        );

        service.createValidation(requestWithoutFilename(), null, false, "https://gateway.example");
        Step step = gateway.lastRun.getTest().getSteps().getFirst();
        ByteArrayProperty runInput = (ByteArrayProperty) gateway.lastRun.getInputs().getFirst();

        assertThat(runInput.getName(), is("inputFile-document"));
        assertThat(((ByteArrayProperty) step.getProperty("document")).getValue(), is("${inputFile-document}"));
    }

    @Test
    @DisplayName("createValidation keeps distinct Maestro inputs when multiple objects have no original filename")
    void createValidationKeepsDistinctInputsWhenOriginalFilenameMissingForMultipleObjects() {
        CapturingGateway gateway = new CapturingGateway();
        ValidationExecutionService service = new ValidationExecutionService(
              new ValidationAccessPolicy(8, new ReadAccessKeyGeneratorImpl(), allowAllAuthz()),
              new ValidationPresentation("https://gateway.example", ""),
              new TestRunMapper(),
              gateway,
              new StubValidationReportService(),
              new AsyncReportState(),
              readProfileService(profileWithInputs("document", "metadata"))
        );

        service.createValidation(requestWithoutFilename("document", "payload", "metadata", "meta"), null, false, "https://gateway.example");
        Step step = gateway.lastRun.getTest().getSteps().getFirst();

        assertThat(((ByteArrayProperty) gateway.lastRun.getInputs().get(0)).getName(), is("inputFile-document"));
        assertThat(((ByteArrayProperty) gateway.lastRun.getInputs().get(1)).getName(), is("inputFile-metadata"));
        assertThat(((ByteArrayProperty) step.getProperty("document")).getValue(), is("${inputFile-document}"));
        assertThat(((ByteArrayProperty) step.getProperty("metadata")).getValue(), is("${inputFile-metadata}"));
    }

    private static ValidationDTO requestWithoutRole() {
        ValidationDTO request = new ValidationDTO();
        ValidationServiceDTO validationService = new ValidationServiceDTO();
        validationService.setName("service-a");
        validationService.setValidator("profile-a");
        request.setValidationService(validationService);

        HandledObjectDTO object = new HandledObjectDTO();
        object.setOriginalFileName("payload.txt");
        object.setContent("payload".getBytes(StandardCharsets.UTF_8));
        request.setObjects(List.of(object));
        return request;
    }

    private static ValidationDTO requestWithoutFilename() {
        return requestWithoutFilename(null, "payload");
    }

    private static ValidationDTO requestWithoutFilename(String firstRole, String firstPayload, String secondRole, String secondPayload) {
        ValidationDTO request = new ValidationDTO();
        ValidationServiceDTO validationService = new ValidationServiceDTO();
        validationService.setName("service-a");
        validationService.setValidator("profile-a");
        request.setValidationService(validationService);

        HandledObjectDTO firstObject = new HandledObjectDTO();
        firstObject.setRole(firstRole);
        firstObject.setContent(firstPayload.getBytes(StandardCharsets.UTF_8));

        HandledObjectDTO secondObject = new HandledObjectDTO();
        secondObject.setRole(secondRole);
        secondObject.setContent(secondPayload.getBytes(StandardCharsets.UTF_8));

        request.setObjects(List.of(firstObject, secondObject));
        return request;
    }

    private static ValidationDTO requestWithoutFilename(String role, String payload) {
        ValidationDTO request = new ValidationDTO();
        ValidationServiceDTO validationService = new ValidationServiceDTO();
        validationService.setName("service-a");
        validationService.setValidator("profile-a");
        request.setValidationService(validationService);

        HandledObjectDTO object = new HandledObjectDTO();
        object.setRole(role);
        object.setContent(payload.getBytes(StandardCharsets.UTF_8));
        request.setObjects(List.of(object));
        return request;
    }

    private static ValidationProfile profileWithInputs(String... inputIds) {
        ValidationProfile profile = new ValidationProfile();
        profile.setSupportedInputs(java.util.Arrays.stream(inputIds)
              .map(id -> new SupportedInput().setId(id))
              .toList());
        return profile;
    }

    private static ReadService<ProfileReadId, ValidationProfile> readProfileService(ValidationProfile profile) {
        return new ReadService<>() {
            @Override
            public ValidationProfile readObject(ProfileReadId id, GazelleIdentity identity) {
                return profile;
            }

            @Override
            public ValidationProfile readObject(ProfileReadId id, String presentationUrl, GazelleIdentity identity) {
                return profile;
            }
        };
    }

    private static Authz allowAllAuthz() {
        return new Authz() {
            @Override
            public boolean isAuthorized(GazelleIdentity identity, String action, Object... context) {
                return true;
            }

            @Override
            public <C extends java.util.Collection<? extends Object>> C filterOutUnauthorized(GazelleIdentity identity,
                                                                                               String action,
                                                                                               C collection) {
                return collection;
            }
        };
    }

    private static class CapturingGateway implements ValidationExecutionGateway {
        private TestRun lastRun;

        @Override
        public TestReport executeValidation(TestRun testRun) {
            this.lastRun = testRun;
            return new TestReport();
        }
    }

    private static class StubValidationReportService implements ValidationReportService {
        @Override
        public com.kereval.gazelle.datahouse.api.business.record.Item readReportItem(String oid) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<net.ihe.gazelle.validation.gateway.evs.business.model.LocatedReportItem> findReportByLegacyOid(String legacyOid) {
            return Optional.empty();
        }

        @Override
        public String extractValidationReportLocation(TestReport report) {
            return "https://reports.example/report-oid";
        }

        @Override
        public String extractReportId(String reportLocation) {
            return "report-oid";
        }

        @Override
        public String resolveExecutionFailureMessage(TestReport report) {
            return "failure";
        }
    }
}
