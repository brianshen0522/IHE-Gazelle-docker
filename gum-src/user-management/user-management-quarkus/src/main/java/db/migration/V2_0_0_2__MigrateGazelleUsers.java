package db.migration;

import io.smallrye.config.SmallRyeConfig;
import net.ihe.gazelle.user.management.api.domain.user.User;
import org.eclipse.microprofile.config.ConfigProvider;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.postgresql.util.PSQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Migration used to migrate users from Gazelle TM database to GUM database. It retrieves users from Gazelle TM database, sanitize them and insert them into GUM database.
 */
public class V2_0_0_2__MigrateGazelleUsers extends BaseJavaMigration {

    private static final int ID_KEY = 1;
    private static final int EMAIL_KEY = 2;
    private static final int ROLE_KEY = 2;
    private static final int DESCRIPTION_KEY = 2;
    private static final int FIRSTNAME_KEY = 3;
    private static final int RESET_PWD_KEY = 3;
    private static final int LASTNAME_KEY = 4;
    private static final int ACTIVATION_CODE_KEY = 5;
    private static final int ACTIVATED_KEY = 6;
    private static final int LOGIN_COUNTER_KEY = 7;
    private static final int REGISTRATION_TIMESTAMP_KEY = 8;
    private static final int LAST_LOGIN_TIMETAMP_KEY = 9;
    private static final int LAST_UPDATE_KEY = 10;
    private static final int ORGANIZATION_ID_KEY = 11;

    Logger logger = LoggerFactory.getLogger(V2_0_0_2__MigrateGazelleUsers.class.getName());

    private static final String SELECT_USERS_FROM_GAZELLE = """
SELECT DISTINCT uu.id AS user_id, username, email, firstname, lastname, activation_code, uu.activated, counter_logins, creation_date, last_login, uu.last_changed, ui.keyword as institution_keyword, roles.role_array as role_array, password
            FROM usr_users uu
            left join (
                select uur.user_id as id, array_agg(ur.name) as role_array
                from usr_role ur
                join usr_user_role uur
                on uur.user_id = user_id and uur.role_id = ur.id
                group by user_id
            )roles using (id)
            left join usr_institution ui
            on ui.id = uu.institution_id""";

    private static final String SQL_INSERT_USERS_IN_GUM = "INSERT INTO gum.user (id, email, firstname, lastname, activation_code, activated, login_counter, registration_timestamp, last_login_timestamp,last_update_timestamp,organization_id) VALUES (?,?,?,?,?,?,?,?,?,?,?)";

    private static final String SQL_INSERT_CREDENTIALS_IN_GUM = "INSERT INTO gum.credentials (user_id,credentials,reset_password) VALUES (?,?,?)";

    private static final String SQL_INSERT_ROLES_IN_GUM = "INSERT INTO gum.role (id, name, description) VALUES (nextval('gum.role_seq'),?,?) ON CONFLICT DO NOTHING";

    private static final String SQL_MAP_ROLE_IN_GUM = "INSERT INTO gum.user_role (user_id,role_id) VALUES (?,(SELECT id from gum.role WHERE name=?))";

    /** Constructor with no parameter, required by Flyway */
    public  V2_0_0_2__MigrateGazelleUsers() {
        // Default constructor
    }

