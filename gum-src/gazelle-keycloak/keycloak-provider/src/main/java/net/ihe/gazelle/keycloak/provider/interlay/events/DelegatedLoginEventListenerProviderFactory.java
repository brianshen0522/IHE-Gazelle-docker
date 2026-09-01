package net.ihe.gazelle.keycloak.provider.interlay.events;

import jakarta.persistence.EntityManager;
import net.ihe.gazelle.user.management.api.application.user.delegation.UserDelegationService;
import net.ihe.gazelle.user.management.commons.application.user.delegation.UserDelegationDAO;
import net.ihe.gazelle.user.management.commons.application.user.delegation.UserDelegationServiceImpl;
import net.ihe.gazelle.user.management.commons.application.user.edit.UserEditDAO;
import net.ihe.gazelle.user.management.commons.application.user.lookup.UserLookupDAO;
import net.ihe.gazelle.user.management.commons.interlay.dao.user.UserDelegationDAOImpl;
import net.ihe.gazelle.user.management.commons.interlay.dao.user.UserEditDAOImpl;
import net.ihe.gazelle.user.management.commons.interlay.dao.user.UserLookupDAOImpl;
import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.models.KeycloakSession;

public class DelegatedLoginEventListenerProviderFactory extends GazelleEventListenerProviderFactory {


    @Override
    public String getId() {
        return "gzl-delegated-login";
    }

    @Override
    public DelegatedLoginEventListenerProvider create(KeycloakSession keycloakSession) {
        EntityManager entityManager = keycloakSession.getProvider(JpaConnectionProvider.class, "gum-store").getEntityManager();
        UserDelegationDAO userDelegationDAO = new UserDelegationDAOImpl(entityManager);
        UserLookupDAO userLookupDAO = new UserLookupDAOImpl(entityManager);
        UserEditDAO userEditDAO = new UserEditDAOImpl(entityManager);
        UserDelegationService userDelegationService = new UserDelegationServiceImpl(userDelegationDAO, userLookupDAO, userEditDAO);
        return new DelegatedLoginEventListenerProvider(keycloakSession, userDelegationService);
    }
}
