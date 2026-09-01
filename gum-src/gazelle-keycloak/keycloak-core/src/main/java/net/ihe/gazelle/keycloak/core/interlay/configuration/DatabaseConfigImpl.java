package net.ihe.gazelle.keycloak.core.interlay.configuration;

import net.ihe.gazelle.user.management.api.application.configuration.DatabaseConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * DB config (cache key)
 */
public class DatabaseConfigImpl implements DatabaseConfig {

    Logger logger = LoggerFactory.getLogger(DatabaseConfigImpl.class);

    private final String gumDBHost;
    private final String gumDBPort;
    private final String gumDBName;
    private final String defaultPoolSize;
    private final String maxPoolSize;
    private final String driverClass;
    private final String username;
    private final String password;

    private final String reapConnectionTimeout;

    /**
     * Constructor for DatabaseConfigImpl, which initializes the database configuration parameters from environment variables.
     */
    public DatabaseConfigImpl() {
        gumDBHost = Objects.requireNonNullElse(System.getenv("DB_GUM_HOST"), "localhost");
        gumDBPort = Objects.requireNonNullElse(System.getenv("DB_GUM_PORT"), "5432");
        gumDBName = Objects.requireNonNullElse(System.getenv("DB_GUM_NAME"), "gum");
        username = Objects.requireNonNull(System.getenv("DB_GUM_USER"), "Environment variable DB_GUM_USER must not be null.");
        password = Objects.requireNonNull(System.getenv("DB_GUM_PASSWORD"), "Environment variable DB_GUM_PASSWORD must not be null.");
        defaultPoolSize = Objects.requireNonNullElse(System.getenv("DB_DEFAULT_POOL_SIZE"), "10");
        maxPoolSize = Objects.requireNonNullElse(System.getenv("DB_MAX_POOL_SIZE"), "50");
        driverClass = Objects.requireNonNullElse(System.getenv("DB_DRIVER_CLASS"), "org.postgresql.Driver");
        reapConnectionTimeout = Objects.requireNonNullElse(System.getenv("DB_REAP_CONNECTION_TIMEOUT"), "60");
    }

    @Override
    public Integer getReapConnectionTimeout() {
        return Integer.parseInt(this.reapConnectionTimeout);
    }

    @Override
    public String getGumDBUrl() {
        logger.debug("jdbc:postgresql://{}:{}/{}", gumDBHost, gumDBPort, gumDBName);
        return "jdbc:postgresql://" + gumDBHost + ":" + gumDBPort + "/" + gumDBName;
    }

    @Override
    public Integer getDefaultPoolSize() {
        return Integer.parseInt(this.defaultPoolSize);
    }

    @Override
    public Integer getMaxPoolSize() {
        return Integer.parseInt(this.maxPoolSize);
    }

    @Override
    public String getDriverClass() {
        return driverClass;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }
}