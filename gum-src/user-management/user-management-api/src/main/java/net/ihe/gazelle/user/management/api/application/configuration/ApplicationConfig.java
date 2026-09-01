package net.ihe.gazelle.user.management.api.application.configuration;

/**
 * Configuration interface for application-wide settings.
 *
 * This interface defines methods to access various configuration parameters
 * used throughout the Gazelle User Management application, including URLs
 * for external services, security settings, and feature toggles.
 *
 */
public interface ApplicationConfig {

    /**
     * The security scheme used in Openapi and Swagger-ui
     */
    String SECURITY_SCHEME = "Keycloak";

    /**
     * Get the url of gazelle Test Management
     * @return the url of the gazelle TM
     */
    String getGazelleTMUrl();
    /**
     * Get the root url of the test bed
     * @return the root url of the test bed
     */
    String getRootTestBedUrl();

    /**
     * Get the base url of Keycloak
     * @return the base url of Keycloak
     */
    String getSSOBaseUrl();

    /**
     * Get the base url of GUM rest api
     * @return the base url of GUM rest api
     */
    String getGUMBaseUrl();

    /**
     * Get the base url of GUM UI
     * @return the base url of GUM UI
     */
    String getGUMUIBaseUrl();

    /**
     * Get the name of the realm used
     * @return the name of th realm
     */
    String getRealmName();

    /**
     * The application is configured to allow or not the creation of an organization
     * @return true if the creation of an organization is enabled, else false
     */
    boolean isOrganizationCreationEnabled();

    /**
     * The application is configured to allow or not the registration of a user
     * @return true if the registration of a user is enabled, else false
     */
    boolean isUserRegistrationEnabled();

    /**
     * Get the terms of use url
     * @return the terms of use url
     */
    String getTermsOfServiceUrl();

    /**
     * Get the privacy policy url
     * @return the privacy policy url
     */
    String getPrivacyPolicyUrl();

    /**
     * Get the limit in days for the users purge.
     * @return an int of limit in days
     */
    int getPurgeInactivatedUsersDaysLimit();

    /**
     * Is the application configured to send mail upon user creation
     * @return true if email notification should be sent after user creation, false otherwise
     */
    boolean isUserCreationEmailNotificationEnabled();
}
