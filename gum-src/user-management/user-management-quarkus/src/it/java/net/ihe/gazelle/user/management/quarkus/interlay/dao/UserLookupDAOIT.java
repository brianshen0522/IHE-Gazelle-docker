package net.ihe.gazelle.user.management.quarkus.interlay.dao;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import net.ihe.gazelle.user.management.api.application.user.lookup.UserQueryParams;
import net.ihe.gazelle.user.management.api.domain.organization.Organization;
import net.ihe.gazelle.user.management.api.domain.user.DelegatedUser;
import net.ihe.gazelle.user.management.api.domain.user.User;
import net.ihe.gazelle.user.management.commons.application.organization.management.OrganizationManagementDAO;
import net.ihe.gazelle.user.management.commons.application.user.delegation.UserDelegationDAO;
import net.ihe.gazelle.user.management.commons.application.user.lookup.UserLookupDAO;
import net.ihe.gazelle.user.management.commons.application.user.registration.UserRegistrationDAO;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import static net.ihe.gazelle.user.management.api.application.user.lookup.UserQueryParams.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActivateRequestContext
@Transactional
@QuarkusTestResource(net.ihe.gazelle.security.mocks.KeycloakMockResource.class)
class UserLookupDAOIT {

    @Inject
    UserLookupDAO userLookupDAO;
    @Inject
    UserDelegationDAO userDelegationDAO;
    @Inject
    UserRegistrationDAO userRegistrationDAO;
    @Inject
    OrganizationManagementDAO organizationManagementDAO;

    private final String PREFIX = getClass().getSimpleName();
    private final String TEST_USER_ID = PREFIX + "ID";
    private final String TEST_USER_FIRST_NAME = "testFirstName";
    private final String TEST_USER_LAST_NAME = "testLastName";
    private final String TEST_USER_EMAIL = PREFIX + "@test.fr";

    @BeforeAll
    public void init() {
        createOrg(PREFIX + "Organization");
        createUser(TEST_USER_ID, TEST_USER_FIRST_NAME, TEST_USER_LAST_NAME, TEST_USER_EMAIL,
                false, "superUniqueSecretActivationCode", PREFIX + "Organization", "org-adm:" + PREFIX + "Organization");


        createOrg(PREFIX + "IheTest");
        createUser("1234", "Jean", "Peuplu", "jean.peuplu@lost.com", true,
                "azerty", PREFIX + "IheTest", "org-adm:" + PREFIX + "IheTest");
        createUser("5678", "Alex", "Ception", "alex.ception@exept.com", true,
                "azerty2", PREFIX + "IheTest", "org-adm:" + PREFIX + "IheTest");
        createUser("9101112", "Alex", "Terieur", "alex.terieur@exept.com", false,
                "azerty3", PREFIX + "IheTest", "org:" + PREFIX + "IheTest");
        createUser("8888", "Jean-Délégué", "Risseur", "guerisseur@gmail.com", true,
                "prout", PREFIX + "IheTest", "org:" + PREFIX + "IheTest");
        createUser("9999", "Pierre-Délégué", "Risseur", "pguerisseur@gmail.com", true,
                "prout", PREFIX + "IheTest", "org:" + PREFIX + "IheTest");
        userDelegationDAO.transformUserIntoDelegatedUser("guerisseur@gmail.com", "external5151", "idpIT");
        userDelegationDAO.transformUserIntoDelegatedUser("pguerisseur@gmail.com", "external8181", "idpIT");
    }

