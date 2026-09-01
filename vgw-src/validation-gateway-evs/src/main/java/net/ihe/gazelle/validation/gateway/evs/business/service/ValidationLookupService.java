package net.ihe.gazelle.validation.gateway.evs.business.service;

import com.kereval.gazelle.datahouse.api.business.record.Item;
import net.ihe.gazelle.validation.gateway.evs.business.model.AsyncReport;
import net.ihe.gazelle.validation.gateway.evs.business.model.LocatedReportItem;

import java.util.Optional;

public class ValidationLookupService {

    private final AsyncReportState asyncReportState;
    private final ValidationReportService reportService;

    public ValidationLookupService(AsyncReportState asyncReportState, ValidationReportService reportService) {
        this.asyncReportState = asyncReportState;
        this.reportService = reportService;
    }

    public Item readReportItem(String oid) {
        AsyncReport asyncReport = asyncReportState.resolve(oid);
        if (asyncReport != null) {
            String reportOid = asyncReport.reportOid();
            if (reportOid == null || reportOid.isBlank()) {
                throw new IllegalStateException("Missing validation report identifier");
            }
            return reportService.readReportItem(reportOid);
        }
        return reportService.readReportItem(oid);
    }

    public Optional<LocatedReportItem> findReportByLegacyOid(String legacyOid) {
        return reportService.findReportByLegacyOid(legacyOid);
    }
}
