package net.ihe.gazelle.keycloak.provider.interlay.requiredActions;

import jakarta.ws.rs.core.Response;
import net.ihe.gazelle.user.management.api.application.configuration.ApplicationConfig;
import net.ihe.gazelle.user.management.api.application.user.registration.ConsentService;
import org.keycloak.authentication.RequiredActionContext;
import org.keycloak.authentication.RequiredActionProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GazelleTermsOfServiceProvider implements RequiredActionProvider {

    private final Logger log = LoggerFactory.getLogger(GazelleTermsOfServiceProvider.class.getName());
    private static final String GAZELLE_TERMS_OF_SERVICE = "GAZELLE_TERMS_OF_SERVICE";

    private final ConsentService consentService;

    private final ApplicationConfig applicationConfig;


    public GazelleTermsOfServiceProvider(ConsentService consentService, ApplicationConfig applicationConfig) {
        this.consentService = consentService;
        this.applicationConfig = applicationConfig;
    }

    @Override
    public void evaluateTriggers(RequiredActionContext context) {
        if (consentService.needToGiveConsent(context.getUser().getUsername())) {
            context.getUser().addRequiredAction(GAZELLE_TERMS_OF_SERVICE);
        }
    }

    @Override
    public void requiredActionChallenge(RequiredActionContext context) {
        Response challenge = context.form()
                .setAttribute("user", context.getAuthenticationSession().getAuthenticatedUser())
                .setAttribute("termsOfServiceUrl", applicationConfig.getTermsOfServiceUrl())
                .setAttribute("privacyPolicyUrl", applicationConfig.getPrivacyPolicyUrl())
                .createForm("terms.ftl");
        context.challenge(challenge);
    }

    @Override
    public void processAction(RequiredActionContext context) {
         if (context.getHttpRequest().getDecodedFormParameters().containsKey("accept")) {
            log.debug("process action of gazelle terms and conditions for user: {}",context.getUser().getUsername() );
            consentService.acceptUserConsent(context.getUser().getUsername());
            context.success();
            return;
        }
        context.challenge(context.form().setError("termsAcceptanceRequired").createForm("error.ftl"));
    }

    @Override
    public void close() {
        // Nothing to do here
    }
}