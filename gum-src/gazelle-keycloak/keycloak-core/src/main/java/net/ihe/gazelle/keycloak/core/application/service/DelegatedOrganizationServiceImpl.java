package net.ihe.gazelle.keycloak.core.application.service;

import net.ihe.gazelle.user.management.api.application.organization.DelegatedOrganizationService;
import net.ihe.gazelle.user.management.api.application.organization.OrganizationManagementService;
import net.ihe.gazelle.user.management.api.domain.organization.DelegatedOrganization;
import net.ihe.gazelle.user.management.api.domain.organization.Organization;
import net.ihe.gazelle.user.management.commons.application.organization.delegation.DelegatedOrganizationDAO;
import net.ihe.gazelle.user.management.commons.application.organization.management.OrganizationEventPublisher;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Function;

/**
 * Implementation of the DelegatedOrganizationService interface, responsible for managing delegated organizations and their association with users.
 * This service provides methods to upsert delegated organizations for users, migrate local organizations to delegated ones
 */
public class DelegatedOrganizationServiceImpl implements DelegatedOrganizationService {

    private static final String DELEGATED_ORGANIZATION_IS_NULL = "Delegated organization should not be null";
    private final DelegatedOrganizationDAO delegatedOrganizationDAO;
    private final OrganizationManagementService organizationManagementService;
    private final OrganizationEventPublisher organizationEventPublisher;

    /**
     * Constructor for DelegatedOrganizationServiceImpl, which initializes the service with the provided DelegatedOrganizationDAO and OrganizationManagementService.
     * @param organizationDelegationDAO the DAO for managing delegated organizations
     * @param organizationManagementService the service for managing organization registrations and user associations
     */
    public DelegatedOrganizationServiceImpl(DelegatedOrganizationDAO organizationDelegationDAO, OrganizationManagementService organizationManagementService,  OrganizationEventPublisher organizationEventPublisher) {
        this.delegatedOrganizationDAO = organizationDelegationDAO;
        this.organizationManagementService = organizationManagementService;
        this.organizationEventPublisher = organizationEventPublisher;
    }

    @Override
    public void upsertDelegatedOrganizationForUser(DelegatedOrganization delegatedOrganization, String userId, Function<DelegatedOrganization, Organization> localOrganizationMatcher) {
        if (!isDelegatedOrganizationExisting(delegatedOrganization)) {
            Organization localMatch = localOrganizationMatcher.apply(delegatedOrganization);
            if (localMatch != null) {
                migrateLocalToDelegated(localMatch, delegatedOrganization.getExternalId(), delegatedOrganization.getIdpId());
            } else {
                createDelegatedOrganization(delegatedOrganization);
            }
        } else {
            updateOrganization(delegatedOrganization);
        }
        joinDelegatedOrganization(delegatedOrganization, userId);
    }

    /**
     * Migrate a local organization to a delegated organization by updating the existing local organization with the external ID and IdP ID from the delegated organization.
     * @param localMatch the local organization that matches the delegated organization based on the provided matching function
     * @param externalId the external ID to set for the local organization to migrate it to a delegated organization
     * @param idpId the IdP ID to set for the local organization to migrate it to a delegated organization
     */
    protected void migrateLocalToDelegated(Organization localMatch, String externalId, String idpId) {
        if (localMatch == null)
            throw new IllegalArgumentException(DELEGATED_ORGANIZATION_IS_NULL);
        DelegatedOrganization delegatedOrganization = new DelegatedOrganization();
        delegatedOrganization.setExternalId(externalId);
        delegatedOrganization.setIdpId(idpId);
        delegatedOrganizationDAO.updateDelegatedOrganization(localMatch.getId(), delegatedOrganization);
    }

