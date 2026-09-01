package net.ihe.gazelle.keycloak.core.interlay.query;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.benmanes.caffeine.cache.RemovalCause;
import io.agroal.api.AgroalDataSource;
import io.agroal.api.configuration.AgroalConnectionFactoryConfiguration;
import io.agroal.api.configuration.AgroalConnectionPoolConfiguration;
import io.agroal.api.configuration.AgroalDataSourceConfiguration;
import io.agroal.api.configuration.supplier.AgroalDataSourceConfigurationSupplier;
import io.agroal.api.security.NamePrincipal;
import io.agroal.api.security.SimplePassword;
import net.ihe.gazelle.user.management.api.application.configuration.DatabaseConfig;
import net.ihe.gazelle.user.management.commons.interlay.exceptions.GazelleSQLException;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.time.Duration;

import static java.time.Duration.ofSeconds;

/**
 * Gazelle data sources cache
 */
public class GazelleDataSources {

    public static final GazelleDataSources INSTANCE = new GazelleDataSources();

    /**
     * cache with datasources
     */
    final LoadingCache<DatabaseConfig, AgroalDataSource> datasourceCache = Caffeine.newBuilder()
            // remove datasource after 1 hour unused
            .expireAfterAccess(Duration.ofHours(1))
            // close datasource on removal
            .removalListener(this::close)
            // loader
            .build(this::open);

    final FunctionWithSQLException<AgroalDataSourceConfigurationSupplier, AgroalDataSource> dataSourceResolver;

    protected GazelleDataSources() {
        this(AgroalDataSource::from);
    }

    protected GazelleDataSources(FunctionWithSQLException<AgroalDataSourceConfigurationSupplier, AgroalDataSource> dataSourceResolver) {
        this.dataSourceResolver = dataSourceResolver;
    }

    /**
     * Gets Gazelle data source.
     *
     * @param gazelleDBConfig gazelle db config
     * @return gazelle data source
     */
    public DataSource getDataSource(DatabaseConfig gazelleDBConfig) {
        return datasourceCache.get(gazelleDBConfig);
    }

    /**
     * Open agroal data source.
     *
     * @param gazelleDBConfig gazelle db config
     * @return agroal data source
     */
    protected AgroalDataSource open(DatabaseConfig gazelleDBConfig) {
        // create an agroal datasource
        AgroalDataSourceConfigurationSupplier configuration = new AgroalDataSourceConfigurationSupplier()
                .dataSourceImplementation(AgroalDataSourceConfiguration.DataSourceImplementation.AGROAL)
                .metricsEnabled(false)
                // https://agroal.github.io/docs.html
                .connectionPoolConfiguration(cp -> cp
                        .minSize(gazelleDBConfig.getDefaultPoolSize())
                        .maxSize(gazelleDBConfig.getMaxPoolSize())
                        .initialSize(gazelleDBConfig.getDefaultPoolSize())
                        .connectionValidator(AgroalConnectionPoolConfiguration.ConnectionValidator.defaultValidatorWithTimeout(15))
                        .acquisitionTimeout(ofSeconds(5))
                        .leakTimeout(ofSeconds(5))
                        .validationTimeout(ofSeconds(50))
                        .reapTimeout(ofSeconds(gazelleDBConfig.getReapConnectionTimeout()))
                        .connectionFactoryConfiguration(cf -> cf
                                .jdbcUrl(gazelleDBConfig.getGumDBUrl())
                                .connectionProviderClassName(gazelleDBConfig.getDriverClass())
                                .autoCommit(false)
                                .jdbcTransactionIsolation(AgroalConnectionFactoryConfiguration.TransactionIsolation.SERIALIZABLE)
                                .principal(new NamePrincipal(gazelleDBConfig.getUsername()))
                                .credential(new SimplePassword(gazelleDBConfig.getPassword()))
                        )
                );

        try {
            return dataSourceResolver.apply(configuration);
        } catch (SQLException e) {
            throw new GazelleSQLException("Failed to create datasource", e);
        }
    }

    /**
     * Close.
     *
     * @param gazelleDBConfig  the gazelle db config
     * @param agroalDataSource the agroal data source
     * @param removalCause     the removal cause
     */
    protected void close(DatabaseConfig gazelleDBConfig,
                         AgroalDataSource agroalDataSource,
                         RemovalCause removalCause) {
        // close datasource
        agroalDataSource.close();
    }

}
