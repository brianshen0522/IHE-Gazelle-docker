package net.ihe.gazelle.validation.gateway.migration.exception;

import net.ihe.gazelle.validation.gateway.migration.dto.MigrationErrorType;

/**
 * Thrown when the ValidationReport document is missing from EVS.
 * This is a critical error - cannot create a validation report without the report document.
 */
public class MissingValidationReportException extends CategorizedMigrationException {
    public MissingValidationReportException(String message) {
        super(message, MigrationErrorType.MISSING_VALIDATION_REPORT);
    }

    public MissingValidationReportException(String message, Throwable cause) {
        super(message, MigrationErrorType.MISSING_VALIDATION_REPORT, cause);
    }
}
