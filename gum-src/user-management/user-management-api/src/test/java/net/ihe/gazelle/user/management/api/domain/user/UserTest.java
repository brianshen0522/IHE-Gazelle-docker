package net.ihe.gazelle.user.management.api.domain.user;

import net.ihe.gazelle.user.management.api.domain.group.Group;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.Arguments;

import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void testBasicConstructor() {
        User user = new User("testId");
        assertEquals("testId", user.getId());
        assertNull(user.getFirstName());
        assertNull(user.getLastName());
        assertNull(user.getEmail());
        assertTrue(user.getGroupIds().isEmpty());
        assertNull(user.getOrganizationId());
        assertNull(user.isActivated());
        assertEquals(0, user.getLastLoginTimestamp());
        assertEquals(0, user.getLastUpdateTimestamp());
        assertEquals(0,user.getLoginCounter());
        assertEquals(0,user.getRegistrationTimestamp());
        assertNull(user.getActivationCode());
    }

    @Test
    void testConstructorNoRole() {
        User user = new User("1", "test", "user", "test@test.com", "orga_XXX");
        assertEquals("test", user.getFirstName());
        assertEquals("user", user.getLastName());
        assertEquals("1", user.getId());
        assertEquals("test@test.com", user.getEmail());
        assertTrue(user.getGroupIds().isEmpty());
        assertEquals("orga_XXX", user.getOrganizationId());
        assertNull(user.isActivated());

        user = new User("1", "test", "user", "test@test.com", "orgaId");
        assertEquals("test", user.getFirstName());
        assertEquals("user", user.getLastName());
        assertEquals("1", user.getId());
        assertEquals("test@test.com", user.getEmail());
        assertTrue(user.getGroupIds().isEmpty());
        assertEquals("orgaId", user.getOrganizationId());
        assertNull(user.isActivated());
    }

    @Test
    void testCompleteConstructor() {
        Set<String> groupIds = Set.of("monitor");
        User user = new User("1", "test", "user", "test@test.com", "orga_XXX", groupIds);

        assertEquals("test", user.getFirstName());
        assertEquals("user", user.getLastName());
        assertEquals("1", user.getId());
        assertEquals("test@test.com", user.getEmail());
        assertEquals(groupIds, user.getGroupIds());

        assertEquals("orga_XXX", user.getOrganizationId());
        assertNull(user.isActivated());
        assertEquals(0, user.getLastLoginTimestamp());
    }

    @Test
    void testCopyConstructor(){
        Set<String> roleIds = Set.of("monitor");

        User user = new User("1", "test", "user", "test@test.com", "orga_XXX", roleIds);
        user.setLastLoginTimestamp(8);
        user.setRegistrationTimestamp(9);
        user.setLoginCounter(10);
        user.setActivated(true);

        User userCopy = new User(user);
        assertEquals(user.getId(), userCopy.getId());
        assertEquals(user.getFirstName(), userCopy.getFirstName());
        assertEquals(user.getLastName(), userCopy.getLastName());
        assertEquals(user.getEmail(), userCopy.getEmail());
        assertEquals(user.getOrganizationId(),userCopy.getOrganizationId());
        assertEquals(user.getGroupIds(), userCopy.getGroupIds());
        assertEquals(user.getLastLoginTimestamp(), userCopy.getLastLoginTimestamp());
        assertEquals(user.getRegistrationTimestamp(), userCopy.getRegistrationTimestamp());
        assertEquals(user.getLoginCounter(), userCopy.getLoginCounter());
        assertEquals(user.getLastUpdateTimestamp(), userCopy.getLastUpdateTimestamp());
        assertTrue(userCopy.isActivated());
    }

    @Test
    void testConstructUserWithSetters() {
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


        assertEquals("testId", user.getId());
        assertEquals("first", user.getFirstName());
        assertEquals("last", user.getLastName());
        assertEquals("first.last@test.com", user.getEmail());
        assertTrue(user.isActivated());
        assertEquals("idGroup", user.getOrganizationId());
        assertEquals(groupIds, user.getGroupIds());
        assertEquals("activationCode", user.getActivationCode());
        assertEquals(5,user.getLoginCounter());
        assertEquals(6,user.getRegistrationTimestamp());
        assertEquals(7, user.getLastLoginTimestamp());
        assertEquals(8, user.getLastUpdateTimestamp());

        user.addGroupId(groupId1);
        user.addGroupId(groupId2);
        assertEquals(Set.of(groupId1, groupId2), user.getGroupIds());
    }

    @Test
    void testEquals() {
        EqualsVerifier.simple().forClass(User.class).withPrefabValues(Group.class, new Group(), new Group("org:test")).verify();
    }

    static Stream<Arguments> provideUsers() {
        User user = new User("1", "test", "user", "test@test.com",
                "orga_XXX", Set.of("monitor")
        );

        User user2 = new User("1", "test", "user", "test@test.com",
                "orga_XXX", Set.of("monitor")
        );
        User user3 = new User("26", "test", "user", "test@test.com",
               "orga_XXX", Set.of("monitor")
        );

        return Stream.of(
                Arguments.of(false, new User("id"), null),
                Arguments.of(true, user, user2),
                Arguments.of(true, user, user),
                Arguments.of(false, user, user3),
                Arguments.of(false, user3, null),
                Arguments.of(true, new User("id"), new User("id")),
                Arguments.of(false, new User("id"), user2)
        );
    }
}
