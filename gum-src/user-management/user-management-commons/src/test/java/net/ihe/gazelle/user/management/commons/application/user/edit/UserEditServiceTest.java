package net.ihe.gazelle.user.management.commons.application.user.edit;

import net.ihe.gazelle.security.business.Authz;
import net.ihe.gazelle.security.business.AuthzImpl;
import net.ihe.gazelle.security.business.GazelleIdentity;
import net.ihe.gazelle.security.business.UnauthorizedException;
import net.ihe.gazelle.security.technical.PermissionStoreSPIProvider;
import net.ihe.gazelle.user.management.api.application.organization.OrganizationLookupService;
import net.ihe.gazelle.user.management.api.application.user.delegation.UserDelegationService;
import net.ihe.gazelle.user.management.api.application.user.edit.UserEditException;
import net.ihe.gazelle.user.management.api.application.user.edit.UserEditService;
import net.ihe.gazelle.user.management.api.application.user.lookup.UserLookupService;
import net.ihe.gazelle.user.management.api.domain.organization.Organization;
import net.ihe.gazelle.user.management.api.domain.user.GazelleDefaultGroup;
import net.ihe.gazelle.user.management.api.domain.user.User;
import net.ihe.gazelle.user.management.commons.application.exception.GazelleDAOException;
import net.ihe.gazelle.user.management.commons.application.user.login.HashPasswordServiceProvider;
import net.ihe.gazelle.user.management.commons.application.user.lookup.UserLookupDAO;
import net.ihe.gazelle.user.management.commons.application.user.lookup.UserLookupServiceImpl;
import net.ihe.gazelle.user.management.commons.interlay.provider.HashPasswordServiceSPIProvider;
import net.ihe.gazelle.user.management.commons.interlay.utils.MockedGazelleIdentity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
class UserEditServiceTest {

    private static final String USER_ID = "id";
    @Mock
    private UserEditDAO userEditDAOMock;
    @Mock
    private UserLookupDAO userLookupDAO;
    @Mock
    private UserEditEmailManager editEmailManager;
    @Mock
    private UserDelegationService userDelegationService;
    @Mock
    private OrganizationLookupService organizationLookupService;

    private UserEditService userEditService;
    private GazelleIdentity mockedGazelleIdentity;
    private final Authz authz = new AuthzImpl(new PermissionStoreSPIProvider());

    @BeforeEach
    void beforeEach() {
        mockedGazelleIdentity = new MockedGazelleIdentity(Set.of(GazelleDefaultGroup.GAZELLE_ADMIN.getName()));
        HashPasswordServiceProvider hashPasswordServiceProvider = new HashPasswordServiceSPIProvider();
        UserLookupService userLookupService = new UserLookupServiceImpl(userLookupDAO, authz);
        userEditService = new UserEditServiceImpl(userEditDAOMock, hashPasswordServiceProvider, authz,
                editEmailManager, userLookupService, userDelegationService, organizationLookupService);
    }

    @Test
    void testUpdateAttributes() {
        User oldUser = new User("userId", "fn", "ln", "email@test.fr", null);
        User newUser = new User(oldUser);
        newUser.setEmail("newEmail@test.fr");
        when(userEditDAOMock.getUserFromUserId("newUserId")).thenReturn(getExistingUser(true));
        when(userEditDAOMock.updateAttributes("newUserId", newUser)).thenReturn(newUser);
        when(userLookupDAO.getUserByEmail("newemail@test.fr")).thenThrow(new NoSuchElementException());
        User user2 = assertDoesNotThrow(() -> userEditService.updateAttributes("newUserId", newUser, mockedGazelleIdentity, Locale.ENGLISH));

        assertEquals("newUserId", user2.getId());
        assertEquals("fn", user2.getFirstName());
        assertEquals("fn", user2.getFirstName());
        assertEquals("newemail@test.fr", user2.getEmail());
    }

