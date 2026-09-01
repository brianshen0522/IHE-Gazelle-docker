package net.ihe.gazelle.user.management.core.interlay.factory;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import net.ihe.gazelle.user.management.commons.application.user.lookup.UserLookupDAO;
import net.ihe.gazelle.user.management.commons.interlay.dao.user.UserLookupDAOImpl;

/**
 * Factory class for creating instances of UserLookupDAO.
 */
@RequestScoped
public class UserLookupDAOFactory {

    private final EntityManager entityManager;

    /**
     * Constructs a new UserLookupDAOFactory with the given EntityManager.
     * @param entityManager the EntityManager to be used for creating UserLookupDAO instances
     */
    @Inject
    public UserLookupDAOFactory(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * Produces an instance of UserLookupDAO.
     * @return a new instance of UserLookupDAO
     */
    @Produces
    @RequestScoped
    public UserLookupDAO getUserLoginDAO() {
        return new UserLookupDAOImpl(entityManager);
    }
}
