package net.ihe.gazelle.user.management.core.interlay.transactional;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import net.ihe.gazelle.user.management.api.domain.user.DelegatedUser;
import net.ihe.gazelle.user.management.api.domain.user.User;
import net.ihe.gazelle.user.management.commons.application.user.delegation.UserDelegationDAO;
import net.ihe.gazelle.user.management.commons.interlay.dao.user.UserDelegationDAOImpl;

/**
 * Transactional wrapper for UserDelegationDAO, ensuring that all operations are executed within a transaction context.
 */
@RequestScoped
public class UserDelegationDAOTransactional implements UserDelegationDAO {

    private final UserDelegationDAO userDelegationDAO;

    /**
     * Constructor that initializes the UserDelegationDAO with an EntityManager, allowing for database interactions.
     * @param entityManager the EntityManager to be used for database operations
     */
    @Inject
    public UserDelegationDAOTransactional(EntityManager entityManager) {
        this.userDelegationDAO = new UserDelegationDAOImpl(entityManager);
    }

    @Override
    @Transactional
    public DelegatedUser createDelegatedUser(User user, String externalId, String idpId) {
        return userDelegationDAO.createDelegatedUser(user, externalId, idpId);
    }

    @Override
    @Transactional
    public DelegatedUser transformUserIntoDelegatedUser(String userEmail, String externalId, String idpId) {
        return userDelegationDAO.transformUserIntoDelegatedUser(userEmail, externalId, idpId);
    }

    @Override
    public DelegatedUser getDelegatedUser(String externalId, String idpId) {
        return userDelegationDAO.getDelegatedUser(externalId, idpId);
    }

    @Override
    public DelegatedUser getDelegatedUserById(String userId) {
        return userDelegationDAO.getDelegatedUserById(userId);
    }

    @Override
    public boolean isDelegatedUserExisting(String externalId, String idpId) {
        return userDelegationDAO.isDelegatedUserExisting(externalId, idpId);
    }
}