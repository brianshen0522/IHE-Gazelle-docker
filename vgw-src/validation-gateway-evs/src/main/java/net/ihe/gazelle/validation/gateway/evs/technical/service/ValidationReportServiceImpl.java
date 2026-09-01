package net.ihe.gazelle.validation.gateway.evs.technical.service;

import com.kereval.gazelle.datahouse.api.business.record.Item;
import com.kereval.gazelle.datahouse.api.business.search.Range;
import com.kereval.gazelle.datahouse.api.business.search.SearchParameter;
import com.kereval.gazelle.datahouse.technical.rest.client.SearchItemClient;
import net.ihe.gazelle.errorhandling.business.UnexpectedError;
import net.ihe.gazelle.maestro.api.business.property.ByteArrayItemProperty;
import net.ihe.gazelle.maestro.api.business.property.Property;
import net.ihe.gazelle.maestro.api.business.testreport.StepRunReport;
import net.ihe.gazelle.maestro.api.business.testreport.TestReport;
import net.ihe.gazelle.maestro.api.business.testreport.TestRunReport;
import net.ihe.gazelle.validation.gateway.evs.business.service.ValidationReportService;
import net.ihe.gazelle.validation.gateway.evs.business.model.LocatedReportItem;

import jakarta.enterprise.context.ApplicationScoped;
import java.net.URI;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

@ApplicationScoped
public class ValidationReportServiceImpl implements ValidationReportService {

    private static final String REPORT_ITEM_TYPE = "VALIDATION_REPORT";
    private static final Set<String> OUTPUT_REPORT_NAMES = Set.of(REPORT_ITEM_TYPE, "VALIDATION_REPORTS");

    private final SearchItemClient searchItemClient;

    public ValidationReportServiceImpl(SearchItemClient searchItemClient) {
        this.searchItemClient = searchItemClient;
    }

    @Override
    public Item readReportItem(String oid) {
        return searchItemClient.readItem(oid);
    }

    @Override
    public Optional<LocatedReportItem> findReportByLegacyOid(String legacyOid) {
        if (legacyOid == null || legacyOid.isBlank()) {
            return Optional.empty();
        }
        List<SearchParameter> searchParameters = List.of(
              new SearchParameter().setName("type").setValue(REPORT_ITEM_TYPE),
              new SearchParameter().setName("evs_oid").setValue(legacyOid)
        );
        var result = searchItemClient.searchItem(searchParameters, new Range().setOffset(0).setLimit(1));
        if (result == null || result.items() == null || result.items().isEmpty()) {
            return Optional.empty();
        }
        Item locatedItem = result.items().getFirst();
        if (locatedItem == null || locatedItem.getId() == null || locatedItem.getId().isBlank()) {
            return Optional.empty();
        }
        Item fullItem = searchItemClient.readItem(locatedItem.getId());
        return Optional.of(new LocatedReportItem(locatedItem.getId(), fullItem));
    }

    @Override
    public String extractValidationReportLocation(TestReport report) {
        String fallbackReference = null;
        for (ByteArrayItemProperty itemProperty : streamOutputItemProperties(report).toList()) {
            String reference = itemProperty.getReference();
            if (reference == null || reference.isBlank()) {
                continue;
            }
            if (isValidationReportOutputName(itemProperty.getName())) {
                return reference;
            }
            if (fallbackReference == null) {
                fallbackReference = reference;
            }
        }
        return fallbackReference;
    }

    @Override
    public String extractReportId(String reportLocation) {
        if (reportLocation == null || reportLocation.isBlank()) {
            return null;
        }
        try {
            URI uri = URI.create(reportLocation);
            String path = uri.getPath();
            if (path == null || path.isBlank()) {
                return reportLocation;
            }
            String[] segments = path.split("/");
            for (int i = segments.length - 1; i >= 0; i--) {
                if (!segments[i].isBlank()) {
                    return segments[i];
                }
            }
        } catch (IllegalArgumentException ignored) {
            // fall through
        }
        int slash = reportLocation.lastIndexOf('/');
        return slash >= 0 ? reportLocation.substring(slash + 1) : reportLocation;
    }

    @Override
    public String resolveExecutionFailureMessage(TestReport report) {
        String unexpectedError = extractUnexpectedErrorMessage(report);
        if (unexpectedError != null && !unexpectedError.isBlank()) {
            return unexpectedError;
        }
        return "Missing validation report identifier";
    }

    private boolean isValidationReportOutputName(String outputName) {
        if (outputName == null || outputName.isBlank()) {
            return false;
        }
        return OUTPUT_REPORT_NAMES.contains(outputName.trim().toUpperCase(Locale.ROOT));
    }

    private String extractUnexpectedErrorMessage(TestReport report) {
        if (report == null) {
            return null;
        }
        String directError = firstUnexpectedErrorMessage(report.getUnexpectedErrors());
        if (directError != null) {
            return directError;
        }
        return streamRunReports(report)
              .map(this::extractUnexpectedErrorMessage)
              .filter(Objects::nonNull)
              .findFirst()
              .orElse(null);
    }

    private String extractUnexpectedErrorMessage(TestRunReport runReport) {
        String runError = firstUnexpectedErrorMessage(runReport.getUnexpectedErrors());
        if (runError != null) {
            return runError;
        }
        return streamStepRunReports(runReport)
              .map(step -> firstUnexpectedErrorMessage(step.getUnexpectedErrors()))
              .filter(Objects::nonNull)
              .findFirst()
              .orElse(null);
    }

    private Stream<ByteArrayItemProperty> streamOutputItemProperties(TestReport report) {
        return streamRunReports(report)
              .flatMap(this::streamStepRunReports)
              .flatMap(this::streamOutputs)
              .filter(ByteArrayItemProperty.class::isInstance)
              .map(ByteArrayItemProperty.class::cast);
    }

    private Stream<TestRunReport> streamRunReports(TestReport report) {
        if (report == null || report.getTestRunReports() == null) {
            return Stream.empty();
        }
        return report.getTestRunReports().stream().filter(Objects::nonNull);
    }

    private Stream<StepRunReport> streamStepRunReports(TestRunReport runReport) {
        if (runReport == null || runReport.getStepRunReports() == null) {
            return Stream.empty();
        }
        return runReport.getStepRunReports().stream().filter(Objects::nonNull);
    }

    private Stream<Property> streamOutputs(StepRunReport stepRunReport) {
        if (stepRunReport == null || stepRunReport.getOutputs() == null) {
            return Stream.empty();
        }
        return stepRunReport.getOutputs().stream().filter(Objects::nonNull);
    }

    private String firstUnexpectedErrorMessage(List<UnexpectedError> unexpectedErrors) {
        if (unexpectedErrors == null) {
            return null;
        }
        for (UnexpectedError unexpectedError : unexpectedErrors) {
            String message = flattenUnexpectedError(unexpectedError);
            if (message != null && !message.isBlank()) {
                return message;
            }
        }
        return null;
    }

    private String flattenUnexpectedError(UnexpectedError unexpectedError) {
        if (unexpectedError == null) {
            return null;
        }
        if (unexpectedError.getMessage() != null && !unexpectedError.getMessage().isBlank()) {
            return unexpectedError.getMessage();
        }
        if (unexpectedError.getName() != null && !unexpectedError.getName().isBlank()) {
            return unexpectedError.getName();
        }
        return flattenUnexpectedError(unexpectedError.getCause());
    }
}
