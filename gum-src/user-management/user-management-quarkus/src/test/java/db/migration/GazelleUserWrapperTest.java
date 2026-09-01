package db.migration;

import io.quarkus.test.junit.TestProfile;
import net.ihe.gazelle.user.management.api.domain.user.User;
import net.ihe.gazelle.user.management.quarkus.GazelleProfiles;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestProfile(GazelleProfiles.UnitTest.class)
class GazelleUserWrapperTest {

    @Test
    void testConstructor() {
        User user = new User("userId");
        GazelleUserWrapper gazelleUserWrapper = new GazelleUserWrapper(user,"password",new String[]{"role1","role2"});

        assertEquals("userId", gazelleUserWrapper.getUser().getId());
        assertEquals("password", gazelleUserWrapper.getPassword());
        assertEquals("role1", gazelleUserWrapper.getRoles()[0]);
        assertEquals("role2", gazelleUserWrapper.getRoles()[1]);
    }

    @Test
    void testSetters() {
        GazelleUserWrapper gazelleUserWrapper = new GazelleUserWrapper(new User("userId"),"password",new String[]{"role1","role2"});
        gazelleUserWrapper.setUser(new User("userId2"));
        gazelleUserWrapper.setPassword("password2");
        gazelleUserWrapper.setRoles(new String[]{"newRole"});

        assertEquals("userId2", gazelleUserWrapper.getUser().getId());
        assertEquals("password2", gazelleUserWrapper.getPassword());
        assertEquals("newRole", gazelleUserWrapper.getRoles()[0]);
    }
}
