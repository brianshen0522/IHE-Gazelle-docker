package net.ihe.gazelle.validation.gateway.migration.engine;

import java.util.Set;

/**
 * Configuration context for migration operations that controls ignore behavior for errors.
 * <p>
 * This context allows selective ignoring of MISSING_INPUT errors during migration:
 * <ul>
 *   <li>Global ignore: Apply to all MISSING_INPUT errors (via {@code ignoreAllMissingInputs})</li>
 *   <li>Selective ignore: Apply to specific reports by EVS OID (via {@code specificIgnoredOids})</li>
 * </ul>
 * <p>
 * When a report's MISSING_INPUT error is ignored, the migration will create a validation report
 * WITHOUT the input attachment, marking it as successful instead of failed.
 */
public class MigrationContext {
    private final boolean ignoreAllMissingInputs;
    private final Set<String> specificIgnoredOids;

    /**
     * Creates a new migration context with no ignore settings (all errors will fail).
     */
    public MigrationContext() {
        this(false, Set.of());
    }

    /**
     * Creates a new migration context with specified ignore settings.
     *
     * @param ignoreAllMissingInputs if true, all MISSING_INPUT errors will be ignored globally
     * @param specificIgnoredOids    set of EVS OIDs whose MISSING_INPUT errors should be ignored
     */
    public MigrationContext(boolean ignoreAllMissingInputs, Set<String> specificIgnoredOids) {
        this.ignoreAllMissingInputs = ignoreAllMissingInputs;
        this.specificIgnoredOids = specificIgnoredOids != null ? specificIgnoredOids : Set.of();
    }

    /**
     * Checks if MISSING_INPUT errors should be ignored for the given EVS OID.
     *
     * @param oid the EVS OID of the report being processed
     * @return true if MISSING_INPUT errors should be ignored for this OID
     */
    public boolean shouldIgnoreMissingInput(String oid) {
        return ignoreAllMissingInputs || specificIgnoredOids.contains(oid);
    }

    public boolean isIgnoreAllMissingInputs() {
        return ignoreAllMissingInputs;
    }

    public Set<String> getSpecificIgnoredOids() {
        return specificIgnoredOids;
    }
}
