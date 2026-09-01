package net.ihe.gazelle.user.management.quarkus.interlay.dao;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import net.ihe.gazelle.search.api.Range;
import net.ihe.gazelle.search.api.SearchResult;
import net.ihe.gazelle.user.management.api.application.user.search.UserSearchCriteria;
import net.ihe.gazelle.user.management.api.application.user.search.UserSearchIndexServiceImpl;
import net.ihe.gazelle.user.management.api.domain.user.User;
import net.ihe.gazelle.user.management.commons.interlay.dao.entities.DelegatedUserEntity;
import net.ihe.gazelle.user.management.commons.interlay.dao.entities.GroupEntity;
import net.ihe.gazelle.user.management.commons.interlay.dao.entities.OrganizationEntity;
import net.ihe.gazelle.user.management.commons.interlay.dao.entities.UserEntity;
import net.ihe.gazelle.user.management.commons.interlay.dao.user.UserSearchDAO;
import net.ihe.gazelle.user.management.commons.interlay.dao.user.UserSearchDAOImpl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.List;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserSearchDAOIT {


    public static final String USER_1 = "user-1";
    public static final String USER_2 = "user-2";
    public static final String USER_3 = "user-3";
    private final EntityManager entityManager;
    private final UserSearchDAO userSearchDAO;

    @Inject
    public UserSearchDAOIT(EntityManager entityManager) {
        this.entityManager = entityManager;
        this.userSearchDAO = new UserSearchDAOImpl(entityManager);
    }

    @BeforeAll
    void setup() {
        GroupEntity adminGroup = new GroupEntity("role:admin");
        GroupEntity monitorGroup = new GroupEntity("role:gazelle_monitor");
        GroupEntity organization1Group = new GroupEntity("org:organization-1");
        GroupEntity organization2Group = new GroupEntity("org:organization-2");
        GroupEntity organization1AdminGroup = new GroupEntity("org-adm:organization-1");

        entityManager.persist(adminGroup);
        entityManager.persist(monitorGroup);
        entityManager.persist(organization1Group);
        entityManager.persist(organization2Group);
        entityManager.persist(organization1AdminGroup);

        OrganizationEntity organization1 = new OrganizationEntity("organization-1", "My first orga", "ORGAONE");
        OrganizationEntity organization2 = new OrganizationEntity("organization-2", "My second orga", "ORGATWO");

        entityManager.persist(organization1);
        entityManager.persist(organization2);

        UserEntity user1 = new UserEntity();
        user1.setId(USER_1);
        user1.setFirstName("Alice");
        user1.setLastName("Martin");
        user1.setEmail("alice.martin@test.fr");
        user1.setActivated(true);
        user1.setActivationCode("activation-code-user-1");
        user1.setOrganizationId("organization-1");
        user1.setLoginCounter(3);
        user1.setGroupEntities(Set.of(organization1Group, organization1AdminGroup, adminGroup));

        UserEntity user2 = new UserEntity();
        user2.setId(USER_2);
        user2.setFirstName("Bob");
        user2.setLastName("Durand");
        user2.setEmail("bob.durand@test.fr");
        user2.setActivated(false);
        user2.setActivationCode("activation-code-user-2");
        user2.setOrganizationId("organization-1");
        user2.setLoginCounter(0);
        user2.setGroupEntities(Set.of(organization1Group, monitorGroup));

        DelegatedUserEntity user3 = new DelegatedUserEntity();
        user3.setId(USER_3);
        user3.setFirstName("Claire");
        user3.setLastName("Marmelade");
        user3.setEmail("claire.marmelade@test.fr");
        user3.setActivated(true);
        user3.setActivationCode("activation-code-user-3");
        user3.setOrganizationId("organization-2");
        user3.setLoginCounter(12);
        user3.setGroupEntities(Set.of(organization2Group));

        entityManager.persist(user1);
        entityManager.persist(user2);
        entityManager.persist(user3);
        entityManager.flush();
    }

    @Test
    void searchNoCriteria_returnsAllCreatedUsers() {
        Range range = new Range(0, 100);
        SearchResult<User> result = userSearchDAO.search(new UserSearchCriteria(), range, List.of());

        assertNotNull(result);
        List<String> ids = result.objects().stream().map(User::getId).toList();
        assertTrue(ids.contains(USER_1));
        assertTrue(ids.contains(USER_2));
        assertTrue(ids.contains(USER_3));
    }

    @Test
    void searchByLastName_returnsMatchingUsers() {
        Range range = new Range(0, 100);
        UserSearchCriteria criteria = new UserSearchCriteria().setLastNameParam("Mar");
        SearchResult<User> result = userSearchDAO.search(criteria, range, List.of());

        assertNotNull(result);
        List<String> ids = result.objects().stream().map(User::getId).toList();
        assertTrue(ids.contains(USER_1));
        assertTrue(ids.contains(USER_3));
    }

    @Test
    void searchByFirstName_returnsMatchingUsers() {
        Range range = new Range(0, 100);
        UserSearchCriteria criteria = new UserSearchCriteria().setFirstNameParam("Cla");
        SearchResult<User> result = userSearchDAO.search(criteria, range, List.of());

        assertNotNull(result);
        List<String> ids = result.objects().stream().map(User::getId).toList();
        assertTrue(ids.contains(USER_3));
    }


    @Test
    void searchByEmail_returnsMatchingUsers() {
        Range range = new Range(0, 100);
        UserSearchCriteria criteria = new UserSearchCriteria().setEmailParam("marmelade@");
        SearchResult<User> result = userSearchDAO.search(criteria, range, List.of());

        assertNotNull(result);
        List<String> ids = result.objects().stream().map(User::getId).toList();
        assertTrue(ids.contains(USER_3));
    }

    @Test
    void searchByOrgName_returnsMatchingUsers() {
        Range range = new Range(0, 100);
        UserSearchCriteria criteria = new UserSearchCriteria().setOrganizationNameParam("second");
        SearchResult<User> result = userSearchDAO.search(criteria, range, List.of());

        assertNotNull(result);
        List<String> ids = result.objects().stream().map(User::getId).toList();
        assertTrue(ids.contains(USER_3));
    }

    @Test
    void searchByOrgaId_returnsMatchingUsers() {
        Range range = new Range(0, 100);
        UserSearchCriteria criteria = new UserSearchCriteria().setOrganizationIdParam("organization-1");
        SearchResult<User> result = userSearchDAO.search(criteria, range, List.of());

        assertNotNull(result);
        List<String> ids = result.objects().stream().map(User::getId).toList();
        assertTrue(ids.contains(USER_1));
        assertTrue(ids.contains(USER_2));
    }

    @Test
    void searchByGroups_returnsMatchingUsers() {
        Range range = new Range(0, 100);
        UserSearchCriteria criteria = new UserSearchCriteria().setGroupParam("monit");
        SearchResult<User> result = userSearchDAO.search(criteria, range, List.of());

        assertNotNull(result);
        List<String> ids = result.objects().stream().map(User::getId).toList();
        assertTrue(ids.contains(USER_2));
    }

    @Test
    void searchByGroups_returnsMatchingUsers2() {
        Range range = new Range(0, 100);
        UserSearchCriteria criteria = new UserSearchCriteria().setGroupParam("org-adm");
        SearchResult<User> result = userSearchDAO.search(criteria, range, List.of());

        assertNotNull(result);
        List<String> ids = result.objects().stream().map(User::getId).toList();
        assertTrue(ids.contains(USER_1));
    }

    @Test
    void searchInactive_returnsMatchingUsers() {
        Range range = new Range(0, 100);
        UserSearchCriteria criteria = new UserSearchCriteria().setActivatedParam(false);
        SearchResult<User> result = userSearchDAO.search(criteria, range, List.of());

        assertNotNull(result);
        List<String> ids = result.objects().stream().map(User::getId).toList();
        assertTrue(ids.contains(USER_2));
    }

    @Test
    void searchDelegated_returnsMatchingUsers() {
        Range range = new Range(0, 100);
        UserSearchCriteria criteria = new UserSearchCriteria().setDelegatedParam(true);
        SearchResult<User> result = userSearchDAO.search(criteria, range, List.of());

        assertNotNull(result);
        List<String> ids = result.objects().stream().map(User::getId).toList();
        assertTrue(ids.contains(USER_3));
    }

    @Test
    void searchOnSearchParamEmail_returnsMatchingUsers() {
        Range range = new Range(0, 100);
        UserSearchCriteria criteria = new UserSearchCriteria().setSearchParam("@test.fr");
        SearchResult<User> result = userSearchDAO.search(criteria, range, List.of());

        assertNotNull(result);
        List<String> ids = result.objects().stream().map(User::getId).toList();
        assertTrue(ids.contains(USER_1));
        assertTrue(ids.contains(USER_2));
        assertTrue(ids.contains(USER_3));
    }

    @Test
    void searchOnSearchParamFirstName_returnsMatchingUsers() {
        Range range = new Range(0, 100);
        UserSearchCriteria criteria = new UserSearchCriteria().setSearchParam("alice");
        SearchResult<User> result = userSearchDAO.search(criteria, range, List.of());

        assertNotNull(result);
        List<String> ids = result.objects().stream().map(User::getId).toList();
        assertTrue(ids.contains(USER_1));
    }

    @Test
    void searchOnSearchParamLastName_returnsMatchingUsers() {
        Range range = new Range(0, 100);
        UserSearchCriteria criteria = new UserSearchCriteria().setSearchParam("durand");
        SearchResult<User> result = userSearchDAO.search(criteria, range, List.of());

        assertNotNull(result);
        List<String> ids = result.objects().stream().map(User::getId).toList();
        assertTrue(ids.contains(USER_2));
    }

    @Test
    void getSuggestionByOrgaName_returnsMatchingValues() {
        UserSearchCriteria criteria = new UserSearchCriteria().setOrganizationNameParam("second");
        List<String> result = userSearchDAO.getSuggestions(UserSearchIndexServiceImpl.ORGANIZATION_NAME, criteria);

        assertNotNull(result);
        assertThat(result.size(), greaterThanOrEqualTo(1));

        assertTrue(result.contains("My second orga"));
        assertTrue(result.contains("ORGATWO"));


        UserSearchCriteria criteria2 = new UserSearchCriteria().setOrganizationNameParam("ORGATWO");
        List<String> result2 = userSearchDAO.getSuggestions(UserSearchIndexServiceImpl.ORGANIZATION_NAME, criteria2);

        assertNotNull(result2);
        assertThat(result2.size(), greaterThanOrEqualTo(1));

        assertTrue(result2.contains("My second orga"));
        assertTrue(result2.contains("ORGATWO"));
    }

    @Test
    void getSuggestionByFirstName_returnsMatchingValues() {
        UserSearchCriteria criteria = new UserSearchCriteria().setFirstNameParam("ali");
        List<String> result = userSearchDAO.getSuggestions(UserSearchIndexServiceImpl.FIRSTNAME, criteria);

        assertNotNull(result);
        assertThat(result.size(), greaterThanOrEqualTo(1));

        assertTrue(result.contains("Alice"));
    }

    @Test
    void getSuggestionByLastName_returnsMatchingValues() {
        UserSearchCriteria criteria = new UserSearchCriteria().setLastNameParam("Mar");
        List<String> result = userSearchDAO.getSuggestions(UserSearchIndexServiceImpl.LASTNAME, criteria);

        assertNotNull(result);
        assertThat(result.size(), greaterThanOrEqualTo(2));

        assertTrue(result.contains("Marmelade"));
        assertTrue(result.contains("Martin"));
    }


    @Test
    void getSuggestionByEmail_returnsMatchingValues() {
        UserSearchCriteria criteria = new UserSearchCriteria().setEmailParam("@test.fr");
        List<String> result = userSearchDAO.getSuggestions(UserSearchIndexServiceImpl.EMAIL, criteria);

        assertNotNull(result);
        assertThat(result.size(), greaterThanOrEqualTo(3));

        assertTrue(result.contains("claire.marmelade@test.fr"));
        assertTrue(result.contains("bob.durand@test.fr"));
        assertTrue(result.contains("alice.martin@test.fr"));
    }

    @Test
    void getSuggestionByGroup_returnsMatchingValues() {
        UserSearchCriteria criteria = new UserSearchCriteria().setGroupParam();
        List<String> result = userSearchDAO.getSuggestions(UserSearchIndexServiceImpl.GROUP, criteria);

        assertNotNull(result);
        assertThat(result.size(), greaterThanOrEqualTo(2));
        assertTrue(result.contains("role:admin"));
        assertTrue(result.contains("role:gazelle_monitor"));
    }
}
