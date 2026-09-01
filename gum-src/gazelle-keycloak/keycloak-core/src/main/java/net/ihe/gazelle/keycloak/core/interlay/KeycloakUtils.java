package net.ihe.gazelle.keycloak.core.interlay;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.jose.jws.JWSInput;
import org.keycloak.jose.jws.JWSInputException;
import org.keycloak.models.UserModel;
import org.keycloak.representations.JsonWebToken;
import org.keycloak.theme.Theme;

import java.io.IOException;
import java.util.Locale;
import java.util.Properties;

/**
 * Utility class for Keycloak-related operations.
 * This class provides helper methods for working with Keycloak's authentication flow and brokered identity context.
 */
public class KeycloakUtils {

    public static final String VALIDATED_ID_TOKEN = "VALIDATED_ID_TOKEN";

    /** Private constructor to prevent instantiation of this utility class. */
    private KeycloakUtils() {
        // Private constructor to prevent instantiation
    }

    /**
     * Retrieves the localized messages for a given authentication flow context, theme type, and user model.
     * @param context the authentication flow context from which to retrieve the theme and locale information
     * @param themeType the type of theme for which to retrieve the messages (e.g., LOGIN, ACCOUNT, etc.)
     * @param userModel the user model for which to resolve the locale
     * @return a Properties object containing the localized messages for the specified theme and locale
     */
    public static Properties getMessages(AuthenticationFlowContext context, Theme.Type themeType, UserModel userModel) {
        try {
            Theme theme = context.getSession().theme().getTheme(themeType);
            Locale locale = context.getSession().getContext().resolveLocale(userModel);
            return theme.getMessages(locale);
        } catch (IOException e) {
            throw new IllegalStateException("Translations are not accessible", e);
        }
    }

    /**
     * Extracts the JsonWebToken (ID token) from the given BrokeredIdentityContext. The method checks if the ID token is present in the context data and attempts to parse it as a JsonWebToken.
     * @param brokerContext the BrokeredIdentityContext from which to extract the ID token
     * @return the extracted JsonWebToken if it is present and valid, or null if the ID token is not present or cannot be parsed as a JsonWebToken
     */
    public static JsonWebToken getJsonWebTokenFromBrokerContext(BrokeredIdentityContext brokerContext) {
        Object rawIdToken = brokerContext.getContextData().get(VALIDATED_ID_TOKEN);
        JsonWebToken idToken = null;

        if (rawIdToken instanceof String) {
            try {
                idToken = new JWSInput(rawIdToken.toString()).readJsonContent(JsonWebToken.class);
            } catch (JWSInputException e) {
                return null;
            }
        } else if (rawIdToken instanceof JsonWebToken jsonWebToken) {
            idToken = jsonWebToken;
        }
        return idToken;
    }
}
