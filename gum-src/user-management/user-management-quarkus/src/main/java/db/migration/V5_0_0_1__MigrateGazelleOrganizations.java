/*
 * Copyright 2026 IHE International.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package db.migration;

import io.smallrye.config.SmallRyeConfig;
import net.ihe.gazelle.user.management.api.domain.organization.DelegatedOrganization;
import net.ihe.gazelle.user.management.api.domain.organization.Organization;
import org.eclipse.microprofile.config.ConfigProvider;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.postgresql.util.PSQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Flyway migration to migrate Gazelle organizations to the new format with delegated organizations
 */
public class V5_0_0_1__MigrateGazelleOrganizations extends BaseJavaMigration {

    private final Logger logger = LoggerFactory.getLogger(V5_0_0_1__MigrateGazelleOrganizations.class.getName());
    private static final int ID_KEY = 1;
    private static final int SHORTNAME_KEY = 2;
    private static final int NAME_KEY = 3;
    private static final int ARCHIVED_KEY = 4;
    private static final int LAST_UPDATE_TIMESTAMP_KEY = 5;
    private static final int DELEG_ID_KEY = 1;
    private static final int DELEG_EXTERNAL_ID_KEY = 2;
    private static final int DELEG_IDP_ID_KEY = 3;
    private static final String RSET_KEYWORD = "keyword";
    private static final String RSET_NAME = "name";
    private static final String RSET_LAST_CHANGED = "last_changed";

    private static final String SQL_SELECT_ORGA_FROM_TM = "SELECT name, keyword, last_changed FROM usr_institution";
    private static final String SQL_SELECT_DELEGATED_ORGA_FROM_TM = "SELECT external_id, idp_id, orga.keyword FROM usr_delegated_organization deleg_orga JOIN usr_institution orga ON orga.id = deleg_orga.organization_id";

    private static final String SQL_INSERT_ORGANIZATIONS_IN_GUM = "INSERT INTO gum.organization (id,shortname,name,archived,last_update_timestamp) VALUES (?,?,?,?,?)";
    private static final String SQL_INSERT_DELEGATED_ORGANIZATIONS_IN_GUM = "INSERT INTO gum.delegated_organization (organization_id,external_id,idp_id) VALUES (?,?,?)";
    private static final String SQL_CHECK_ORGANIZATION_REFERENCED_BY_USER = "SELECT EXISTS(SELECT 1 FROM gum.user WHERE organization_id = ?)";

    /**
     * Default constructor required by Flyway.
     */
    public V5_0_0_1__MigrateGazelleOrganizations() {
        // Nothing to do here
    }

    @Override
    public void migrate(Context context) {
        try {
            // Retrieve Gazelle database connection parameters from Quarkus properties
            String dbTMJdbcUrl = ConfigProvider.getConfig().getValue("gzl.tm.jdbc.url", String.class);
            String dbTMJdbcUsername = ConfigProvider.getConfig().getValue("gzl.tm.jdbc.username", String.class);
            String dbTMJdbcPassword = ConfigProvider.getConfig().getValue("gzl.tm.jdbc.password", String.class);
            Connection connectionTM = getGazelleTMDatabaseConnection(dbTMJdbcUrl, dbTMJdbcUsername, dbTMJdbcPassword);
            if (connectionTM == null || !connectionTM.isValid(10)) {
                logger.warn("No gazelle-tm database detected, skipping migration of organizations from TM to GUM.");
                return;
            }

            Connection connectionGUM = getGUMDatabaseConnection(context);

            List<Organization> organizations = retrieveOrganizations(connectionTM);
            logger.info("Migrate {} organizations from TM to GUM", organizations.size());
            insertOrganizationsInGUM(connectionGUM, organizations);

            List<DelegatedOrganization> delegatedOrganizations = retrieveDelegatedOrganizations(connectionTM);
            logger.info("Migrate {} delegated organizations from TM to GUM", delegatedOrganizations.size());
            insertDelegatedOrganizationsInGUM(connectionGUM, delegatedOrganizations);
        } catch (SQLException e) {
            throw new GazelleMigrationException("Error while migrating organizations", e);
        }
    }

    private Connection getGUMDatabaseConnection(Context context) {
        return context.getConnection();
    }

    private Connection getGazelleTMDatabaseConnection(String dbTMJdbcUrl, String dbTMJdbcUsername, String dbTMJdbcPassword) throws SQLException {
        try {
            return DriverManager.getConnection(dbTMJdbcUrl, dbTMJdbcUsername, dbTMJdbcPassword);
        } catch (PSQLException _) {
            return null;
        }
    }

    /**
     * Retrieves organizations from Gazelle TM database.
     */
    private List<Organization> retrieveOrganizations(Connection connection) throws SQLException {
        try (PreparedStatement statementSelectRoles = connection.prepareStatement(SQL_SELECT_ORGA_FROM_TM)) {
            ResultSet resultSet = statementSelectRoles.executeQuery();
            List<Organization> gazelleOrganizations = new ArrayList<>();
            while (resultSet.next()) {
                Organization organization = new Organization();
                organization.setId(resultSet.getString(RSET_KEYWORD)); // We put keyword as new orga id here to avoid to have to migrate all organizationId attributes of users
                organization.setShortname(resultSet.getString(RSET_KEYWORD));
                organization.setName(resultSet.getString(RSET_NAME));
                if (resultSet.getTimestamp(RSET_LAST_CHANGED) != null)
                    organization.setLastUpdateTimestamp(resultSet.getTimestamp(RSET_LAST_CHANGED).getTime());
                gazelleOrganizations.add(organization);
            }
            return gazelleOrganizations;
        }
    }

