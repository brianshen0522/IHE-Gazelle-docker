package net.ihe.gazelle.user.management.commons.application.user.search;

import net.ihe.gazelle.search.api.Range;
import net.ihe.gazelle.search.api.SearchQuery;
import net.ihe.gazelle.search.api.SearchResult;
import net.ihe.gazelle.search.api.Sort;
import net.ihe.gazelle.security.business.*;
import net.ihe.gazelle.security.technical.PermissionStoreSPIProvider;
import net.ihe.gazelle.user.management.api.application.user.search.UserSearchCriteria;
import net.ihe.gazelle.user.management.api.application.user.search.UserSearchService;
import net.ihe.gazelle.user.management.api.domain.user.User;
import net.ihe.gazelle.user.management.commons.interlay.dao.user.UserSearchDAO;
import net.ihe.gazelle.user.management.commons.interlay.utils.MockedGazelleIdentity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserSearchServiceImplTest {
    public static final String ORGA_ADMIN = Groups.PREFIX_ORGANIZATION_ADMIN + "orga";
    public static final String ORGA_MEMBER = Groups.PREFIX_ORGANIZATION + "orga";
    public static final String USER_1_ID = "user1";
    public static final String USER_2_ID = "user2";
    public static final String USER_3_ID = "user3";
    private final Range defaultRange = new Range(Range.DEFAULT_OFFSET, Range.DEFAULT_LIMIT);
    private final List<Sort> defaultSorts = List.of(new Sort("lastName", Sort.Order.ASCENDING));

    @Mock
    UserSearchDAO userSearchDAOMock;

    private UserSearchService userSearchService;

    private final GazelleIdentity adminIdentity = new MockedGazelleIdentity(Set.of(Groups.ROLE_ADMIN));
    private final GazelleIdentity orgaAdminIdentity = new MockedGazelleIdentity(Set.of(ORGA_ADMIN)).setOrganizationId("orga");
    private final GazelleIdentity memberIdentity = new MockedGazelleIdentity(Set.of(ORGA_MEMBER)).setOrganizationId("orga").setIdentityId(USER_2_ID);


    @BeforeEach
    void setup() {
        userSearchService = new UserSearchServiceImpl(userSearchDAOMock, new AuthzImpl(new PermissionStoreSPIProvider()));

        User user = new User(USER_1_ID, "User", "One", "user.one@test.com", "orga");
        user.setGroupIds(Set.of(ORGA_ADMIN, ORGA_MEMBER));

        User user2 = new User(USER_2_ID, "User", "Two", "user.two@test.com", "orga");
        user2.setGroupIds(Set.of(ORGA_MEMBER));

        User user3 = new User(USER_3_ID, "User", "Three", "user.three@test.com", "orga2");
        user3.setGroupIds(Set.of(ORGA_ADMIN, ORGA_MEMBER, Groups.ROLE_ADMIN));


        when(userSearchDAOMock.search(new UserSearchCriteria(), defaultRange, defaultSorts))
                .thenReturn(new SearchResult<User>(List.of(user, user2, user3), defaultRange.getOffset(), defaultRange.getLimit(), 3));

        UserSearchCriteria userSearchCriteriaWithOrgaId = new UserSearchCriteria().setOrganizationIdParam(orgaAdminIdentity.getOrganizationId());
        when(userSearchDAOMock.search(userSearchCriteriaWithOrgaId, defaultRange, defaultSorts))
                .thenReturn(new SearchResult<User>(List.of(user, user2), defaultRange.getOffset(), defaultRange.getLimit(), 2));

        when(userSearchDAOMock.getUserById(memberIdentity.getId()))
                .thenReturn(new SearchResult<User>(List.of(user), defaultRange.getOffset(), defaultRange.getLimit(), 1));
    }

    @Test
    void testSearchAllAdmin() {
        SearchQuery<UserSearchCriteria> query = new SearchQuery<>(new UserSearchCriteria(), defaultRange, List.of());
        SearchResult<User> searchResult = userSearchService.search(query, adminIdentity);

        assertNotNull(searchResult);

        List<String> ids = searchResult.objects().stream().map(User::getId).toList();

        assertTrue(ids.containsAll(List.of(USER_1_ID, USER_2_ID, USER_3_ID)));

    }

    @Test
    void testSearchAllOrgaAdminAdmin() {
        SearchQuery<UserSearchCriteria> query = new SearchQuery<>(new UserSearchCriteria(), defaultRange, List.of());
        SearchResult<User> searchResult = userSearchService.search(query, orgaAdminIdentity);

        assertNotNull(searchResult);

        List<String> ids = searchResult.objects().stream().map(User::getId).toList();

        assertTrue(ids.containsAll(List.of(USER_1_ID, USER_2_ID)));

    }

    @Test
    void testSearchAllOrgaMemberAdmin() {
        SearchQuery<UserSearchCriteria> query = new SearchQuery<>(new UserSearchCriteria(), defaultRange, List.of());
        SearchResult<User> searchResult = userSearchService.search(query, adminIdentity);

        assertNotNull(searchResult);

        List<String> ids = searchResult.objects().stream().map(User::getId).toList();

        assertTrue(ids.containsAll(List.of(USER_2_ID)));

    }

    @Test
    void testSearchUnauthenticatedUser() {
        SearchQuery<UserSearchCriteria> query = new SearchQuery<>(new UserSearchCriteria(), defaultRange, List.of());
        BaseGazelleIdentity identity = BaseGazelleIdentity.unauthenticatedIdentity();
        assertThrows(UnauthorizedException.class, () -> userSearchService.search(query, identity));
    }
}