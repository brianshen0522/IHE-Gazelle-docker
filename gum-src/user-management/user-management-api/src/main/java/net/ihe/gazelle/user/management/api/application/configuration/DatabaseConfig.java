package net.ihe.gazelle.user.management.api.application.configuration;

/**
 * Configuration interface for database connection settings.
 *
 * This interface defines methods to access database configuration parameters
 * including connection URLs, pool settings, authentication credentials, and
 * connection management options for the Gazelle User Management application.
 *
 */
public interface DatabaseConfig {

    /**
     * The duration for eviction of idle connections in seconds
     * @return reap connection timeout (60 by default)
     */
    Integer getReapConnectionTimeout();

    /**
     * Get the url of the gazelle database
     * @return the url of the gazelle database
     */
    String getGumDBUrl();

    /**
     * Get the minimum number of connections in the pool
     * @return the minimum number of connections in the pool (1 by default)
     */
    Integer getDefaultPoolSize();

    /**
     * Get the maximum number of connections in the pool
     * @return the maximum number of connections in the pool (5 by default)
     */
    Integer getMaxPoolSize();

    /**
     * Get the driver class
     * @return the driver class (org.postgresql.Driver by default)
     */
    String getDriverClass();

    /**
     * Get the username to connect to the database
     * @return the username or null if not set
     */
    String getUsername();

    /**
     * Get the password to connect to the database
     * @return the password or null if not set
     */
    String getPassword();
}