    /**
     * Retrieves delegated organizations from Gazelle TM database.
     */
    private List<DelegatedOrganization> retrieveDelegatedOrganizations(Connection connection) throws SQLException {
        try (PreparedStatement statementSelectRoles = connection.prepareStatement(SQL_SELECT_DELEGATED_ORGA_FROM_TM)) {
            ResultSet resultSet = statementSelectRoles.executeQuery();
            List<DelegatedOrganization> gazelleOrganizations = new ArrayList<>();
            while (resultSet.next()) {
                DelegatedOrganization organization = new DelegatedOrganization();
                organization.setId(resultSet.getString(RSET_KEYWORD));
                organization.setExternalId(resultSet.getString("external_id"));
                organization.setIdpId(resultSet.getString("idp_id"));
                gazelleOrganizations.add(organization);
            }
            return gazelleOrganizations;
        }
    }

    /**
     * Inserts organizations in GUM database.
     */
    private void insertOrganizationsInGUM(Connection connection, List<Organization> organizations) throws SQLException {
        try (PreparedStatement statementInsertOrga = connection.prepareStatement(SQL_INSERT_ORGANIZATIONS_IN_GUM)) {

            int counter = 0;
            for (Organization currentOrganization : organizations) {
                if (isOrgaDontNeedToBeMigrated(connection, currentOrganization)) {
                    logger.info("Organization {} ({}) not migrated", currentOrganization.getShortname(), currentOrganization.getName());
                    continue;
                }

                addOrganizationToBatch(statementInsertOrga, currentOrganization);
                logger.info("Add organization {} ({}).", currentOrganization.getShortname(), currentOrganization.getName());

                // Keep existing behavior: execute once on first row, then every 25 rows.
                if (shouldExecuteBatch(counter++)) {
                    executeAndValidateBatch(statementInsertOrga, "organization", "organizations");
                }
            }
            // Execute the batch
            executeAndValidateBatch(statementInsertOrga, "organization", "organizations");
        }
    }

    /**
     * Inserts delegated organizations in GUM database.
     */
    private void insertDelegatedOrganizationsInGUM(Connection connection, List<DelegatedOrganization> delegatedOrganizations) throws SQLException {
        try (PreparedStatement statementInsertOrga = connection.prepareStatement(SQL_INSERT_DELEGATED_ORGANIZATIONS_IN_GUM)) {

            int counter = 0;
            for (DelegatedOrganization currentOrganization : delegatedOrganizations) {
                if (isOrgaDontNeedToBeMigrated(connection, currentOrganization)) {
                    continue;
                }

                addDelegatedOrganizationToBatch(statementInsertOrga, currentOrganization);
                logger.info("Add delegated organization {} ({},{}).", currentOrganization.getId(), currentOrganization.getExternalId(), currentOrganization.getIdpId());

                // Keep existing behavior: execute once on first row, then every 25 rows.
                if (shouldExecuteBatch(counter++)) {
                    executeAndValidateBatch(statementInsertOrga, "delegated organization", "delegated organizations");
                }
            }
            // Execute the batch
            executeAndValidateBatch(statementInsertOrga, "delegated organization", "delegated organizations");
        }
    }

    private void addOrganizationToBatch(PreparedStatement statementInsertOrga, Organization currentOrganization) throws SQLException {
        statementInsertOrga.clearParameters();
        statementInsertOrga.setString(ID_KEY, currentOrganization.getId());
        statementInsertOrga.setString(SHORTNAME_KEY, currentOrganization.getShortname());
        statementInsertOrga.setString(NAME_KEY, currentOrganization.getName());
        statementInsertOrga.setBoolean(ARCHIVED_KEY, false);
        statementInsertOrga.setTimestamp(LAST_UPDATE_TIMESTAMP_KEY, new Timestamp(currentOrganization.getLastUpdateTimestamp()));
        statementInsertOrga.addBatch();
    }

    private void addDelegatedOrganizationToBatch(PreparedStatement statementInsertOrga, DelegatedOrganization currentOrganization) throws SQLException {
        statementInsertOrga.clearParameters();
        statementInsertOrga.setString(DELEG_ID_KEY, currentOrganization.getId());
        statementInsertOrga.setString(DELEG_EXTERNAL_ID_KEY, currentOrganization.getExternalId());
        statementInsertOrga.setString(DELEG_IDP_ID_KEY, currentOrganization.getIdpId());
        statementInsertOrga.addBatch();
    }

    private boolean shouldExecuteBatch(int counter) {
        return (counter % 25) == 0;
    }

    private void executeAndValidateBatch(PreparedStatement statement, String itemName, String itemsName) throws SQLException {
        int[] result = statement.executeBatch();
        for (int status : result) {
            if (status == 0) {
                logger.warn("Failed to insert {}", itemName);
                if (isProduction()) {
                    throw new GazelleMigrationException("Failed to insert " + itemsName);
                }
            }
        }
    }

    /**
     * Checks if the organization don't need to be migrated to GUM.
     * If no user references the orga, we don't migrate it
     */
    private boolean isOrgaDontNeedToBeMigrated(Connection connection, Organization organization) throws SQLException {
        if (organization.getId() == null || organization.getId().isEmpty()) {
            return true;
        }
        try (PreparedStatement statementCheckOrganization = connection.prepareStatement(SQL_CHECK_ORGANIZATION_REFERENCED_BY_USER)) {
            statementCheckOrganization.setString(1, organization.getId());
            try (ResultSet resultSet = statementCheckOrganization.executeQuery()) {
                return !resultSet.next() || !resultSet.getBoolean(1);
            }
        }
    }

    /**
     * Check if we are in production profile
     *
     * @return true if the application is in production profile false otherwise
     */
    private boolean isProduction() {
        return ConfigProvider.getConfig().unwrap(SmallRyeConfig.class).getProfiles().contains("prod");
    }
}
