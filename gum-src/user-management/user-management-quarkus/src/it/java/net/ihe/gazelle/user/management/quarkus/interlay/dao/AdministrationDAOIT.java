package net.ihe.gazelle.user.management.quarkus.interlay.dao;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import net.ihe.gazelle.user.management.api.application.user.registration.ConsentService;
import net.ihe.gazelle.user.management.api.domain.user.User;
import net.ihe.gazelle.user.management.commons.application.user.edit.UserEditDAO;
import net.ihe.gazelle.user.management.commons.application.user.registration.UserRegistrationDAO;
import net.ihe.gazelle.user.management.core.interlay.dao.AdministrationDAO;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.sql.Timestamp;

import static net.ihe.gazelle.user.management.core.application.service.AdministrationServiceImpl.getCurrentTimestampPlusDays;
import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActivateRequestContext
class AdministrationDAOIT {

    @Inject
    AdministrationDAO administrationDAO;
    @Inject
    UserRegistrationDAO userRegistrationDAO;
    @Inject
    UserEditDAO userEditDAO;
    @Inject
    UserRegistrationMock userRegistrationMock;
    @Inject
    ConsentService consentService;

    private final String TEST_GROUP_ID = "ADMINDAO_ID";
    private static final int DAYS_IN_ONE_MONTH = 31;

    @BeforeAll
    public void init() {
        // Purge before test
        administrationDAO.purgeInactiveAndNonConsentUsers(getCurrentTimestampPlusDays(DAYS_IN_ONE_MONTH * 2));
    }

    @Test
    void purgeInactiveUsers() {
        User user = new User("id", "firstname", "lastname", "email@test.fr", TEST_GROUP_ID);
        user.setActivationCode("activationCode");
        user.addGroupId("role:ROLE_USER");
        user.setActivated(false);
        userRegistrationDAO.registerUser(user);

        int numberOfDeletedUsers = administrationDAO.purgeInactiveAndNonConsentUsers(getCurrentTimestampPlusDays(DAYS_IN_ONE_MONTH));
        assertEquals(1, numberOfDeletedUsers);
    }

    @Test
    void purgeUsersWithoutConsent() {
        User user = new User("id2", "firstname2", "lastname2", "email@test2.fr", TEST_GROUP_ID);
        user.addGroupId("role:ROLE_USER");
        user.setActivated(true);
        userRegistrationDAO.registerUser(user);

        int numberOfDeletedUsers = administrationDAO.purgeInactiveAndNonConsentUsers(getCurrentTimestampPlusDays(DAYS_IN_ONE_MONTH * 2));
        assertEquals(1, numberOfDeletedUsers);
    }

    @Test
    void purgeInactiveUsers12Months() {
        User user = new User("id3", "firstname3", "lastname3", "email@test.fr", TEST_GROUP_ID);
        user.addGroupId("role:ROLE_USER");
        user.setActivated(false);
        userRegistrationDAO.registerUser(user);
        //user registered 1 year and a half ago
        int update = userRegistrationMock.updateRegistrationDateByUserId(user.getId(),
                getCurrentTimestampPlusDays(-(DAYS_IN_ONE_MONTH * 18)).getTime());
        assertEquals(1, update);

        //delete all users that registered more than two years ago
        assertEquals(0, administrationDAO.purgeInactiveAndNonConsentUsers(getCurrentTimestampPlusDays((-(DAYS_IN_ONE_MONTH * 24)))));
        //delete all users that registered more than 1 year ago
        assertEquals(1, administrationDAO.purgeInactiveAndNonConsentUsers(getCurrentTimestampPlusDays(-(DAYS_IN_ONE_MONTH * 12))));
    }

    @Test
    @Transactional
    void purgeNoConsentUsers12Months() {
        User user = new User("id3", "firstname3", "lastname3", "email@test.fr", TEST_GROUP_ID);
        user.addGroupId("role:ROLE_USER");
        user.setActivated(true);
        userRegistrationDAO.registerUser(user);


        int update = userRegistrationMock.updateRegistrationDateByUserId(user.getId(),
                getCurrentTimestampPlusDays(-(DAYS_IN_ONE_MONTH * 13)).getTime());
        assertEquals(1, update);
        assertEquals(0, administrationDAO.purgeInactiveAndNonConsentUsers(getCurrentTimestampPlusDays(-(DAYS_IN_ONE_MONTH * 24))));
        assertEquals(1, administrationDAO.purgeInactiveAndNonConsentUsers(getCurrentTimestampPlusDays(-(DAYS_IN_ONE_MONTH * 12))));
    }

    @Test
    @Transactional
    void purgeZeroUsers() {
        User user = new User("id3", "firstname3", "lastname3", "email@test3.fr", TEST_GROUP_ID);
        user.addGroupId("role:ROLE_USER");
        user.setActivated(true);
        userRegistrationDAO.registerUser(user);
        consentService.acceptUserConsent(user.getId());

        Timestamp limitRegistrationTimestamp = getCurrentTimestampPlusDays(DAYS_IN_ONE_MONTH * 2);
        int numberOfDeletedUsers = administrationDAO.purgeInactiveAndNonConsentUsers(limitRegistrationTimestamp);
        assertEquals(0, numberOfDeletedUsers);
    }
}

