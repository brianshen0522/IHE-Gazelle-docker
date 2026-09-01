package net.ihe.gazelle.user.management.core.interlay.factory;

import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import net.ihe.gazelle.user.management.commons.application.organization.lookup.OrganizationLookupDAO;
import net.ihe.gazelle.user.management.commons.interlay.dao.organization.OrganizationLookupDAOImpl;

/**
 * Factory class for producing instances of OrganizationLookupService and OrganizationManagementService.
 * This class uses dependency injection to provide the necessary DAOs for service instantiation.
 */
public class OrganizationDAOFactory {

    private final EntityManager entityManager;

    /**
     * Constructor for OrganizationServiceFactory, initializing the DAOs for organization lookup and registration.
     * @param entityManager the EntityManager to be used for DAO operations, injected by CDI
     */
    @Inject
    public OrganizationDAOFactory(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * Produces an instance of OrganizationLookupService using the injected OrganizationLookupDAO.
     * @return an instance of OrganizationLookupService
     */
    @Produces
    public OrganizationLookupDAO getOrganizationLookupDAO() {
        return new OrganizationLookupDAOImpl(entityManager);
    }
}
