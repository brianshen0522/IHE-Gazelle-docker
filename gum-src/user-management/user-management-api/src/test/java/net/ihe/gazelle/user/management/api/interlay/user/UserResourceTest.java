package net.ihe.gazelle.user.management.api.interlay.user;

import net.ihe.gazelle.user.management.api.domain.user.User;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserResourceTest {

    @Test
    void testBasicConstructorWithSetters() {
        UserResource userResource = new UserResource("id")
                .setFirstName("firstName")
                .setLastName("lastName")
                .setEmail("email")
                .setGroupIds(Set.of("groups"))
                .setOrganizationId("organizationId")
                .setActivated(true)
                .setLastLoginTimestamp(1L)
                .setLastUpdateTimestamp(2L)
                .setExternalId("externalId")
                .setIdpId("idp");

        userResource.setConsent(true);
        userResource.setLoginCounter(18);

        assertEquals("id", userResource.getId());
        assertEquals("firstName", userResource.getFirstName());
        assertEquals("lastName", userResource.getLastName());
        assertEquals("email", userResource.getEmail());
        assertTrue(userResource.getGroupIds().contains("groups"));
        assertEquals("organizationId", userResource.getOrganizationId());
        assertTrue(userResource.isActivated());
        assertTrue(userResource.getConsent());
        assertEquals(1L, userResource.getLastLoginTimestamp());
        assertEquals(2L, userResource.getLastUpdateTimestamp());
        assertEquals(18, userResource.getLoginCounter());
        assertEquals(Boolean.TRUE, userResource.isDelegated());
        assertEquals("externalId", userResource.getExternalId());
        assertEquals("idp", userResource.getIdpId());

        userResource = new UserResource();
        assertNull(userResource.getId());
    }

    @Test
    void testCompleteConstructor() {
        UserResource userResource = new UserResource("firstName", "lastName", "organization", "group", true);
        assertEquals("firstName", userResource.getFirstName());
        assertEquals("lastName", userResource.getLastName());
        assertNull(userResource.getEmail());
        assertEquals("organization", userResource.getOrganizationId());
        assertEquals(0,userResource.getLoginCounter());
        assertTrue(userResource.getGroupIds().contains("group"));
        assertTrue(userResource.isActivated());
        assertEquals(Boolean.FALSE, userResource.isDelegated());
    }

    @Test
    void testConstructFromAnotherUser() {
        User user = new User("userId","userFirstName","userLastName","userEmail", "organizationId2");
        Set<String> groups = Set.of("role1");
        user.setLoginCounter(19);
        user.setGroupIds(groups);
        user.setLastUpdateTimestamp(17L);
        user.setLastLoginTimestamp(18L);
        UserResource userResource = new UserResource(user);

        assertEquals("userId", userResource.getId());
        assertEquals("userFirstName", userResource.getFirstName());
        assertEquals("userLastName", userResource.getLastName());
        assertEquals("userEmail", userResource.getEmail());
        assertEquals("organizationId2", userResource.getOrganizationId());
        assertNotNull(userResource.getGroupIds());
        assertTrue(userResource.getGroupIds().contains("role1"));
        assertEquals(19, userResource.getLoginCounter());
        assertEquals(17L, userResource.getLastUpdateTimestamp());
        assertEquals(18L, userResource.getLastLoginTimestamp());
        assertNull(userResource.isActivated());
        assertNull(userResource.getConsent());
    }

    @Test
    void testNullOrganizationAndGroup() {
        User user = new User("userId","userFirstName","userLastName","userEmail", "organizationId3");
        UserResource userResource = new UserResource(user);
        assertTrue(userResource.getGroupIds().isEmpty());
        assertEquals("organizationId3", userResource.getOrganizationId());
    }

    @Test
    void testUserResourceAsUser() {
        UserResource userQuery = new UserResource("id")
                .setFirstName("FIRSTNAME")
                .setLastName("LASTNAME")
                .setGroupIds(Set.of("roles1"))
                .setEmail("EMAIL");
        User user = userQuery.asUser();
        assertEquals("id", user.getId());
        assertEquals("FIRSTNAME", user.getFirstName());
        assertEquals("LASTNAME", user.getLastName());
        assertEquals("EMAIL", user.getEmail());
        assertFalse(user.getGroupIds().isEmpty());
        assertTrue(user.getGroupIds().contains("roles1"));
        assertNull(user.getOrganizationId());
    }

    @Test
    void testNullOrganizationAndGroup2() {
        UserResource userResource = new UserResource("id")
                .setFirstName("FIRSTNAME")
                .setLastName("LASTNAME")
                .setGroupIds(null)
                .setOrganizationId("organizationId");
        User user = userResource.asUser();
        assertTrue(user.getGroupIds().isEmpty());
        assertEquals("organizationId", user.getOrganizationId());
    }

    @Test
    void testToStringUserResource() {
        UserResource userQuery = new UserResource("id")
                .setFirstName("FIRSTNAME")
                .setLastName("LASTNAME")
                .setEmail("EMAIL");

        String expectedString = userQuery.toString();
        assertTrue(expectedString.contains("FIRSTNAME"));
        assertTrue(expectedString.contains("LASTNAME"));
        assertTrue(expectedString.contains("EMAIL"));
    }

    @Test
    void testEqualsAndHashcode() {
        EqualsVerifier.simple().forClass(UserResource.class).verify();
    }
}
