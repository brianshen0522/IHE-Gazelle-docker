package net.ihe.gazelle.user.management.quarkus.interlay.controller;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import net.ihe.gazelle.user.management.api.application.configuration.ApplicationConfig;
import net.ihe.gazelle.user.management.api.domain.configuration.ConfigurationsResource;

/**
 * Implementation of the application configuration endpoint.
 */
@RequestScoped
public class ApplicationControllerImpl implements ApplicationController {

    private final ApplicationConfig applicationConfig;

    /**
     * Creates the controller with the application configuration source.
     * @param applicationConfig configuration provider
     */
    @Inject
    public ApplicationControllerImpl(ApplicationConfig applicationConfig) {
        this.applicationConfig = applicationConfig;
    }

    @Override
    public Response getConfigurations() {
        ConfigurationsResource configurationsQuery = new ConfigurationsResource();
        configurationsQuery.setOrganizationCreationEnabled(applicationConfig.isOrganizationCreationEnabled());
        configurationsQuery.setUserRegistrationEnabled(applicationConfig.isUserRegistrationEnabled());
        configurationsQuery.setTermsOfServiceUrl(applicationConfig.getTermsOfServiceUrl());
        configurationsQuery.setPrivacyPolicyUrl(applicationConfig.getPrivacyPolicyUrl());
        configurationsQuery.setPurgeInactivatedUsersAfterDays(applicationConfig.getPurgeInactivatedUsersDaysLimit());
        configurationsQuery.setUserCreationEmailNotificationEnabled(applicationConfig.isUserCreationEmailNotificationEnabled());
        return Response.ok().entity(configurationsQuery).build();
    }
}
