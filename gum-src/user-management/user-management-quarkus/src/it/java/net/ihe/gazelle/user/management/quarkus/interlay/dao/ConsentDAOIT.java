package net.ihe.gazelle.user.management.quarkus.interlay.dao;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import net.ihe.gazelle.user.management.api.domain.user.User;
import net.ihe.gazelle.user.management.commons.application.user.registration.ConsentDAO;
import net.ihe.gazelle.user.management.commons.application.user.registration.UserRegistrationDAO;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static net.ihe.gazelle.user.management.api.domain.user.GazelleDefaultGroup.GAZELLE_ADMIN;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActivateRequestContext
@Transactional
class ConsentDAOIT {

    @Inject
    ConsentDAO consentDAO;
    @Inject
    UserRegistrationDAO userRegistrationDAO;

    private final String PREFIX = getClass().getSimpleName();
    private final String TEST_USER_ID = PREFIX +"ID";
    private final String TEST_USER_EMAIL = PREFIX+"@test.fr";

    @BeforeAll
    public void init() {
        createUser(TEST_USER_ID, TEST_USER_EMAIL, PREFIX + "Organization", GAZELLE_ADMIN.getName());
    }

    private void createUser(String id, String email,
                            String organizationId, String... roles) {
        User newUser = new User(id);
        newUser.setFirstName("consentFirstName");
        newUser.setLastName("consentLastName");
        newUser.setEmail(email);
        newUser.setActivated(false);
        newUser.setOrganizationId(organizationId);
        for (String role : roles) {
            newUser.addGroupId(role);
        }
        userRegistrationDAO.registerUser(newUser);
    }

    @Test
    void testNeedToGiveConsent() {
        assertTrue(consentDAO.needToGiveConsent(TEST_USER_ID));
        assertTrue(consentDAO.needToGiveConsent("bad-user-id"));

        consentDAO.acceptUserConsent(TEST_USER_ID);
        assertFalse(consentDAO.needToGiveConsent(TEST_USER_ID));
    }
}
