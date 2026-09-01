package net.ihe.gazelle.user.management.core.interlay.factory;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import net.ihe.gazelle.user.management.commons.application.group.GroupDAO;
import net.ihe.gazelle.user.management.commons.interlay.dao.GroupDAOImpl;

/**
 * Factory class for creating instances of GroupDAO.
 * This factory is responsible for providing a GroupDAO instance with the necessary dependencies injected.
 */
public class GroupDAOFactory {

    private final EntityManager entityManager;

    /**
     * Constructs a new GroupDAOFactory with the given EntityManager.
     * @param entityManager the EntityManager to be used by the GroupDAO instances created by this factory
     */
    @Inject
    public GroupDAOFactory(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * Produces a new instance of GroupDAO with the necessary dependencies injected.
     * @return a new GroupDAO instance
     */
    @Produces
    @RequestScoped
    public GroupDAO getGroupDAO() {
        return new GroupDAOImpl(entityManager);
    }
}
