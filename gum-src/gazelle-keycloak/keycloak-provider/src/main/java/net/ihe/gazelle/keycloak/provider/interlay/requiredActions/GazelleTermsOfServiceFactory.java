package net.ihe.gazelle.keycloak.provider.interlay.requiredActions;

import jakarta.persistence.EntityManager;
import net.ihe.gazelle.keycloak.core.interlay.configuration.ApplicationConfigImpl;
import net.ihe.gazelle.user.management.api.application.configuration.ApplicationConfig;
import net.ihe.gazelle.user.management.api.application.user.registration.ConsentService;
import net.ihe.gazelle.user.management.commons.application.user.registration.ConsentDAO;
import net.ihe.gazelle.user.management.commons.application.user.registration.ConsentServiceImpl;
import net.ihe.gazelle.user.management.commons.interlay.dao.ConsentDAOImpl;
import org.keycloak.Config;
import org.keycloak.authentication.RequiredActionFactory;
import org.keycloak.authentication.RequiredActionProvider;
import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

public class GazelleTermsOfServiceFactory implements RequiredActionFactory {
    public static final String PROVIDER_ID = "GAZELLE_TERMS_OF_SERVICE";

    private ApplicationConfig applicationConfig;

    @Override
    public RequiredActionProvider create(KeycloakSession session) {
        EntityManager entityManager = session.getProvider(JpaConnectionProvider.class, "gum-store").getEntityManager();
        ConsentDAO consentDAO = new ConsentDAOImpl(entityManager);
        ConsentService consentService = new ConsentServiceImpl(consentDAO);
        return new GazelleTermsOfServiceProvider(consentService,applicationConfig);
    }

    @Override
    public void init(Config.Scope config) {
        this.applicationConfig = new ApplicationConfigImpl();
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
        // Nothing to do here
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public String getDisplayText() {
        return "Gazelle Terms of Service";
    }

    @Override
    public void close() {
        // Nothing to do here
    }
}