package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static net.ihe.gazelle.user.management.api.domain.user.GazelleDefaultGroup.*;

/**
 * Flyway migration to migrate user roles to groups in GUM database.
 * This migration retrieves user roles from the GUM database, creates corresponding groups, and associates users with these groups.
 */
public class V4_0_0_5__MigrateRolesToGroups extends BaseJavaMigration {

    private final Logger logger = LoggerFactory.getLogger(V4_0_0_5__MigrateRolesToGroups.class.getName());
    private static final String ADMIN_ROLE = "admin_role";
    private static final String MONITOR_ROLE = "monitor_role";
    private static final String TESTING_SESSION_ADMIN_ROLE = "testing_session_admin_role";
    private static final String VENDOR_LATE_REGISTRATION_ROLE = "vendor_late_registration_role";
    private static final String PROJECT_MANAGER_ROLE = "project-manager_role";
    private static final String TESTS_EDITOR_ROLE = "tests_editor_role";
    private static final String VENDOR_ADMIN_ROLE = "vendor_admin_role";
    private static final String VENDOR_ROLE = "vendor_role";

    private static final int GROUP_ID_KEY = 1;
    private static final int GROUP_TYPE_KEY = 2;
    private static final int USER_ID_KEY = 2;
    private static final int GROUP_REFERENCE_KEY = 3;
    private static final int GROUP_NAME_KEY = 4;

    private static final String SELECT_ROLES_FROM_GUM = "SELECT user_id, name, description, organization_id FROM gum.user_role LEFT JOIN gum.role ON role_id = id LEFT JOIN gum.user b ON user_id = b.id";
    private static final String SQL_INSERT_GROUP_IN_GUM = "INSERT INTO gum.group (id, type, reference, name) VALUES (?,?,?,?) ON CONFLICT DO NOTHING";
    private static final String SQL_INSERT_USER_GROUP_IN_GUM = "INSERT INTO gum.user_group (group_id, user_id) VALUES (?,?) ON CONFLICT DO NOTHING";

    /** Default constructor required by Flyway. */
    public V4_0_0_5__MigrateRolesToGroups() {
        // Nothing to do here
    }

    @Override
    public void migrate(Context context) {
        try {
            Connection connectionGUM = getGUMDatabaseConnection(context);

            List<GazelleUserRoleWrapper> prefWrapper = retrieveUserRoles(connectionGUM);
            logger.info("Retrieved {} user roles", prefWrapper.size());

            // Insert data into GUM
            logger.info("Insert {} user group to GUM database", prefWrapper.size());
            createGroupsInGUM(connectionGUM, prefWrapper);
            insertUserGroupsInGUM(connectionGUM, prefWrapper);
        } catch (SQLException e) {
            throw new GazelleMigrationException("Error while migrating roles to groups", e);
        }
    }

    private Connection getGUMDatabaseConnection(Context context) {
        return context.getConnection();
    }

    /**
     * Retrieve user roles from gum database
     */
    private List<GazelleUserRoleWrapper> retrieveUserRoles(Connection connection) throws SQLException {
        try (PreparedStatement statementSelectRoles = connection.prepareStatement(SELECT_ROLES_FROM_GUM)) {
            ResultSet resultSet = statementSelectRoles.executeQuery();
            List<GazelleUserRoleWrapper> gazelleRoleWrappers = new ArrayList<>();

            while (resultSet.next()) {
                GazelleUserRoleWrapper roleWrapper = new GazelleUserRoleWrapper();
                roleWrapper.setUserId(resultSet.getString("user_id"));
                roleWrapper.setRoleName(resultSet.getString("name"));
                roleWrapper.setRoleDescription(resultSet.getString("description"));
                roleWrapper.setOrganizationId(resultSet.getString("organization_id"));
                gazelleRoleWrappers.add(roleWrapper);
            }

            return gazelleRoleWrappers;
        }
    }

    /**
     * Create groups in GUM
     */
    private void createGroupsInGUM(Connection connection, List<GazelleUserRoleWrapper> gazelleRoleWrappers) throws SQLException {
        try (PreparedStatement statementInsertGroup = connection.prepareStatement(SQL_INSERT_GROUP_IN_GUM)) {

            int counter = 0;
            logger.info("Inserting groups into {}", connection.getMetaData().getURL());
            for (GazelleUserRoleWrapper roleWrapper : gazelleRoleWrappers) {
                if (isRoleNeedToBeMigrated(roleWrapper)) {
                    addBatchForRoleCreation(roleWrapper, statementInsertGroup);
                    addBatchForOrgaCreation(roleWrapper, statementInsertGroup);

                    // Execute the batch every 20 statements
                    if ((counter++ % 20) == 0) {
                        statementInsertGroup.executeBatch();
                    }
                }
            }
            statementInsertGroup.executeBatch();
        }
    }

