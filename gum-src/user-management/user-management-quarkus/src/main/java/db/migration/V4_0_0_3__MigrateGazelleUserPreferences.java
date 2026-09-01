package db.migration;

import io.smallrye.config.SmallRyeConfig;
import net.ihe.gazelle.user.management.api.domain.user.UserPreference;
import net.ihe.gazelle.user.management.commons.application.user.preference.ImageTransformationService;
import net.ihe.gazelle.user.management.commons.interlay.utils.ScalrImageService;
import org.eclipse.microprofile.config.ConfigProvider;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.postgresql.util.PSQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Flyway migration responsible for migrating user preferences from Gazelle database to GUM database.
 */
public class V4_0_0_3__MigrateGazelleUserPreferences extends BaseJavaMigration {

    private static final int USERID_KEY = 1;
    private static final int TABLE_LABEL_KEY = 2;
    private static final int EMAIL_NOTIFICATION_KEY = 3;
    private static final int SPOKEN_LANGUAGE_KEY = 4;
    private static final int PROFILE_PICTURE_KEY = 5;
    private static final int PROFILE_THUMBNAIL_KEY = 6;

    private final Logger logger = LoggerFactory.getLogger(V4_0_0_3__MigrateGazelleUserPreferences.class.getName());

    private static final String SELECT_USER_PREFERENCES_FROM_GAZELLE = """
SELECT username, spoken_languages, email_notification, connectathon_table, userphoto_id, photo_bytes
    FROM tm_user_preferences
    LEFT JOIN tm_user_photo as uph ON userphoto_id = uph.id""";
    private static final String SELECT_USER_ID_FROM_GUM = "SELECT id FROM gum.user";
    private static final String SQL_INSERT_USER_PREFERENCES_IN_GUM = "INSERT INTO gum.user_preference (user_id, table_label, notified_by_email, languages_spoken, profile_picture, profile_thumbnail) VALUES (?,?,?,?,?,?)";

    private static final ImageTransformationService imageTransformationService = new ScalrImageService();

    /** Creates the migration instance. */
    public V4_0_0_3__MigrateGazelleUserPreferences() {
        // Default constructor
    }

