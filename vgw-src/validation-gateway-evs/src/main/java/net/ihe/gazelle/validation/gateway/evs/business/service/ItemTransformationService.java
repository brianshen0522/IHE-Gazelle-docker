package net.ihe.gazelle.validation.gateway.evs.business.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.kereval.gazelle.datahouse.api.business.record.Item;
import net.ihe.gazelle.evsapi.client.business.response.report.ConstraintPriority;
import net.ihe.gazelle.evsapi.client.business.response.report.ConstraintValidation;
import net.ihe.gazelle.evsapi.client.business.response.report.Metadata;
import net.ihe.gazelle.evsapi.client.business.response.report.SeverityLevel;
import net.ihe.gazelle.evsapi.client.business.response.report.UnexpectedError;
import net.ihe.gazelle.evsapi.client.business.response.report.ValidationCounters;
import net.ihe.gazelle.evsapi.client.business.response.report.ValidationOverview;
import net.ihe.gazelle.evsapi.client.business.response.report.ValidationReport;
import net.ihe.gazelle.evsapi.client.business.response.report.ValidationSubReport;
import net.ihe.gazelle.evsapi.client.business.response.report.ValidationTestResult;
import net.ihe.gazelle.validation.v2.api.business.report.AssertionReport;
import net.ihe.gazelle.validation.v2.api.technical.dto.report.ValidationReportDTO;

import java.util.ArrayList;
import java.util.List;

public class ItemTransformationService {

    private final ObjectMapper jsonMapper;
    private final XmlMapper xmlMapper;

