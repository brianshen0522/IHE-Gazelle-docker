package net.ihe.gazelle.user.management.commons.application.organization.lookup;

import net.ihe.gazelle.user.management.api.application.organization.OrganizationLookupService;
import net.ihe.gazelle.user.management.api.domain.organization.Organization;

public class OrganizationLookupServiceImpl implements OrganizationLookupService {

    OrganizationLookupDAO organizationLookupDAO;

    public OrganizationLookupServiceImpl(OrganizationLookupDAO organizationLookupDAO) {
        this.organizationLookupDAO = organizationLookupDAO;
    }

    @Override
    public Organization getOrganizationById(String organizationId) {
        return this.organizationLookupDAO.getOrganizationById(organizationId);
    }
}
