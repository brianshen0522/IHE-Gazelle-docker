package net.ihe.gazelle.validation.gateway.migration.exception;

import net.ihe.gazelle.validation.gateway.migration.dto.MigrationErrorType;

/**
 * Base class for migration exceptions that carry error type categorization.
 * This enables the system to distinguish between different failure reasons and
 * apply selective retry/ignore logic.
 */
public abstract class CategorizedMigrationException extends RuntimeException {
    private final MigrationErrorType type;

    protected CategorizedMigrationException(String message, MigrationErrorType type) {
        super(message);
        this.type = type;
    }

    protected CategorizedMigrationException(String message, MigrationErrorType type, Throwable cause) {
        super(message, cause);
        this.type = type;
    }

    public MigrationErrorType getType() {
        return type;
    }
}
