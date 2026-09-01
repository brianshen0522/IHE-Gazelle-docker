package net.ihe.gazelle.user.management.core.interlay.dao;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Default;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import net.ihe.gazelle.user.management.api.application.ConflictException;
import net.ihe.gazelle.user.management.api.domain.organization.Organization;
import net.ihe.gazelle.user.management.commons.application.organization.management.OrganizationManagementDAO;
import net.ihe.gazelle.user.management.commons.application.user.edit.UserEditDAO;
import net.ihe.gazelle.user.management.commons.interlay.dao.entities.OrganizationEntity;
import net.ihe.gazelle.user.management.commons.interlay.dao.entities.UserEntity;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * Implementation of OrganizationManagementDAO using Quarkus and JPA.
 */
@RequestScoped
@Default
public class OrganizationManagementDAOQuarkus implements OrganizationManagementDAO {

    private final EntityManager entityManager;
    private final UserEditDAO userEditDAO;

    /**
     * Constructor for OrganizationManagementDAOQuarkus, which initializes the UserEditDAO dependency.
     *
     * @param userEditDAO the UserEditDAO to use for updating user organization information
     */
    @Inject
    public OrganizationManagementDAOQuarkus(EntityManager entityManager, UserEditDAO userEditDAO) {
        this.entityManager = entityManager;
        this.userEditDAO = userEditDAO;
    }

    @Override
    public void createOrganization(Organization organization) {
        OrganizationEntity organizationEntity = new OrganizationEntity(organization);
        entityManager.persist(organizationEntity);
    }

    @Override
    public boolean isOrganizationAlreadyExist(Organization organization) {
        OrganizationEntity organizationEntity = entityManager.createQuery(
                        "SELECT o FROM OrganizationEntity o WHERE LOWER(o.shortname) = LOWER(:shortname) OR LOWER(o.id) = LOWER(:id) OR LOWER(o.name) = LOWER(:name)",
                        OrganizationEntity.class)
                .setParameter("shortname", organization.getShortname())
                .setParameter("id", organization.getId())
                .setParameter("name", organization.getName())
                .getResultList()
                .stream()
                .findFirst()
                .orElse(null);

        return organizationEntity != null;
    }

    @Override
    public Organization getOrganizationFromId(String organizationId) {
        OrganizationEntity organizationEntity = entityManager.find( OrganizationEntity.class, organizationId);
        if (organizationEntity == null) {
            throw new NoSuchElementException("Organization with id " + organizationId + " not found");
        }

        return organizationEntity.asOrganization();
    }

    @Override
    public void joinOrganization(String userId, String organizationId) {
        userEditDAO.updateUserOrganization(organizationId, userId);
    }

    @Override
    public Organization updateOrganization(String organizationId, Organization organization) {
        OrganizationEntity organizationEntity = entityManager.find(OrganizationEntity.class, organizationId);
        if (organizationEntity == null) {
            throw new NoSuchElementException("Organization with id " + organizationId + " not found");
        }

        assertNameNotInConflictWithOtherOrganization(organizationId, organization.getName());
        organizationEntity.setName(organization.getName());
        OrganizationEntity mergedOrganizationEntity = entityManager.merge(organizationEntity);
        entityManager.flush();
        entityManager.refresh(mergedOrganizationEntity);
        return mergedOrganizationEntity.asOrganization();
    }

    private void assertNameNotInConflictWithOtherOrganization(String orgaId, String orgaName) {
        List<OrganizationEntity> organizationsWithSameName = entityManager.createQuery(
                        "SELECT o FROM OrganizationEntity o WHERE LOWER(o.name) = LOWER(:name) AND o.id <> :id",
                        OrganizationEntity.class)
                .setParameter("name", orgaName)
                .setParameter("id", orgaId)
                .getResultList();

        if (!organizationsWithSameName.isEmpty()) {
            throw new ConflictException("Organization name " + orgaName + " is already in use by another organization");
        }
    }

    @Override
    public void archiveOrganization(String organizationId) {
        OrganizationEntity organizationEntity = entityManager.find(OrganizationEntity.class, organizationId);
        if (organizationEntity == null) {
            throw new NoSuchElementException("Organization with id " + organizationId + " not found");
        }
        organizationEntity.setArchived(true);
        OrganizationEntity mergedOrganizationEntity = entityManager.merge(organizationEntity);
        entityManager.flush();
        entityManager.refresh(mergedOrganizationEntity);
    }

    @Override
    public void disableUserOfOrganization(String organizationId) {
        List<UserEntity> users = entityManager.createQuery(
                        "SELECT u FROM UserEntity u WHERE u.organizationId = :organizationId", UserEntity.class)
                .setParameter("organizationId", organizationId)
                .getResultList();

        for (UserEntity user : users) {
            user.setActivated(false);
            entityManager.merge(user);
        }
    }
}
