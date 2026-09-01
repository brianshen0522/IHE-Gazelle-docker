package net.ihe.gazelle.validation.gateway.migration.dto;

import java.time.Instant;

public record PreMigrationStats(
      long totalReports,
      Instant oldestReportDate,
      Instant newestReportDate,
      long totalSizeBytes
) {
}