    @Override
    public void migrate(Context context) {
        // Get connection to Gazelle database
        try {
            // Retrieve Gazelle database connection parameters from Quarkus properties
            String dbTMJdbcUrl = ConfigProvider.getConfig().getValue("gzl.tm.jdbc.url", String.class);
            String dbTMJdbcUsername = ConfigProvider.getConfig().getValue("gzl.tm.jdbc.username", String.class);
            String dbTMJdbcPassword = ConfigProvider.getConfig().getValue("gzl.tm.jdbc.password", String.class);

            Connection connectionGazelle = getGazelleTMDatabaseConnection(dbTMJdbcUrl, dbTMJdbcUsername, dbTMJdbcPassword);
            Connection connectionGUM = getGUMDatabaseConnection(context);

            // If gazelle database is not found, we skip de migration
            // it's possible to deploy GUM without TM and so potential no user to migrate
             if (connectionGazelle == null || !connectionGazelle.isValid(10)) {
               logger.warn("No gazelle-tm database detected, skipping migration of users from TM to GUM.");
               return;
             }

            List<GazelleUserWrapper> gazelleUsers = retrieveUsersFromGazelle(connectionGazelle);
            logger.info("Retrieved {} users from Gazelle database", gazelleUsers.size());
            connectionGazelle.close();

            // Sanitize users (lowercase emails and ids)
            sanitizeUsers(gazelleUsers);

            // Insert data into GUM
            insertUsersIntoGUM(connectionGUM, gazelleUsers);
            insertCredentialsIntoGUM(connectionGUM, gazelleUsers);
            insertRolesIntoGUM(connectionGUM, gazelleUsers);
        } catch (SQLException e) {
            throw new GazelleMigrationException("Error while migrating users from Gazelle to GUM", e);
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
     * Check if we are in production profile
     * @return true if the application is in production profile false otherwise
     */
    private boolean isProduction() {
        return ConfigProvider.getConfig().unwrap(SmallRyeConfig.class).getProfiles().contains("prod");
    }

    private List<GazelleUserWrapper> retrieveUsersFromGazelle(Connection connection) throws SQLException {
        try (PreparedStatement statementSelectUsers = connection.prepareStatement(SELECT_USERS_FROM_GAZELLE)) {
            ResultSet resultSet = statementSelectUsers.executeQuery();

            List<GazelleUserWrapper> gazelleUserWrappers = new LinkedList<>();

            // Iterate on all resultSet rows
            while (resultSet.next()) {
                User user = new User(resultSet.getString("username"));
                user.setEmail(resultSet.getString("email"));
                user.setFirstName(resultSet.getString("firstname"));
                user.setLastName(resultSet.getString("lastname"));
                user.setActivationCode(resultSet.getString("activation_code"));
                user.setActivated(resultSet.getBoolean("activated"));
                user.setLoginCounter(resultSet.getInt("counter_logins"));
                user.setOrganizationId(resultSet.getString("institution_keyword"));
                user.setRegistrationTimestamp(resultSet.getTimestamp("creation_date") != null ? resultSet.getTimestamp("creation_date").getTime() : 0);
                user.setLastLoginTimestamp(resultSet.getTimestamp("last_login") != null ? resultSet.getTimestamp("last_login").getTime() : 0);
                user.setLastUpdateTimestamp(resultSet.getTimestamp("last_changed") != null ? resultSet.getTimestamp("last_changed").getTime() : 0);

                String[] rolesArray = null;
                Array roles = resultSet.getArray("role_array");
                if (roles != null) {
                    rolesArray = (String[]) roles.getArray();
                } else {
                    rolesArray = new String[0];
                }
                GazelleUserWrapper gazelleUserWrapper = new GazelleUserWrapper(user, resultSet.getString("password"), rolesArray);
                gazelleUserWrappers.add(gazelleUserWrapper);
            }
            return gazelleUserWrappers;
        }
    }

    private void insertUsersIntoGUM(Connection connection, List<GazelleUserWrapper> gazelleUserWrappers) throws SQLException {
        try (PreparedStatement statementInsertUser = connection.prepareStatement(SQL_INSERT_USERS_IN_GUM)) {

            int counter = 0;
            logger.info("Inserting users into {}", connection.getMetaData().getURL());
            for (GazelleUserWrapper gazelleUserWrapper : gazelleUserWrappers) {
                User user = gazelleUserWrapper.getUser();

                // Set parameters to the statementInsertUser
                statementInsertUser.clearParameters();
                statementInsertUser.setString(ID_KEY, user.getId());
                statementInsertUser.setString(EMAIL_KEY, user.getEmail());
                statementInsertUser.setString(FIRSTNAME_KEY, user.getFirstName());
                statementInsertUser.setString(LASTNAME_KEY, user.getLastName());
                statementInsertUser.setString(ACTIVATION_CODE_KEY, user.getActivationCode());
                statementInsertUser.setBoolean(ACTIVATED_KEY, user.isActivated());
                statementInsertUser.setInt(LOGIN_COUNTER_KEY, user.getLoginCounter());
                statementInsertUser.setTimestamp(REGISTRATION_TIMESTAMP_KEY, new Timestamp(user.getRegistrationTimestamp()));
                statementInsertUser.setTimestamp(LAST_LOGIN_TIMETAMP_KEY, new Timestamp(user.getLastLoginTimestamp()));
                statementInsertUser.setTimestamp(LAST_UPDATE_KEY, new Timestamp(user.getLastUpdateTimestamp()));
                statementInsertUser.setString(ORGANIZATION_ID_KEY, user.getOrganizationId());

                // Add statementInsertUser to the batch
                statementInsertUser.addBatch();
                logger.info("Add user {} ({}).", user.getId(), user.getEmail());
                // Execute the batch every 25 statements
                if ((counter++ % 25) == 0) {
                    int[] result = statementInsertUser.executeBatch();
                    for (int i : result) {
                        if (i == 0) {
                            logger.warn("Failed to insert users");
                            if (isProduction()) throw new GazelleMigrationException("Failed to insert users");
                        }
                    }
                }
            }
            // Execute the batch
            statementInsertUser.executeBatch();
        }
    }

    private void insertCredentialsIntoGUM(Connection connection, List<GazelleUserWrapper> gazelleUserWrappers) throws SQLException {
        try (PreparedStatement statementInsertCredentials = connection.prepareStatement(SQL_INSERT_CREDENTIALS_IN_GUM)) {

            int counter = 0;
            for (GazelleUserWrapper gazelleUserWrapper : gazelleUserWrappers) {
                // Add parameters to the statementInsertCredentials
                statementInsertCredentials.clearParameters();
                statementInsertCredentials.setString(ID_KEY, gazelleUserWrapper.getUser().getId());
                String credentialsAsJson = "{\"password\":\"" + gazelleUserWrapper.getPassword() + "\", \"hashMethod\": \"MD5\"}";
                statementInsertCredentials.setString(EMAIL_KEY, credentialsAsJson);
                statementInsertCredentials.setBoolean(RESET_PWD_KEY, true);

                statementInsertCredentials.addBatch();

                // Execute the batch every 25 statements
                if ((counter++ % 25) == 0) {
                    statementInsertCredentials.executeBatch();
                }
            }
            // Execute the batch
            statementInsertCredentials.executeBatch();
        }
    }

    private void insertRolesIntoGUM(Connection connection, List<GazelleUserWrapper> gazelleUserWrappers) throws SQLException {
        try (PreparedStatement statementInsertRoles = connection.prepareStatement(SQL_INSERT_ROLES_IN_GUM)) {
            try (PreparedStatement statementMapRoles = connection.prepareStatement(SQL_MAP_ROLE_IN_GUM)) {

                int counter = 0;
                // For each user and for each role
                for (GazelleUserWrapper gazelleUserWrapper : gazelleUserWrappers) {
                    for (String roleName : gazelleUserWrapper.getRoles()) {
                        statementInsertRoles.clearParameters();
                        statementInsertRoles.setString(ID_KEY, roleName);
                        statementInsertRoles.setString(DESCRIPTION_KEY, "Migrated role, description not available");
                        statementInsertRoles.addBatch();

                        statementMapRoles.clearParameters();
                        statementMapRoles.setString(ID_KEY, gazelleUserWrapper.getUser().getId());
                        statementMapRoles.setString(ROLE_KEY, roleName);
                        statementMapRoles.addBatch();
                    }

                    // Execute the batch every 15 statements
                    if ((counter++ % 20) == 0) {
                        statementInsertRoles.executeBatch();
                        statementMapRoles.executeBatch();

                    }
                }
                // Execute the batch
                statementInsertRoles.executeBatch();
                statementMapRoles.executeBatch();
            }
        }
    }

    /**
     * Sanitize users (lowercase emails and ids) + Manage conflicts (remove the user with 0 login and not activated)
     * @param gazelleUserWrappers list of users to sanitize
     */
    private void sanitizeUsers(List<GazelleUserWrapper> gazelleUserWrappers) {
        Map<String,GazelleUserWrapper> userIds = new HashMap<>();
        Map<String,String> userEmails = new HashMap<>();
        List<GazelleUserWrapper> usersToRemove = new LinkedList<>();

        gazelleUserWrappers.forEach(gazelleUserWrapper -> {
            User user = gazelleUserWrapper.getUser();
            user.setId(user.getId().toLowerCase());
            user.setEmail(user.getEmail().toLowerCase());

            // Retrieve potential existing user in the map
            GazelleUserWrapper potentialUserWrapperById = userIds.get(user.getId());
            GazelleUserWrapper potentialUserWrapperByEmail = userIds.get(userEmails.get(user.getEmail()));

            if (isThereAConflict(potentialUserWrapperById, user)) {
                manageConflict(gazelleUserWrapper, potentialUserWrapperById, usersToRemove);
                // Keep the email of the user to avoid conflict with another user with the same email
                if (!usersToRemove.contains(gazelleUserWrapper))
                    userEmails.put(user.getEmail(), user.getId());
            } else if (isThereAConflict(potentialUserWrapperByEmail, user)) {
                manageConflict(gazelleUserWrapper, potentialUserWrapperByEmail, usersToRemove);
                // Keep the id of the user to avoid conflict with another user with the same id
                if (!usersToRemove.contains(gazelleUserWrapper))
                    userIds.put(user.getId(), gazelleUserWrapper);
            } else {
                // Add the user to the map
                userIds.put(user.getId(), gazelleUserWrapper);
                userEmails.put(user.getEmail(), user.getId());
            }
        });

        gazelleUserWrappers.removeAll(usersToRemove);
    }

    /**
     * Manage conflict between a left and a right user
     * @param leftUserWrapper the left user
     * @param rightUserWrapper the right user
     * @param usersToRemove list of users to remove where the user to remove must be added
     */
    private void manageConflict(GazelleUserWrapper leftUserWrapper, GazelleUserWrapper rightUserWrapper, List<GazelleUserWrapper> usersToRemove) {
        User leftUser = leftUserWrapper.getUser();
        User rightUser = rightUserWrapper.getUser();

        logger.warn("[CONFLICT] between users {} ({}) and {} ({}).",
                leftUser.getId(), leftUser.getEmail(), rightUser.getId(), rightUser.getEmail());
        // If the potential existing user is not activated and has 0 login, we remove it
        if (isANeverActivatedUser(rightUserWrapper))  {
            logger.warn("Remove user {} because it not activated with 0 login.",
                    rightUserWrapper.getUser().getId());
            usersToRemove.add(rightUserWrapper);
        // If the current user is not activated and has 0 login, we remove it
        } else if (isANeverActivatedUser(leftUserWrapper)) {
            logger.warn("Remove user {} because it not activated with 0 login.",
                    leftUserWrapper.getUser().getId());
            usersToRemove.add(leftUserWrapper);
        // If unable to select one of the users, we stop the migration
        } else {
            logger.warn("[FATAL] Unable to select one of the users, stop migration !!!");
            throw new GazelleMigrationException("[CONFLICT] between users " + leftUser.getId()
                    +" ("+ leftUser.getEmail() + ") and "+ rightUser.getId()+" ("+ rightUser.getEmail()+").");
        }
    }

    private static boolean isThereAConflict(GazelleUserWrapper potentialUserWrapper, User user) {
        if (potentialUserWrapper == null) return false;
        User potentialUser = potentialUserWrapper.getUser();
        return potentialUser.getEmail().toLowerCase().equals(user.getEmail())
                || potentialUser.getId().toLowerCase().equals(user.getId());
    }

    private static boolean isANeverActivatedUser(GazelleUserWrapper gazelleUserWrapper) {
        User user = gazelleUserWrapper.getUser();
        return !user.isActivated() && user.getLoginCounter() == 0;
    }
}
