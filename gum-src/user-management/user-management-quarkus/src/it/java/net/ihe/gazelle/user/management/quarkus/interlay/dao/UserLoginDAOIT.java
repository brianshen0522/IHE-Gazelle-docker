package net.ihe.gazelle.user.management.quarkus.interlay.dao;


import io.quarkus.test.junit.QuarkusTest;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import net.ihe.gazelle.user.management.api.domain.organization.Organization;
import net.ihe.gazelle.user.management.api.domain.user.Credentials;
import net.ihe.gazelle.user.management.api.domain.user.User;
import net.ihe.gazelle.user.management.commons.application.exception.GazelleDAOException;
import net.ihe.gazelle.user.management.commons.application.organization.management.OrganizationManagementDAO;
import net.ihe.gazelle.user.management.commons.application.user.edit.UserEditDAO;
import net.ihe.gazelle.user.management.commons.application.user.login.UserLoginDAO;
import net.ihe.gazelle.user.management.commons.application.user.lookup.UserLookupDAO;
import net.ihe.gazelle.user.management.commons.application.user.registration.UserRegistrationDAO;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.sql.Timestamp;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActivateRequestContext
@Transactional
class UserLoginDAOIT {

    @Inject
    UserRegistrationDAO userRegistrationDAO;
    @Inject
    OrganizationManagementDAO organizationManagementDAO;
    @Inject
    UserEditDAO userEditDAO;
    @Inject
    UserLoginDAO userLoginDAO;
    @Inject
    UserLookupDAO userLookupDAO;
    @Inject
    EntityManager entityManager;

    private final String PREFIX = getClass().getSimpleName();
    private final String TEST_USER_ID = PREFIX + "ID";

    @BeforeAll
    public void init() {
        User newUser = new User(TEST_USER_ID);
        newUser.setFirstName("testFirstName");
        newUser.setLastName("testLastName");
        newUser.setEmail(PREFIX + "@test.fr");
        String ACTIVATION_CODE = "superSecretActivationCode";
        newUser.setActivationCode(ACTIVATION_CODE);
        newUser.setOrganizationId(PREFIX + "Organization");
        newUser.setActivated(false);
        newUser.addGroupId("role:gazelle_role");

        organizationManagementDAO.createOrganization(new Organization(PREFIX + "Organization"));
        userRegistrationDAO.registerUser(newUser);
        userEditDAO.updateCredentialsForUserId(TEST_USER_ID, new Credentials("testPassword"));

    }

    @Test
    void testGetCredentialsForUserId() {
        // Test bad userId
        assertThrows(NoSuchElementException.class, () -> userLoginDAO.getCredentialsForUserId("badUserId"));

        Credentials credentials = userLoginDAO.getCredentialsForUserId(TEST_USER_ID);
        assertEquals("testPassword", credentials.getPassword());
        assertNull(credentials.getSalt());
        assertNull(credentials.getIterations());
    }

    @Test
    void testUpdateLoginMetrics() {
        User oldUser = userLookupDAO.getUserById(TEST_USER_ID);
        entityManager.clear();
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        assertThrows(GazelleDAOException.class, () -> userLoginDAO.updateLoginMetricsForUserId("badUserId", timestamp));
        userLoginDAO.updateLoginMetricsForUserId(TEST_USER_ID, timestamp);
        User user = userLookupDAO.getUserById(TEST_USER_ID);
        assertEquals(timestamp.getTime(), user.getLastLoginTimestamp());
        assertEquals(oldUser.getLoginCounter() + 1, user.getLoginCounter());
    }

    @Test
    void testNeedToChangePassword() {
        assertThrows(IllegalArgumentException.class, () -> userLoginDAO.needToChangePassword(null));
        assertFalse(userLoginDAO.needToChangePassword(TEST_USER_ID));
    }
}
