package net.ihe.gazelle.validation.gateway.migration.output;

import net.ihe.gazelle.validation.gateway.migration.dto.MigrationError;
import net.ihe.gazelle.validation.gateway.migration.dto.MigrationCheckpoint;
import net.ihe.gazelle.validation.gateway.migration.dto.MigrationPreview;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

public record PersistedMigrationState(
      long total,
      long processed,
      long succeeded,
      long failed,
      Instant startedAt,
      boolean completed,
      Instant completedAt,
      List<MigrationPreview> previews,
      List<MigrationError> recentErrors,
      MigrationCheckpoint lastSuccessfulCheckpoint,
      String lastRunMode,
      Set<String> allFailedOids,
      Map<String, MigrationError> allFailedReportsMap
) {
}
