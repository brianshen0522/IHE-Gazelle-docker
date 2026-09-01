package net.ihe.gazelle.user.management.commons.application.group;

import net.ihe.gazelle.security.business.Authz;
import net.ihe.gazelle.security.business.AuthzImpl;
import net.ihe.gazelle.security.business.UnauthorizedException;
import net.ihe.gazelle.security.technical.PermissionStoreSPIProvider;
import net.ihe.gazelle.user.management.api.application.group.GroupService;
import net.ihe.gazelle.user.management.api.domain.group.Group;
import net.ihe.gazelle.user.management.api.domain.group.GroupType;
import net.ihe.gazelle.user.management.api.domain.user.GazelleDefaultGroup;
import net.ihe.gazelle.user.management.api.domain.user.User;
import net.ihe.gazelle.user.management.commons.application.user.lookup.UserLookupDAO;
import net.ihe.gazelle.user.management.commons.interlay.utils.MockedGazelleIdentity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
class GroupServiceTest {

    @Mock
    private GroupDAO groupDAOMock;
    @Mock
    private UserLookupDAO userLookupDAOMock;
    private GroupService groupService;

    private final Authz authzService = new AuthzImpl(new PermissionStoreSPIProvider());
    private final MockedGazelleIdentity mockedGzlIdentity = new MockedGazelleIdentity(Set.of(GazelleDefaultGroup.GAZELLE_ADMIN.getName()));

    @BeforeEach
    void beforeEach() {
        groupService = new GroupServiceImpl(authzService, groupDAOMock, userLookupDAOMock);
    }

    @Test
    void testSearchForGroups() {
        Group group = new Group(GroupType.ROLE, "orga-administrator");
        when(groupDAOMock.searchForGroup(null, null, 0, 500)).thenReturn(Set.of(group));
        Set<Group> groups = groupService.searchForGroup(null, null, null, null, mockedGzlIdentity);
        assertEquals(1, groups.size());
        assertTrue(groups.stream().anyMatch(group1 -> group1.getReference().equals("orga-administrator")));
        assertTrue(groups.stream().anyMatch(group1 -> group1.getType().equals(GroupType.ROLE)));

        when(groupDAOMock.searchForGroup(null, null, 0, 1)).thenReturn(Set.of(group));
        groups = groupService.searchForGroup(null, null, 0, 1, mockedGzlIdentity);
        assertEquals(1, groups.size());
    }

    @Test
    void testCreateGroup() {
        assertThrows(IllegalArgumentException.class, () -> groupService.createGroup(null, mockedGzlIdentity));
        assertThrows(IllegalArgumentException.class, () -> new Group("bad-prefix:orga-administrator"));

        Group badGroupId = new Group();
        badGroupId.setId("bad-prefix");
        badGroupId.setReference("orga-administrator");
        assertThrows(IllegalArgumentException.class, () -> groupService.createGroup(badGroupId, mockedGzlIdentity));

        Group badGroupRef = new Group(GroupType.ROLE, "orga:administrator");
        badGroupRef.setName("Organization administrator");
        assertThrows(IllegalArgumentException.class, () -> groupService.createGroup(badGroupRef, mockedGzlIdentity));

        Group inconsistentGroup = new Group(GroupType.ROLE, "orga-administrator");
        inconsistentGroup.setId("inconsistanttype:orga-administrator");
        assertThrows(IllegalArgumentException.class, () -> groupService.createGroup(badGroupRef, mockedGzlIdentity));

        Group group = new Group(GroupType.ROLE, "orga-administrator");
        when(groupDAOMock.createGroup(group)).thenReturn(group);
        groupService.createGroup(group, mockedGzlIdentity);
    }

    @Test
    void testUpdateGroup() {
        assertThrows(IllegalArgumentException.class, () -> groupService.updateGroup(null, null, null, mockedGzlIdentity));

        Group groupToUpdate = new Group(GroupType.ROLE, "orga-administrator");
        groupToUpdate.setName("Orga administrator");

        when(groupDAOMock.updateGroup("group:orga-administrator", "Orga administrator", new HashSet<>())).thenReturn(groupToUpdate);
        Group updatedGroup = groupService.updateGroup("group:orga-administrator", "Orga administrator", new HashSet<>(), mockedGzlIdentity);
        assertEquals("Orga administrator", updatedGroup.getName());
    }

    @Test
    void testDeleteGroup() {
        assertThrows(IllegalArgumentException.class, () -> groupService.deleteGroup(null, mockedGzlIdentity));

        doNothing().when(groupDAOMock).deleteGroup("myDeletedGroup");
        groupService.deleteGroup("myDeletedGroup", mockedGzlIdentity);
    }

    @Test
    void testJoinGroup() {
        String groupId = GazelleDefaultGroup.SUT_OPERATOR.getName();
        assertThrows(IllegalArgumentException.class, () -> groupService.joinGroup(null, null, mockedGzlIdentity));
        assertThrows(IllegalArgumentException.class, () -> groupService.joinGroup(null, groupId, mockedGzlIdentity));

        User targetUser = user("roleUser1", "orga1");
        when(userLookupDAOMock.getUserById("roleUser1")).thenReturn(targetUser);
        doNothing().when(groupDAOMock).joinGroup("roleUser1", groupId);
        groupService.joinGroup("roleUser1", groupId, mockedGzlIdentity);
    }

