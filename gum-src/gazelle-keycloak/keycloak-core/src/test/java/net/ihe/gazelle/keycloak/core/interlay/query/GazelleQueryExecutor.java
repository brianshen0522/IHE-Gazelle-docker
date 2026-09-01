package net.ihe.gazelle.keycloak.core.interlay.query;

import net.ihe.gazelle.user.management.commons.application.exception.GazelleDAOException;
import net.ihe.gazelle.user.management.commons.interlay.exceptions.GazelleSQLException;
import org.slf4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Util for accessing Gazelle DB
 */
public class GazelleQueryExecutor {

    Logger log = org.slf4j.LoggerFactory.getLogger(GazelleQueryExecutor.class);

    /**
     * Connection supplier.
     */
    protected final SupplierWithSQLException<Connection> connectionSupplier;

    /**
     * Instantiates a new Gazelle query executor.
     *
     * @param connectionSupplier connection supplier
     */
    public GazelleQueryExecutor(SupplierWithSQLException<Connection> connectionSupplier) {
        this.connectionSupplier = connectionSupplier;
    }

    /**
     * Execute query with a single result
     *
     * @param <T>        output type
     * @param sql        sql
     * @param parameters sql parameters
     * @param mapper     resultset mapper
     * @return unique result
     */
    public <T> T executeQuerySingleResult(String sql, List<Object> parameters,
                                          FunctionWithSQLException<ResultSet, T> mapper) {
        List<T> result = executeQuery(sql, parameters, mapper);
        // no result
        if (result.isEmpty()) {
            return null;
        }
        // more than one results
        if (result.size() > 1) {
            return null;
        }
        // return single result
        return result.get(0);
    }

    /**
     * Execute query with a list as result
     *
     * @param <T>        output type
     * @param sql        sql
     * @param parameters sql parameters
     * @param mapper     resultset mapper
     * @param numberOfRow     number of row to return
     * @return result list
     */
    public <T> List<T> executeQuery(String sql, List<Object> parameters,
                                    FunctionWithSQLException<ResultSet, T> mapper, Integer numberOfRow) {
        log.debug("Query : {}", sql);
        return doInTransaction(connection -> {
            List<T> result = new ArrayList<>();
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            try {
                // set parameters
                for (int i = 0; i < parameters.size(); i++) {
                    preparedStatement.setObject(i + 1, parameters.get(i));
                }
                if (numberOfRow != null)
                    preparedStatement.setMaxRows(numberOfRow);
                // execute query
                preparedStatement.execute();
                ResultSet rs = preparedStatement.getResultSet();

                // map each result line
                while (rs.next()) {
                    result.add(mapper.apply(rs));
                }
            } finally {
                preparedStatement.close();
            }
            return result;
        });
    }

    /**
     * Execute query with a list as result
     *
     * @param <T>        output type
     * @param sql        sql
     * @param parameters sql parameters
     * @param mapper     resultset mapper
     * @return result list
     */
    public <T> List<T> executeQuery(String sql, List<Object> parameters, FunctionWithSQLException<ResultSet, T> mapper) {
        return executeQuery(sql, parameters, mapper, 0);
    }


    /**
     * Execute update query
     *
     * @param sql        sql
     * @param parameters sql parameters
     * @return result list
     */
    public int executeUpdate(String sql, List<Object> parameters) {
        log.debug("Query : {}", sql);
        return doInTransaction(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            try {
                // set parameters
                for (int i = 0; i < parameters.size(); i++) {
                    preparedStatement.setObject(i + 1, parameters.get(i));
                }
                // execute query
                return preparedStatement.executeUpdate();
            } finally {
                preparedStatement.close();
            }
        });
    }

    protected <A> A doInTransaction(FunctionWithSQLException<Connection, A> functionWithSQLException) {
        // retrieve connection
        Connection connection = getConnection();

        boolean initialAutocommit = false;
        A returnValue;
        try {
            // set auto commit as false
            initialAutocommit = disableAutoCommit(connection);
            // perform something on connection
            returnValue = functionWithSQLException.apply(connection);
            connection.commit();
            return returnValue;
        } catch (SQLException sqlException) {
            try {
                // rollback exception
                connection.rollback();
            } catch (SQLException e) {
                log.error("Failed to rollback", e);
            }
            throw new GazelleSQLException("Failed to execute query", sqlException);
        } finally {
            closeConnection(connection, initialAutocommit);
        }
    }

    private Connection getConnection() {
        try {
            Connection connection = connectionSupplier.get();
            if (connection == null) {
                throw new GazelleDAOException("connection is null");
            }
            return connection;
        } catch (SQLException e) {
            throw new GazelleSQLException("failed to get connection", e);
        }
    }

    private boolean disableAutoCommit(Connection connection) {
        try {
            boolean initialAutocommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            return initialAutocommit;
        } catch (SQLException e) {
            throw new GazelleSQLException("failed to disable autocommit", e);
        }
    }

    private void closeConnection(Connection connection, boolean initialAutocommit) {
        try {
            // reset connection auto commit
            if (initialAutocommit) {
                connection.setAutoCommit(true);
            }
            // close connection
            connection.close();
        } catch (SQLException e) {
            log.error("Failed to close connection", e);
        }
    }

}