    /**
     * Update an existing delegated organization in the database with the information from the provided DelegatedOrganization object.
     * @param delegatedOrganization the DelegatedOrganization object containing the updated information for the organization to be updated in the database
     */
    protected void updateOrganization(DelegatedOrganization delegatedOrganization) {
        if (delegatedOrganization == null)
            throw new IllegalArgumentException(DELEGATED_ORGANIZATION_IS_NULL);
        Map<String, String> parameters = getMapParametersFromOrganization(delegatedOrganization);
        List<Organization> organizations = delegatedOrganizationDAO.searchForOrganization(parameters);
        if (organizations.size() != 1)
            throw new IllegalStateException("Organization not found or multiple organizations found for an update.");
        Organization organization = organizations.getFirst();
        delegatedOrganizationDAO.updateDelegatedOrganization(organization.getId(), delegatedOrganization);
        organizationEventPublisher.publishOrganizationUpdateEvent(delegatedOrganization);
    }

    /**
     * Create a new delegated organization in the database using the provided DelegatedOrganization object.
     * @param delegatedOrganization the DelegatedOrganization object containing the information for the new delegated organization to be created in the database
     */
    protected void createDelegatedOrganization(DelegatedOrganization delegatedOrganization) {
        if (delegatedOrganization == null)
            throw new IllegalArgumentException(DELEGATED_ORGANIZATION_IS_NULL);
        delegatedOrganizationDAO.createDelegatedOrganization(delegatedOrganization);
        organizationEventPublisher.publishOrganizationCreateEvent(delegatedOrganization);
    }

    /**
     * Check if a delegated organization already exists in the database based on the information provided in the DelegatedOrganization object.
     * @param delegatedOrganization the DelegatedOrganization object containing the information to check for existence in the database
     * @return true if the delegated organization exists in the database, false otherwise
     */
    protected boolean isDelegatedOrganizationExisting(DelegatedOrganization delegatedOrganization) {
        if (delegatedOrganization == null)
            throw new IllegalArgumentException(DELEGATED_ORGANIZATION_IS_NULL);
        return delegatedOrganizationDAO.isDelegatedOrganizationExist(delegatedOrganization);
    }


    /**
     * Join a user to a delegated organization by searching for the organization in the database using the information from the provided DelegatedOrganization object and then associating the user with the found organization.
     * @param delegatedOrganization the DelegatedOrganization object containing the information to search for the organization in the database and join the user to
     * @param userId the ID of the user to be joined to the found organization
     */
    void joinDelegatedOrganization(DelegatedOrganization delegatedOrganization, String userId) {
        if (delegatedOrganization == null || userId == null)
            throw new IllegalArgumentException((delegatedOrganization == null ? "Delegated organization" : "User id") + " should not be null");
        Map<String, String> parameters = getMapParametersFromOrganization(delegatedOrganization);
        List<Organization> organizations = delegatedOrganizationDAO.searchForOrganization(parameters);
        if (organizations.size() != 1)
            throw new IllegalStateException("Organization not found or multiple organizations found for a join.");
        organizationManagementService.joinOrganization(userId, organizations.get(0).getId());
    }

    @Override
    public Function<DelegatedOrganization, Organization> getDefaultLocalOrganizationMatcher() {
        return delegatedOrganization -> {
            try {
                Organization orga = delegatedOrganizationDAO.getOrganizationByName(delegatedOrganization.getName());
                if (orga == null) {
                    return delegatedOrganizationDAO.getOrganizationById(delegatedOrganization.getId());
                }
                return orga;
            } catch (NoSuchElementException _) {
                return null;
            }
        };
    }

    @Override
    public Organization getOrganizationById(String organizationId) {
        if (organizationId == null) {
            throw new IllegalArgumentException("organizationId is null");
        }
        return delegatedOrganizationDAO.getOrganizationById(organizationId);
    }

    private Map<String, String> getMapParametersFromOrganization(DelegatedOrganization delegatedOrganization) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("externalId", delegatedOrganization.getExternalId());
        parameters.put("idpId", delegatedOrganization.getIdpId());
        parameters.put("delegated", "true");
        return parameters;
    }
}
