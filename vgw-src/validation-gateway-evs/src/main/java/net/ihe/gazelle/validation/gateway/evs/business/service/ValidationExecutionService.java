package net.ihe.gazelle.validation.gateway.evs.business.service;

import net.ihe.gazelle.maestro.api.business.testreport.TestReport;
import net.ihe.gazelle.maestro.api.business.testrun.TestRun;
import net.ihe.gazelle.search.api.ReadService;
import net.ihe.gazelle.security.business.GazelleIdentity;
import net.ihe.gazelle.security.business.acl.AccessControlList;
import net.ihe.gazelle.validation.gateway.business.ProfileReadId;
import net.ihe.gazelle.validation.gateway.evs.business.exception.InvalidValidationRequestException;
import net.ihe.gazelle.validation.gateway.evs.business.exception.ValidationExecutionException;
import net.ihe.gazelle.validation.gateway.evs.business.model.ValidationCreationResult;
import net.ihe.gazelle.validation.gateway.evs.business.model.ValidationRequest;
import net.ihe.gazelle.validation.gateway.evs.business.model.ValidationServiceValidationResult;
import net.ihe.gazelle.validation.gateway.evs.technical.dto.ValidationDTO;
import net.ihe.gazelle.validation.gateway.evs.business.model.AsyncReport;
import net.ihe.gazelle.validation.v2.api.business.profile.SupportedInput;
import net.ihe.gazelle.validation.v2.api.business.profile.ValidationProfile;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class ValidationExecutionService {

    private final ValidationAccessPolicy accessPolicy;
    private final ValidationPresentation presentation;
    private final TestRunMapper runMapper;
    private final ValidationExecutionGateway executionGateway;
    private final ValidationReportService reportService;
    private final AsyncReportState asyncReportState;
    private final ReadService<ProfileReadId, ValidationProfile> readProfileService;

    public ValidationExecutionService(ValidationAccessPolicy accessPolicy,
                                      ValidationPresentation presentation,
                                      TestRunMapper runMapper,
                                      ValidationExecutionGateway executionGateway,
                                      ValidationReportService reportService,
                                      AsyncReportState asyncReportState,
                                      ReadService<ProfileReadId, ValidationProfile> readProfileService) {
        this.accessPolicy = accessPolicy;
        this.presentation = presentation;
        this.runMapper = runMapper;
        this.executionGateway = executionGateway;
        this.reportService = reportService;
        this.asyncReportState = asyncReportState;
        this.readProfileService = readProfileService;
    }

    public ValidationCreationResult createValidation(ValidationDTO validationRequest,
                                                     GazelleIdentity identity,
                                                     boolean async,
                                                     String baseUri) {
        ValidationServiceValidationResult validationResult = validateRequest(validationRequest);
        if (!validationResult.isValid()) {
            throw new InvalidValidationRequestException(validationResult.getMessage());
        }
        AccessControlList acl = accessPolicy.buildAccessControlList(identity);
        ValidationDTO baseValidation = presentation.enrichValidationMetadata(validationRequest, acl, identity);
        String profileInputName = resolveProfileInputName(baseValidation, identity);
        if (async) {
            String requestOid = runMapper.generateOid();
            TestRun testRun = runMapper.toTestRun(requestOid, baseValidation, acl, profileInputName);
            CompletableFuture<AsyncReport> future = CompletableFuture.supplyAsync(() -> {
                TestReport report = executionGateway.executeValidation(testRun);
                String reportLocation = reportService.extractValidationReportLocation(report);
                String reportOid = requireReportId(report, reportLocation);
                return new AsyncReport(reportLocation, reportOid);
            });
            asyncReportState.register(requestOid, future);
            String validationUrl = presentation.buildValidationUrl(baseUri, requestOid, acl.getReadAccessKey());
            return new ValidationCreationResult(requestOid, validationUrl, null, acl.getReadAccessKey(), true);
        }

        TestRun testRun = runMapper.toTestRun(runMapper.generateOid(), baseValidation, acl, profileInputName);
        TestReport report = executionGateway.executeValidation(testRun);
        String reportLocation = reportService.extractValidationReportLocation(report);
        String reportOid = requireReportId(report, reportLocation);
        String validationUrl = presentation.buildValidationUrl(baseUri, reportOid, acl.getReadAccessKey());
        return new ValidationCreationResult(reportOid, validationUrl, reportLocation, acl.getReadAccessKey(), false);
    }

    public ValidationServiceValidationResult validateRequest(ValidationDTO validationRequest) {
        return validateBusinessRequest(toBusinessRequest(validationRequest));
    }

    private String requireReportId(TestReport report, String reportLocation) {
        String reportOid = reportService.extractReportId(reportLocation);
        if (reportOid == null || reportOid.isBlank()) {
            throw new ValidationExecutionException(reportService.resolveExecutionFailureMessage(report));
        }
        return reportOid;
    }

    private ValidationRequest toBusinessRequest(ValidationDTO validationRequest) {
        if (validationRequest == null) {
            return null;
        }
        ValidationRequest businessRequest = new ValidationRequest();
        businessRequest.setObjectType(validationRequest.getObjectType());
        if (validationRequest.getValidationService() != null) {
            businessRequest
                  .setValidationServiceName(validationRequest.getValidationService().getName())
                  .setValidationServiceValidator(validationRequest.getValidationService().getValidator());
        }
        List<byte[]> contents = new ArrayList<>();
        if (validationRequest.getObjects() != null) {
            validationRequest.getObjects().forEach(object -> {
                if (object != null) {
                    contents.add(object.getContent());
                }
            });
        }
        businessRequest.setObjectContents(contents);
        return businessRequest;
    }

    private ValidationServiceValidationResult validateBusinessRequest(ValidationRequest validationRequest) {
        if (validationRequest == null) {
            return new ValidationServiceValidationResult(false, "missing-validation-request");
        }
        if ((validationRequest.getValidationServiceName() == null
              || validationRequest.getValidationServiceName().isBlank()
              || validationRequest.getValidationServiceValidator() == null
              || validationRequest.getValidationServiceValidator().isBlank()) && (validationRequest.getObjectType() == null || validationRequest.getObjectType().isBlank())) {
                return new ValidationServiceValidationResult(false, "missing-validation-service");
            }

        if (validationRequest.getObjectContents() == null || validationRequest.getObjectContents().isEmpty()) {
            return new ValidationServiceValidationResult(false, "missing-validation-object");
        }
        boolean isContentDefined = validationRequest.getObjectContents().stream()
              .anyMatch(content -> content != null && content.length > 0);
        if (!isContentDefined) {
            return new ValidationServiceValidationResult(false, "missing-validation-object-content");
        }
        return new ValidationServiceValidationResult(true, null);
    }

    private String resolveProfileInputName(ValidationDTO validationRequest, GazelleIdentity identity) {
        if (readProfileService == null || validationRequest == null || validationRequest.getValidationService() == null) {
            return null;
        }
        String serviceName = validationRequest.getValidationService().getName();
        String profileId = validationRequest.getValidationService().getValidator();
        if (serviceName == null || serviceName.isBlank() || profileId == null || profileId.isBlank()) {
            return null;
        }
        try {
            ValidationProfile profile = readProfileService.readObject(new ProfileReadId(profileId, serviceName), identity);
            return extractSingleSupportedInputId(profile);
        } catch (RuntimeException e) {
            return null;
        }
    }

    private String extractSingleSupportedInputId(ValidationProfile profile) {
        if (profile == null || profile.getSupportedInputs() == null) {
            return null;
        }
        List<String> supportedInputIds = profile.getSupportedInputs().stream()
              .filter(Objects::nonNull)
              .map(SupportedInput::getId)
              .filter(Objects::nonNull)
              .map(String::trim)
              .filter(id -> !id.isBlank())
              .distinct()
              .toList();
        return supportedInputIds.size() == 1 ? supportedInputIds.getFirst() : null;
    }
}
