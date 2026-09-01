package net.ihe.gazelle.user.management.commons.application.user.edit;

import net.ihe.gazelle.security.business.Authz;
import net.ihe.gazelle.security.business.AuthzImpl;
import net.ihe.gazelle.security.business.UnauthorizedException;
import net.ihe.gazelle.security.technical.PermissionStoreSPIProvider;
import net.ihe.gazelle.user.management.api.application.organization.OrganizationLookupService;
import net.ihe.gazelle.user.management.api.application.user.delegation.UserDelegationService;
import net.ihe.gazelle.user.management.api.application.user.edit.UserEditService;
import net.ihe.gazelle.user.management.api.application.user.lookup.UserLookupService;
import net.ihe.gazelle.user.management.api.domain.user.User;
import net.ihe.gazelle.user.management.commons.application.user.login.HashPasswordServiceProvider;
import net.ihe.gazelle.user.management.commons.application.user.lookup.UserLookupDAO;
import net.ihe.gazelle.user.management.commons.application.user.lookup.UserLookupServiceImpl;
import net.ihe.gazelle.user.management.commons.interlay.provider.HashPasswordServiceSPIProvider;
import net.ihe.gazelle.user.management.commons.interlay.utils.MockedGazelleIdentity;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Locale;
import java.util.Set;

import static net.ihe.gazelle.user.management.api.domain.user.GazelleDefaultGroup.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
class UserEditAuthzTest {
    //TODO Test new authz for roles
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

    private static User user1;
    private static Authz authz;

    @BeforeAll
    static void setup() {
        user1 = createUsersForTests();
        authz = new AuthzImpl(new PermissionStoreSPIProvider());
    }

    @BeforeEach
    void beforeEach() {
        HashPasswordServiceProvider hashPasswordServiceProvider = new HashPasswordServiceSPIProvider();
        UserLookupService userLookupService = new UserLookupServiceImpl(userLookupDAO, authz);
        userEditService = new UserEditServiceImpl(userEditDAOMock, hashPasswordServiceProvider, authz,
                editEmailManager, userLookupService, userDelegationService, organizationLookupService);
    }

    @Test
    void testEditUserWithNoGroup() {
        MockedGazelleIdentity testIdentity = new MockedGazelleIdentity(Set.of()).setIdentityId("id");
        String userId = user1.getId();
        User shellUser = new User();
        shellUser.setFirstName("new FirstName");
        assertThrows(UnauthorizedException.class, () -> userEditService.updateAttributes(userId, shellUser, testIdentity, Locale.ENGLISH));
        assertThrows(UnauthorizedException.class, () -> userEditService.deleteUser(userId, testIdentity, Locale.ENGLISH));
        assertThrows(UnauthorizedException.class, () -> userEditService.deactivateUser(userId, testIdentity));
        assertThrows(UnauthorizedException.class, () -> userEditService.activateUser(userId, testIdentity));
    }

    @Test
    void testEditUserWithBadGroup() {
        MockedGazelleIdentity testIdentity = new MockedGazelleIdentity(Set.of(MONITOR.getName())).setIdentityId("id");
        String userId = user1.getId();
        User shellUser = new User();
        shellUser.setFirstName("new FirstName");
        assertThrows(UnauthorizedException.class, () -> userEditService.updateAttributes(userId, shellUser, testIdentity, Locale.ENGLISH));
        assertThrows(UnauthorizedException.class, () -> userEditService.deleteUser(userId, testIdentity, Locale.ENGLISH));
        assertThrows(UnauthorizedException.class, () -> userEditService.deactivateUser(userId, testIdentity));
        assertThrows(UnauthorizedException.class, () -> userEditService.activateUser(userId, testIdentity));
    }

    @Test
    void testEditOwnUser() {
        String userId = user1.getId();
        MockedGazelleIdentity testOKIdentity = new MockedGazelleIdentity(Set.of(MONITOR.getName())).setIdentityId(userId);
        MockedGazelleIdentity testKOIdentity = new MockedGazelleIdentity(Set.of(MONITOR.getName())).setIdentityId("badId");
        User shellUser = new User();
        shellUser.setFirstName("new-firstName");
        shellUser.setGroupIds(user1.getGroupIds()); //User has the same groups so no update

        // Test user edit its own firstName attribute
        when(userEditDAOMock.updateAttributes(userId, shellUser)).thenReturn(shellUser);
        when(userEditDAOMock.getUserFromUserId(userId)).thenReturn(user1);
        User user = userEditService.updateAttributes(userId, shellUser, testOKIdentity, Locale.ENGLISH);
        assertEquals("new-firstName", user.getFirstName());

        // Test user edit firstName attribute of another user without admin role
        assertThrows(UnauthorizedException.class, () -> userEditService.updateAttributes(userId, shellUser, testKOIdentity, Locale.ENGLISH));
    }

