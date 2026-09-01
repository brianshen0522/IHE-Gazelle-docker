package net.ihe.gazelle.user.management.commons;

import net.ihe.gazelle.user.management.api.application.configuration.ApplicationConfig;
import net.ihe.gazelle.user.management.api.application.configuration.DatabaseConfig;

import java.util.Objects;

/**
 * DB config (cache key)
 */
public class ConfigurationsMock implements DatabaseConfig, ApplicationConfig {
    private boolean organizationCreationEnabled;
    private boolean userRegistrationEnabled;
    String jdbcUrl;
    String driverClass;
    String username;
    String password;

    public ConfigurationsMock(String driverClass, String jdbcUrl, String username, String password) {
        this.jdbcUrl =  jdbcUrl;
        this.driverClass =  driverClass;
        this.username =  username;
        this.password =  password;
    }

    public ConfigurationsMock() {
        this.organizationCreationEnabled = true;
        this.userRegistrationEnabled = true;
    }

    @Override
    public Integer getReapConnectionTimeout() { return 60; }

    @Override
    public String getGumDBUrl() {
        return this.jdbcUrl;
    }

    @Override
    public Integer getDefaultPoolSize() { return 1; }

    @Override
    public Integer getMaxPoolSize() { return 5; }

    @Override
    public String getDriverClass() {
        return Objects.requireNonNullElse(driverClass,"org.postgresql.Driver");
    }

    @Override
    public String getUsername() {
        return Objects.requireNonNullElse(username,"username");
    }

    @Override
    public String getPassword() {
        return Objects.requireNonNullElse(password,"password");
    }

    @Override
    public String getGazelleTMUrl() {
        return "http://localhost:8080/gazelle";
    }

    @Override
    public String getRootTestBedUrl() {
        return "http://localhost:8080/";
    }

    @Override
    public String getSSOBaseUrl() {
        return "http://localhost:28080/";
    }

    @Override
    public String getGUMBaseUrl() {
        return "http://localhost:8080/gum";
    }

    @Override
    public String getGUMUIBaseUrl() {
        return "http://localhost:3000";
    }

    @Override
    public String getRealmName() {
        return "gazelle";
    }

    @Override
    public boolean isOrganizationCreationEnabled() { return organizationCreationEnabled; }

    @Override
    public boolean isUserRegistrationEnabled() { return userRegistrationEnabled; }

    public void enableOrganizationCreation(boolean enable) {
        this.organizationCreationEnabled = enable;
    }

    public void enableUserRegistration(boolean enable) {
        this.userRegistrationEnabled = enable;
    }

    @Override
    public String getTermsOfServiceUrl() { return "https://termsOfUse.fr"; }

    @Override
    public String getPrivacyPolicyUrl() { return "https://privacyPolicy.fr"; }

    @Override
    public int getPurgeInactivatedUsersDaysLimit() {
        return 31;
    }

    @Override
    public boolean isUserCreationEmailNotificationEnabled() {
        return true;
    }
}