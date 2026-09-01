package net.ihe.gazelle.keycloak.provider.utils;

import net.ihe.gazelle.user.management.api.application.configuration.DatabaseConfig;

public class SystemPropertyDBConfig implements DatabaseConfig {

    private final String dbUsername;
    private final String dbPassword;
    private final String dbName;
    private final String dbPort;
    private final String dbHost;

    /**
     *
     */
    public SystemPropertyDBConfig() {
        dbUsername=System.getProperty("dbExtUsername");
        dbPassword=System.getProperty("dbExtPassword");
        dbName=System.getProperty("dbExtName");
        dbPort=System.getProperty("dbExtPort");
        dbHost=System.getProperty("dbExtHost");
    }
    @Override
    public Integer getReapConnectionTimeout() {
        return 60;
    }

    @Override
    public String getGumDBUrl() {
        return "jdbc:postgresql://" + dbHost + ":" + dbPort + "/" + dbName;
    }

    @Override
    public Integer getDefaultPoolSize() {
        return 1;
    }

    @Override
    public Integer getMaxPoolSize() {
        return 5;
    }

    @Override
    public String getDriverClass() {
        return "org.postgresql.Driver";
    }

    @Override
    public String getUsername() {
        return dbUsername;
    }

    @Override
    public String getPassword() {
        return dbPassword;
    }
}
