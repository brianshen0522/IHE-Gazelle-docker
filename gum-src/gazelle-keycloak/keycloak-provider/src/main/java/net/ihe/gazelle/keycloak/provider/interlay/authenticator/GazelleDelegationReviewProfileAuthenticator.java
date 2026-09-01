package net.ihe.gazelle.keycloak.provider.interlay.authenticator;

import net.ihe.gazelle.keycloak.core.application.error.ErrorServiceGUI;
import net.ihe.gazelle.keycloak.core.interlay.KeycloakUtils;
import org.keycloak.authentication.AbstractAuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.authenticators.broker.IdpReviewProfileAuthenticator;
import org.keycloak.authentication.authenticators.broker.util.SerializedBrokeredIdentityContext;
import org.keycloak.broker.oidc.mappers.AbstractClaimMapper;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.theme.Theme;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class GazelleDelegationReviewProfileAuthenticator extends IdpReviewProfileAuthenticator {
    private static final Logger log = LoggerFactory.getLogger(GazelleDelegationReviewProfileAuthenticator.class);
    public static final String GAZELLE_REQUIRES_ACCESS_TO = "gazelleRequiresAccessTo";
    public static final String YOUR_EMAIL = "yourEmail";
    public static final String YOUR_FIRST_NAME = "yourFirstName";
    public static final String YOUR_LAST_NAME = "yourLastName";
    public static final String YOUR_ORGANIZATION_NAME = "yourOrganizationName";
    public static final String YOUR_ORGANIZATION_ID = "yourOrganizationId";
    public static final String VERIFY_CONSENT_IS_GIVEN = "verifyConsentIsGiven";
    public static final String ORGANIZATION_ID_CLAIM_PROPERTY = "organizationIdClaim";
    public static final String ORGANIZATION_NAME_CLAIM_PROPERTY = "organizationNameClaim";
    private final ErrorServiceGUI errorService;

    public GazelleDelegationReviewProfileAuthenticator(ErrorServiceGUI errorService) {
        this.errorService = errorService;
    }

    @Override
    protected void authenticateImpl(AuthenticationFlowContext context, SerializedBrokeredIdentityContext userCtx, BrokeredIdentityContext brokerContext) {
        IdpReviewProfileReport report = new IdpReviewProfileReport();
        if (!report.isValid(context, userCtx, brokerContext)) {
            log.error(generateLogMessage(report.getErrors(), brokerContext));
            errorService.generateCustomErrorPage(context, generateErrorMessage(report.getErrors(), context, brokerContext));
        } else {
            // Profile is complete. Marked success
            context.success();
        }
    }

    private String generateLogMessage(Map<String, String> errors, BrokeredIdentityContext brokerContext) {
        StringBuilder logMessage = new StringBuilder();
        logMessage.append("The user is missing ");
        for (String error : errors.keySet()) {
            logMessage.append(error).append(", ");
        }
        logMessage = new StringBuilder(logMessage.substring(0, Math.max(0, logMessage.lastIndexOf(", ")))).append(". ");
        logMessage.append(String.format("This user comes from the IDP with the following alias: %s .", brokerContext.getIdpConfig().getAlias()));
        return logMessage.toString();
    }

    private String generateErrorMessage(Map<String, String> errors, AuthenticationFlowContext context, BrokeredIdentityContext brokerContext) {
        StringBuilder message = new StringBuilder();
        //Create error message with translations
        Properties messagesBundle = KeycloakUtils.getMessages(context, Theme.Type.LOGIN, context.getUser());
        message.append(messagesBundle.getProperty(GAZELLE_REQUIRES_ACCESS_TO)).append(" ");

        for (String error : errors.values()) {
            message.append(messagesBundle.getProperty(error)).append(", ");
        }

        message = new StringBuilder(message.substring(0, Math.max(0, message.lastIndexOf(", "))));
        message.append(".");
        message.append("<BR><BR>").append(messagesBundle.getProperty(VERIFY_CONSENT_IS_GIVEN)).append(" ");
        message.append(brokerContext.getIdpConfig().getDisplayName());
        message.append(".");

        return message.toString();
    }

    class IdpReviewProfileReport {
        Map<String, String> errors;

        /**
         * Validate that the IDP profile has the correct attributes
         *
         * @param context       the current AuthenticationFlowContext
         * @param userCtx       the current SerializedBrokeredIdentityContext
         * @param brokerContext the current BrokeredIdentityContext
         * @return true if there is no error, false otherwise
         */
        public boolean isValid(AuthenticationFlowContext context, SerializedBrokeredIdentityContext userCtx, BrokeredIdentityContext brokerContext) {
            //Reset the previous report
            errors = new HashMap<>();
            //errors = map
            if (requiresUpdateProfilePage(context, userCtx, brokerContext))
                validateUserAttribute(userCtx);

            boolean isOrgaIdMissing = isOrganizationClaimMissing(ORGANIZATION_ID_CLAIM_PROPERTY, context, brokerContext);
            boolean isOrgaNameMissing = isOrganizationClaimMissing(ORGANIZATION_NAME_CLAIM_PROPERTY, context, brokerContext);

            if (isOrgaIdMissing)
                errors.put("organization id", YOUR_ORGANIZATION_ID);

            if (isOrgaNameMissing)
                errors.put("organization name", YOUR_ORGANIZATION_NAME);

            return errors.isEmpty();
        }

        /**
         * Return the list of errors found after the validation of a IPD profile.
         * It is mandatory to validate the profile first, otherwise this list will be empty.
         *
         * @return a map where the key is the missing attribute and the value is the translation key.
         */
        public Map<String, String> getErrors() {
            return new HashMap<>(errors);
        }

        private void validateUserAttribute(SerializedBrokeredIdentityContext userCtx) {
            if (userCtx.getEmail() == null) {
                errors.put("email", YOUR_EMAIL);
            }
            if (userCtx.getFirstName() == null) {
                errors.put("firstName", YOUR_FIRST_NAME);
            }
            if (userCtx.getLastName() == null) {
                errors.put("lastName", YOUR_LAST_NAME);
            }
        }

        private boolean isOrganizationClaimMissing(String claimNameProperty, AbstractAuthenticationFlowContext context, BrokeredIdentityContext brokerContext) {
            AuthenticatorConfigModel authenticatorConfig = context.getAuthenticatorConfig();
            try {
                //If the property is not configured in GUI, it will result in a NullPointerException.
                String claimName = authenticatorConfig.getConfig().get(claimNameProperty);

                //By using AbstractClaimMapper class we search the claim in the JWT, the Id token and in UserInfo
                Object claimValue = AbstractClaimMapper.getClaimValue(brokerContext, claimName);

                return claimValue == null;
            } catch (NullPointerException e) {
                log.error("Wrong or missing property for organization claim", e);
                return false;
            }
        }
    }
}
