package net.ihe.gazelle.keycloak.core.interlay.dao;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import net.ihe.gazelle.user.management.api.domain.organization.Organization;
import net.ihe.gazelle.user.management.commons.application.exception.ErrorMessage;
import net.ihe.gazelle.user.management.commons.application.exception.GazelleDAOException;
import net.ihe.gazelle.user.management.commons.application.organization.management.OrganizationManagementDAO;
import net.ihe.gazelle.user.management.commons.interlay.dao.entities.UserEntity;

/**
 * DAO implementation for organization registration and membership using Keycloak as backend.
 * <p>
 * This class provides methods to check organization existence, join organizations, and handles unsupported operations for registration and leaving organizations in the Keycloak context.
 * </p>
 */
public class OrganizationManagementDAOKeycloak implements OrganizationManagementDAO {

    /** JPA entity manager for database operations. */
    private final EntityManager entityManager;

    /**
     * Constructs a DAO with the given entity manager.
     * @param entityManager the JPA entity manager
     */
    public OrganizationManagementDAOKeycloak(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * Not supported: registering organizations from Keycloak is not allowed.
     * @param organization the organization to register
     * @throws UnsupportedOperationException always
     */
    @Override
    public void createOrganization(Organization organization) {
        throw new UnsupportedOperationException("It is not possible to register an organization from Keycloak");
    }

    /**
     * Checks if an organization already exists by searching for users with the given organization ID.
     * @param organization the organization to check
     * @return true if the organization exists, false otherwise
     */
    @Override
    public boolean isOrganizationAlreadyExist(Organization organization) {
        long result = entityManager.createQuery("select count(u) from UserEntity u where u.organizationId = :organizationId", Long.class)
                .setParameter("organizationId", organization.getId())
                .getSingleResult();
        return result > 0;
    }

    @Override
    public Organization getOrganizationFromId(String organizationId) {
        throw new UnsupportedOperationException("It is not possible to get organization from its id in Keycloak");
    }

    /**
     * Joins a user to an organization by updating the user's organization ID.
     * @param userId the user ID
     * @param organizationId the organization ID
     * @throws GazelleDAOException if the user is not found
     */
    @Override
    @Transactional
    public void joinOrganization(String userId, String organizationId) {
        UserEntity userEntity = entityManager.find(UserEntity.class, userId);
        if (userEntity == null) {
            throw new GazelleDAOException(ErrorMessage.USER_NOT_FOUND.getMessage());
        }
        userEntity.setOrganizationId(organizationId);
        entityManager.merge(userEntity);
    }

    @Override
    public Organization updateOrganization(String organizationId, Organization organization) {
        throw new UnsupportedOperationException("It is not possible to edit an organization from Keycloak");
    }

    @Override
    public void archiveOrganization(String organizationId) {
        throw new UnsupportedOperationException("It is not possible to delete an organization from Keycloak");
    }

    @Override
    public void disableUserOfOrganization(String organizationId) {
        throw new UnsupportedOperationException("It is not possible to disable users of an organization from Keycloak");
    }
}
