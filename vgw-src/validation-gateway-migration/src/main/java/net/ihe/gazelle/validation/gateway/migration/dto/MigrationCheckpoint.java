package net.ihe.gazelle.validation.gateway.migration.dto;

import java.time.Instant;

public record MigrationCheckpoint(
      Instant validationDate,
      Integer sourceId
) {
}
