package net.ihe.gazelle.keycloak.core.interlay.query;

import java.sql.SQLException;

/**
 * Function interface with error
 *
 * @param <T> input
 * @param <R> output
 */
public interface FunctionWithSQLException<T, R> {
    /**
     * map a t
     *
     * @param t input
     * @return output
     * @throws SQLException SQLException
     */
    R apply(T t) throws SQLException;
}
