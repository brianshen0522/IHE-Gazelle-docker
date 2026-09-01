package net.ihe.gazelle.user.management.core.interlay.factory;

import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import net.ihe.gazelle.user.management.commons.application.user.registration.ConsentDAO;
import net.ihe.gazelle.user.management.commons.interlay.dao.ConsentDAOImpl;

/**
 * Factory class for producing instances of ConsentDAO.
 */
public class ConsentDAOFactory {

    private final EntityManager em;

    /**
     * Constructs a new ConsentDAOFactory with the given EntityManager.
     * @param em the EntityManager to be used for creating ConsentDAO instances
     */
    @Inject
    public ConsentDAOFactory(EntityManager em) {
        this.em = em;
    }

    /**
     * Produces an instance of ConsentDAO.
     * @return a new instance of ConsentDAO
     */
    @Produces
    public ConsentDAO getConsentService() {
        return new ConsentDAOImpl(em);
    }
}
