package net.ihe.gazelle.user.management.api.interlay.user;

import net.ihe.gazelle.user.management.api.domain.user.User;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserEditionResourceTest {

    @Test
    void testBasicConstructor() {
        UserEditionResource userEditionResource = new UserEditionResource("firstname", "lastname", "orgaId", "groupId", false);

        assertEquals("firstname", userEditionResource.getFirstName());
        assertEquals("lastname", userEditionResource.getLastName());
        assertNull(userEditionResource.getEmail());
        assertEquals("orgaId", userEditionResource.getOrganizationId());
        assertFalse(userEditionResource.isActivated());
        assertNull(userEditionResource.getConsent());
    }

    @Test
    void testConstructFromAnotherUser() {
        User user = new User("userId","userFirstName","userLastName","userEmail", "organizationId2");
        Set<String> groups = Set.of("role1");
        user.setLoginCounter(19);
        user.setGroupIds(groups);
        user.setLastUpdateTimestamp(17L);
        user.setLastLoginTimestamp(18L);
        UserEditionResource userEditionResource = new UserEditionResource(user);

        assertEquals("userFirstName", userEditionResource.getFirstName());
        assertEquals("userLastName", userEditionResource.getLastName());
        assertEquals("userEmail", userEditionResource.getEmail());
        assertEquals("organizationId2", userEditionResource.getOrganizationId());
        assertNull(userEditionResource.isActivated());
        assertNull(userEditionResource.getConsent());
    }

    @Test
    void testUserResourceAsUser() {
        UserEditionResource userEditionResource = new UserEditionResource();
        userEditionResource.setFirstName("FIRSTNAME");
        userEditionResource.setLastName("LASTNAME");
        userEditionResource.setEmail("EMAIL");
        userEditionResource.setGroupIds(Set.of("roles1"));
        userEditionResource.setOrganizationId("ORGAID");

        User user = userEditionResource.asUser();
        assertEquals("FIRSTNAME", user.getFirstName());
        assertEquals("LASTNAME", user.getLastName());
        assertEquals("EMAIL", user.getEmail());
        assertEquals("ORGAID", user.getOrganizationId());
        assertFalse(user.getGroupIds().isEmpty());
        assertTrue(user.getGroupIds().contains("roles1"));
    }

    @Test
    void testEqualsAndHashcode() {
        EqualsVerifier.simple().forClass(UserEditionResource.class).verify();
    }
}
