package net.ihe.gazelle.validation.gateway.migration.exception;

import net.ihe.gazelle.validation.gateway.migration.dto.MigrationErrorType;

/**
 * Thrown when file content cannot be parsed (corrupted or invalid format).
 * This is a critical error that requires investigation.
 */
public class FileParsingException extends CategorizedMigrationException {
    public FileParsingException(String message) {
        super(message, MigrationErrorType.FILE_PARSING_ERROR);
    }

    public FileParsingException(String message, Throwable cause) {
        super(message, MigrationErrorType.FILE_PARSING_ERROR, cause);
    }
}
