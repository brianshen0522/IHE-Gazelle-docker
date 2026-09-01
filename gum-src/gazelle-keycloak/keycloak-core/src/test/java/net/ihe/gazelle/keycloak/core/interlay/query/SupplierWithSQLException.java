package net.ihe.gazelle.keycloak.core.interlay.query;

import java.sql.SQLException;

/**
 * Supplier interface with error
 *
 * @param <R> output
 */
public interface SupplierWithSQLException<R> {
    /**
     * provide a r
     *
     * @return output
     * @throws SQLException SQLException
     */
    R get() throws SQLException;
}
