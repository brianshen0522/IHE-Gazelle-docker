package net.ihe.gazelle.validation.gateway.evs.business.service;

import net.ihe.gazelle.security.business.Groups;
import net.ihe.gazelle.validation.gateway.evs.business.model.ValidationRequestIdentity;
import net.ihe.gazelle.security.business.GazelleIdentity;
import net.ihe.gazelle.security.business.acl.AccessControlList;
import net.ihe.gazelle.validation.gateway.evs.technical.dto.CallerMetadataDTO;
import net.ihe.gazelle.validation.gateway.evs.technical.dto.EntryPoint;
import net.ihe.gazelle.validation.gateway.evs.technical.dto.HandledObjectDTO;
import net.ihe.gazelle.validation.gateway.evs.technical.dto.OwnerMetadataDTO;
import net.ihe.gazelle.validation.gateway.evs.technical.dto.SharingMetadataDTO;
import net.ihe.gazelle.validation.gateway.evs.technical.dto.ValidationDTO;
import net.ihe.gazelle.validation.gateway.evs.technical.dto.ValidationReportRefDTO;
import net.ihe.gazelle.validation.gateway.evs.technical.dto.ValidationServiceDTO;
import net.ihe.gazelle.validation.gateway.evs.technical.dto.ValidationStatus;
import net.ihe.gazelle.validation.v2.api.business.Input;
import net.ihe.gazelle.validation.v2.api.business.report.ValidationMethod;
import net.ihe.gazelle.validation.v2.api.business.report.ValidationReport;
import net.ihe.gazelle.validation.v2.api.business.report.ValidationTestResult;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

public class ValidationPresentation {
    private final TestRunMapper runMapper;
    private final String datahouseBaseUrl;
    private final String validationPortalBaseUrl;

    public ValidationPresentation(String datahouseBaseUrl, String validationPortalBaseUrl) {
        this.runMapper = new TestRunMapper();
        this.datahouseBaseUrl = normalizeBaseUrl(datahouseBaseUrl);
        this.validationPortalBaseUrl = normalizeBaseUrl(validationPortalBaseUrl);
    }

    public ValidationDTO enrichValidationMetadata(ValidationDTO validation, AccessControlList acl, GazelleIdentity identity) {
        ValidationDTO normalized = runMapper.normalizeRequest(validation);
        SharingMetadataDTO sharing = new SharingMetadataDTO();
        sharing.setPrivate(!acl.isPublic());
        normalized.setSharing(sharing);
        normalized.setOwner(toOwnerMetadata(acl, identity));
        CallerMetadataDTO caller = new CallerMetadataDTO();
        caller.setEntryPoint(EntryPoint.WS);
        normalized.setCaller(caller);
        if (normalized.getDate() == null) {
            normalized.setDate(OffsetDateTime.now());
        }
        return normalized;
    }

    public ValidationDTO toValidationResponse(String oid,
                                              ValidationReport report,
                                              AccessControlList acl,
                                              String baseUri,
                                              String privacyKey) {
        ValidationDTO response = new ValidationDTO();
        response.setOid(oid);
        response.setStatus(toValidationStatus(report));
        response.setValidationService(toValidationService(report != null ? report.getValidationMethod() : null));
        response.setObjects(toHandledObjects(report != null ? report.getInputs() : List.of()));
        String resolvedKey = privacyKey;
        if (resolvedKey == null || resolvedKey.isBlank()) {
            resolvedKey = acl != null && !acl.isPublic() ? acl.getReadAccessKey() : null;
        }
        response.setValidationReportRef(new ValidationReportRefDTO(buildReportUrl(baseUri, oid, resolvedKey)));
        response.setSharing(toSharingMetadata(acl));
        response.setOwner(toOwnerMetadata(acl));
        response.setCaller(toCallerMetadata());
        response.setDate(toOffsetDateTime(report != null ? report.getDateTime() : null));
        return response;
    }

    public ValidationStatus toValidationStatus(ValidationReport report) {
        ValidationTestResult result = report != null ? report.getOverallResult() : null;
        if (result == null) {
            return ValidationStatus.DONE_UNDEFINED;
        }
        return switch (result) {
            case PASSED -> ValidationStatus.DONE_PASSED;
            case FAILED -> ValidationStatus.DONE_FAILED;
            default -> ValidationStatus.DONE_UNDEFINED;
        };
    }

