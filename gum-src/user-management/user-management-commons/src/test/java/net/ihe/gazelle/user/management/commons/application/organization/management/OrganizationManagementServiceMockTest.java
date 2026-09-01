package net.ihe.gazelle.user.management.commons.application.organization.management;

import net.ihe.gazelle.security.business.Authz;
import net.ihe.gazelle.security.business.GazelleIdentity;
import net.ihe.gazelle.user.management.api.application.organization.OrganizationManagementService;
import net.ihe.gazelle.user.management.api.domain.organization.Organization;
import net.ihe.gazelle.user.management.commons.interlay.utils.MockedGazelleIdentity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Set;

import static net.ihe.gazelle.user.management.api.domain.user.GazelleDefaultGroup.GAZELLE_ADMIN;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
class OrganizationManagementServiceMockTest {

    @Mock
    private OrganizationManagementDAO organizationRegistrationDAO;
    @Mock
    private Authz authz;
    private OrganizationManagementService organizationManagementService;
    @Mock
    private OrganizationEventPublisher organizationManagementEventPublisher;

    private final GazelleIdentity mockIdentity = new MockedGazelleIdentity(Set.of(GAZELLE_ADMIN.getName()));

    @BeforeEach
    void beforeEach() {
        organizationManagementService = new OrganizationManagementServiceImpl(organizationRegistrationDAO, organizationManagementEventPublisher, authz);
    }

    @Test
    void testCreateOrganization() {
        Organization organization = generateTestOrganization();

        assertThrows(IllegalArgumentException.class, () -> organizationManagementService.createOrganization(null, mockIdentity));
        assertDoesNotThrow(() -> organizationManagementService.createOrganization(organization, mockIdentity));
    }

    @Test
    void testOrganizationAlreadyExist() {
        assertThrows(IllegalArgumentException.class, () -> organizationManagementService.isOrganizationAlreadyExist(null));
        when(organizationRegistrationDAO.isOrganizationAlreadyExist(new Organization("organizationId"))).thenReturn(true);
        assertTrue(organizationManagementService.isOrganizationAlreadyExist(new Organization("organizationId")));
    }

    @Test
    void testJoinOrganization() {
        Organization organization = generateTestOrganization();
        assertThrows(IllegalArgumentException.class, () -> organizationManagementService.joinOrganization(null, null));
        String orgaId = organization.getId();
        assertThrows(IllegalArgumentException.class, () -> organizationManagementService.joinOrganization(null, orgaId));
        assertThrows(IllegalArgumentException.class, () -> organizationManagementService.joinOrganization("userId", null));
        assertDoesNotThrow(() -> organizationManagementService.joinOrganization("userId", orgaId));
    }

    @Test
    void testUpdateOrganization() {
        GazelleIdentity identity = mock(GazelleIdentity.class);
        Organization organizationToUpdate = new Organization();
        organizationToUpdate.setName("updatedName");

        Organization updatedOrganization = new Organization("organizationId");
        updatedOrganization.setName("updatedName");

        when(organizationRegistrationDAO.getOrganizationFromId("organizationId")).thenReturn(organizationToUpdate);
        when(organizationRegistrationDAO.updateOrganization(eq("organizationId"), any(Organization.class))).thenReturn(updatedOrganization);

        Organization result = organizationManagementService.updateOrganization("organizationId", organizationToUpdate, identity);

        assertEquals(updatedOrganization, result);
        verify(organizationRegistrationDAO).updateOrganization(eq("organizationId"), argThat(org ->
                "updatedName".equals(org.getName())));
    }

    @Test
    void testUpdateOrganizationValidation() {
        GazelleIdentity identity = mock(GazelleIdentity.class);
        Organization organization = generateTestOrganization();

        assertThrows(IllegalArgumentException.class, () -> organizationManagementService.updateOrganization(null, organization, identity));
        assertThrows(IllegalArgumentException.class, () -> organizationManagementService.updateOrganization("organizationId", null, identity));
    }

    @Test
    void testArchiveOrganization() {
        GazelleIdentity identity = mock(GazelleIdentity.class);

        assertThrows(IllegalArgumentException.class, () -> organizationManagementService.archiveOrganization(null, identity));
        assertThrows(IllegalArgumentException.class, () -> organizationManagementService.archiveOrganization("", identity));
        assertDoesNotThrow(() -> organizationManagementService.archiveOrganization("organizationId", identity));
    }

    private Organization generateTestOrganization() {
        Organization organization = new Organization("organizationId");
        organization.setName("organizationName");
        organization.setShortname("shortname");
        return organization;
    }
}
