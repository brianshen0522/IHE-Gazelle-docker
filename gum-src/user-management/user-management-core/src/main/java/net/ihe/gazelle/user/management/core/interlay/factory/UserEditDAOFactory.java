package net.ihe.gazelle.user.management.core.interlay.factory;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import net.ihe.gazelle.user.management.commons.application.user.edit.UserEditDAO;
import net.ihe.gazelle.user.management.commons.interlay.dao.user.UserEditDAOImpl;

/**
 * Factory class for producing instances of UserEditDAO.
 */
public class UserEditDAOFactory {

    private final EntityManager entityManager;

    /**
     * Constructor for UserEditDAOFactory, injecting the EntityManager for database operations.
     * @param entityManager the EntityManager to be used for creating UserEditDAO instances
     */
    @Inject
    public UserEditDAOFactory(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * Produces an instance of UserEditDAO using the injected EntityManager.
     * @return a new instance of UserEditDAO
     */
    @Produces
    @RequestScoped
    public UserEditDAO getUserEditService() {
        return new UserEditDAOImpl(entityManager);
    }
}