    public String buildValidationUrl(String baseUri, String oid, String privacyKey) {
        String base = normalizeBaseUrl(baseUri) + "/evs/rest/validations/" + oid;
        if (privacyKey == null || privacyKey.isBlank()) {
            return base;
        }
        return base + "?readAccessKey=" + privacyKey;
    }

    public String buildReportUrl(String baseUri, String oid, String privacyKey) {
        String url = normalizeBaseUrl(baseUri) + "/evs/rest/validations/" + oid + "/report";
        return url + ((privacyKey == null || privacyKey.isBlank()) ? "" : "?readAccessKey=" + privacyKey);
    }

    public String buildValidationPortalUrl(String baseUri, String oid) {
        String portalBase = validationPortalBaseUrl.isBlank()
              ? normalizeBaseUrl(baseUri) + "/validation-portal"
              : validationPortalBaseUrl;
        return portalBase + "/reports/" + oid;
    }

    public OffsetDateTime toOffsetDateTime(java.util.Date date) {
        if (date == null) {
            return null;
        }
        return OffsetDateTime.ofInstant(date.toInstant(), ZoneOffset.UTC);
    }

    public String resolveInputReference(Input input) {
        if (input == null) {
            return null;
        }
        String location = input.getLocation();
        if (location != null && !location.isBlank()) {
            if (location.startsWith("/") && !datahouseBaseUrl.isBlank()) {
                return datahouseBaseUrl + location;
            }
            return location;
        }
        if (input.getItemId() != null && !input.getItemId().isBlank() && !datahouseBaseUrl.isBlank()) {
            return datahouseBaseUrl + "/attachments/" + input.getItemId();
        }
        return input.getItemId();
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

    private ValidationServiceDTO toValidationService(ValidationMethod validationMethod) {
        if (validationMethod == null) {
            return null;
        }
        ValidationServiceDTO dto = new ValidationServiceDTO();
        dto.setName(validationMethod.getValidationServiceName());
        dto.setValidator(validationMethod.getValidationProfileID());
        return dto;
    }

    private List<HandledObjectDTO> toHandledObjects(List<Input> inputs) {
        List<HandledObjectDTO> objects = new ArrayList<>();
        if (inputs == null) {
            return objects;
        }
        for (Input input : inputs) {
            if (input == null) {
                continue;
            }
            HandledObjectDTO dto = new HandledObjectDTO();
            dto.setRole(input.getId());
            dto.setContent(input.getContent());
            dto.setRef(resolveInputReference(input));
            objects.add(dto);
        }
        return objects;
    }

    private SharingMetadataDTO toSharingMetadata(AccessControlList acl) {
        SharingMetadataDTO sharing = new SharingMetadataDTO();
        if (acl == null) {
            sharing.setPrivate(false);
            return sharing;
        }
        sharing.setPrivate(!acl.isPublic());
        return sharing;
    }

    private OwnerMetadataDTO toOwnerMetadata(AccessControlList acl) {
        return toOwnerMetadata(acl, null);
    }

    private OwnerMetadataDTO toOwnerMetadata(AccessControlList acl, GazelleIdentity identity) {
        OwnerMetadataDTO owner = new OwnerMetadataDTO();
        if (acl == null || acl.getOwners().isEmpty()) {
            owner.setUsername(Groups.ROLE_ADMIN);
            return owner;
        }
        String username = acl.getOwners().iterator().next();
        owner.setUsername(username);
        if (!Groups.ROLE_ADMIN.equals(username)
              && identity != null
              && identity.isAuthenticated()
              && !isMachineToMachine(identity)
              && username.equals(identity.getId())) {
            owner.setOrganization(identity.getOrganizationId());
        }
        return owner;
    }

    private CallerMetadataDTO toCallerMetadata() {
        CallerMetadataDTO caller = new CallerMetadataDTO();
        caller.setEntryPoint(EntryPoint.WS);
        return caller;
    }

    private boolean isMachineToMachine(GazelleIdentity identity) {
        return identity instanceof ValidationRequestIdentity requestIdentity && requestIdentity.isMachineToMachine();
    }
}
