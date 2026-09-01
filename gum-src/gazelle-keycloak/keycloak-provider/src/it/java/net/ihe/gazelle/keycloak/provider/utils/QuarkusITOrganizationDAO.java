package net.ihe.gazelle.keycloak.provider.utils;

import net.ihe.gazelle.keycloak.core.interlay.query.FunctionWithSQLException;
import net.ihe.gazelle.keycloak.core.interlay.query.GazelleConnectionProvider;
import net.ihe.gazelle.keycloak.core.interlay.query.GazelleQueryExecutor;
import net.ihe.gazelle.user.management.api.application.configuration.DatabaseConfig;
import net.ihe.gazelle.user.management.api.domain.organization.DelegatedOrganization;
import net.ihe.gazelle.user.management.api.domain.organization.Organization;
import net.ihe.gazelle.user.management.commons.interlay.exceptions.GazelleSQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class QuarkusITOrganizationDAO {

    private static final Logger log = LoggerFactory.getLogger(QuarkusITOrganizationDAO.class);

    public static final String SQL_GET_ORGANIZATION_BY_SHORTNAME = """
        SELECT id, shortname, name, last_update_timestamp FROM gum.organization where shortname=?
        """;

    public static final String SQL_GET_DELEGATED_ORGANIZATION_BY_SHORTNAME = """
        SELECT id, shortname, name, external_id, idp_id, last_update_timestamp
        FROM gum.delegated_organization delegated_organization
        JOIN gum.organization organization ON organization.id = delegated_organization.organization_id
        WHERE organization.shortname=?
        """;
    private final GazelleQueryExecutor queryExecutor;

    public QuarkusITOrganizationDAO(DatabaseConfig databaseConfig) {
        queryExecutor = new GazelleQueryExecutor(new GazelleConnectionProvider(databaseConfig));
    }

    public Organization getOrganizationByShortname(String orgaShortname){
        return queryExecutor.executeQuerySingleResult(SQL_GET_ORGANIZATION_BY_SHORTNAME, List.of(orgaShortname), new OrganizationMapper());
    }

    public Organization getDelegatedOrganizationByShortname(String orgaShortname){
        return queryExecutor.executeQuerySingleResult(SQL_GET_DELEGATED_ORGANIZATION_BY_SHORTNAME, List.of(orgaShortname), new OrganizationMapper());
    }


    public static class OrganizationMapper implements FunctionWithSQLException<ResultSet, Organization>  {

        /**
         * Map an organization row to a GUM organization
         * @param resultSet SQL resultSet
         * @return gazelle organization
         */
        public Organization apply(ResultSet resultSet) {
            log.info(resultSet.toString());
            Organization gazelleOrganization;
            try {
                gazelleOrganization = new Organization();
                gazelleOrganization.setId(resultSet.getString("id"));
                gazelleOrganization.setName(resultSet.getString("name"));
                gazelleOrganization.setShortname(resultSet.getString("shortname"));
                gazelleOrganization.setLastUpdateTimestamp(resultSet.getTimestamp("last_update_timestamp") != null ? resultSet.getTimestamp("last_update_timestamp").getTime() : 0);
                if (isADelegatedOrganization(resultSet)) {
                    String externalId = resultSet.getString("external_id");
                    String idpId = resultSet.getString("idp_id");
                    return new DelegatedOrganization(gazelleOrganization, externalId, idpId);
                }
                return gazelleOrganization;
            } catch (SQLException e) {
                throw new GazelleSQLException("Failed to map organization", e);
            }
        }

        private static boolean isADelegatedOrganization(ResultSet resultSet) {
            try {
                return resultSet.getString("external_id") != null;
            } catch (SQLException e) {
                return false;
            }
        }
    }
}