    @Test
    void searchForUsersTest() {
        UserQueryParams userQueryParams = UserQueryParams.nullQuery().setAttribute(ATTR_FIRST_NAME, TEST_USER_FIRST_NAME);
        List<User> users = userLookupDAO.searchForUsers(userQueryParams, 0, 10, null, null);

        assertFalse(users.isEmpty());
        User user = users.getFirst();
        assertThat(users.size(), greaterThanOrEqualTo(1));
        assertNotNull(user.getFirstName());
        assertNotNull(user.getLastName());
        assertNotNull(user.getEmail());

        userQueryParams = userQueryParams.setSearch(TEST_USER_LAST_NAME.toLowerCase());
        users = userLookupDAO.searchForUsers(userQueryParams, 0, 10, null, null);
        assertFalse(users.isEmpty());

        userQueryParams = userQueryParams.setSearch("badSearchPattern");
        users = userLookupDAO.searchForUsers(userQueryParams, 0, 10, null, null);
        assertTrue(users.isEmpty());

        userQueryParams = userQueryParams.setSearch("");
        users = userLookupDAO.searchForUsers(userQueryParams, 0, 0, null, null);
        assertTrue(users.isEmpty());
    }

    @Test
    void searchForDelegatedUsersTest() {
        UserQueryParams userQueryParams = UserQueryParams.nullQuery();
        List<User> users = userLookupDAO.searchForUsers(userQueryParams, 0, 6, null, null);
        assertEquals(6, users.size());

        userQueryParams = UserQueryParams.nullQuery().setAttribute(ATTR_DELEGATED, true);
        users = userLookupDAO.searchForUsers(userQueryParams, 0, 10, null, null);
        users.forEach(user -> assertInstanceOf(DelegatedUser.class, user));

        userQueryParams = UserQueryParams.nullQuery().setAttribute(ATTR_DELEGATED, false);
        users = userLookupDAO.searchForUsers(userQueryParams, 0, 6, null, null);
        users.forEach(user -> assertFalse(user instanceof DelegatedUser));

        userQueryParams = UserQueryParams.nullQuery().setAttribute(ATTR_EXTERNAL_ID, "external5151");
        users = userLookupDAO.searchForUsers(userQueryParams, 0, 10, null, null);
        assertEquals(1, users.size());
        assertInstanceOf(DelegatedUser.class, users.getFirst());

        userQueryParams = UserQueryParams.nullQuery().setAttribute(ATTR_IDP_ID, "idpIT");
        users = userLookupDAO.searchForUsers(userQueryParams, 0, 10, null, null);
        assertEquals(2, users.size());
        assertInstanceOf(DelegatedUser.class, users.getFirst());
    }

    @Test
    void searchUsersMultipleParamsTest() {
        UserQueryParams userQueryParams = UserQueryParams.nullQuery()
                .setAttribute(ATTR_DELEGATED, "true")
                .setAttribute(ATTR_ACTIVATED, "true")
                .setAttribute(ATTR_LAST_NAME, "Risseur")
                .setAttribute(ATTR_EMAIL, "pguerisseur@gmail.com")
                .setAttribute(ATTR_SEARCH, "9999");

        List<User> users = userLookupDAO.searchForUsers(userQueryParams, 0, 6, null, null);
        assertEquals(1, users.size());

    }

