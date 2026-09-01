package net.ihe.gazelle.keycloak.provider.interlay.events;

import jakarta.persistence.EntityManager;
import net.ihe.gazelle.user.management.commons.application.user.login.HashPasswordServiceProvider;
import net.ihe.gazelle.user.management.commons.application.user.login.UserLoginDAO;
import net.ihe.gazelle.user.management.commons.application.user.login.UserLoginServiceImpl;
import net.ihe.gazelle.user.management.commons.interlay.dao.user.UserLoginDAOImpl;
import net.ihe.gazelle.user.management.commons.interlay.provider.HashPasswordServiceSPIProvider;
import org.keycloak.Config;
import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.models.KeycloakSession;

public class LastLoginListenerProviderFactory extends GazelleEventListenerProviderFactory {

    private HashPasswordServiceProvider hashPasswordServiceProvider;

    @Override
    public LastLoginEventListenerProvider create(KeycloakSession session) {
        EntityManager entityManager = session.getProvider(JpaConnectionProvider.class, "gum-store").getEntityManager();
        UserLoginDAO userLoginDAO = new UserLoginDAOImpl(entityManager);
        net.ihe.gazelle.user.management.api.application.user.login.UserLoginService userLoginService = new UserLoginServiceImpl(userLoginDAO, hashPasswordServiceProvider);
        return new LastLoginEventListenerProvider(session, userLoginService);
    }

    @Override
    public void init(Config.Scope scope) {
        hashPasswordServiceProvider = new HashPasswordServiceSPIProvider();
    }

    @Override
    public String getId() {
        //name of the event displayed in keycloak
        return "gzl-last-login";
    }
}
