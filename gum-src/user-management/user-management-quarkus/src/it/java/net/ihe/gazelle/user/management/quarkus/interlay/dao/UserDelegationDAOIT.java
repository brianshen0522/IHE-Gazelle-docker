package net.ihe.gazelle.user.management.quarkus.interlay.dao;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.inject.Inject;
import net.ihe.gazelle.user.management.api.domain.user.DelegatedUser;
import net.ihe.gazelle.user.management.api.domain.user.User;
import net.ihe.gazelle.user.management.commons.application.user.delegation.UserDelegationDAO;
import net.ihe.gazelle.user.management.commons.application.user.registration.UserRegistrationDAO;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.NoSuchElementException;

import static io.smallrye.common.constraint.Assert.assertNotNull;
import static io.smallrye.common.constraint.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActivateRequestContext
class UserDelegationDAOIT {

    private static final String DELEG_USER_ID = "delegatedId";
    private static final String DELEG_USER_FIRSTNAME = "delegatedFirstName";
    private static final String DELEG_USER_LASTNAME = "delegatedLastName";
    private static final String NONDELEG_USER_ID = "nonDelegatedId";
    private static final String NONDELEG_USER_FIRSTNAME = "nondelegatedFirstName";
    private static final String NONDELEG_USER_LASTNAME = "nondelegatedLastName";
    @Inject
    UserDelegationDAO userDelegatedDAO;
    @Inject
    UserRegistrationDAO userRegistrationDAO;

    private final String PREFIX = getClass().getSimpleName();
    private final String NONDELEG_USER_EMAIL = PREFIX + "@test.fr";
    private final String DELEG_USER_EMAIL = PREFIX + "deleg@test.fr";

    @BeforeAll
    public void init() {
        createUser(NONDELEG_USER_ID, NONDELEG_USER_FIRSTNAME, NONDELEG_USER_LASTNAME, NONDELEG_USER_EMAIL,
                false, NONDELEG_USER_ID, PREFIX + "Organization", "org-adm:" + PREFIX + "Organization");
    }

    @Test
    void testCreateDelegatedUser() {
        User user = new User(DELEG_USER_ID);
        user.setFirstName(DELEG_USER_FIRSTNAME);
        user.setLastName(DELEG_USER_LASTNAME);
        user.setEmail(DELEG_USER_EMAIL);
        user.setActivated(true);
        user.setActivationCode(DELEG_USER_ID);
        user.setOrganizationId(PREFIX + "Organization");
        user.addGroupId("org-adm:" + PREFIX + "Organization");

        assertThrows(NoSuchElementException.class, () -> userDelegatedDAO.getDelegatedUser("externalId", "idpId"));
        DelegatedUser delegatedUser = userDelegatedDAO.createDelegatedUser(user, "externalId", "idpId");

        assertNotNull(delegatedUser);
        assertNotNull(delegatedUser.getId());
        assertNotEquals(NONDELEG_USER_ID, delegatedUser.getId());
        assertEquals(DELEG_USER_FIRSTNAME, delegatedUser.getFirstName());
        assertEquals(DELEG_USER_LASTNAME, delegatedUser.getLastName());
        assertEquals(DELEG_USER_EMAIL, delegatedUser.getEmail());
        assertTrue(delegatedUser.isActivated());

        // Test getDelegatedUser
        DelegatedUser getUser = userDelegatedDAO.getDelegatedUser("externalId", "idpId");
        assertEquals(delegatedUser.getId(), getUser.getId());

        // Test getDelegatedUserById
        DelegatedUser getUserById = userDelegatedDAO.getDelegatedUserById(delegatedUser.getId());
        assertEquals(delegatedUser.getEmail(), getUserById.getEmail());
    }

    @Test
    void testTransformUserIntoDelegatedUser() {
        assertThrows(NoSuchElementException.class, () -> userDelegatedDAO.getDelegatedUser("newExternalId", "idp2"));
        DelegatedUser delegatedUser = userDelegatedDAO.transformUserIntoDelegatedUser(NONDELEG_USER_EMAIL, "newExternalId", "idp2");

        DelegatedUser getUser = userDelegatedDAO.getDelegatedUser("newExternalId", "idp2");
        assertEquals(delegatedUser.getId(), getUser.getId());
    }

    private void createUser(String id, String firstName, String lastName, String email, boolean activated,
                            String activationCode, String organizationId, String... roles) {
        User newUser = new User(id);
        newUser.setFirstName(firstName);
        newUser.setLastName(lastName);
        newUser.setEmail(email);
        newUser.setActivated(activated);
        newUser.setActivationCode(activationCode);
        newUser.setOrganizationId(organizationId);
        for (String role : roles) {
            newUser.addGroupId(role);
        }
        userRegistrationDAO.registerUser(newUser);
    }
}