    @Test
    void testUpdateOwnGroupsAdmin() {
        User identityUser = new User(user1);
        identityUser.setId("identityUserId");
        identityUser.setEmail("identityUserId@email.com");
        identityUser.addGroupId(GAZELLE_ADMIN.getName());

        MockedGazelleIdentity gazelleAdminIdentity =
                new MockedGazelleIdentity(identityUser.getGroupIds()).setIdentityId(identityUser.getId());
        User userWithNewAttributes = new User();
        userWithNewAttributes.setId(user1.getId());
        userWithNewAttributes.addGroupId(PROJECT_ADMIN.getName());
        userWithNewAttributes.addGroupId(TESTING_SESSION_MANAGER.getName());
        userWithNewAttributes.addGroupId(TEST_DESIGNER.getName());
        userWithNewAttributes.addGroupId(SUT_OPERATOR.getName());
        userWithNewAttributes.addGroupId(MONITOR.getName());

        String user1Id = user1.getId();
        when(userEditDAOMock.getUserFromUserId(user1Id)).thenReturn(user1);
        when(userEditDAOMock.updateAttributes(user1Id, userWithNewAttributes)).thenReturn(userWithNewAttributes);
        assertDoesNotThrow(() ->
                userEditService.updateAttributes(user1Id, userWithNewAttributes, gazelleAdminIdentity, Locale.ENGLISH));

    }

    @Test
    void testAdminRemoveAdminRole() {
        User identityUser = new User(user1);
        identityUser.setId("identityUserId");
        identityUser.setEmail("identityUserId@email.com");
        identityUser.addGroupId(GAZELLE_ADMIN.getName());

        MockedGazelleIdentity gazelleAdminIdentity =
                new MockedGazelleIdentity(identityUser.getGroupIds()).setIdentityId(identityUser.getId());
        User userWithNewAttributes = new User();
        userWithNewAttributes.setId(identityUser.getId());
        userWithNewAttributes.setGroupIds(Set.of());

        String identityUserId = identityUser.getId();
        when(userEditDAOMock.getUserFromUserId(identityUserId)).thenReturn(identityUser);
        assertThrows(UnauthorizedException.class, () ->
                userEditService.updateAttributes(identityUserId, userWithNewAttributes, gazelleAdminIdentity, Locale.ENGLISH));
    }

    @Test
    void testUpdateGroupsProjectAdmin() {
        User identityUser = new User(user1);
        identityUser.setId("identityUserId");
        identityUser.setEmail("identityUserId@email.com");
        identityUser.addGroupId(PROJECT_ADMIN.getName());

        MockedGazelleIdentity gazelleAdminIdentity =
                new MockedGazelleIdentity(identityUser.getGroupIds()).setIdentityId(identityUser.getId());
        User userWithNewAttributes = new User();
        userWithNewAttributes.setId(user1.getId());
        userWithNewAttributes.addGroupId(TESTING_SESSION_MANAGER.getName());
        userWithNewAttributes.addGroupId(TEST_DESIGNER.getName());
        userWithNewAttributes.addGroupId(MONITOR.getName());
        userWithNewAttributes.addGroupId(SUT_OPERATOR.getName());
        String user1Id = user1.getId();

        when(userEditDAOMock.getUserFromUserId(user1Id)).thenReturn(user1);
        when(userEditDAOMock.updateAttributes(user1Id, userWithNewAttributes)).thenReturn(userWithNewAttributes);
        assertDoesNotThrow(() ->
                userEditService.updateAttributes(user1Id, userWithNewAttributes, gazelleAdminIdentity, Locale.ENGLISH));
        userWithNewAttributes.addGroupId(GAZELLE_ADMIN.getName());

        assertThrows(UnauthorizedException.class, () ->
                userEditService.updateAttributes(user1Id, userWithNewAttributes, gazelleAdminIdentity, Locale.ENGLISH));
    }

