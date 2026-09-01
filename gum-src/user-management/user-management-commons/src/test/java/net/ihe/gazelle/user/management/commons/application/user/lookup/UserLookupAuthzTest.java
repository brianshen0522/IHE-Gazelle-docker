package net.ihe.gazelle.user.management.commons.application.user.lookup;

import net.ihe.gazelle.security.business.*;
import net.ihe.gazelle.security.technical.PermissionStoreSPIProvider;
import net.ihe.gazelle.user.management.api.application.user.lookup.SortOrder;
import net.ihe.gazelle.user.management.api.application.user.lookup.UserLookupService;
import net.ihe.gazelle.user.management.api.application.user.lookup.UserQueryParams;
import net.ihe.gazelle.user.management.api.domain.user.User;
import net.ihe.gazelle.user.management.commons.interlay.utils.MockedGazelleIdentity;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static net.ihe.gazelle.user.management.api.application.user.lookup.UserQueryParams.ATTR_ORGANIZATION_ID;
import static net.ihe.gazelle.user.management.api.domain.user.GazelleDefaultGroup.GAZELLE_ADMIN;
import static net.ihe.gazelle.user.management.api.domain.user.GazelleDefaultGroup.MONITOR;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
class UserLookupAuthzTest {

    public static final String MYUSERID = "myuserid";
    private static UserQueryParams nullQuery;
    @Mock
    private UserLookupDAO userLookupDAOMock;
    private UserLookupService userLookupService;

    private static User user1;
    private static User user2;
    private static User user3;
    private static Authz authz;

    @BeforeAll
    static void setup() {
        createUsersForTests();
        nullQuery = UserQueryParams.nullQuery();
        authz = new AuthzImpl(new PermissionStoreSPIProvider());
    }

    @BeforeEach
    void beforeEach() {
        userLookupService = new UserLookupServiceImpl(userLookupDAOMock, authz);
    }

    @Test
    void testGetUserByIdWithNoGroup() {
        MockedGazelleIdentity mockedGazelleIdentity = new MockedGazelleIdentity(Set.of()).setIdentityId("id");
        assertThrows(UnauthorizedException.class, () -> userLookupService.getUserById("otherId", mockedGazelleIdentity));
    }

    @Test
    void testGetOwnUserByIdWithNoGroup() {
        User user = new User(MYUSERID);
        MockedGazelleIdentity mockedGazelleIdentity = new MockedGazelleIdentity(Set.of()).setIdentityId(MYUSERID);
        when(userLookupDAOMock.getUserById(MYUSERID)).thenReturn(user);

        User userRetrieved = userLookupService.getUserById(MYUSERID, mockedGazelleIdentity);
        assertEquals(user, userRetrieved);
    }

    @Test
    void testGetUserSummaryByIdUnauthenticated() {
        GazelleIdentity identity = BaseGazelleIdentity.unauthenticatedIdentity();
        assertThrows(UnauthorizedException.class, () -> userLookupService.getUserSummaryById("otherId", identity));
    }

    @Test
    void testGetOwnUserSummaryById() {
        User user = new User(MYUSERID);
        user.setEmail("email@email.com");
        MockedGazelleIdentity mockedGazelleIdentity = new MockedGazelleIdentity(Set.of("user")).setIdentityId(MYUSERID);
        when(userLookupDAOMock.getUserSummaryById(MYUSERID)).thenReturn(getUserSummary(user));

        User userRetrieved = userLookupService.getUserSummaryById(MYUSERID, mockedGazelleIdentity);
        assertNotEquals(user, userRetrieved);
    }

    @Test
    void testSearchUsersWithNoGroup() {
        MockedGazelleIdentity mockedGazelleIdentity = new MockedGazelleIdentity(Set.of("bad_role")).setIdentityId(MYUSERID);

        UserQueryParams queryParams = UserQueryParams.nullQuery().setSearch(MYUSERID);
        when(userLookupDAOMock.searchForUsers(eq(queryParams), any(), any(), eq("firstName"), eq(SortOrder.ASC)))
                .thenReturn(new ArrayList<>(List.of(user1)));

        List<User> result = userLookupService.searchAndFilterUsersWithCount(nullQuery, null,
                null, "firstName", SortOrder.ASC, mockedGazelleIdentity).users();
        assertEquals(1, result.size());
    }

