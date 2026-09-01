package net.ihe.gazelle.user.management.api.domain.user;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DelegatedUserTest {

    @Test
    void constructorWithSetterTest() {
        DelegatedUser delegatedUser = new DelegatedUser()
                .setExternalId("externalId")
                .setIdpId("idp");

        assertEquals("externalId", delegatedUser.getExternalId());
        assertEquals("idp", delegatedUser.getIdpId());

        delegatedUser = new DelegatedUser("delegatedId");
        assertEquals("delegatedId", delegatedUser.getId());
    }

    @Test
    void equalsTest() {
        DelegatedUser delegatedUser = new DelegatedUser()
                .setExternalId("externalId")
                .setIdpId("idp");

        DelegatedUser delegatedUser2 = new DelegatedUser()
                .setExternalId("externalId")
                .setIdpId("idp");

        assertEquals(delegatedUser, delegatedUser2);
    }

    @Test
    void constructorWithUserTest() {
        User user = new User("badTestId");
        String groupId1 = "roleTest";
        String groupId2 = "roleTest2";
        Set<String> groupIds = Set.of(groupId1, groupId2);

        user.setId("testId");
        user.setFirstName("first");
        user.setLastName("last");
        user.setEmail("first.last@test.com");
        user.setActivated(true);
        user.setOrganizationId("idGroup");
        user.setGroupIds(groupIds);
        user.setActivationCode("activationCode");
        user.setLoginCounter(5);
        user.setRegistrationTimestamp(6);
        user.setLastLoginTimestamp(7);
        user.setLastUpdateTimestamp(8);
        DelegatedUser delegatedUser = new DelegatedUser(user, "externalId", "idp");

        assertEquals("testId", delegatedUser.getId());
        assertEquals("externalId", delegatedUser.getExternalId());
        assertEquals("idp", delegatedUser.getIdpId());
        assertEquals("first.last@test.com", delegatedUser.getEmail());

        User userCopy = delegatedUser.asUser();
        assertEquals(user, userCopy);

        DelegatedUser delegatedUserCopy = new DelegatedUser(userCopy, "externalId", "idp");
        assertEquals(delegatedUser, delegatedUserCopy);
    }
}
