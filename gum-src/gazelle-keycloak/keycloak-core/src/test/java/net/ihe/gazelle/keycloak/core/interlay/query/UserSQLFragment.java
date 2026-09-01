package net.ihe.gazelle.keycloak.core.interlay.query;

/**
 * Gazelle DAO
 */
public class UserSQLFragment {

    private UserSQLFragment() {
        // utility class
    }

    /**
     * The constant USER_ID.
     */
    public static final String ID = "user_id";

    /**
     * The constant EMAIL.
     */
    public static final String EMAIL = "email";

    /**
     * The constant FIRSTNAME.
     */
    public static final String FIRSTNAME = "firstname";

    /**
     * The constant LASTNAME.
     */
    public static final String LASTNAME = "lastname";

    /**
     * The constant IS_ACTIVATED.
     */
    public static final String IS_ACTIVATED = "activated";

    /**
     * The constant LAST_LOGIN.
     */
    public static final String LAST_LOGIN = "last_login_timestamp";

    /**
     * The constant ORGANIZATION_KEYWORD.
     */
    public static final String ORGANIZATION_KEYWORD = "organization_id";
}
