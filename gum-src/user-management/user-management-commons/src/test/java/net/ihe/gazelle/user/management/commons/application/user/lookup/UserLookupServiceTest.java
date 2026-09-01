package net.ihe.gazelle.user.management.commons.application.user.lookup;

import net.ihe.gazelle.security.business.Authz;
import net.ihe.gazelle.security.business.AuthzImpl;
import net.ihe.gazelle.security.technical.PermissionStoreSPIProvider;
import net.ihe.gazelle.user.management.api.application.user.lookup.SortOrder;
import net.ihe.gazelle.user.management.api.application.user.lookup.UserLookupService;
import net.ihe.gazelle.user.management.api.application.user.lookup.UserQueryParams;
import net.ihe.gazelle.user.management.api.domain.user.DelegatedUser;
import net.ihe.gazelle.user.management.api.domain.user.GazelleDefaultGroup;
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

import java.util.*;

import static net.ihe.gazelle.user.management.commons.GazelleAssertions.assertExceptionContains;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
class UserLookupServiceTest {

    private static UserQueryParams nullQuery;
    @Mock
    private UserLookupDAO userLookupDAOMock;
    private UserLookupService userLookupService;

    private MockedGazelleIdentity mockedGazelleIdentity;
    private static User user1;
    private static User user2;
    private static User user3;
    private static DelegatedUser user4;
    private static UserQueryParams allQuery;
    private static Authz authz;

    @BeforeAll
    static void setup() {
        createUsersForTests();
        nullQuery = UserQueryParams.nullQuery();
        allQuery = UserQueryParams.nullQuery().setAttribute("search", "*");
        authz = new AuthzImpl(new PermissionStoreSPIProvider());
    }

    @BeforeEach
    void beforeEach() {
        userLookupService = new UserLookupServiceImpl(userLookupDAOMock, authz);
        mockedGazelleIdentity = new MockedGazelleIdentity(Set.of(GazelleDefaultGroup.GAZELLE_ADMIN.getName()));
    }

