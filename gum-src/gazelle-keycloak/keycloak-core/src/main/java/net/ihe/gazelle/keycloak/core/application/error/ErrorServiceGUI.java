package net.ihe.gazelle.keycloak.core.application.error;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.broker.provider.IdentityBrokerException;

/**
 * Service interface for handling error pages in Keycloak GUI.
 * This service provides methods to generate custom error pages with specific messages for different contexts, such as authentication and claim mapping.
 */
public interface ErrorServiceGUI {

    /**
     * Generate a keycloak error page with custom message when we are in an authenticationContext
     * @param context The authentication context
     * @param message The message to be displayed in GUI
     */
    void generateCustomErrorPage(AuthenticationFlowContext context, String message);

    /**
     * Generate a keycloak error page when we are in a claim mapper context
     * @param logMessage The message (developer oriented) that will be in displayed server side
     * @param sourceErrorClass The Class from where the error occurred, this used to provide better information in the logs
     * @param messageCode The translation key from Keycloak Theme
     * @return The keycloak exception that will be thrown by the calling mapper and used to create the error page
     */
    IdentityBrokerException generateIdentityProviderCustomErrorPage(String logMessage, Class<?> sourceErrorClass, String messageCode);
}
