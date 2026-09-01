package net.ihe.gazelle.validation.gateway.migration.exception;

import net.ihe.gazelle.validation.gateway.migration.dto.MigrationErrorType;

/**
 * Thrown when the target service (Datahouse/Mongo) fails during migration.
 * This is a critical error that typically requires retry.
 */
public class DatahouseException extends CategorizedMigrationException {
    public DatahouseException(String message) {
        super(message, MigrationErrorType.DATAHOUSE_ERROR);
    }

    public DatahouseException(String message, Throwable cause) {
        super(message, MigrationErrorType.DATAHOUSE_ERROR, cause);
    }
}
