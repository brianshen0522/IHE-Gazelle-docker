package net.ihe.gazelle.user.management.quarkus.application.service;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import net.ihe.gazelle.user.management.api.application.user.delegation.UserDelegationService;
import net.ihe.gazelle.user.management.api.domain.user.DelegatedUser;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class UserDelegationServiceIT {

    @Inject
    UserDelegationService userDelegationService;

    @Test
    void createAndGetDelegatedUserTest() {
        String NEW_DELEGATED_USER_EXTERNAL_ID = "newDelegatedUserExternalId";
        String NEW_DELEGATED_USER_IDP = "newDelegatedUserIdp";
        DelegatedUser delegatedUser = new DelegatedUser("newDelegatedUser");
        delegatedUser.setFirstName("newDelegatedUser fn");
        delegatedUser.setLastName("newDelegatedUser ln");
        delegatedUser.setEmail("newDelegatedUser@test.fr");
        delegatedUser.setOrganizationId("testGroup_delegated");
        delegatedUser.setActivated(true);
        delegatedUser.addGroupId("role:gazelle_role");

        assertThrows(NoSuchElementException.class, () -> userDelegationService.getDelegatedUser(NEW_DELEGATED_USER_EXTERNAL_ID, NEW_DELEGATED_USER_IDP));
        DelegatedUser createdUser = userDelegationService.createDelegatedUser(delegatedUser, NEW_DELEGATED_USER_EXTERNAL_ID, NEW_DELEGATED_USER_IDP);
        assertEquals("newDelegatedUser@test.fr", createdUser.getEmail());
        assertEquals("testGroup_delegated", createdUser.getOrganizationId());
        assertTrue(createdUser.isActivated());

        DelegatedUser retrievedUser = userDelegationService.getDelegatedUser(NEW_DELEGATED_USER_EXTERNAL_ID, NEW_DELEGATED_USER_IDP);
        assertEquals(createdUser.getId(), retrievedUser.getId());
    }

    @Test
    void createAndGetBadDelegatedUserTest() {
        String NEW_DELEGATED_USER_EXTERNAL_ID = "newDelegatedUserExternalId2";
        String NEW_DELEGATED_USER_IDP = "newDelegatedUserIdp2";

        assertThrows(IllegalArgumentException.class, () -> userDelegationService.getDelegatedUser(null, null));
        assertThrows(IllegalArgumentException.class, () -> userDelegationService.getDelegatedUser(null, NEW_DELEGATED_USER_IDP));
        assertThrows(IllegalArgumentException.class, () -> userDelegationService.getDelegatedUser(NEW_DELEGATED_USER_EXTERNAL_ID, null));

    }
}