    @Test
    void getValueCountTest() {
        UserQueryParams queryParams = new UserQueryParams(null, null, null, null, null, null, null, null, null, null);
        Map<String, Long> count = userLookupDAO.getValueCount("organizationId", queryParams);
        assertThat(count.size(), greaterThanOrEqualTo(2));
        assertEquals(5, count.get(PREFIX + "IheTest"));
        count = userLookupDAO.getValueCount("organizationId", new UserQueryParams("Alex", null, null, null, null, null,
                null, null, null, null));
        assertThat(count.size(), greaterThanOrEqualTo(1));
        assertEquals(2, count.get(PREFIX + "IheTest"));
        count = userLookupDAO.getValueCount("activated", queryParams);
        assertEquals(2, count.size());
        assertThat(count.get("true"), greaterThanOrEqualTo(2L));
        assertThat(count.get("false"), greaterThanOrEqualTo(2L));
        count = userLookupDAO.getValueCount("activated", new UserQueryParams(null, null, null, null, null, PREFIX + "IheTest",
                null, null, null, null));
        assertEquals(2, count.size());
        assertThat(count.get("true"), greaterThanOrEqualTo(2L));
        assertThat(count.get("false"), greaterThanOrEqualTo(1L));

        UserQueryParams userQueryParams = UserQueryParams.nullQuery()
                .setAttribute(ATTR_ORGANIZATION_ID, PREFIX + "IheTest")
                .setAttribute(ATTR_ACTIVATED, false);
        count = userLookupDAO.getValueCount("groups", userQueryParams);
        assertEquals(1, count.size());

        userQueryParams = UserQueryParams.nullQuery().setAttribute(ATTR_GROUP, "org:" + PREFIX + "IheTest");
        count = userLookupDAO.getValueCount("organizationId", userQueryParams);
        assertThat(count.size(), greaterThanOrEqualTo(1));

        userQueryParams = UserQueryParams.nullQuery().setAttribute(ATTR_GROUP, "impossibleTHatTHisGroupExists");
        count = userLookupDAO.getValueCount("organizationId", userQueryParams);
        assertEquals(0, count.size());

        userQueryParams = UserQueryParams.nullQuery().setAttribute(ATTR_GROUP, "org:" + PREFIX + "IheTest");
        count = userLookupDAO.getValueCount("firstName", userQueryParams);
        assertThat(count.size(), greaterThanOrEqualTo(1));
        assertEquals(1, count.get("Alex"));
    }


    @Test
    void getUserByIdTest() {
        User user = userLookupDAO.getUserById(TEST_USER_ID);

        assertNotNull(user);
        assertEquals(TEST_USER_FIRST_NAME, user.getFirstName());
        assertEquals(TEST_USER_LAST_NAME, user.getLastName());
        assertEquals(TEST_USER_EMAIL, user.getEmail());

        // Test with bad IDs
        assertThrows(NoSuchElementException.class, () -> userLookupDAO.getUserById("badUserID"));

        assertThrows(NoSuchElementException.class, () -> userLookupDAO.getUserById(""));
    }

    @Test
    void getActivationCodeForUserIdTest() {
        assertThrows(NoSuchElementException.class, () -> userLookupDAO.getActivationCodeForUserId("testUserNotExisting"));

        String code = userLookupDAO.getActivationCodeForUserId(TEST_USER_ID);
        assertEquals("superUniqueSecretActivationCode", code);
    }


    @Test
    void getUserByEmailTest() {
        User user = userLookupDAO.getUserByEmail(TEST_USER_EMAIL);

        assertNotNull(user);
        assertEquals(TEST_USER_FIRST_NAME, user.getFirstName());
        assertEquals(TEST_USER_LAST_NAME, user.getLastName());
        assertEquals(TEST_USER_EMAIL, user.getEmail());

        // Test with bad emails
        assertThrows(NoSuchElementException.class, () -> userLookupDAO.getUserByEmail("badEmail"));
        assertThrows(NoSuchElementException.class, () -> userLookupDAO.getUserByEmail(""));
    }

    @Test
    void getUserByActivationCodeTest() {
        assertThrows(NoSuchElementException.class, () -> userLookupDAO.getUserByActivationCode(""));
        User user = userLookupDAO.getUserByActivationCode("superUniqueSecretActivationCode");
        assertNotNull(user);
        assertEquals("superUniqueSecretActivationCode", user.getActivationCode());
    }

    private void createUser(String id, String firstName, String lastName, String email, boolean activated,
                            String activationCode, String organizationId, String... groups) {
        User newUser = new User(id);
        newUser.setFirstName(firstName);
        newUser.setLastName(lastName);
        newUser.setEmail(email);
        newUser.setActivated(activated);
        newUser.setActivationCode(activationCode);
        newUser.setOrganizationId(organizationId);
        for (String groupId : groups) {
            newUser.addGroupId(groupId);
        }
        userRegistrationDAO.registerUser(newUser);
    }

    private void createOrg(String id) {
        Organization organization = new Organization(id);
        organizationManagementDAO.createOrganization(organization);
    }
}
