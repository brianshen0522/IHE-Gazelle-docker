package net.ihe.gazelle.user.management.core.interlay.configuration;

import jakarta.enterprise.context.RequestScoped;
import net.ihe.gazelle.m2m.client.technical.converter.CleanedString;
import net.ihe.gazelle.user.management.api.application.configuration.ApplicationConfig;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.Optional;

/**
 * Implementation of ApplicationConfig that retrieves configuration values from the application's configuration properties.
 * This class is annotated with @RequestScoped, meaning that a new instance will be created for each HTTP request.
 */
@RequestScoped
public class ApplicationConfigImpl implements ApplicationConfig {

    @ConfigProperty(name = "gzl.tm.url")
    String gazelleTMUrl;
    @ConfigProperty(name = "gzl.root.test.bed.url")
    String rootTestBedUrl;
    @ConfigProperty(name = "gzl.sso.url")
    String ssoUrl;
    @ConfigProperty(name = "gzl.user.management.front.url")
    Optional<String> gumUIBaseUrl;
    @ConfigProperty(name = "gzl.sso.realm.name")
    String realmName;
    @ConfigProperty(name = "gzl.organization.creation.enabled")
    Optional<String> organizationCreationEnabled;
    @ConfigProperty(name = "gzl.user.registration.enabled")
    Optional<String> userRegistrationEnabled;
    @ConfigProperty(name = "gzl.terms.of.service.url")
    CleanedString termsOfServiceUrl;
    @ConfigProperty(name = "gzl.privacy.policy.url")
    CleanedString privacyPolicyUrl;
    @ConfigProperty(name = "quarkus.http.root-path")
    String basePath;
    @ConfigProperty(name = "gzl.user.inactivated.purge.after.days")
    int purgeInactivatedUsersAfterDays;
    @ConfigProperty(name = "gzl.user.creation.email.notification.enabled")
    boolean userCreationEmailNotificationEnabled;

    /** Default constructor for CDI. */
    public ApplicationConfigImpl() {
        // Nothing to do here
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
        return ssoUrl;
    }

    @Override
    public String getGUMBaseUrl() {
        return getRootTestBedUrl() + basePath;
    }

    @Override
    public String getGUMUIBaseUrl() {
        return gumUIBaseUrl.orElse(null);
    }

    @Override
    public String getRealmName() {
        return realmName;
    }

    @Override
    public boolean isOrganizationCreationEnabled() {
        return organizationCreationEnabled.isEmpty() || Boolean.parseBoolean(organizationCreationEnabled.get());
    }

    @Override
    public boolean isUserRegistrationEnabled() {
        return userRegistrationEnabled.isEmpty() || Boolean.parseBoolean(userRegistrationEnabled.get());
    }

    @Override
    public String getTermsOfServiceUrl() {
        return termsOfServiceUrl.getString();
    }

    @Override
    public String getPrivacyPolicyUrl() {
        return privacyPolicyUrl.getString();
    }

    @Override
    public int getPurgeInactivatedUsersDaysLimit() {
        return purgeInactivatedUsersAfterDays;
    }

    @Override
    public boolean isUserCreationEmailNotificationEnabled() {
        return userCreationEmailNotificationEnabled;
    }
}
