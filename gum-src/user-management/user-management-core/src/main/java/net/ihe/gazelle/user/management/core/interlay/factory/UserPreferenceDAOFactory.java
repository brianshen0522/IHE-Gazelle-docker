package net.ihe.gazelle.user.management.core.interlay.factory;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import net.ihe.gazelle.user.management.commons.application.user.preference.UserPreferenceDAO;
import net.ihe.gazelle.user.management.commons.interlay.dao.UserPreferenceDAOImpl;

/**
 * Factory for creating instances of UserPreferenceDAO.
 */
public class UserPreferenceDAOFactory {

    private final EntityManager entityManager;

    /**
     * Constructs a new UserPreferenceDAOFactory with the given EntityManager.
     * @param entityManager the EntityManager to be used for creating UserPreferenceDAO instances
     */
    @Inject
    public UserPreferenceDAOFactory(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * Produces a new instance of UserPreferenceDAO.
     * @return a new UserPreferenceDAO instance
     */
    @Produces
    @RequestScoped
    public UserPreferenceDAO getUserPreferenceDAO() {
        return new UserPreferenceDAOImpl(entityManager);
    }
}