    public ItemTransformationService() {
        this.jsonMapper = new ObjectMapper()
              .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.xmlMapper = new XmlMapper();
        this.xmlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public net.ihe.gazelle.validation.v2.api.business.report.ValidationReport readReport(Item item) {
        if (item == null || item.getContent() == null || item.getContent().isBlank()) {
            throw new IllegalStateException("Validation report content is empty");
        }
        try {
            ValidationReportDTO dto = jsonMapper.readValue(item.getContent(), ValidationReportDTO.class);
            return dto.getBusinessObject();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse validation report content", e);
        }
    }

    public String toReportPayload(Item item, String accept) {
        ValidationReport legacyReport = toLegacyValidationReport(readReport(item));
        if (accept != null && accept.contains("xml")) {
            try {
                return xmlMapper.writeValueAsString(legacyReport);
            } catch (Exception e) {
                throw new IllegalStateException("Failed to serialize validation report as XML", e);
            }
        }
        try {
            return jsonMapper.writeValueAsString(legacyReport);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize validation report as JSON", e);
        }
    }

    ValidationReport toLegacyValidationReport(net.ihe.gazelle.validation.v2.api.business.report.ValidationReport report) {
        ValidationReport legacy = new ValidationReport();
        if (report == null) {
            return legacy;
        }
        legacy.setUuid(report.getUuid());
        legacy.setValidationOverview(toLegacyOverview(report));
        legacy.setCounters(toLegacyCounters(report.getCounters()));
        legacy.setSubReport(toLegacySubReports(report.getReports()));
        return legacy;
    }

    private ValidationOverview toLegacyOverview(net.ihe.gazelle.validation.v2.api.business.report.ValidationReport report) {
        ValidationOverview overview = new ValidationOverview();
        overview.setDisclaimer(report.getDisclaimer());
        overview.setValidationDateTime(report.getDateTime());
        overview.setValidationOverallResult(toLegacyTestResult(report.getOverallResult()));
        if (report.getValidationMethod() != null) {
            overview.setValidationServiceName(report.getValidationMethod().getValidationServiceName());
            overview.setValidationServiceVersion(report.getValidationMethod().getValidationServiceVersion());
            overview.setValidatorID(report.getValidationMethod().getValidationProfileID());
            overview.setValidatorVersion(report.getValidationMethod().getValidationProfileVersion());
            overview.setValidatorName(report.getValidationMethod().getValidationProfileID());
        }
        overview.setAdditionalMetadata(toLegacyMetadata(report.getAdditionalMetadata()));
        return overview;
    }

    private List<Metadata> toLegacyMetadata(List<net.ihe.gazelle.validation.v2.api.business.report.Metadata> metadata) {
        List<Metadata> result = new ArrayList<>();
        if (metadata == null) {
            return result;
        }
        for (net.ihe.gazelle.validation.v2.api.business.report.Metadata entry : metadata) {
            if (entry == null) {
                continue;
            }
            result.add(new Metadata()
                  .setName(entry.getName())
                  .setValue(entry.getValue()));
        }
        return result;
    }

    private ValidationCounters toLegacyCounters(net.ihe.gazelle.validation.v2.api.business.report.ValidationCounters counters) {
        ValidationCounters result = new ValidationCounters();
        if (counters == null) {
            return result;
        }
        result.setNumberOfConstraints(counters.getNumberOfAssertions());
        result.setFailedWithInfoNumber(counters.getNumberOfFailedWithInfos());
        result.setNumberOfWarnings(counters.getNumberOfFailedWithWarnings());
        result.setNumberOfErrors(counters.getNumberOfFailedWithErrors());
        return result;
    }

    private List<ValidationSubReport> toLegacySubReports(List<net.ihe.gazelle.validation.v2.api.business.report.ValidationSubReport> subReports) {
        List<ValidationSubReport> result = new ArrayList<>();
        if (subReports == null) {
            return result;
        }
        for (net.ihe.gazelle.validation.v2.api.business.report.ValidationSubReport subReport : subReports) {
            if (subReport == null) {
                continue;
            }
            ValidationSubReport legacy = new ValidationSubReport();
            legacy.setName(subReport.getName());
            legacy.setStandards(subReport.getStandards());
            legacy.setSubReportResult(toLegacyTestResult(subReport.getSubReportResult()));
            legacy.setSubCounters(toLegacyCounters(subReport.getSubCounters()));
            legacy.setUnexpectedErrors(toLegacyUnexpectedErrors(subReport.getUnexpectedErrors()));
            legacy.setConstraints(toLegacyConstraints(subReport.getAssertionReports()));
            legacy.setSubReport(toLegacySubReports(subReport.getSubReports()));
            result.add(legacy);
        }
        return result;
    }

    private List<ConstraintValidation> toLegacyConstraints(List<AssertionReport> assertions) {
        List<ConstraintValidation> result = new ArrayList<>();
        if (assertions == null) {
            return result;
        }
        for (AssertionReport assertion : assertions) {
            if (assertion == null) {
                continue;
            }
            ConstraintValidation constraint = new ConstraintValidation();
            constraint.setConstraintID(assertion.getAssertionID());
            constraint.setConstraintType(assertion.getAssertionType());
            constraint.setConstraintDescription(assertion.getDescription());
            constraint.setFormalExpression(assertion.getFormalExpression());
            constraint.setValueInValidatedObject(assertion.getSubjectValue());
            constraint.setAssertionIDs(assertion.getRequirementIDs());
            constraint.setTestResult(toLegacyTestResult(assertion.getResult()));
            constraint.setSeverity(toLegacySeverity(assertion.getSeverity()));
            constraint.setPriority(toLegacyPriority(assertion.getPriority()));
            constraint.setLocationInValidatedObject(firstSubjectLocation(assertion));
            List<UnexpectedError> errors = toLegacyUnexpectedErrors(assertion.getUnexpectedErrors());
            for (UnexpectedError error : errors) {
                constraint.addUnexpectedError(error);
            }
            result.add(constraint);
        }
        return result;
    }

    private String firstSubjectLocation(AssertionReport assertion) {
        if (assertion.getSubjectLocations() == null || assertion.getSubjectLocations().isEmpty()) {
            return null;
        }
        return assertion.getSubjectLocations().get(0) != null ? assertion.getSubjectLocations().get(0).getValue() : null;
    }

    private List<UnexpectedError> toLegacyUnexpectedErrors(List<net.ihe.gazelle.validation.v2.api.business.report.UnexpectedError> errors) {
        List<UnexpectedError> result = new ArrayList<>();
        if (errors == null) {
            return result;
        }
        for (net.ihe.gazelle.validation.v2.api.business.report.UnexpectedError error : errors) {
            if (error == null) {
                continue;
            }
            UnexpectedError legacy = new UnexpectedError()
                  .setName(error.getName())
                  .setMessage(error.getMessage());
            if (error.getCause() != null) {
                legacy.setCause(new UnexpectedError()
                      .setName(error.getCause().getName())
                      .setMessage(error.getCause().getMessage()));
            }
            result.add(legacy);
        }
        return result;
    }

    private ValidationTestResult toLegacyTestResult(net.ihe.gazelle.validation.v2.api.business.report.ValidationTestResult result) {
        if (result == null) {
            return ValidationTestResult.UNDEFINED;
        }
        return switch (result) {
            case PASSED -> ValidationTestResult.PASSED;
            case FAILED -> ValidationTestResult.FAILED;
            case UNDEFINED -> ValidationTestResult.UNDEFINED;
        };
    }

    private SeverityLevel toLegacySeverity(net.ihe.gazelle.validation.v2.api.business.report.SeverityLevel severity) {
        if (severity == null) {
            return null;
        }
        return switch (severity) {
            case INFO -> SeverityLevel.INFO;
            case WARNING -> SeverityLevel.WARNING;
            case ERROR -> SeverityLevel.ERROR;
        };
    }

    private ConstraintPriority toLegacyPriority(net.ihe.gazelle.validation.v2.api.business.report.RequirementPriority priority) {
        if (priority == null) {
            return null;
        }
        return switch (priority) {
            case MANDATORY -> ConstraintPriority.MANDATORY;
            case RECOMMENDED -> ConstraintPriority.RECOMMENDED;
            case PERMITTED -> ConstraintPriority.PERMITTED;
        };
    }
}
