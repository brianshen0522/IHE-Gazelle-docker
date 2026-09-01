package net.ihe.gazelle.validation.gateway.migration.dto;

/**
 * Categorizes migration errors to enable selective retry and ignore logic.
 *
 * <ul>
 *   <li>{@link #MISSING_INPUT} - Input attachment not found in EVS (may be ignorable)</li>
 *   <li>{@link #MISSING_VALIDATION_REPORT} - ValidationReport document missing (critical)</li>
 *   <li>{@link #FILE_PARSING_ERROR} - Cannot parse file content (critical)</li>
 *   <li>{@link #DATAHOUSE_ERROR} - Target service failure (critical)</li>
 *   <li>{@link #UNKNOWN_ERROR} - Unexpected exception (critical)</li>
 * </ul>
 */
public enum MigrationErrorType {
    /**
     * Input attachment not found in EVS.
     * This may be acceptable - users can choose to create validation reports without input attachments.
     */
    MISSING_INPUT,

    /**
     * ValidationReport document missing from EVS.
     * This is a critical error - cannot create a validation report without the report document.
     */
    MISSING_VALIDATION_REPORT,

    /**
     * Cannot parse file content (corrupted or invalid format).
     * This is a critical error that requires investigation.
     */
    FILE_PARSING_ERROR,

    /**
     * Target service (Datahouse/Mongo) failure.
     * This is a critical error that typically requires retry.
     */
    DATAHOUSE_ERROR,

    /**
     * Unexpected exception with unknown categorization.
     * This is a critical error that requires investigation.
     */
    UNKNOWN_ERROR
}
