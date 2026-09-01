package net.ihe.gazelle.validation.gateway.migration.exception;

import net.ihe.gazelle.validation.gateway.migration.dto.MigrationErrorType;

/**
 * Thrown when input attachment files are not found or cannot be read from EVS.
 * This error type may be ignorable - users can choose to create validation reports
 * without input attachments in certain scenarios.
 */
public class MissingInputException extends CategorizedMigrationException {
    public MissingInputException(String message) {
        super(message, MigrationErrorType.MISSING_INPUT);
    }

    public MissingInputException(String message, Throwable cause) {
        super(message, MigrationErrorType.MISSING_INPUT, cause);
    }
}
