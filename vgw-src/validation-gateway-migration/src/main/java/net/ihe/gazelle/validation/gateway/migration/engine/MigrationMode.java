package net.ihe.gazelle.validation.gateway.migration.engine;

/**
 * Defines the mode of migration execution.
 */
public enum MigrationMode {
   /**
    * Process all reports from the source database.
    * This is used for initial migration or full rerun.
    */
   FULL,

   /**
    * Process only reports created after the last successful migration checkpoint.
    */
   INCREMENTAL,

   /**
    * Process only previously failed reports.
    * This mode is used for selective retry after a migration completes with failures.
    */
   RETRY_FAILED_ONLY
}
