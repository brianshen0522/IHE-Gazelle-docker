/*
 * Copyright 2026 IHE International.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.ihe.gazelle.user.management.commons.interlay.dao.organization;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import net.ihe.gazelle.user.management.api.domain.organization.DelegatedOrganization;
import net.ihe.gazelle.user.management.api.domain.organization.Organization;
import net.ihe.gazelle.user.management.commons.application.organization.delegation.DelegatedOrganizationDAO;
import net.ihe.gazelle.user.management.commons.interlay.dao.entities.DelegatedOrganizationEntity;
import net.ihe.gazelle.user.management.commons.interlay.dao.entities.OrganizationEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Implementation of the DelegatedOrganizationDAO interface for organization persistence
 */
@RequestScoped
public class DelegatedOrganizationDAOImpl implements DelegatedOrganizationDAO {

    public static final String EXTERNAL_ID = "externalId";
    public static final String IDP_ID = "idpId";
    private final EntityManager entityManager;

    @Inject
    public DelegatedOrganizationDAOImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public Organization getOrganizationById(String organizationId) {
        DelegatedOrganizationEntity delegatedOrganizationEntity = entityManager.find(DelegatedOrganizationEntity.class, organizationId);
        if (delegatedOrganizationEntity == null) {
            throw new NoSuchElementException("Delegated organization with id " + organizationId + " not found");
        }
        return delegatedOrganizationEntity.asOrganization();
    }

    @Override
    public Organization getOrganizationByName(String organizationName) {
        List<DelegatedOrganizationEntity> delegatedOrganizations = entityManager.createQuery(
                        "SELECT o FROM DelegatedOrganizationEntity o WHERE LOWER(o.name) = LOWER(:name)",
                        DelegatedOrganizationEntity.class)
                .setParameter("name", organizationName)
                .setFirstResult(0)
                .setMaxResults(1)
                .getResultList();

        if (delegatedOrganizations.isEmpty()) {
            return null;
        }
        return delegatedOrganizations.getFirst().asOrganization();
    }

    @Override
    public List<Organization> searchForOrganization(Map<String,String> parameters) {
        String externalId = parameters.get(EXTERNAL_ID);
        String idpId = parameters.get(IDP_ID);
        List<Organization> organizations = new ArrayList<>();
        Boolean delegated = parameters.containsKey("delegated") ? Boolean.parseBoolean(parameters.get("delegated")) : null;

        if (Boolean.FALSE.equals(delegated)) {
            TypedQuery<OrganizationEntity> query = entityManager.createQuery(
                    "SELECT o FROM OrganizationEntity o WHERE TYPE(o) = :orgType",
                    OrganizationEntity.class);
            query.setParameter("orgType", OrganizationEntity.class);
            query.getResultList().forEach(organizationEntity -> organizations.add(organizationEntity.asOrganization()));
            return organizations;
        }

        StringBuilder queryBuilder = new StringBuilder("SELECT o FROM DelegatedOrganizationEntity o");
        List<String> whereClauses = new ArrayList<>();
        if (externalId != null) {
            whereClauses.add("o.externalId = :externalId");
        }
        if (idpId != null) {
            whereClauses.add("o.idpId = :idpId");
        }
        if (!whereClauses.isEmpty()) {
            queryBuilder.append(" WHERE ").append(String.join(" AND ", whereClauses));
        }

        TypedQuery<DelegatedOrganizationEntity> query = entityManager.createQuery(
                queryBuilder.toString(),
                DelegatedOrganizationEntity.class);
        if (externalId != null) {
            query.setParameter(EXTERNAL_ID, externalId);
        }
        if (idpId != null) {
            query.setParameter(IDP_ID, idpId);
        }
        query.getResultList().forEach(organizationEntity -> organizations.add(organizationEntity.asOrganization()));
        return organizations;
    }

    @Override
    public void createDelegatedOrganization(DelegatedOrganization delegatedOrganization) {
        DelegatedOrganizationEntity delegatedOrganizationEntity = new DelegatedOrganizationEntity(
                delegatedOrganization,
                delegatedOrganization.getExternalId(),
                delegatedOrganization.getIdpId());
        entityManager.persist(delegatedOrganizationEntity);
    }

    @Override
    public boolean isDelegatedOrganizationExist(DelegatedOrganization delegatedOrganization) {
        Long result = entityManager.createQuery(
                        "SELECT COUNT(o) FROM DelegatedOrganizationEntity o WHERE o.externalId = :externalId AND o.idpId = :idpId",
                        Long.class)
                .setParameter(EXTERNAL_ID, delegatedOrganization.getExternalId())
                .setParameter(IDP_ID, delegatedOrganization.getIdpId())
                .getSingleResult();
        return result.intValue() != 0;
    }

    @Override
    public Organization updateDelegatedOrganization(String organizationId, DelegatedOrganization delegatedOrganization) {
        DelegatedOrganizationEntity delegatedOrganizationEntity = entityManager.find(DelegatedOrganizationEntity.class, organizationId);
        if (delegatedOrganizationEntity == null) {
            throw new NoSuchElementException("Delegated organization with id " + organizationId + " not found");
        }

        if (delegatedOrganization.getName() != null) {
            delegatedOrganizationEntity.setName(delegatedOrganization.getName());
        }
        if (delegatedOrganization.getShortname() != null) {
            delegatedOrganizationEntity.setShortname(delegatedOrganization.getShortname());
        }
        if (delegatedOrganization.getExternalId() != null) {
            delegatedOrganizationEntity.setExternalId(delegatedOrganization.getExternalId());
        }
        if (delegatedOrganization.getIdpId() != null) {
            delegatedOrganizationEntity.setIdpId(delegatedOrganization.getIdpId());
        }

        return entityManager.merge(delegatedOrganizationEntity).asOrganization();
    }
}
