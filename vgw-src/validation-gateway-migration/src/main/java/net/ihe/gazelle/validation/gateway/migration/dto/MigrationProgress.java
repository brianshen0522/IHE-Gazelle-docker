package net.ihe.gazelle.validation.gateway.migration.dto;

import java.util.List;

public record MigrationProgress(
      long total,
      long processed,
      long succeeded,
      long failed,
      double percentage,
      double reportsPerSecond,
      long elapsedSeconds,
      Long etaSeconds,
      List<MigrationPreview> previews,
      List<MigrationError> recentErrors
) {
}
