package net.ihe.gazelle.keycloak.core.interlay.configuration;

import net.ihe.gazelle.user.management.api.application.configuration.ApplicationConfig;

import java.util.Objects;

/**
 * Implementation of ApplicationConfig that retrieves configuration values from environment variables.
 */
public class ApplicationConfigImpl implements ApplicationConfig {

    private static final String BASE_GUM_PATH = "/gum";
    private final String gazelleTMUrl;
    private final String rootTestBedUrl;
    private final String keycloakBaseUrl;
    private final String gumUIBaseUrl;
    private final String organizationCreationEnabled;
    private final String userRegistrationEnabled;
    private final String realmName;
    private final int purgeInactivatedUsersAfterDays;
    private final String userCreationEmailNotificationEnabled;

    /**
     * Constructor for ApplicationConfigImpl that initializes configuration values from environment variables.
     */
    public ApplicationConfigImpl() {
        gazelleTMUrl = Objects.requireNonNull(System.getenv("GZL_TM_URL"),
                "Environment variable GZL_TM_URL must not be null.");
        rootTestBedUrl = Objects.requireNonNull(System.getenv("ROOT_TEST_BED_URL"),
                "Environment variable ROOT_TEST_BED_URL must not be null.");
        keycloakBaseUrl = Objects.requireNonNull(System.getenv("KC_HOSTNAME"));

        gumUIBaseUrl = System.getenv("GZL_USER_MANAGEMENT_FRONT_URL");
        organizationCreationEnabled = Objects.requireNonNullElse(System.getenv("GZL_ORGANIZATION_CREATION_ENABLED"), "true");
        userRegistrationEnabled = Objects.requireNonNullElse(System.getenv("GZL_USER_REGISTRATION_ENABLED"), "true");
        realmName = Objects.requireNonNull(System.getenv("GZL_SSO_REALM"),
                "Environment variable GZL_SSO_REALM must not be null.");
        purgeInactivatedUsersAfterDays = System.getenv("GZL_USER_INACTIVATED_PURGE_AFTER_DAYS") != null
                ? Integer.parseInt(System.getenv("GZL_USER_INACTIVATED_PURGE_AFTER_DAYS"))
                : 31;
        userCreationEmailNotificationEnabled = Objects.requireNonNullElse(System.getenv("GZL_USER_CREATION_EMAIL_NOTIFICATION_ENABLED"), "true");

    }

    @Override
    public String getGazelleTMUrl() {
        return gazelleTMUrl;
    }

    @Override
    public String getRootTestBedUrl() {
        return rootTestBedUrl;
    }

    @Override
    public String getSSOBaseUrl() {
        return keycloakBaseUrl;
    }

    @Override
    public String getGUMBaseUrl() {
        return getRootTestBedUrl() + BASE_GUM_PATH;
    }

    @Override
    public String getGUMUIBaseUrl() {
        return gumUIBaseUrl;
    }

    @Override
    public String getRealmName() {
        return realmName;
    }

    @Override
    public boolean isOrganizationCreationEnabled() {
        return Boolean.parseBoolean(organizationCreationEnabled);
    }

    @Override
    public boolean isUserRegistrationEnabled() {
        return Boolean.parseBoolean(userRegistrationEnabled);
    }

    @Override
    public String getTermsOfServiceUrl() {
        return System.getenv("GZL_TERMS_OF_SERVICE_URL");
    }

    @Override
    public String getPrivacyPolicyUrl() {
        return System.getenv("GZL_PRIVACY_POLICY_URL");
    }


    @Override
    public int getPurgeInactivatedUsersDaysLimit() {
        return purgeInactivatedUsersAfterDays;
    }

    @Override
    public boolean isUserCreationEmailNotificationEnabled() {
        return Boolean.parseBoolean(userCreationEmailNotificationEnabled);
    }
}
