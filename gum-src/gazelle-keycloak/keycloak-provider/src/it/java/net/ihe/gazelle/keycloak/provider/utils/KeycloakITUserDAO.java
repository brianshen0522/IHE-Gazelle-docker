package net.ihe.gazelle.keycloak.provider.utils;

import net.ihe.gazelle.keycloak.core.interlay.query.FunctionWithSQLException;
import net.ihe.gazelle.keycloak.core.interlay.query.GazelleConnectionProvider;
import net.ihe.gazelle.keycloak.core.interlay.query.GazelleQueryExecutor;
import net.ihe.gazelle.keycloak.core.interlay.query.UserSQLFragment;
import net.ihe.gazelle.user.management.api.application.configuration.DatabaseConfig;
import net.ihe.gazelle.user.management.api.domain.user.User;
import net.ihe.gazelle.user.management.commons.interlay.exceptions.GazelleSQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class KeycloakITUserDAO {

    private static final Logger log = LoggerFactory.getLogger(KeycloakITUserDAO.class);

    private final static String SQL_REMOVE_GROUP_USER ="delete from gum.user_group where (user_id=? and group_id=?)";
    private final static String SQL_UPDATE_USER_ORGANIZATION ="update gum.user set organization_id=? where id=?";

    public static final String SQL_INSERT_ADD_GROUP_MOCK = "insert into gum.user_group (user_id,group_id) values " +
            "((select id from gum.user where id = ?), ?)";

    public static final String SQL_GET_USER_ORGANIZATION_GROUPS = """
        select distinct uu.id as user_id, firstname, lastname, email, uu.activated, last_login_timestamp, organization_id, grp.group_array as group_array
        from gum.user uu
        left join(
            select gum.user_group.user_id as id, array_agg(ug.id) as group_array
            from gum.group ug
            join gum.user_group
            on gum.user_group.user_id = user_id and gum.user_group.group_id = ug.id
            group by user_id
        ) grp using (id)
        """;

    /**
     * The constant SQL_USER_BY_EMAIL.
     */
    public static final String SQL_USER_BY_EMAIL = SQL_GET_USER_ORGANIZATION_GROUPS +
            "where lower(email) = lower(?)";

    private final GazelleQueryExecutor queryExecutor;

    public KeycloakITUserDAO(DatabaseConfig databaseConfig) {
        queryExecutor=new GazelleQueryExecutor(new GazelleConnectionProvider(databaseConfig));
    }

    /**
     * Retrieve password and reset password from GUM
     * @param userId the id of the user
     * @return a Map of String containing the password (0) and if the user needs to  reset it password (1)
     */
    public Map<String,String> getPasswordDataFromUserId(String userId){
        // Retrieve additional user data from GUM
        String CUSTOM_SQL_USER_BY_EMAIL = "SELECT credentials, reset_password FROM gum.credentials WHERE user_id=?";
        return queryExecutor.executeQuerySingleResult(
                CUSTOM_SQL_USER_BY_EMAIL,
                List.of(userId),
                (rs) -> Map.of("credentials",rs.getString("credentials"),"reset_password",rs.getString("reset_password"))
        );
    }

    public User getUserByEmail(String email){
        // Retrieve user from GUM
        User user = queryExecutor.executeQuerySingleResult(
                SQL_USER_BY_EMAIL,
                List.of(email),
                new UserMapper()
        );

        // Retrieve additional user data from GUM
        String CUSTOM_SQL_USER_BY_EMAIL = "SELECT activation_code, last_update_timestamp, registration_timestamp, login_counter FROM gum.user WHERE email=?";
        queryExecutor.executeQuerySingleResult(
                CUSTOM_SQL_USER_BY_EMAIL,
                List.of(email),
                (rs) -> {
                    user.setActivationCode(rs.getString("activation_code"));
                    if (rs.getTimestamp("last_update_timestamp") != null)
                        user.setLastUpdateTimestamp(rs.getTimestamp("last_update_timestamp").getTime());
                    if (rs.getTimestamp("registration_timestamp") != null)
                        user.setRegistrationTimestamp(rs.getTimestamp("registration_timestamp").getTime());
                    user.setLoginCounter(rs.getInt("login_counter"));
                    return user;
                }
        );
        return user;
    }

    public int addRoleForUserId(String userId, String group){
        return queryExecutor.executeUpdate(
                SQL_INSERT_ADD_GROUP_MOCK,
                List.of(userId,group)
        );
    }

    public int removeRoleForUserId(String userId, String group){
        return queryExecutor.executeUpdate(
                SQL_REMOVE_GROUP_USER,
                List.of(userId, group)
        );
    }

    public int updateUserOrganization(String organizationKeyword, String userId){
        return queryExecutor.executeUpdate(
                SQL_UPDATE_USER_ORGANIZATION,
                List.of(organizationKeyword,userId)
        );
    }

    public static class UserMapper implements FunctionWithSQLException<ResultSet, User> {
        /**
         * Map a user row to a Keycloak user
         *
         * @param rs SQL resultset
         * @return gazelle user
         */
        @Override
        public User apply(ResultSet rs) {
            User gazelleUser;
            try {
                // Create new user and set attributes
                gazelleUser = new User(rs.getString(UserSQLFragment.ID));
                log.debug("mapUser {}", gazelleUser.getId());
                gazelleUser.setFirstName(rs.getString(UserSQLFragment.FIRSTNAME));
                gazelleUser.setLastName(rs.getString(UserSQLFragment.LASTNAME));
                gazelleUser.setEmail(rs.getString(UserSQLFragment.EMAIL));
                gazelleUser.setActivated(rs.getBoolean(UserSQLFragment.IS_ACTIVATED));
                gazelleUser.setLastLoginTimestamp(rs.getTimestamp(UserSQLFragment.LAST_LOGIN).getTime());


                //the keyword used as an id is hashed because there is a bug in keycloak if there is a space in it
                if (rs.getString(UserSQLFragment.ORGANIZATION_KEYWORD) != null) {
                    gazelleUser.setOrganizationId(rs.getString(UserSQLFragment.ORGANIZATION_KEYWORD));
                }

                addGroupsToUser(rs, gazelleUser);

            } catch (SQLException e) {
                throw new GazelleSQLException("Failed to map user", e);
            }

            return gazelleUser;
        }

        private void addGroupsToUser(ResultSet rs, User gazelleUser) {
            try {
                String[] groups = (String[]) rs.getArray("group_array").getArray();

                gazelleUser.setGroupIds(Set.of(groups));
            } catch (Exception e) {
                log.error("No groups for the user {} ", gazelleUser.getId());
            }
        }
    }
}