    @Test
    void normalUserCannotGrantGazelleAdminRole() {
        User targetUser = user("attacker", "orga1");
        MockedGazelleIdentity normalUser = new MockedGazelleIdentity(Set.of())
              .setIdentityId("attacker")
              .setOrganizationId("orga1");
        when(userLookupDAOMock.getUserById("attacker")).thenReturn(targetUser);
        String adminGroupId = GazelleDefaultGroup.GAZELLE_ADMIN.getName();

        assertThrows(UnauthorizedException.class, () -> groupService.joinGroup(
              "attacker", adminGroupId, normalUser));
        verify(groupDAOMock, never()).joinGroup("attacker", GazelleDefaultGroup.GAZELLE_ADMIN.getName());
    }

    @Test
    void organizationAdminCannotGrantRoleOutsideItsOrganization() {
        User targetUser = user("target", "orga2");
        MockedGazelleIdentity orgaAdmin = new MockedGazelleIdentity(Set.of("org-adm:orga1"))
              .setIdentityId("orgaAdmin")
              .setOrganizationId("orga1");
        when(userLookupDAOMock.getUserById("target")).thenReturn(targetUser);
        String sutOperatorGroupId = GazelleDefaultGroup.SUT_OPERATOR.getName();

        assertThrows(UnauthorizedException.class, () -> groupService.joinGroup(
            "target", sutOperatorGroupId, orgaAdmin));
        verify(groupDAOMock, never()).joinGroup("target", GazelleDefaultGroup.SUT_OPERATOR.getName());
    }

    @Test
    void keycloakInternalIdentityCanSynchronizeCustomRole() {
        User targetUser = user("target", "orga1");
        MockedGazelleIdentity mockedGazelleIdentity = new MockedGazelleIdentity(Set.of("keycloak_admin"))
              .setIdentityId("keycloakID");
        when(userLookupDAOMock.getUserById("target")).thenReturn(targetUser);

        groupService.joinGroup("target", "role:custom", mockedGazelleIdentity);

        verify(groupDAOMock).joinGroup("target", "role:custom");
    }

    @Test
    void testJoinOrgaGroupUnauthorized() {
       String groupId = "org-adm:my_orga";
       MockedGazelleIdentity gazelleIdentity = new MockedGazelleIdentity(Set.of("org:orga1"));
       assertThrows(UnauthorizedException.class, () -> groupService.joinGroup("roleUser1", groupId, gazelleIdentity));
    }

    @Test
    void testJoinOrgaGroupAuthorized() {
        String groupId = "org:my_orga";
        assertDoesNotThrow(() -> groupService.joinGroup("roleUser1", groupId, mockedGzlIdentity));
    }

    @Test
    void testLeaveGroup() {
        String groupId = GazelleDefaultGroup.SUT_OPERATOR.getName();
        assertThrows(IllegalArgumentException.class, () -> groupService.leaveGroup(null, null, mockedGzlIdentity));
        assertThrows(IllegalArgumentException.class, () -> groupService.leaveGroup(null, groupId, mockedGzlIdentity));

        doNothing().when(groupDAOMock).leaveGroup("roleUser1", groupId);
        groupService.leaveGroup("roleUser1", groupId, mockedGzlIdentity);
    }

    @Test
    void normalUserCannotRemoveGazelleAdminRoleFromAnotherUser() {
        User targetUser = user("admin", "orga1");
        targetUser.setGroupIds(Set.of(GazelleDefaultGroup.GAZELLE_ADMIN.getName()));
        MockedGazelleIdentity normalUser = new MockedGazelleIdentity(Set.of())
              .setIdentityId("attacker")
              .setOrganizationId("orga1");
        when(userLookupDAOMock.getUserById("admin")).thenReturn(targetUser);
        String adminGroupId = GazelleDefaultGroup.GAZELLE_ADMIN.getName();

        assertThrows(UnauthorizedException.class, () -> groupService.leaveGroup(
            "admin", adminGroupId, normalUser));
        verify(groupDAOMock, never()).leaveGroup("admin", GazelleDefaultGroup.GAZELLE_ADMIN.getName());
    }

    private static User user(String id, String organizationId) {
        User user = new User(id);
        user.setOrganizationId(organizationId);
        user.setGroupIds(new HashSet<>());
        return user;
    }

   @Test
    void testLeaveOrgaGroupUnauthorized() {
        String groupId = "org-adm:my_orga";
        MockedGazelleIdentity gazelleIdentity = new MockedGazelleIdentity(Set.of("org:orga1"));
        assertThrows(UnauthorizedException.class, () -> groupService.leaveGroup("roleUser1", groupId, gazelleIdentity));
    }

    @Test
    void testLeaveOrgaGroupAuthorized() {
        String groupId = "org:my_orga";
        assertDoesNotThrow(() -> groupService.leaveGroup("roleUser1", groupId, mockedGzlIdentity));
    }
}
