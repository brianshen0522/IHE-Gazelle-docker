package net.ihe.gazelle.user.management.api.interlay.user;

import net.ihe.gazelle.user.management.api.domain.user.User;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserSummaryResourceTest {

    @Test
    void testBasicConstructorWithSetters() {
        UserSummaryResource userResource = new UserSummaryResource("id")
                .setFirstName("firstName")
                .setLastName("lastName")
                .setOrganizationId("organizationId");


        assertEquals("id", userResource.getId());
        assertEquals("firstName", userResource.getFirstName());
        assertEquals("lastName", userResource.getLastName());
        assertEquals("organizationId", userResource.getOrganizationId());

        userResource = new UserSummaryResource();
        assertNull(userResource.getId());
    }

    @Test
    void testCompleteConstructor() {
        UserSummaryResource userResource = new UserSummaryResource("firstName", "lastName", "organization");
        assertEquals("firstName", userResource.getFirstName());
        assertEquals("lastName", userResource.getLastName());
        assertEquals("organization", userResource.getOrganizationId());
    }

    @Test
    void testConstructFromAnotherUser() {
        User user = new User("userId", "userFirstName", "userLastName", "userEmail", "organizationId2");
        Set<String> groups = Set.of("role1");
        user.setLoginCounter(19);
        user.setGroupIds(groups);
        user.setLastUpdateTimestamp(17L);
        user.setLastLoginTimestamp(18L);
        UserSummaryResource userResource = new UserSummaryResource(user);

        assertEquals("userId", userResource.getId());
        assertEquals("userFirstName", userResource.getFirstName());
        assertEquals("userLastName", userResource.getLastName());
        assertEquals("organizationId2", userResource.getOrganizationId());
    }

    @Test
    void testNullOrganizationAndGroup() {
        User user = new User("userId", "userFirstName", "userLastName", "userEmail", "organizationId3");
        UserSummaryResource userResource = new UserSummaryResource(user);
        assertEquals("organizationId3", userResource.getOrganizationId());
    }

    @Test
    void testUserResourceAsUser() {
        UserSummaryResource userQuery = new UserSummaryResource("id")
                .setFirstName("FIRSTNAME")
                .setLastName("LASTNAME")
                .setOrganizationId("organizationId");
        User user = userQuery.asUser();
        assertEquals("id", user.getId());
        assertEquals("FIRSTNAME", user.getFirstName());
        assertEquals("LASTNAME", user.getLastName());
        assertEquals("organizationId", user.getOrganizationId());
    }

    @Test
    void testNullOrganizationAndGroup2() {
        UserSummaryResource userResource = new UserSummaryResource("id")
                .setFirstName("FIRSTNAME")
                .setLastName("LASTNAME")
                .setOrganizationId("organizationId");
        User user = userResource.asUser();
        assertTrue(user.getGroupIds().isEmpty());
        assertEquals("organizationId", user.getOrganizationId());
    }

    @Test
    void testToStringUserResource() {
        UserSummaryResource userQuery = new UserSummaryResource("id")
                .setFirstName("FIRSTNAME")
                .setLastName("LASTNAME");

        String expectedString = userQuery.toString();
        assertTrue(expectedString.contains("FIRSTNAME"));
        assertTrue(expectedString.contains("LASTNAME"));
    }

    @Test
    void testEqualsAndHashcode() {
        EqualsVerifier.simple().forClass(UserSummaryResource.class).verify();
    }
}
