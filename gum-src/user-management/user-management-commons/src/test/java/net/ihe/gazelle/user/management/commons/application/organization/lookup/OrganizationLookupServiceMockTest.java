package net.ihe.gazelle.user.management.commons.application.organization.lookup;

import net.ihe.gazelle.user.management.api.application.organization.OrganizationLookupService;
import net.ihe.gazelle.user.management.api.domain.organization.Organization;
import net.ihe.gazelle.user.management.commons.application.exception.ErrorMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
class OrganizationLookupServiceMockTest {

    @Mock
    private OrganizationLookupDAO organizationLookupDAOMock;
    private OrganizationLookupService organizationLookupService;

    @BeforeEach
    void beforeEach() {
        organizationLookupService = new OrganizationLookupServiceImpl(organizationLookupDAOMock);
    }

    @Test
    void testGetOrganizationById() {
        Organization organization = generateTestOrganization();
        when(organizationLookupDAOMock.getOrganizationById("orgaId")).thenReturn(organization);
        Organization organizationResult = assertDoesNotThrow(() -> organizationLookupService.getOrganizationById("orgaId"));

        assertEquals("orgaId", organizationResult.getId());
    }

    @Test
    void testGetOrganizationByIdThrowsNoSuchElementException() {
        when(organizationLookupDAOMock.getOrganizationById("testOrga")).thenThrow(new NoSuchElementException(ErrorMessage.ORGANIZATION_NOT_FOUND.getMessage()));
        assertThrows(NoSuchElementException.class, () -> organizationLookupService.getOrganizationById("testOrga"));
    }

    private Organization generateTestOrganization() {
        Organization organization = new Organization("orgaId");
        organization.setName("organizationName");
        return organization;
    }
}
