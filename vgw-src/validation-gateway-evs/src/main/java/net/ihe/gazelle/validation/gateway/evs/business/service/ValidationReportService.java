package net.ihe.gazelle.validation.gateway.evs.business.service;

import com.kereval.gazelle.datahouse.api.business.record.Item;
import net.ihe.gazelle.maestro.api.business.testreport.TestReport;
import net.ihe.gazelle.validation.gateway.evs.business.model.LocatedReportItem;

import java.util.Optional;

public interface ValidationReportService {

    Item readReportItem(String oid);

    Optional<LocatedReportItem> findReportByLegacyOid(String legacyOid);

    String extractValidationReportLocation(TestReport report);

    String extractReportId(String reportLocation);

    String resolveExecutionFailureMessage(TestReport report);
}