    @Override
    public void migrate(Context context) {
        try {
            // Retrieve Gazelle database connection parameters from Quarkus properties
            String dbTMJdbcUrl = ConfigProvider.getConfig().getValue("gzl.tm.jdbc.url", String.class);
            String dbTMJdbcUsername = ConfigProvider.getConfig().getValue("gzl.tm.jdbc.username", String.class);
            String dbTMJdbcPassword = ConfigProvider.getConfig().getValue("gzl.tm.jdbc.password", String.class);

            Connection connectionGazelle = getGazelleTMDatabaseConnection(dbTMJdbcUrl, dbTMJdbcUsername, dbTMJdbcPassword);
            if (connectionGazelle == null || !connectionGazelle.isValid(10)) {
                logger.warn("No gazelle-tm database detected, skipping migration of user preferences from TM to GUM.");
                return;
            }
            Connection connectionGUM = getGUMDatabaseConnection(context);

            List<GazelleUserPreferencesWrapper> prefWrapper = retrieveUserPreferencesFromGazelle(connectionGazelle);
            logger.info("Retrieved {} user preferences from Gazelle database", prefWrapper.size());
            connectionGazelle.close();

            // Sanitize user preferences
            sanitizeUserPref(connectionGUM, prefWrapper);

            // Insert data into GUM
            logger.info("Insert {} user preferences to GUM database", prefWrapper.size());
            insertUserPreferencesInGUM(connectionGUM, prefWrapper);
        } catch (SQLException e) {
            logger.error("Error while migrating users from Gazelle to GUM.", e);
            if (isProduction())
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

    /**
     * Retrieve user preferences (with picture) from Gazelle database
     */
    private List<GazelleUserPreferencesWrapper> retrieveUserPreferencesFromGazelle(Connection connection) throws SQLException {
        try (PreparedStatement statementSelectUserPref = connection.prepareStatement(SELECT_USER_PREFERENCES_FROM_GAZELLE)) {
            ResultSet resultSet = statementSelectUserPref.executeQuery();
            List<GazelleUserPreferencesWrapper> listUserPreferences = new LinkedList<>();

            // Iterate on all resultSet rows
            while (resultSet.next()) {
                UserPreference userPreference = new UserPreference();
                userPreference.setUserId(resultSet.getString("username"));
                userPreference.setNotifiedByEmail(resultSet.getBoolean("email_notification"));
                userPreference.setTableLabel(resultSet.getString("connectathon_table"));

                GazelleUserPreferencesWrapper prefWrapper = new GazelleUserPreferencesWrapper();
                prefWrapper.setUserPreference(userPreference);
                prefWrapper.setSpokenLanguagesString(resultSet.getString("spoken_languages"));
                prefWrapper.setUserPhoto(resultSet.getBytes("photo_bytes"));

                listUserPreferences.add(prefWrapper);
            }
            return listUserPreferences;
        }
    }

    /**
     * Sanitize users preferences (resize pictures) + Remove pref corresponding to non-existing user
     */
    private void sanitizeUserPref(Connection connection, List<GazelleUserPreferencesWrapper> prefWrappers) throws SQLException {
        try (PreparedStatement statementSelectUserId = connection.prepareStatement(SELECT_USER_ID_FROM_GUM)) {
            ResultSet resultSet = statementSelectUserId.executeQuery();
            List<String> userIds = new ArrayList<>();
            while (resultSet.next()) {
                userIds.add(resultSet.getString("id"));
            }
            prefWrappers.removeIf(pref -> !userIds.contains(pref.getUserPreference().getUserId()));

            for (GazelleUserPreferencesWrapper prefWrapper : prefWrappers) {
                if (prefWrapper.getUserPicture() != null && prefWrapper.getUserPicture().length != 0) {
                    byte[] userPhoto = imageTransformationService.transformImageToJpeg(prefWrapper.getUserPicture());
                    prefWrapper.setUserPhoto(userPhoto);

                    byte[] userPhotoThumbnail = imageTransformationService.generateThumbnail(prefWrapper.getUserPicture());
                    prefWrapper.setUserPhotoThumbnail(userPhotoThumbnail);
                }
            }
        }
    }

    /**
     * Insert user preferences in GUM
     */
    private void insertUserPreferencesInGUM(Connection connection, List<GazelleUserPreferencesWrapper> listPrefWrapper) throws SQLException {
        try (PreparedStatement statementInsertUser = connection.prepareStatement(SQL_INSERT_USER_PREFERENCES_IN_GUM)) {

            int counter = 0;
            logger.info("Inserting users into {}", connection.getMetaData().getURL());
            for (GazelleUserPreferencesWrapper preferencesWrapper : listPrefWrapper) {

                // Set parameters to the statementInsertUser
                statementInsertUser.clearParameters();
                statementInsertUser.setString(USERID_KEY, preferencesWrapper.getUserPreference().getUserId());
                statementInsertUser.setString(TABLE_LABEL_KEY, preferencesWrapper.getUserPreference().getTableLabel());
                statementInsertUser.setBoolean(EMAIL_NOTIFICATION_KEY, preferencesWrapper.getUserPreference().isNotifiedByEmail());
                statementInsertUser.setString(SPOKEN_LANGUAGE_KEY, preferencesWrapper.getSpokenLanguagesString());
                statementInsertUser.setBytes(PROFILE_PICTURE_KEY, preferencesWrapper.getUserPicture());
                statementInsertUser.setBytes(PROFILE_THUMBNAIL_KEY, preferencesWrapper.getUserThumbnail());

                // Add statementInsertUser to the batch
                statementInsertUser.addBatch();
                logger.info("Add preferences of user {}.", preferencesWrapper.getUserPreference().getUserId());
                // Execute the batch every 25 statements
                if ((counter++ % 25) == 0) {
                    int[] result = statementInsertUser.executeBatch();
                    for (int i : result) {
                        if (i == 0) {
                            logger.warn("Failed to insert user preferences");
                            // For dev and tests deployments we don't want that this migration is critical, only for prod
                            if (isProduction()) throw new GazelleMigrationException("Failed to insert user preferences");
                        }
                    }
                }
            }
            statementInsertUser.executeBatch();
        }
    }
}
