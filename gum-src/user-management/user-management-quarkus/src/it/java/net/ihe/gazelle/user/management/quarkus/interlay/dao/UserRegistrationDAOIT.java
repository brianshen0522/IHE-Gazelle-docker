package net.ihe.gazelle.user.management.quarkus.interlay.dao;


import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import net.ihe.gazelle.user.management.api.domain.group.Group;
import net.ihe.gazelle.user.management.api.domain.user.User;
import net.ihe.gazelle.user.management.commons.application.exception.ErrorMessage;
import net.ihe.gazelle.user.management.commons.application.exception.GazelleDAOException;
import net.ihe.gazelle.user.management.commons.application.group.GroupDAO;
import net.ihe.gazelle.user.management.commons.application.user.edit.UserEditDAO;
import net.ihe.gazelle.user.management.commons.application.user.registration.UserRegistrationDAO;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Transactional
class UserRegistrationDAOIT {
    public static final String TEST_FIRST_NAME = "testFirstName";
    public static final String TEST_LAST_NAME = "testLastName";
    public static final String TEST_ORGA = "testOrga";
    public static final String GROUP_ID = "org-adm:" + TEST_ORGA;
    public static final String TEST_USER_MAIL = "test@mail.fr";
    public static final String TEST_USER_ID = "testUserID";
    public static final String TEST_USER_MAIL2 = "test2@mail.fr";
    public static final String TEST_USER_ID2 = "test2UserID";
    public static final String TEST_USER_ACTIVATION_CODE = "superSecretActivationCode";

    @Inject
    UserRegistrationDAO userRegistrationDAO;
    @Inject
    UserEditDAO userEditDAO;
    @Inject
    GroupDAO groupDAO;

    /**
     * Warning : The following tests are order dependent
     * Be careful when you are updating one of them
     */

    @Test
    @Order(1)
    void testRegisterUser() {
        User newUser = new User(TEST_USER_ID);
        newUser.setFirstName(TEST_FIRST_NAME);
        newUser.setLastName(TEST_LAST_NAME);
        newUser.setEmail(TEST_USER_MAIL);
        newUser.setActivationCode(TEST_USER_ACTIVATION_CODE);
        newUser.setOrganizationId(TEST_ORGA);
        newUser.setActivated(true);
        newUser.addGroupId(GROUP_ID);

        groupDAO.createGroup(new Group(GROUP_ID));
        User createdUser = userRegistrationDAO.registerUser(newUser);

        assertEquals(TEST_USER_ID, createdUser.getId());
        assertEquals(TEST_FIRST_NAME, createdUser.getFirstName());
        assertEquals(TEST_LAST_NAME, createdUser.getLastName());
        assertEquals(TEST_USER_MAIL, createdUser.getEmail());
        assertTrue(createdUser.getGroupIds().contains(GROUP_ID));
        assertEquals(TEST_ORGA, createdUser.getOrganizationId());
        assertTrue(createdUser.isActivated());
    }

    @Test
    @Order(1)
    void testRegisterSecondUser() {
        User newUser = new User(TEST_USER_ID2);
        newUser.setFirstName(TEST_FIRST_NAME);
        newUser.setLastName(TEST_LAST_NAME);
        newUser.setEmail(TEST_USER_MAIL2);
        newUser.setOrganizationId("testGroup_2");
        newUser.setActivated(true);
        newUser.addGroupId("role:gazelle_admin");

        User createdUser = userRegistrationDAO.registerUser(newUser);

        assertEquals(TEST_USER_ID2, createdUser.getId());
        assertEquals(TEST_USER_MAIL2, createdUser.getEmail());
        assertEquals("testGroup_2", createdUser.getOrganizationId());
        assertTrue(createdUser.isActivated());
    }

    @Test
    @Order(2)
    void testIsEmailAlreadyExistToBeRegistered() {
        assertTrue(userRegistrationDAO.isEmailAlreadyExist(TEST_USER_MAIL));
        assertTrue(userRegistrationDAO.isEmailAlreadyExist(TEST_USER_MAIL.toLowerCase()));
        assertFalse(userRegistrationDAO.isEmailAlreadyExist("testEmailNotTaken@test.fr"));
    }

    @Test
    @Order(2)
    void testGetAdminsOfOrganization() {
        List<User> users = userRegistrationDAO.getActiveAdminsOfOrganization(TEST_ORGA);

        assertFalse(users.isEmpty());
        User user = users.getFirst();
        assertEquals(TEST_FIRST_NAME, user.getFirstName());
        assertEquals(TEST_LAST_NAME, user.getLastName());
        assertEquals(TEST_USER_MAIL, user.getEmail());
    }

    @Test
    void testGetAllUsersCount() {
        int numberOfUsers = userRegistrationDAO.getAllUsersCount();
        assertNotEquals(0, numberOfUsers);
    }

    @Test
    @Order(3)
    void testRollbackUserRegistration() {
        // Rollback user registration
        userRegistrationDAO.rollbackUserRegistration(TEST_USER_ID2);

        // Check that the user is deleted
        assertFalse(userRegistrationDAO.isEmailAlreadyExist(TEST_USER_MAIL2));
        User user = new User() {{
            setFirstName("New Firstname");
        }};
        Exception e = assertThrows(NoSuchElementException.class, () -> userEditDAO.updateAttributes(TEST_USER_ID2, user));
        assertTrue(e.getMessage().contains(ErrorMessage.USER_NOT_FOUND.getMessage()));
    }

    @Test
    void testActivateUserWithActivationCode() {
        assertThrows(GazelleDAOException.class, () -> userRegistrationDAO.activateUserWithActivationCode("badActivationCode"));
        User user = userRegistrationDAO.activateUserWithActivationCode(TEST_USER_ACTIVATION_CODE);
        assertTrue(user.isActivated());
        assertNull(user.getActivationCode());
    }

    @Test
    void testRetrieveActiveOrgaAdmins() {
        assertTrue(userRegistrationDAO.getActiveAdminsOfOrganization("badOrganisationId").isEmpty());

        User newUser = new User("inativeUserId");
        newUser.setFirstName("inactive fn");
        newUser.setLastName("inactive ln");
        newUser.setEmail("inactive@email.com");
        newUser.setOrganizationId(TEST_ORGA);
        newUser.addGroupId(GROUP_ID);
        newUser.setActivated(false);
        userRegistrationDAO.registerUser(newUser);

        List<User> users = userRegistrationDAO.getActiveAdminsOfOrganization(TEST_ORGA);
        assertTrue(users.stream().noneMatch(user -> user.getEmail().equals(newUser.getEmail())));
    }
}