    private void addBatchForRoleCreation(GazelleUserRoleWrapper roleWrapper, PreparedStatement statementInsertGroup) throws SQLException {
        String groupId = getNewRoleNameFromOldOne(roleWrapper.getRoleName());
        if (groupId != null) {
            statementInsertGroup.clearParameters();
            statementInsertGroup.setString(GROUP_ID_KEY, groupId);
            statementInsertGroup.setString(GROUP_TYPE_KEY, "ROLE");
            statementInsertGroup.setString(GROUP_REFERENCE_KEY, roleWrapper.getRoleName());
            statementInsertGroup.setString(GROUP_NAME_KEY, roleWrapper.getRoleDescription());
            statementInsertGroup.addBatch();
        }
    }

    private void addBatchForOrgaCreation(GazelleUserRoleWrapper roleWrapper, PreparedStatement statementInsertGroup) throws SQLException {
        String groupId = getNewOrgaGroupFromOldRole(roleWrapper.getRoleName(), roleWrapper.getOrgaId());
        if (groupId != null) {
            statementInsertGroup.clearParameters();
            statementInsertGroup.setString(GROUP_ID_KEY, groupId);
            String groupType = roleWrapper.getRoleName().equals(VENDOR_ADMIN_ROLE) ? "ORGANIZATION_ADMIN" : "ORGANIZATION";
            statementInsertGroup.setString(GROUP_TYPE_KEY, groupType);
            statementInsertGroup.setString(GROUP_REFERENCE_KEY, roleWrapper.getRoleName());
            statementInsertGroup.setString(GROUP_NAME_KEY, roleWrapper.getRoleDescription());
            statementInsertGroup.addBatch();
        }
    }

    /**
     * Insert user groups in GUM
     */
    private void insertUserGroupsInGUM(Connection connection, List<GazelleUserRoleWrapper> gazelleRoleWrappers) throws SQLException {
        try (PreparedStatement statementInsertUser = connection.prepareStatement(SQL_INSERT_USER_GROUP_IN_GUM)) {

            int counter = 0;
            logger.info("Inserting user groups into {}", connection.getMetaData().getURL());
            for (GazelleUserRoleWrapper roleWrapper : gazelleRoleWrappers) {
                if (isRoleNeedToBeMigrated(roleWrapper)) {
                    String groupId = getNewRoleNameFromOldOne(roleWrapper.getRoleName());
                    addUserAndGroupAssociation(roleWrapper, statementInsertUser, groupId);

                    String groupId2 = getNewOrgaGroupFromOldRole(roleWrapper.getRoleName(), roleWrapper.getOrgaId());
                    addUserAndGroupAssociation(roleWrapper, statementInsertUser, groupId2);
                    // Execute the batch every 20 statements
                    if ((counter++ % 20) == 0) {
                        statementInsertUser.executeBatch();
                    }
                }
            }
            statementInsertUser.executeBatch();
        }
    }

    private void addUserAndGroupAssociation(GazelleUserRoleWrapper roleWrapper, PreparedStatement statementInsertUser, String groupId) throws SQLException {
        if (groupId != null) {
            statementInsertUser.clearParameters();
            statementInsertUser.setString(GROUP_ID_KEY, groupId);
            statementInsertUser.setString(USER_ID_KEY, roleWrapper.getUserId());
            statementInsertUser.addBatch();
        }
    }

    private boolean isRoleNeedToBeMigrated(GazelleUserRoleWrapper roleWrapper) {
        return List.of(ADMIN_ROLE, MONITOR_ROLE, TESTING_SESSION_ADMIN_ROLE, VENDOR_LATE_REGISTRATION_ROLE,
                        PROJECT_MANAGER_ROLE, TESTS_EDITOR_ROLE, VENDOR_ROLE, VENDOR_ADMIN_ROLE)
                .contains(roleWrapper.getRoleName());
    }

    private String getNewRoleNameFromOldOne(String oldRoleName) {
        return switch (oldRoleName) {
            case ADMIN_ROLE -> GAZELLE_ADMIN.getName();
            case MONITOR_ROLE -> MONITOR.getName();
            case TESTING_SESSION_ADMIN_ROLE -> TESTING_SESSION_MANAGER.getName();
            case VENDOR_LATE_REGISTRATION_ROLE -> LATE_REGISTRATION.getName();
            case PROJECT_MANAGER_ROLE -> PROJECT_ADMIN.getName();
            case TESTS_EDITOR_ROLE -> TEST_DESIGNER.getName();
            case VENDOR_ROLE -> SUT_OPERATOR.getName();
            default -> null;
        };
    }

    private String getNewOrgaGroupFromOldRole(String oldRoleName, String orgaId) {
        if (orgaId == null)
            return null;
        if (oldRoleName.equals(VENDOR_ADMIN_ROLE))
            return PREFIX_ORGANIZATION_ADMIN.getName() + orgaId;
        else
            return PREFIX_ORGANIZATION_MEMBER.getName() + orgaId;
    }
}
