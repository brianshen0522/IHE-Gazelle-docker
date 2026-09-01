package net.ihe.gazelle.validation.gateway.migration.output;

public record CompletedMigrationMetrics(
      long total,
      long succeeded,
      long failed,
      long elapsedSeconds
) {
}
