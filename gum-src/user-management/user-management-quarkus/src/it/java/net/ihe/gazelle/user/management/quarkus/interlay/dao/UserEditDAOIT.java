package net.ihe.gazelle.user.management.quarkus.interlay.dao;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import net.ihe.gazelle.user.management.api.domain.group.Group;
import net.ihe.gazelle.user.management.api.domain.user.Credentials;
import net.ihe.gazelle.user.management.api.domain.user.User;
import net.ihe.gazelle.user.management.commons.application.exception.GazelleDAOException;
import net.ihe.gazelle.user.management.commons.application.group.GroupDAO;
import net.ihe.gazelle.user.management.commons.application.user.edit.UserEditDAO;
import net.ihe.gazelle.user.management.commons.application.user.login.UserLoginDAO;
import net.ihe.gazelle.user.management.commons.application.user.lookup.UserLookupDAO;
import net.ihe.gazelle.user.management.commons.application.user.registration.UserRegistrationDAO;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.NoSuchElementException;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActivateRequestContext
@Transactional
class UserEditDAOIT {

    @Inject
    UserEditDAO userEditDAO;
    @Inject
    UserRegistrationDAO userRegistrationDAO;
    @Inject
    UserLookupDAO userLookupDAO;
    @Inject
    UserLoginDAO userLoginDAO;
    @Inject
    GroupDAO groupDAO;

    private final String PREFIX = getClass().getSimpleName();
    private final String ACTIVATION_CODE = "superSecretActivationCode";
    private final String TEST_USER_ID = PREFIX + "ID";

    @BeforeAll
    public void init() {
        registerUserForTesting("", false);
        registerUserForTesting("2", true);
    }

    @Test
    void testActivateUser() {
        assertThrows(GazelleDAOException.class, () -> userRegistrationDAO.activateUserWithActivationCode("badActivationCode"));

        User user = userRegistrationDAO.activateUserWithActivationCode(ACTIVATION_CODE);
        assertTrue(user.isActivated());
        assertNull(user.getActivationCode());
    }


    @Test
    void testUpdateCredentials() {
        // Test bad userId
        Credentials credentials = new Credentials();
        assertThrows(NoSuchElementException.class, () -> userEditDAO.updateCredentialsForUserId("badUserId", credentials));

        // Update credentials
        credentials.setPassword("newPassword");
        credentials.setSalt("newSalt");
        credentials.setIterations(3999);
        userEditDAO.updateCredentialsForUserId(TEST_USER_ID, credentials);

        // Check that credentials have been updated
        Credentials newCredentials = userLoginDAO.getCredentialsForUserId(TEST_USER_ID);
        assertEquals(credentials.getPassword(), newCredentials.getPassword());
        assertEquals(credentials.getSalt(), newCredentials.getSalt());
        assertEquals(credentials.getIterations(), newCredentials.getIterations());
    }

    @Test
    void testUpdateAttributes() {
        groupDAO.createGroup(new Group("role:administrator"));
        groupDAO.createGroup(new Group("role:roleJustForTesting2"));
        User user = new User();
        user.setFirstName("newFirstname");
        user.setLastName("newLastname");
        user.setEmail("newEmail");
        user.setGroupIds(Set.of("role:roleJustForTesting2", "role:administrator", "role:NotExistingButAddedAnyway"));
        // Test bad userId
        assertThrows(NoSuchElementException.class, () -> userEditDAO.updateAttributes("badUserId", user));

        userEditDAO.updateAttributes(TEST_USER_ID, user);

        User userUpdated = userLookupDAO.getUserById(TEST_USER_ID);
        assertEquals(user.getFirstName(), userUpdated.getFirstName());
        assertEquals(user.getLastName(), userUpdated.getLastName());
        assertEquals(user.getEmail(), userUpdated.getEmail());
        assertEquals(Set.of("role:roleJustForTesting2", "role:administrator", "role:NotExistingButAddedAnyway"), userUpdated.getGroupIds());
    }

    @Test
    void testUpdateActivatedStatus() {
        // Test bad userId
        assertThrows(NoSuchElementException.class, () -> userEditDAO.updateActivatedStatusOfUser("badUserId", true));

        // Activate user
        userEditDAO.updateActivatedStatusOfUser(TEST_USER_ID, true);
        User userUpdated = userLookupDAO.getUserById(TEST_USER_ID);
        assertTrue(userUpdated.isActivated());

        // Deactivate user
        userEditDAO.updateActivatedStatusOfUser(TEST_USER_ID + "2", false);
        userUpdated = userLookupDAO.getUserById(TEST_USER_ID + "2");
        assertFalse(userUpdated.isActivated());
    }

    @Test
    void testClearActivationCode() {
        userEditDAO.clearActivationCode(TEST_USER_ID);
        User user = userLookupDAO.getUserById(TEST_USER_ID);
        assertNull(user.getActivationCode());
    }

    @Test
    void testUpdateUserRoles() {
        groupDAO.createGroup(new Group("role:roleJustForTesting"));
        Set<String> groupIds = Set.of("role:roleJustForTesting");
        User userUpdated = userLookupDAO.getUserById(TEST_USER_ID);
        userUpdated.setGroupIds(groupIds);
        User user = userEditDAO.updateAttributes(TEST_USER_ID, userUpdated);
        assertEquals(groupIds, user.getGroupIds());
    }

    @Test
    void testUpdateUserOrganization() {
        userEditDAO.updateUserOrganization(TEST_USER_ID, "newOrganizationId");
        User userUpdated = userLookupDAO.getUserById(TEST_USER_ID);
        assertEquals("newOrganizationId", userUpdated.getOrganizationId());
        assertTrue(userUpdated.getGroupIds().contains("org:newOrganizationId"));
    }

    @Test
    void testDeleteUser() {
        String userId = registerUserForTesting("3", true);
        User user = userLookupDAO.getUserById(userId);
        assertEquals(userId, user.getId());
        userEditDAO.deleteUser(userId);
        assertThrows(NoSuchElementException.class, () -> userLookupDAO.getUserById(userId));
    }

    @Test
    void testDeleteUserComplete() {
        String userId = registerUserForTesting("3", true);
        userEditDAO.updateCredentialsForUserId(userId, new Credentials("MegaStrongPassword!11"));
        userEditDAO.deleteUser(userId);
        assertFalse(userLoginDAO.needToChangePassword(userId));
    }

    @Test
    void getUserFromUserId() {
        assertNull(userEditDAO.getUserFromUserId("NonExistingUser"));
        assertEquals(PREFIX + "Organization", userEditDAO.getUserFromUserId(TEST_USER_ID + "2").getOrganizationId());
    }

    private String registerUserForTesting(String id, boolean activated) {
        User newUser2 = new User(TEST_USER_ID + id);
        newUser2.setFirstName("testFirstName" + id);
        newUser2.setLastName("testLastName" + id);
        newUser2.setEmail(PREFIX + id + "@test.fr");
        newUser2.setActivated(activated);
        newUser2.setActivationCode(ACTIVATION_CODE);
        newUser2.setOrganizationId(PREFIX + "Organization");
        newUser2.addGroupId("role:gazelle_role");
        newUser2.addGroupId("org:" + PREFIX + "Organization");
        userRegistrationDAO.registerUser(newUser2);
        return TEST_USER_ID + id;
    }
}
