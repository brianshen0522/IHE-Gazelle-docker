package net.ihe.gazelle.user.management.quarkus.interlay.controller.organization.search;

import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import net.ihe.gazelle.search.jaxrs.api.AbstractSearchServiceRest;
import net.ihe.gazelle.security.business.GazelleIdentity;
import net.ihe.gazelle.user.management.api.domain.organization.Organization;
import net.ihe.gazelle.user.management.api.interlay.organization.OrganizationDto;
import net.ihe.gazelle.user.management.commons.application.organization.lookup.OrganizationSearchService;

/**
 * REST controller for organization searches.
 */
@Path("/rest/organizations")
public class OrganizationSearchControllerImpl extends
        AbstractSearchServiceRest<Organization, OrganizationDto, OrganizationQueryBeanParam, net.ihe.gazelle.user.management.commons.application.organization.lookup.OrganizationSearchCriteria> implements OrganizationSearchController {


    /**
     * Constructor for OrganizationSearchControllerImpl.
     *
     * @param organizationSearchService the service to search for organizations
     * @param identity                      the identity who requests the operations on organizations, injected by CDI
     */
    @Inject
    public OrganizationSearchControllerImpl(OrganizationSearchService organizationSearchService, GazelleIdentity identity) {
        super(organizationSearchService,
                organizationSearchService,
                new OrganizationQueryMapper(organizationSearchService),
                organizationSearchService,
                identity);
    }

    @Override
    protected String getContentRangeType() {
        return "Organization";
    }

    @Override
    protected OrganizationDto mapToDTO(Organization organization) {
        return new OrganizationDto(organization);
    }
}