    @Test
    void testSearchUsersWithVendorAdminGroup() {
        MockedGazelleIdentity mockedGazelleIdentity = new MockedGazelleIdentity(Set.of("org-adm:orga1")).setIdentityId(MYUSERID).setOrganizationId("orga1");

        UserQueryParams queryParams = UserQueryParams.nullQuery().setSearch("").setAttribute(ATTR_ORGANIZATION_ID, "orga1");
        when(userLookupDAOMock.searchForUsers(eq(queryParams), any(), any(), eq("firstName"), eq(SortOrder.ASC)))
                .thenReturn(new ArrayList<>(List.of(user1, user2)));

        List<User> result = userLookupService.searchAndFilterUsersWithCount(nullQuery, null,
                null, "firstName", SortOrder.ASC, mockedGazelleIdentity).users();
        assertEquals(2, result.size());
    }

    @Test
    void testSearchUsersWithAdminGroup() {
        MockedGazelleIdentity mockedGazelleIdentity = new MockedGazelleIdentity(Set.of(GAZELLE_ADMIN.getName())).setIdentityId(MYUSERID).setOrganizationId("orgaId");

        UserQueryParams queryParams = UserQueryParams.nullQuery().setSearch("");
        when(userLookupDAOMock.searchForUsers(eq(queryParams), any(), any(), eq("firstName"), eq(SortOrder.ASC)))
                .thenReturn(new ArrayList<>(List.of(user1, user2, user3)));

        List<User> result = userLookupService.searchAndFilterUsersWithCount(nullQuery, null,
                null, "firstName", SortOrder.ASC, mockedGazelleIdentity).users();
        assertEquals(3, result.size());
    }

    @Test
    void testSearchUsersWithMonitorGroup() {
        MockedGazelleIdentity mockedGazelleIdentity = new MockedGazelleIdentity(Set.of(MONITOR.getName())).setIdentityId(MYUSERID);

        UserQueryParams queryParams = UserQueryParams.nullQuery().setSearch(mockedGazelleIdentity.getId());
        when(userLookupDAOMock.searchForUsers(eq(queryParams), any(), any(), eq("firstName"), eq(SortOrder.ASC)))
                .thenReturn(new ArrayList<>(List.of(user1)));

        List<User> result = userLookupService.searchAndFilterUsersWithCount(nullQuery, null,
                null, "firstName", SortOrder.ASC, mockedGazelleIdentity).users();
        assertEquals(1, result.size());
    }


    @Test
    void testSearchUsersSummaryAuthenticated() {
        MockedGazelleIdentity mockedGazelleIdentity = new MockedGazelleIdentity(Set.of("user")).setIdentityId(MYUSERID);

        when(userLookupDAOMock.searchForUsersSummary(any(), any(), any(), any(), eq(SortOrder.ASC)))
                .thenReturn(new ArrayList<>(List.of(getUserSummary(user1))));

        List<User> result = userLookupService.searchAndFilterUsersSummary(nullQuery, null,
                null, "firstName", SortOrder.ASC, mockedGazelleIdentity).users();
        assertEquals(1, result.size());
    }

    @Test
    void testSearchUsersSummaryUnauthenticated() {
        GazelleIdentity identity = BaseGazelleIdentity.unauthenticatedIdentity();

        assertThrows(UnauthorizedException.class, () -> userLookupService.searchAndFilterUsersSummary(nullQuery, null,
                null, "firstName", SortOrder.ASC, identity));

    }

    private static void createUsersForTests() {
        user1 = new User("user1");
        user1.setFirstName("fn1");
        user1.setLastName("ln1");
        user1.setOrganizationId("orga1");
        user1.addGroupId("admin");
        user1.setEmail("user1@email.com");
        user1.setActivated(true);

        user2 = new User("user2");
        user2.setEmail("inactive@email.com");
        user2.setFirstName("fn2");
        user2.setLastName("ln2");
        user2.setOrganizationId("orga1");
        user2.setActivated(false);

        user3 = new User("user3");
        user3.setOrganizationId("orga2");
        user3.setFirstName("fn3");
        user3.addGroupId("admin");
        user3.setActivated(true);
    }

    private User getUserSummary(User user) {
        User userSummary = new User();
        userSummary.setId(user.getId());
        userSummary.setFirstName(user.getFirstName());
        userSummary.setLastName(user.getLastName());
        userSummary.setOrganizationId(user.getOrganizationId());
        return userSummary;
    }
}
