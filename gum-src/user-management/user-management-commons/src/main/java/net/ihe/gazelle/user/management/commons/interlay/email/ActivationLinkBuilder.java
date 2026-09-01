package net.ihe.gazelle.user.management.commons.interlay.email;

import net.ihe.gazelle.user.management.api.application.configuration.ApplicationConfig;

/**
 * Class responsible for building activation links for user accounts.
 */
public class ActivationLinkBuilder {

    private final ApplicationConfig applicationConfig;
    public static final String ACTIVATE_USER_PATH = "/validate/";

    /**
     * Creates a new instance of ActivationLinkBuilder with the given application configuration.
     * @param applicationConfig the application configuration to use for building activation links
     */
    public ActivationLinkBuilder(ApplicationConfig applicationConfig) {
        this.applicationConfig = applicationConfig;
    }

    /**
     * Builds an activation link for a user account using the provided activation code.
     * @param activationCode the activation code to include in the activation link
     * @return the complete activation link, or null if the activation code is null or empty
     */
    public String buildActivationLink(String activationCode) {
        try {
            if (activationCode == null || activationCode.isEmpty())
                return null;
            return applicationConfig.getGUMUIBaseUrl() + ACTIVATE_USER_PATH + activationCode;
        } catch (Exception e) {
            throw new IllegalStateException("Unable to generate activation link", e);
        }
    }
}
