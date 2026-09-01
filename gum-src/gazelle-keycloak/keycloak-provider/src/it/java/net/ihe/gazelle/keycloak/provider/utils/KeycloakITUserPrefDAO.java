package net.ihe.gazelle.keycloak.provider.utils;

import net.ihe.gazelle.keycloak.core.interlay.query.FunctionWithSQLException;
import net.ihe.gazelle.keycloak.core.interlay.query.GazelleConnectionProvider;
import net.ihe.gazelle.keycloak.core.interlay.query.GazelleQueryExecutor;
import net.ihe.gazelle.keycloak.core.interlay.query.UserSQLFragment;
import net.ihe.gazelle.user.management.api.application.configuration.DatabaseConfig;
import net.ihe.gazelle.user.management.api.domain.user.UserPreference;
import net.ihe.gazelle.user.management.commons.interlay.exceptions.GazelleSQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class    KeycloakITUserPrefDAO {

    private static final Logger log = LoggerFactory.getLogger(KeycloakITUserPrefDAO.class);

    public static final String SQL_GET_USER_PREFERENCES_BY_ID = """
        SELECT user_id, table_label, profile_picture, profile_thumbnail, notified_by_email, languages_spoken FROM gum.user_preference where user_id=?
        """;
    public static final String LANGUAGES_SPOKEN = "languages_spoken";
    public static final String TABLE_LABEL = "table_label";
    public static final String PROFILE_PICTURE = "profile_picture";
    public static final String NOTIFIED_BY_EMAIL = "notified_by_email";

    private final GazelleQueryExecutor queryExecutor;

    public KeycloakITUserPrefDAO(DatabaseConfig databaseConfig) {
        queryExecutor = new GazelleQueryExecutor(new GazelleConnectionProvider(databaseConfig));
    }

    public UserPreference getUserPrefById(String userId){
        return queryExecutor.executeQuerySingleResult(SQL_GET_USER_PREFERENCES_BY_ID, List.of(userId), new UserPrefMapper());
    }


    public static class UserPrefMapper  implements FunctionWithSQLException<ResultSet, UserPreference>  {

        public static final String PROFILE_THUMBNAIL = "profile_thumbnail";

        /**
         * Map a userPreference row to a GUM UserPreference
         * @param resultSet SQL resultSet
         * @return gazelle user
         */
        public UserPreference apply(ResultSet resultSet) {
            log.info(resultSet.toString());
            UserPreference gazelleUser;
            try {
                gazelleUser = new UserPreference();
                gazelleUser.setUserId(resultSet.getString(UserSQLFragment.ID));
                log.debug("mapUser {}", gazelleUser.getUserId());

                List<String> languages = resultSet.getString(LANGUAGES_SPOKEN) != null && !resultSet.getString(LANGUAGES_SPOKEN).isEmpty() ?
                        Arrays.stream(resultSet.getString(LANGUAGES_SPOKEN).split(",")).toList() : List.of();
                gazelleUser.setLanguagesSpoken(languages);

                gazelleUser.setTableLabel(resultSet.getString(TABLE_LABEL).equals("null") ? null : resultSet.getString(TABLE_LABEL));
                gazelleUser.setProfileThumbnailUri(resultSet.getString(PROFILE_THUMBNAIL)); // This attribute is usually used from URI but here we just need to check that there is data
                gazelleUser.setProfilePictureUri(resultSet.getString(PROFILE_PICTURE)); // This attribute is usually used from URI but here we just need to check that there is data
                gazelleUser.setNotifiedByEmail(resultSet.getBoolean(NOTIFIED_BY_EMAIL));
            } catch (SQLException e) {
                throw new GazelleSQLException("Failed to map user preference", e);
            }
            return gazelleUser;
        }
    }
}
