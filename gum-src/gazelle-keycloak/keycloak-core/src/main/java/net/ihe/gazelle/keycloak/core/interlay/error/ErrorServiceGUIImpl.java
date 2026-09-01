package net.ihe.gazelle.keycloak.core.interlay.error;

import jakarta.ws.rs.core.Response;
import net.ihe.gazelle.keycloak.core.application.error.ErrorServiceGUI;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.broker.provider.IdentityBrokerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of ErrorServiceGUI that provides methods to generate custom error pages and exceptions for Keycloak authentication flows.
 */
public class ErrorServiceGUIImpl implements ErrorServiceGUI {

    @Override
    public void generateCustomErrorPage(AuthenticationFlowContext context, String message) {
        Response challenge = context.form()
                .setError(message)
                .createErrorPage(Response.Status.INTERNAL_SERVER_ERROR);
        context.failure(AuthenticationFlowError.INTERNAL_ERROR, challenge);
    }

    @Override
    public IdentityBrokerException generateIdentityProviderCustomErrorPage(String logMessage, Class<?> sourceErrorClass, String messageCode) {
        Logger log = LoggerFactory.getLogger(sourceErrorClass);
        if (messageCode != null) {
            //If the message code is not null Keycloak will not show the error in the console only GUI message will be
            //displayed, so we do it manually here.
            log.error(logMessage);
        }
        return new IdentityBrokerException(logMessage).withMessageCode(messageCode);
    }
}