    @Test
    void testEditUserAttribute() {
        // Prepare data
        User user = new User(USER_ID);
        user.setFirstName("firstName");
        user.setEmail("email@test.fr");
        user.setActivated(true);

        when(userEditDAOMock.getUserFromUserId(null)).thenReturn(null);
        assertThrows(IllegalArgumentException.class, () -> userEditService.updateAttributes(null, user, mockedGazelleIdentity, Locale.ENGLISH));

        when(userEditDAOMock.getUserFromUserId(USER_ID)).thenReturn(getExistingUser(true));
        when(userEditDAOMock.updateAttributes(USER_ID, user)).thenReturn(generateTestUser());
        assertThrows(IllegalArgumentException.class, () -> userEditService.updateAttributes(USER_ID, null, mockedGazelleIdentity, Locale.ENGLISH));
        User updatedUser = userEditService.updateAttributes(USER_ID, user, mockedGazelleIdentity, Locale.ENGLISH);
        assertNotNull(updatedUser);
        assertEquals("firstName", updatedUser.getFirstName());
        assertEquals("email", updatedUser.getEmail());
        assertFalse(updatedUser.isActivated());
    }

    @Test
    void testEditUserOrgaRemoveOrgaAdminGroup() {
        // Prepare data
        User existingUser = new User(USER_ID);
        existingUser.setFirstName("firstName");
        existingUser.setOrganizationId("orga1");
        existingUser.setGroupIds(Set.of("org-adm:orga1", "org-adm:orga2", "org:orga1"));

        User expectedUser = new User(existingUser);
        expectedUser.setOrganizationId("orga1");
        expectedUser.setGroupIds(Set.of("org:orga1"));

        when(userEditDAOMock.getUserFromUserId(USER_ID)).thenReturn(existingUser);
        when(userEditDAOMock.updateAttributes(USER_ID, expectedUser)).thenReturn(expectedUser);
        User updatedUser = userEditService.updateAttributes(USER_ID, expectedUser, mockedGazelleIdentity, Locale.ENGLISH);
        assertEquals("firstName", updatedUser.getFirstName());
        assertEquals(Set.of("org:orga1"), updatedUser.getGroupIds());
    }

    @Test
    void testEditUserOrgaAsKeycloak() {
        // Prepare data
        User existingUser = new User(USER_ID);
        existingUser.setFirstName("firstName");
        existingUser.setOrganizationId("orga1");
        existingUser.setGroupIds(Set.of("org:orga1", "role:monitor"));

        User userRequest = new User(existingUser);
        userRequest.setOrganizationId("orga2");
        userRequest.setGroupIds(null);

        User expectedUser = new User(existingUser);
        expectedUser.setOrganizationId("orga2");
        expectedUser.setGroupIds(Set.of("org:orga2", "role:monitor"));

        GazelleIdentity keycloakIdentity = new MockedGazelleIdentity(Set.of("keycloak_admin"));

        when(userEditDAOMock.getUserFromUserId(USER_ID)).thenReturn(existingUser);
        when(organizationLookupService.getOrganizationById("orga2")).thenReturn(new Organization("orga2").setName("orgaName"));
        when(userEditDAOMock.updateAttributes(USER_ID, expectedUser)).thenReturn(expectedUser);
        User updatedUser = userEditService.updateAttributes(USER_ID, userRequest, keycloakIdentity, Locale.ENGLISH);
        assertEquals("firstName", updatedUser.getFirstName());
        assertEquals(Set.of("org:orga2", "role:monitor"), updatedUser.getGroupIds());
        assertEquals("orga2", updatedUser.getOrganizationId());
    }

    @Test
    void testEditUserOrgaUnauthorized() {
        // Prepare data
        User existingUser = new User(USER_ID);
        existingUser.setFirstName("firstName");
        existingUser.setOrganizationId("orga1");
        existingUser.setGroupIds(Set.of("org:orga1", "role:monitor"));

        User userRequest = new User(existingUser);
        userRequest.setOrganizationId("orga2");
        userRequest.setGroupIds(null);

        when(userEditDAOMock.getUserFromUserId(USER_ID)).thenReturn(existingUser);
        assertThrows(UnauthorizedException.class, () -> userEditService.updateAttributes(USER_ID, userRequest, mockedGazelleIdentity, Locale.ENGLISH));
    }

