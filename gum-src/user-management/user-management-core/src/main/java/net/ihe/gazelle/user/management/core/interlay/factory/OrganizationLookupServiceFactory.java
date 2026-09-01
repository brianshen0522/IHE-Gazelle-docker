package net.ihe.gazelle.user.management.core.interlay.factory;

import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import net.ihe.gazelle.user.management.api.application.organization.OrganizationLookupService;
import net.ihe.gazelle.user.management.commons.application.organization.lookup.OrganizationLookupDAO;
import net.ihe.gazelle.user.management.commons.application.organization.lookup.OrganizationLookupServiceImpl;

/**
    * Factory for creating instances of {@link OrganizationLookupService}.
 */
public class OrganizationLookupServiceFactory {

    private final OrganizationLookupDAO organizationLookupDAO;

    /**
     * Constructs a OrganizationLookupServiceFactory with the specified DAOs.
     * @param organizationLookupDAO the OrganizationLookupDAO to be used by the service
     */
    @Inject
    public OrganizationLookupServiceFactory(OrganizationLookupDAO organizationLookupDAO) {
        this.organizationLookupDAO = organizationLookupDAO;
    }

    /**
     * Produces an instance of OrganizationLookupService.
     * @return a new instance of OrganizationLookupService
     */
    @Produces
    public OrganizationLookupService getOrganizationLookupService() {
        return new OrganizationLookupServiceImpl(organizationLookupDAO);
    }
}
