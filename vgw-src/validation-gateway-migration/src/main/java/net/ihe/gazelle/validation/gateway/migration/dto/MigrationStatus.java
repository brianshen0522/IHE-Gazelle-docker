package net.ihe.gazelle.validation.gateway.migration.dto;

import java.time.Instant;

public record MigrationStatus(
      String state,
      boolean migrationCompleted,
      Instant completedAt,
      String message,
      boolean evsDatabaseAccessible,
      String evsDatabaseMessage,
      String targetType,
      boolean targetAccessible,
      String targetMessage,
      boolean newReportsCountKnown,
      long newReportsCount,
      Instant oldestNewReportDate,
      Instant newestNewReportDate
) {
}