    @Test
    void testEditDisabledUser() {
        // Prepare data
        User user = new User(USER_ID);
        user.setFirstName("firstName");
        user.setEmail("email@test.fr");
        user.setActivated(false);

        when(userEditDAOMock.getUserFromUserId(USER_ID)).thenReturn(getExistingUser(false));
        assertThrows(IllegalStateException.class, () -> userEditService.updateAttributes(USER_ID, user, mockedGazelleIdentity, Locale.ENGLISH));

        user.setActivated(true);
        assertDoesNotThrow(() -> userEditService.updateAttributes(USER_ID, user, mockedGazelleIdentity, Locale.ENGLISH));
    }


    @Test
    void testEditUserPassword() {
        // Prepare data
        String newPassword = "!!newPasswordSecure22";

        // Prepare mock
        doNothing().when(userEditDAOMock).updateCredentialsForUserId(ArgumentMatchers.eq(USER_ID), ArgumentMatchers.any());
        doThrow(new GazelleDAOException("not found")).when(userEditDAOMock).updateCredentialsForUserId(ArgumentMatchers.eq("badId"), ArgumentMatchers.any());

        // Perform asserts
        userEditService.updatePasswordForUserId(USER_ID, newPassword, newPassword);
        assertThrows(IllegalArgumentException.class, () -> userEditService.updatePasswordForUserId(null, newPassword, newPassword));
        assertThrows(IllegalArgumentException.class, () -> userEditService.updatePasswordForUserId(USER_ID, null, newPassword));
        assertThrows(GazelleDAOException.class, () -> userEditService.updatePasswordForUserId("badId", newPassword, newPassword));

        // test for delegated user
        when(userDelegationService.isUserDelegatedFromId(anyString())).thenReturn(true);
        assertThrows(UserEditException.class, () -> userEditService.updatePasswordForUserId(USER_ID, newPassword, newPassword));
    }

    @Test
    void testActivateUser() {
        assertThrows(IllegalArgumentException.class, () -> userEditService.deactivateUser(null, mockedGazelleIdentity));

        when(userEditDAOMock.getUserFromUserId("userID")).thenReturn(getExistingUser(true));
        assertDoesNotThrow(() -> userEditService.deactivateUser("userID", mockedGazelleIdentity));
        assertDoesNotThrow(() -> userEditService.activateUser("userID", mockedGazelleIdentity));
    }

    @Test
    void testDeleteUser() {
        assertThrows(IllegalArgumentException.class, () -> userEditService.deleteUser(null, mockedGazelleIdentity, Locale.ENGLISH));
        doThrow(new NoSuchElementException()).when(userEditDAOMock).getUserFromUserId("wrongId");
        assertThrows(NoSuchElementException.class, () -> userEditService.deleteUser("wrongId", mockedGazelleIdentity, Locale.ENGLISH));

        doNothing().when(userEditDAOMock).deleteUser("userID");
        doNothing().when(userEditDAOMock).archiveOrgaIfNoMembers("orga");
        User user = new User(USER_ID, "fn", "ln", "email@test.fr");
        user.setOrganizationId("orga");
        when(userEditDAOMock.getUserFromUserId("userID")).thenReturn(user);
        assertDoesNotThrow(() -> userEditService.deleteUser("userID", mockedGazelleIdentity, Locale.ENGLISH));
    }


    private User generateTestUser() {
        User user = new User(USER_ID);
        user.setFirstName("firstName");
        user.setLastName("lastname");
        user.setLastLoginTimestamp(1000);
        user.setEmail("email");
        user.setActivated(false);
        user.setActivationCode("goodActivationCode");
        return user;
    }


    static User getExistingUser(boolean activated) {
        User user = new User();
        user.setOrganizationId("orga");
        user.setEmail("email@test.fr");
        user.setActivated(activated);

        return user;
    }
}
