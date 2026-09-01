package net.ihe.gazelle.keycloak.core.application.service;

import net.ihe.gazelle.keycloak.core.interlay.publisher.NoOpOrganizationEventPublisher;
import net.ihe.gazelle.user.management.api.application.organization.OrganizationManagementService;
import net.ihe.gazelle.user.management.api.domain.organization.DelegatedOrganization;
import net.ihe.gazelle.user.management.api.domain.organization.Organization;
import net.ihe.gazelle.user.management.commons.application.organization.delegation.DelegatedOrganizationDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class DelegatedOrganizationServiceTest {

    DelegatedOrganizationDAO delegatedOrganizationDAO = mock(DelegatedOrganizationDAO.class);
    OrganizationManagementService organizationManagementService = mock(OrganizationManagementService.class);
    DelegatedOrganizationServiceImpl delegatedOrganizationService;
    DelegatedOrganization delegatedOrganization;
    Organization organization;
    List<Organization> organizations = new ArrayList<>();

    @BeforeEach
    void init() {
        organization = new Organization("Test_Delegation", "Test_Delegation", "Test Delegation");
        delegatedOrganization = new DelegatedOrganization("Test_Delegation", "Test_Delegation", "Test Delegation", "externalId","IDP_TEST");
        delegatedOrganizationService = new DelegatedOrganizationServiceImpl(delegatedOrganizationDAO, organizationManagementService, new NoOpOrganizationEventPublisher());
        organizations.add(organization);
    }

    @Test
    void upsertDelegatedOrganizationForUserShouldCreateDelegatedOrganization() {
        Map<String,String> parameters = getMapParametersFromDelegatedOrganization(delegatedOrganization);
        when(delegatedOrganizationDAO.searchForOrganization(parameters)).thenReturn(List.of(delegatedOrganization));
        when(delegatedOrganizationDAO.isDelegatedOrganizationExist(delegatedOrganization)).thenReturn(false);
        Function<DelegatedOrganization, Organization> organizationFunction = delegatedOrganizationService.getDefaultLocalOrganizationMatcher();
        doNothing().when(delegatedOrganizationDAO).createDelegatedOrganization(delegatedOrganization);
        doNothing().when(organizationManagementService).joinOrganization("userId", "Test_Delegation");

        delegatedOrganizationService.upsertDelegatedOrganizationForUser(delegatedOrganization, "userId", organizationFunction);
        verify(delegatedOrganizationDAO).createDelegatedOrganization(delegatedOrganization);
        verify(organizationManagementService).joinOrganization("userId", "Test_Delegation");
    }

    @Test
    void upsertDelegatedOrganizationForUserShouldUpdateOrganization() {
        Map<String,String> parameters = getMapParametersFromDelegatedOrganization(delegatedOrganization);
        when(delegatedOrganizationDAO.searchForOrganization(parameters)).thenReturn(organizations);
        when(delegatedOrganizationDAO.isDelegatedOrganizationExist(delegatedOrganization)).thenReturn(true);
        Function<DelegatedOrganization, Organization> organizationFunction = delegatedOrganizationService.getDefaultLocalOrganizationMatcher();
        DelegatedOrganization delegatedOrga = new DelegatedOrganization();
        delegatedOrga.setId(delegatedOrganization.getId());
        delegatedOrga.setName(delegatedOrganization.getName());
        delegatedOrga.setExternalId(delegatedOrganization.getExternalId());
        delegatedOrga.setIdpId(delegatedOrganization.getIdpId());


        when(delegatedOrganizationDAO.updateDelegatedOrganization(delegatedOrganization.getId(), delegatedOrga)).thenReturn(organization);
        doNothing().when(organizationManagementService).joinOrganization("userId", "Test_Delegation");
        delegatedOrganizationService.upsertDelegatedOrganizationForUser(delegatedOrganization, "userId", organizationFunction);
        verify(delegatedOrganizationDAO).updateDelegatedOrganization(delegatedOrganization.getId(), delegatedOrganization);
        verify(organizationManagementService).joinOrganization("userId", "Test_Delegation");
    }

    @Test
    void updateOrganization() {
        when(delegatedOrganizationDAO.updateDelegatedOrganization(delegatedOrganization.getId(), delegatedOrganization)).thenReturn(organization);
        Map<String,String> parameters = getMapParametersFromDelegatedOrganization(delegatedOrganization);
        when(delegatedOrganizationDAO.searchForOrganization(parameters)).thenReturn(List.of(delegatedOrganization));

        assertDoesNotThrow(()->delegatedOrganizationService.updateOrganization(delegatedOrganization));
        verify(delegatedOrganizationDAO).updateDelegatedOrganization(delegatedOrganization.getId(), delegatedOrganization);
    }
    @Test
    void updateOrganizationMultipleMatches() {
        Map<String,String> parameters = getMapParametersFromDelegatedOrganization(delegatedOrganization);
        //Case where the parameters match multiple organizations
        when(delegatedOrganizationDAO.searchForOrganization(parameters)).thenReturn(List.of(delegatedOrganization, delegatedOrganization));
        assertThrows(IllegalStateException.class, ()->delegatedOrganizationService.updateOrganization(delegatedOrganization));
    }

    @Test
    void updateOrganizationThrowsIllegalArgumentException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            delegatedOrganizationService.updateOrganization(null);
        });
        assertTrue(exception.getMessage().toLowerCase().contains("delegated organization"));
    }

    @Test
    void createDelegated() {
        doNothing().when(delegatedOrganizationDAO).createDelegatedOrganization(delegatedOrganization);
        delegatedOrganizationService.createDelegatedOrganization(delegatedOrganization);
        verify(delegatedOrganizationDAO).createDelegatedOrganization(delegatedOrganization);
    }

    @Test
    void createDelegatedThrowsIllegalArgumentException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            delegatedOrganizationService.createDelegatedOrganization(null);
        });
        assertTrue(exception.getMessage().toLowerCase().contains("delegated organization"));
    }

    @Test
    void isDelegatedOrganizationExisting() {
        when(delegatedOrganizationDAO.isDelegatedOrganizationExist(delegatedOrganization)).thenReturn(true);
        assertTrue(delegatedOrganizationService.isDelegatedOrganizationExisting(delegatedOrganization));
    }

    @Test
    void isDelegatedOrganizationExistingThrowsIllegalArgumentException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            delegatedOrganizationService.isDelegatedOrganizationExisting(null);
        });
        assertTrue(exception.getMessage().toLowerCase().contains("delegated organization"));
    }

    @Test
    void joinDelegatedOrganization() {
        doNothing().when(organizationManagementService).joinOrganization("userId", "Test_Delegation");
        Map<String,String> parameters = getMapParametersFromDelegatedOrganization(delegatedOrganization);
        when(delegatedOrganizationDAO.searchForOrganization(parameters)).thenReturn(organizations);
        delegatedOrganizationService.joinDelegatedOrganization(delegatedOrganization, "userId");
        verify(organizationManagementService).joinOrganization("userId", "Test_Delegation");
    }

    @Test
    void joinDelegatedOrganizationThrowsIllegalArgumentException() {
        Exception exceptionOrgaNull = assertThrows(IllegalArgumentException.class, () -> {
            delegatedOrganizationService.joinDelegatedOrganization(null, "userID");
        });
        assertTrue(exceptionOrgaNull.getMessage().toLowerCase().contains("should not be null"));

        Exception exceptionUserIdNull = assertThrows(IllegalArgumentException.class, () -> {
            delegatedOrganizationService.joinDelegatedOrganization(delegatedOrganization, null);
        });
        assertTrue(exceptionUserIdNull.getMessage().toLowerCase().contains("should not be null"));
    }

    @Test
    void getDefaultLocalOrganizationMatcher() {
        when(delegatedOrganizationDAO.getOrganizationByName("Test Delegation")).thenReturn(organization);
        Function<DelegatedOrganization, Organization> organizationFunction = delegatedOrganizationService.getDefaultLocalOrganizationMatcher();
        assertNotNull(organizationFunction.apply(delegatedOrganization));

        DelegatedOrganization unknownDelegatedOrganization = new DelegatedOrganization("Organization_not_exist", "Organization_not_exist", "Organization not exist", "externalId","IDP_TEST");

        when(delegatedOrganizationDAO.getOrganizationByName("Organization not exist")).thenThrow(new NoSuchElementException());
        assertNull(organizationFunction.apply(unknownDelegatedOrganization));
    }

    public static Map<String,String> getMapParametersFromDelegatedOrganization(DelegatedOrganization delegatedOrganization) {
        Map<String,String> parameters = new HashMap<>();
        parameters.put("externalId", delegatedOrganization.getExternalId());
        parameters.put("idpId", delegatedOrganization.getIdpId());
        parameters.put("delegated", "true");
        return parameters;
    }
}