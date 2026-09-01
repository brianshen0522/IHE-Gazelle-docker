package net.ihe.gazelle.user.management.core.interlay.factory;

import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import net.ihe.gazelle.user.management.commons.interlay.dao.user.UserSearchDAO;
import net.ihe.gazelle.user.management.commons.interlay.dao.user.UserSearchDAOImpl;

/**
 * Factory class for producing instances of DAO related to user search.
 */
public class UserSearchDAOFactory {

    private final EntityManager entityManager;

    @Inject
    public UserSearchDAOFactory(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Produces
    public UserSearchDAO getUserSearchDAO() {
        return new UserSearchDAOImpl(entityManager);
    }
}
