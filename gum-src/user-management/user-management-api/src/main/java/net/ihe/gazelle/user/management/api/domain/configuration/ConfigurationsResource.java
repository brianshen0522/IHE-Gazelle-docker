package net.ihe.gazelle.user.management.api.domain.configuration;

import java.util.Objects;

/**
 * Resource class for configuration data in Gazelle User Management.
 * <p>
 * This class is used to transfer configuration information between API layers.
 * </p>
 */
public class ConfigurationsResource {

    /**
     * Flag indicating whether user registration is enabled.
     */
    private boolean userRegistrationEnabled;

    /**
     * Flag indicating whether organization creation is enabled.
     */
    private boolean organizationCreationEnabled;

    /**
     * URL pointing to the terms of service document.
     */
    private String termsOfServiceUrl;

    /**
     * URL pointing to the privacy policy document.
     */
    private String privacyPolicyUrl;

    /**
     * Number of days after which inactive users should be purged.
     */
    private int purgeInactivatedUsersAfterDays;

    /**
     * Flag indicating whether email notifications should be sent upon user creation.
     */
    private boolean userCreationEmailNotificationEnabled;

    /**
     * Default constructor.
     *
     * Creates an empty ConfigurationsResource instance with default values.
     */
    public ConfigurationsResource() {
        // Default constructor
    }

    /**
     * Checks if user registration is enabled.
     *
     * @return true if user registration is enabled, false otherwise
     */
    public boolean isUserRegistrationEnabled() {
        return userRegistrationEnabled;
    }

    /**
     * Sets the user registration enabled flag.
     *
     * @param userRegistrationEnabled true to enable user registration, false to disable
     */
    public void setUserRegistrationEnabled(boolean userRegistrationEnabled) {
        this.userRegistrationEnabled = userRegistrationEnabled;
    }

    /**
     * Checks if organization creation is enabled.
     *
     * @return true if organization creation is enabled, false otherwise
     */
    public boolean isOrganizationCreationEnabled() {
        return organizationCreationEnabled;
    }

    /**
     * Sets the organization creation enabled flag.
     *
     * @param organizationCreationEnabled true to enable organization creation, false to disable
     */
    public void setOrganizationCreationEnabled(boolean organizationCreationEnabled) {
        this.organizationCreationEnabled = organizationCreationEnabled;
    }

    /**
     * Gets the terms of service URL.
     *
     * @return the URL pointing to the terms of service document, or null if not set
     */
    public String getTermsOfServiceUrl() {
        return termsOfServiceUrl;
    }

    /**
     * Sets the terms of service URL.
     *
     * @param termsOfServiceUrl the URL pointing to the terms of service document
     */
    public void setTermsOfServiceUrl(String termsOfServiceUrl) {
        this.termsOfServiceUrl = termsOfServiceUrl;
    }


    /**
     * Gets the privacy policy URL.
     *
     * @return the URL pointing to the privacy policy document, or null if not set
     */
    public String getPrivacyPolicyUrl() {
        return privacyPolicyUrl;
    }

    /**
     * Sets the privacy policy URL.
     *
     * @param privacyPolicyUrl the URL pointing to the privacy policy document
     */
    public void setPrivacyPolicyUrl(String privacyPolicyUrl) {
        this.privacyPolicyUrl = privacyPolicyUrl;
    }

    /**
     * Gets the number of days after which inactive users should be purged.
     *
     * @return the number of days for user purge threshold
     */
    public int getPurgeInactivatedUsersAfterDays() {
        return purgeInactivatedUsersAfterDays;
    }

    /**
     * Sets the number of days after which inactive users should be purged.
     *
     * @param purgeInactivatedUsersAfterDays the number of days for user purge threshold
     */
    public void setPurgeInactivatedUsersAfterDays(int purgeInactivatedUsersAfterDays) {
        this.purgeInactivatedUsersAfterDays = purgeInactivatedUsersAfterDays;
    }

    /**
     * Checks if email notifications are enabled for user creation.
     *
     * @return true if email notifications should be sent upon user creation, false otherwise
     */
    public boolean isUserCreationEmailNotificationEnabled() {
        return userCreationEmailNotificationEnabled;
    }

    /**
     * Sets the user creation email notification flag.
     *
     * @param userCreationEmailNotificationEnabled true to enable email notifications, false to disable
     */
    public void setUserCreationEmailNotificationEnabled(boolean userCreationEmailNotificationEnabled) {
        this.userCreationEmailNotificationEnabled = userCreationEmailNotificationEnabled;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        ConfigurationsResource that = (ConfigurationsResource) object;
        return userRegistrationEnabled == that.userRegistrationEnabled
                && organizationCreationEnabled == that.organizationCreationEnabled
                && purgeInactivatedUsersAfterDays == that.purgeInactivatedUsersAfterDays
                && userCreationEmailNotificationEnabled == that.userCreationEmailNotificationEnabled
                && Objects.equals(termsOfServiceUrl, that.termsOfServiceUrl)
                && Objects.equals(privacyPolicyUrl, that.privacyPolicyUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userRegistrationEnabled,
                organizationCreationEnabled,
                termsOfServiceUrl,
                privacyPolicyUrl,
                purgeInactivatedUsersAfterDays,
                userCreationEmailNotificationEnabled);
    }
}
