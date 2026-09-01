package net.ihe.gazelle.keycloak.core.interlay.query;

import net.ihe.gazelle.user.management.api.application.configuration.DatabaseConfig;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Gazelle connection provider.
 */
public class GazelleConnectionProvider implements SupplierWithSQLException<Connection> {

    // current datasource
    protected DataSource dataSource;

    /**
     * Instantiates a new Gazelle connection provider.
     *
     * @param gazelleDBConfig Provider configuration
     */
    public GazelleConnectionProvider(DatabaseConfig gazelleDBConfig) {
        this.dataSource = GazelleDataSources.INSTANCE.getDataSource(gazelleDBConfig);
    }

    @Override
    public Connection get() throws SQLException {
        // create a connection from provider configuration
        return dataSource.getConnection();
    }

}
