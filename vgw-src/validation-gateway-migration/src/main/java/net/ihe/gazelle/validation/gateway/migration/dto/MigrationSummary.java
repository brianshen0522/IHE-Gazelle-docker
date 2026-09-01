package net.ihe.gazelle.validation.gateway.migration.dto;

import java.time.Instant;
import java.util.List;

public record MigrationSummary(
      boolean migrationCompleted,
      Instant completedAt,
      long total,
      long succeeded,
      long failed,
      List<MigrationPreview> previews,
      List<MigrationError> errors
) {
}
