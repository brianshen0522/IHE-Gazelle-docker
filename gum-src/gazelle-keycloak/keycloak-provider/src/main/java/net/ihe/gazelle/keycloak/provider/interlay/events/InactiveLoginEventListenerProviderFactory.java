package net.ihe.gazelle.keycloak.provider.interlay.events;

import jakarta.persistence.EntityManager;
import net.ihe.gazelle.security.business.Authz;
import net.ihe.gazelle.security.business.AuthzImpl;
import net.ihe.gazelle.security.technical.PermissionStoreSPIProvider;
import net.ihe.gazelle.user.management.api.application.user.delegation.UserDelegationService;
import net.ihe.gazelle.user.management.api.application.user.lookup.UserLookupService;
import net.ihe.gazelle.user.management.commons.application.user.delegation.UserDelegationDAO;
import net.ihe.gazelle.user.management.commons.application.user.delegation.UserDelegationServiceImpl;
import net.ihe.gazelle.user.management.commons.application.user.edit.UserEditDAO;
import net.ihe.gazelle.user.management.commons.application.user.lookup.UserLookupDAO;
import net.ihe.gazelle.user.management.commons.application.user.lookup.UserLookupServiceImpl;
import net.ihe.gazelle.user.management.commons.interlay.dao.user.UserDelegationDAOImpl;
import net.ihe.gazelle.user.management.commons.interlay.dao.user.UserEditDAOImpl;
import net.ihe.gazelle.user.management.commons.interlay.dao.user.UserLookupDAOImpl;
import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.models.KeycloakSession;

public class InactiveLoginEventListenerProviderFactory extends GazelleEventListenerProviderFactory {

    @Override
    public InactiveLoginEventListenerProvider create(KeycloakSession session) {
        EntityManager entityManager = session.getProvider(JpaConnectionProvider.class, "gum-store").getEntityManager();
        UserLookupDAO userLookupDAO = new UserLookupDAOImpl(entityManager);
        Authz authz = new AuthzImpl(new PermissionStoreSPIProvider());
        UserLookupService userLookupService = new UserLookupServiceImpl(userLookupDAO, authz);
        UserDelegationDAO userDelegationDAO = new UserDelegationDAOImpl(entityManager);
        UserEditDAO userEditDAO = new UserEditDAOImpl(entityManager);
        UserDelegationService userDelegationService = new UserDelegationServiceImpl(userDelegationDAO, userLookupDAO, userEditDAO);
        return new InactiveLoginEventListenerProvider(session, userLookupService, userDelegationService);
    }

    @Override
    public String getId() {
        return "gzl-inactive-login";
    }
}
