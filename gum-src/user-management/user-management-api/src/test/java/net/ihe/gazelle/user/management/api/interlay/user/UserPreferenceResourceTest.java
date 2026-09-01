package net.ihe.gazelle.user.management.api.interlay.user;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserPreferenceResourceTest {

    @Test
    void constructorAndGettersTest() {
        UserPreferenceResource resource = new UserPreferenceResource("userId","table",true, List.of("de","en"));
        assertEquals("userId", resource.getUserId());
        assertEquals("table", resource.getTableLabel());
        assertTrue(resource.isNotifiedByEmail());
        assertEquals(List.of("de","en"), resource.getLanguagesSpoken() );
    }

    @Test
    void setterTests(){
        UserPreferenceResource resource = new UserPreferenceResource("userId","table",true, List.of("de","en"));
        resource.setUserId("otherUserId");
        resource.setTableLabel("otherTableLabel");
        resource.setNotifiedByEmail(false);
        resource.setLanguagesSpoken(List.of("it","fr"));

        assertEquals("otherUserId", resource.getUserId());
        assertEquals("otherTableLabel", resource.getTableLabel());
        assertFalse(resource.isNotifiedByEmail());
        assertEquals(List.of("it","fr"), resource.getLanguagesSpoken() );
    }

    @Test
    void testEquals() {
        EqualsVerifier.simple().forClass(UserPreferenceResource.class).verify();
    }
}