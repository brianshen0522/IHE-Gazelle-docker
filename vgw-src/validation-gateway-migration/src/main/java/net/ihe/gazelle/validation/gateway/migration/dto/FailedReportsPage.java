package net.ihe.gazelle.validation.gateway.migration.dto;

import java.util.List;

public record FailedReportsPage(
      List<MigrationError> errors,
      long total
) {
}
