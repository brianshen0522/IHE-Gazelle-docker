package net.ihe.gazelle.keycloak.core.interlay.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import net.ihe.gazelle.user.management.api.domain.organization.DelegatedOrganization;
import net.ihe.gazelle.user.management.api.domain.organization.Organization;
import net.ihe.gazelle.user.management.commons.interlay.dao.entities.DelegatedOrganizationEntity;
import net.ihe.gazelle.user.management.commons.interlay.dao.entities.OrganizationEntity;
import net.ihe.gazelle.user.management.commons.interlay.dao.organization.DelegatedOrganizationDAOImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class DelegatedOrganizationDAOTest {

    private static final String TEST_IDP = "TEST_IDP";
    private static final String EXTERNAL_ID = "9d33f3-feda-4688-a663-cc6f9ad7f3a2";

    private EntityManager entityManager;
    private DelegatedOrganizationDAOImpl delegatedOrganizationDAO;

    @BeforeEach
    void setUp() {
        entityManager = mock(EntityManager.class);
        delegatedOrganizationDAO = new DelegatedOrganizationDAOImpl(entityManager);
    }

    @Test
    void getOrganizationByIdReturnsDelegatedOrganization() {
        DelegatedOrganizationEntity entity = new DelegatedOrganizationEntity(
                new Organization("orga-id", "orga-short", "My organization"),
                EXTERNAL_ID,
                TEST_IDP
        );
        when(entityManager.find(DelegatedOrganizationEntity.class, "orga-id")).thenReturn(entity);

        Organization received = delegatedOrganizationDAO.getOrganizationById("orga-id");

        assertEquals("orga-id", received.getId());
        assertEquals("My organization", received.getName());
    }

    @Test
    void getOrganizationByIdThrowsWhenNotFound() {
        when(entityManager.find(DelegatedOrganizationEntity.class, "missing")).thenReturn(null);
        assertThrows(NoSuchElementException.class, () -> delegatedOrganizationDAO.getOrganizationById("missing"));
    }

    @Test
    void getOrganizationByNameReturnsFirstMatch() {
        TypedQuery<DelegatedOrganizationEntity> query = mock(TypedQuery.class);
        when(entityManager.createQuery(anyString(), eq(DelegatedOrganizationEntity.class))).thenReturn(query);
        when(query.setParameter("name", "my name")).thenReturn(query);
        when(query.setFirstResult(0)).thenReturn(query);
        when(query.setMaxResults(1)).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of(new DelegatedOrganizationEntity(
                new Organization("orga-id", "short", "my name"), EXTERNAL_ID, TEST_IDP)));

        Organization found = delegatedOrganizationDAO.getOrganizationByName("my name");

        assertNotNull(found);
        assertEquals("orga-id", found.getId());
    }

    @Test
    void searchForOrganizationUsesDelegatedEntityQuery() {
        TypedQuery<DelegatedOrganizationEntity> query = mock(TypedQuery.class);
        ArgumentCaptor<String> queryCaptor = ArgumentCaptor.forClass(String.class);
        when(entityManager.createQuery(queryCaptor.capture(), eq(DelegatedOrganizationEntity.class))).thenReturn(query);
        when(query.setParameter("externalId", EXTERNAL_ID)).thenReturn(query);
        when(query.setParameter("idpId", TEST_IDP)).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of(new DelegatedOrganizationEntity(
                new Organization("orga-id", "short", "my name"), EXTERNAL_ID, TEST_IDP)));

        List<Organization> organizations = delegatedOrganizationDAO.searchForOrganization(
                Map.of("externalId", EXTERNAL_ID, "idpId", TEST_IDP, "delegated", "true")
        );

        assertEquals(1, organizations.size());
        assertTrue(queryCaptor.getValue().contains("DelegatedOrganizationEntity"));
        assertTrue(queryCaptor.getValue().contains("o.externalId = :externalId"));
        assertTrue(queryCaptor.getValue().contains("o.idpId = :idpId"));
    }

    @Test
    void searchForOrganizationWithDelegatedFalseUsesOrganizationEntityQuery() {
        TypedQuery<OrganizationEntity> query = mock(TypedQuery.class);
        when(entityManager.createQuery("SELECT o FROM OrganizationEntity o WHERE TYPE(o) = :orgType", OrganizationEntity.class)).thenReturn(query);
        when(query.setParameter("orgType", OrganizationEntity.class)).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of(new OrganizationEntity("orga-id", "my name", "short")));

        List<Organization> organizations = delegatedOrganizationDAO.searchForOrganization(Map.of("delegated", "false"));

        assertEquals(1, organizations.size());
        assertEquals("orga-id", organizations.getFirst().getId());
    }

    @Test
    void createDelegatedOrganizationPersistsDelegatedEntity() {
        DelegatedOrganization delegatedOrganization = new DelegatedOrganization(
                "orga-id", "short", "my name", EXTERNAL_ID, TEST_IDP
        );

        delegatedOrganizationDAO.createDelegatedOrganization(delegatedOrganization);

        ArgumentCaptor<DelegatedOrganizationEntity> captor = ArgumentCaptor.forClass(DelegatedOrganizationEntity.class);
        verify(entityManager, times(1)).persist(captor.capture());
        assertEquals(EXTERNAL_ID, captor.getValue().getExternalId());
        assertEquals(TEST_IDP, captor.getValue().getIdpId());
    }

    @Test
    void isDelegatedOrganizationExistReturnsTrueWhenCountIsPositive() {
        TypedQuery<Long> query = mock(TypedQuery.class);
        when(entityManager.createQuery(anyString(), eq(Long.class))).thenReturn(query);
        when(query.setParameter("externalId", EXTERNAL_ID)).thenReturn(query);
        when(query.setParameter("idpId", TEST_IDP)).thenReturn(query);
        when(query.getSingleResult()).thenReturn(1L);

        DelegatedOrganization delegatedOrganization = new DelegatedOrganization()
                .setExternalId(EXTERNAL_ID)
                .setIdpId(TEST_IDP);

        assertTrue(delegatedOrganizationDAO.isDelegatedOrganizationExist(delegatedOrganization));
    }

    @Test
    void updateDelegatedOrganizationUpdatesProvidedFields() {
        DelegatedOrganizationEntity entity = new DelegatedOrganizationEntity(
                new Organization("orga-id", "short", "old-name"),
                "old-external-id",
                "old-idp"
        );
        when(entityManager.find(DelegatedOrganizationEntity.class, "orga-id")).thenReturn(entity);
        when(entityManager.merge(any(DelegatedOrganizationEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DelegatedOrganization patch = new DelegatedOrganization();
        patch.setName("new-name");
        patch.setExternalId(EXTERNAL_ID);
        patch.setIdpId(TEST_IDP);

        Organization updated = delegatedOrganizationDAO.updateDelegatedOrganization("orga-id", patch);

        assertEquals("new-name", updated.getName());
        assertEquals("orga-id", updated.getId());
    }
}