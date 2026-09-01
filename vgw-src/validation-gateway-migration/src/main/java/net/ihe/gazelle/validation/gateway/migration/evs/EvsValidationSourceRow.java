package net.ihe.gazelle.validation.gateway.migration.evs;

import java.time.Instant;

public record EvsValidationSourceRow(
      int id,
      String oid,
      Instant validationDate,
      String status,
      String validationType,
      String validationService,
      String validationServiceVersion,
      String validatorKeyword,
      String validatorVersion,
      String ownerUsername,
      String ownerOrganization,
      Boolean privateValidation,
      String privacyKey,
      String entryPoint,
      String validationReportArchivePath
) {
}
