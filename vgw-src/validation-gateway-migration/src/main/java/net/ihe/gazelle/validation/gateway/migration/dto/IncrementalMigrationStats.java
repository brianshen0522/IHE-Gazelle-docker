package net.ihe.gazelle.validation.gateway.migration.dto;

import java.time.Instant;

public record IncrementalMigrationStats(
      long newReportsCount,
      Instant oldestNewReportDate,
      Instant newestNewReportDate
) {
}