    @Test
    void testGetActivationCodeForNullUserId() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> userLookupService.getActivationCodeForUserId(null));
        assertExceptionContains("is null", exception);
    }

    @Test
    void testGetActivationCodeForUserId() {
        when(userLookupDAOMock.getActivationCodeForUserId("userId")).thenReturn("activationCode");
        String activationCode = userLookupService.getActivationCodeForUserId("userId");
        assertEquals("activationCode", activationCode);
    }

    @Test
    void testSearchForUsers() {
        UserQueryParams existingEmailLikeQuery = new UserQueryParams("user1@email.com", null, null, null, null, null, null, null, null, null);
        UserQueryParams notExistingEmailQuery = new UserQueryParams("NonExistingUser", null, null, null, null, null, null, null, null, null);

        when(userLookupDAOMock.searchForUsers(eq(nullQuery.setSearch("")), any(), any(), any(), any())).thenReturn(List.of(user1
                , user2, user3));

        assertTrue(userLookupService.searchAndFilterUsers(nullQuery, null, null, mockedGazelleIdentity).stream().map(User::getEmail).toList().containsAll(List.of("user1@email.com", "inactive@email.com")));
        assertTrue(userLookupService.searchAndFilterUsers(nullQuery, 0, 10, mockedGazelleIdentity).stream().map(User::getEmail).toList().containsAll(List.of("user1@email.com", "inactive@email.com")));
        assertTrue(userLookupService.searchAndFilterUsers(allQuery, 0, 10, mockedGazelleIdentity).stream().map(User::getEmail).toList().containsAll(List.of("user1@email.com", "inactive@email.com")));

        when(userLookupDAOMock.searchForUsers(eq(existingEmailLikeQuery), eq(0), eq(10), any(), any())).thenReturn(List.of(user1));
        assertTrue(userLookupService.searchAndFilterUsers(existingEmailLikeQuery, 0, 10, mockedGazelleIdentity).stream().map(User::getEmail).toList().contains("user1@email.com"));

        when(userLookupDAOMock.searchForUsers(eq(notExistingEmailQuery), eq(0), eq(10), any(), any())).thenReturn(List.of());
        assertTrue(userLookupService.searchAndFilterUsers(notExistingEmailQuery, 0, 10, mockedGazelleIdentity).stream().map(User::getEmail).toList().isEmpty());
    }

    @Test
    void testSearchForDelegatedUsers() {
        UserQueryParams userQueryParams = new UserQueryParams("", null, null, null, null, null, null, true, null, null);

        when(userLookupDAOMock.searchForUsers(eq(userQueryParams), eq(0), eq(10), any(), any())).thenReturn(List.of(user4));
        assertTrue(userLookupService.searchAndFilterUsers(userQueryParams, 0, 10, mockedGazelleIdentity).stream().map(User::getEmail).toList().contains("delegated@email.com"));
    }

    @Test
    void testSearchForUsersWithCount() {
        when(userLookupDAOMock.searchForUsers(eq(new UserQueryParams("", null, null,
                null, null, null, null, null, null, null)), any(), any(), eq("firstName"), eq(SortOrder.ASC)))
                .thenReturn(new ArrayList<>(List.of(user1, user2, user3)));
        assertEquals(userLookupService.searchAndFilterUsersWithCount(nullQuery, null,
                        null, "firstName", SortOrder.ASC, mockedGazelleIdentity).users().get(0).getLastName(),
                user1.getLastName());
        when(userLookupDAOMock.searchForUsers(eq(new UserQueryParams("", null, null,
                null, null, null, null, null, null, null)), any(), any(), eq("lastName"), eq(SortOrder.ASC)))
                .thenReturn(new ArrayList<>(List.of(user3, user2, user1)));
        assertEquals(userLookupService.searchAndFilterUsersWithCount(nullQuery, null,
                        null, "lastName", SortOrder.ASC, mockedGazelleIdentity).users().get(0).getLastName(),
                user3.getLastName());
    }


    @Test
    void testSearchSummaryForUsersWithCount() {
        User userSummary1 = getUserSummary(user1);
        User userSummary2 = getUserSummary(user2);
        User userSummary3 = getUserSummary(user3);

        when(userLookupDAOMock.searchForUsersSummary(eq(new UserQueryParams("", null, null,
                null, null, null, null, null, null, null)), any(), any(), eq("firstName"), eq(SortOrder.ASC)))
                .thenReturn(new ArrayList<>(List.of(userSummary1, userSummary2, userSummary3)));

        User userSearch1 = userLookupService.searchAndFilterUsersSummary(nullQuery, null,
                null, "firstName", SortOrder.ASC, mockedGazelleIdentity).users().getFirst();
        assertNotEquals(userSearch1, user1);
        assertEquals(userSearch1, userSummary1);


        when(userLookupDAOMock.searchForUsers(eq(new UserQueryParams("", null, null,
                null, null, null, null, null, null, null)), any(), any(), eq("lastName"), eq(SortOrder.ASC)))
                .thenReturn(new ArrayList<>(List.of(userSummary3, userSummary2, userSummary1)));

        assertEquals(userLookupService.searchAndFilterUsersWithCount(nullQuery, null,
                        null, "lastName", SortOrder.ASC, mockedGazelleIdentity).users().get(0).getLastName(),
                user3.getLastName());
    }

    @Test
    void testGetValueCount() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> userLookupService.getValueCount(
                "group", nullQuery, mockedGazelleIdentity));
        assertExceptionContains("propertyName must be define and with value: [firstName, lastName, organizationId, roles, activated]", exception);
        Map<String, Long> count = new HashMap<>();
        count.put("roles", 3L);
        when(userLookupDAOMock.getValueCount("roles", nullQuery))
                .thenReturn(count);
        assertEquals(userLookupService.getValueCount("roles", nullQuery, mockedGazelleIdentity), count);

    }

    @Test
    void testFilterUsers() {
        // Test with null query
        when(userLookupDAOMock.searchForUsers(eq(new UserQueryParams("", null, null, null, null, null, null, null, null, null)), any(), any(), any(), any()))
                .thenReturn(new ArrayList<>(List.of(user1, user2, user3)));
        assertDoesNotThrow(() -> userLookupService.searchAndFilterUsers(nullQuery, 0, 10, mockedGazelleIdentity));
        assertEquals(3, userLookupService.searchAndFilterUsers(nullQuery, 0, 10, mockedGazelleIdentity).size());
        assertEquals(3, userLookupService.searchAndFilterUsers(allQuery, null, 10, mockedGazelleIdentity).size());

        // Test with firstname
        UserQueryParams query = new UserQueryParams("", "fn1", null, null, null, null, null, null, null, null);
        when(userLookupDAOMock.searchForUsers(eq(query), eq(0), eq(10), any(), any())).thenReturn(List.of(user1, user3));
        assertEquals(2, userLookupService.searchAndFilterUsers(query, 0, 10, mockedGazelleIdentity).size());

        // Test with lastname
        query = new UserQueryParams(null, null, "ln2", null, null, null, null, null, null, null);
        when(userLookupDAOMock.searchForUsers(eq(query.setSearch("")), eq(0), eq(10), any(), any())).thenReturn(List.of(user2));
        assertEquals(1, userLookupService.searchAndFilterUsers(query, 0, 10, mockedGazelleIdentity).size());

        // Test with organization
        query = new UserQueryParams(null, null, null, null, null, "orga1", null, null, null, null);
        when(userLookupDAOMock.searchForUsers(eq(query.setSearch("")), eq(0), eq(10), any(), any())).thenReturn(List.of(user1,
                user2));
        assertEquals(2, userLookupService.searchAndFilterUsers(query, 0, 10, mockedGazelleIdentity).size());

        // Test with organization and group
        query = new UserQueryParams(null, null, null, null, "admin", "orga1", null, null, null, null);
        when(userLookupDAOMock.searchForUsers(eq(query.setSearch("")), eq(0), eq(10), any(), any())).thenReturn(List.of(user1, user3));
        assertEquals(2, userLookupService.searchAndFilterUsers(query, 0, 10, mockedGazelleIdentity).size());

        // Test with activated status
        query = new UserQueryParams(null, null, null, null, null, null, false, null, null, null);
        when(userLookupDAOMock.searchForUsers(eq(query.setSearch("")), eq(0), eq(10), any(), any())).thenReturn(List.of(user2));
        assertEquals(1, userLookupService.searchAndFilterUsers(query, 0, 10, mockedGazelleIdentity).size());

        // Test with group and organization and activated status
        query = new UserQueryParams(null, null, null, null, "admin", "orga1", true, null, null, null);
        when(userLookupDAOMock.searchForUsers(eq(query.setSearch("")), eq(0), eq(100), any(), any())).thenReturn(List.of(user1));
        assertEquals(1, userLookupService.searchAndFilterUsers(query, 0, null, mockedGazelleIdentity).size());
    }

    @Test
    void testSearchAndFilterUsers() {
        // Test with firstname
        UserQueryParams query = new UserQueryParams("fn1", "fn1", null, null, null, null, null, null, null, null);
        when(userLookupDAOMock.searchForUsers(eq(query), eq(0), eq(10), any(), any())).thenReturn(List.of(user1, user3));
        assertEquals(2, userLookupService.searchAndFilterUsers(query, 0, 10, mockedGazelleIdentity).size());

        // Test with organization and activated status
        query = new UserQueryParams("ln1", null, "ln1", null, null, "orga1", true, null, null, null);
        when(userLookupDAOMock.searchForUsers(eq(query), eq(0), eq(10), any(), any())).thenReturn(List.of(user1));
        assertEquals(1, userLookupService.searchAndFilterUsers(query, 0, 10, mockedGazelleIdentity).size());
    }

    @Test
    void testGetUserById() {
        User user = new User("lookupTestUser1");
        when(userLookupDAOMock.getUserById("fakeUser")).thenThrow(new NoSuchElementException("User not found"));
        when(userLookupDAOMock.getUserById("lookupTestUser1")).thenReturn(user);
        Exception exception = assertThrows(NoSuchElementException.class, () -> userLookupService.getUserById("fakeUser", mockedGazelleIdentity));
        assertExceptionContains("not found", exception);
        assertEquals("lookupTestUser1", userLookupService.getUserById("lookupTestUser1", mockedGazelleIdentity).getId());
    }

    @Test
    void testGetUserSummaryById() {
        User user = new User("lookupTestUser1");
        user.setEmail("mycool@email.mail");
        when(userLookupDAOMock.getUserSummaryById("fakeUser")).thenThrow(new NoSuchElementException("User not found"));
        User userSummary = getUserSummary(user);
        when(userLookupDAOMock.getUserSummaryById("lookupTestUser1")).thenReturn(userSummary);

        Exception exception = assertThrows(NoSuchElementException.class, () -> userLookupService.getUserSummaryById("fakeUser", mockedGazelleIdentity));
        assertExceptionContains("not found", exception);
        User actualSummary = userLookupService.getUserSummaryById("lookupTestUser1", mockedGazelleIdentity);
        assertEquals(userSummary, actualSummary);
        assertNotEquals(user, actualSummary);
    }

    @Test
    void testGetUserByEmail() {
        User user = new User("lookupTestUser1");
        user.setEmail("user2@email.com");
        when(userLookupDAOMock.getUserByEmail("fakeUser@email.com")).thenThrow(new NoSuchElementException("User not found"));
        when(userLookupDAOMock.getUserByEmail("user2@email.com")).thenReturn(user);
        Exception exception = assertThrows(NoSuchElementException.class, () -> userLookupService.getUserByEmail("fakeUser@email.com", mockedGazelleIdentity));
        assertExceptionContains("not found", exception);
        assertEquals("user2@email.com", userLookupService.getUserByEmail("user2@email.com", mockedGazelleIdentity).getEmail());
    }

    @Test
    void testGetUserByIdNull() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> userLookupService.getUserById(null, mockedGazelleIdentity));

        assertEquals("userId is null", exception.getMessage());
    }

    @Test
    void testGetUserByEmailNull() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> userLookupService.getUserByEmail(null, mockedGazelleIdentity));
        assertEquals("email is null", exception.getMessage());
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

        user4 = new DelegatedUser("delegateduser");
        user4.setEmail("delegated@email.com");
        user4.setOrganizationId("orga2");
        user4.setFirstName("delegated fn");
        user4.setLastName("delegated ln");
        user4.addGroupId("admin");
        user4.setActivated(true);
    }

    private User getUserSummary(User user){
        User userSummary = new User();
        userSummary.setId(user.getId());
        userSummary.setFirstName(user.getFirstName());
        userSummary.setLastName(user.getLastName());
        userSummary.setOrganizationId(user.getOrganizationId());
        return userSummary;
    }
}