    @Test
    void testUpdateGroupsTestingSessionManager() {
        User identityUser = new User(user1);
        identityUser.setId("identityUserId");
        identityUser.setEmail("identityUserId@email.com");
        identityUser.addGroupId(TESTING_SESSION_MANAGER.getName());

        MockedGazelleIdentity gazelleAdminIdentity =
                new MockedGazelleIdentity(identityUser.getGroupIds()).setIdentityId(identityUser.getId());
        User userWithNewAttributes = new User();
        userWithNewAttributes.setId(user1.getId());
        userWithNewAttributes.addGroupId(TEST_DESIGNER.getName());
        userWithNewAttributes.addGroupId(MONITOR.getName());
        userWithNewAttributes.addGroupId(SUT_OPERATOR.getName());
        String user1Id = user1.getId();

        when(userEditDAOMock.getUserFromUserId(user1Id)).thenReturn(user1);
        when(userEditDAOMock.updateAttributes(user1Id, userWithNewAttributes)).thenReturn(userWithNewAttributes);
        assertDoesNotThrow(() ->
                userEditService.updateAttributes(user1Id, userWithNewAttributes, gazelleAdminIdentity, Locale.ENGLISH));

        userWithNewAttributes.setGroupIds(Set.of(GAZELLE_ADMIN.getName()));
        assertThrows(UnauthorizedException.class, () ->
                userEditService.updateAttributes(user1Id, userWithNewAttributes, gazelleAdminIdentity, Locale.ENGLISH));

        userWithNewAttributes.setGroupIds(Set.of(PROJECT_ADMIN.getName()));
        assertThrows(UnauthorizedException.class, () ->
                userEditService.updateAttributes(user1Id, userWithNewAttributes, gazelleAdminIdentity, Locale.ENGLISH));
    }

    @Test
    void testUpdateOrgaAdmin() {
        User identityUser = new User(createUsersForTests());
        identityUser.setId("identityUserId");
        identityUser.setEmail("identityUserId@email.com");

        identityUser.setGroupIds(Set.of(PREFIX_ORGANIZATION_ADMIN.getName() + user1.getOrganizationId()));


        MockedGazelleIdentity gazelleAdminIdentity =
                new MockedGazelleIdentity(identityUser.getGroupIds()).setIdentityId(identityUser.getId());
        gazelleAdminIdentity.setOrganizationId(identityUser.getOrganizationId());
        User userWithNewAttributes = new User(createUsersForTests());
        userWithNewAttributes.addGroupId(SUT_OPERATOR.getName());
        userWithNewAttributes.addGroupId(PREFIX_ORGANIZATION_ADMIN.getName() + user1.getOrganizationId());
        String user1Id = user1.getId();

        when(userEditDAOMock.getUserFromUserId(user1Id)).thenReturn(user1);
        when(userEditDAOMock.updateAttributes(user1Id, userWithNewAttributes)).thenReturn(userWithNewAttributes);
        assertDoesNotThrow(() ->
                userEditService.updateAttributes(user1Id, userWithNewAttributes, gazelleAdminIdentity, Locale.ENGLISH));

        userWithNewAttributes.addGroupId(GAZELLE_ADMIN.getName());

        assertThrows(UnauthorizedException.class, () ->
                userEditService.updateAttributes(user1Id, userWithNewAttributes, gazelleAdminIdentity, Locale.ENGLISH));
    }

    @Test
    void testUpdateOwnOrgaAdmin() {
        String user1Id = "identityUserId";
        User identityUser = new User(createUsersForTests());
        identityUser.setId(user1Id);
        identityUser.setEmail("identityUserId@email.com");
        identityUser.setGroupIds(Set.of(PREFIX_ORGANIZATION_ADMIN.getName() + user1.getOrganizationId(), SUT_OPERATOR.getName()));

        MockedGazelleIdentity mockedGazelleIdentity =
                new MockedGazelleIdentity(identityUser.getGroupIds()).setIdentityId(identityUser.getId());
        mockedGazelleIdentity.setOrganizationId(identityUser.getOrganizationId());
        User userWithoutOrgaAdminGroup = new User(identityUser);
        userWithoutOrgaAdminGroup.setGroupIds(Set.of(SUT_OPERATOR.getName()));
        userWithoutOrgaAdminGroup.setEmail(null);


        when(userEditDAOMock.getUserFromUserId(user1Id)).thenReturn(identityUser);
        assertThrows(UnauthorizedException.class, () ->
                userEditService.updateAttributes(user1Id, userWithoutOrgaAdminGroup, mockedGazelleIdentity, Locale.ENGLISH));

        User userWithoutSutOperatorGroup = new User(identityUser);
        userWithoutSutOperatorGroup.setGroupIds(Set.of(PREFIX_ORGANIZATION_ADMIN.getName() + user1.getOrganizationId()));
        userWithoutSutOperatorGroup.setEmail(null);

        when(userEditDAOMock.getUserFromUserId(user1Id)).thenReturn(identityUser);
        assertThrows(UnauthorizedException.class, () ->
                userEditService.updateAttributes(user1Id, userWithoutSutOperatorGroup, mockedGazelleIdentity, Locale.ENGLISH));

    }


    private static User createUsersForTests() {
        User user = new User("user1");
        user.setFirstName("fnOne");
        user.setLastName("lnOne");
        user.setOrganizationId("orgaOne");
        user.addGroupId(SUT_OPERATOR.getName());
        user.setEmail("user1@email.com");
        user.setActivated(true);
        return user;
    }
}
