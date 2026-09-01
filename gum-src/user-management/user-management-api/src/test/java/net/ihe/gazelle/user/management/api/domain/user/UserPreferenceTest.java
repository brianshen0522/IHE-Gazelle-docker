package net.ihe.gazelle.user.management.api.domain.user;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserPreferenceTest {

    @Test
    void constructorAndGetterTest() {
        UserPreference userPreference = new UserPreference("userId","pictureUri","thumbnailUri","label",true, List.of("fr"));
        assertEquals("userId", userPreference.getUserId());
        assertEquals("pictureUri",userPreference.getProfilePictureUri());
        assertEquals("thumbnailUri", userPreference.getProfileThumbnailUri());
        assertEquals("label",userPreference.getTableLabel());
        assertTrue(userPreference.isNotifiedByEmail());
        assertEquals(List.of("fr"), userPreference.getLanguagesSpoken());
    }

    @Test
    void setterTest() {
        UserPreference userPreference = new UserPreference("userId","pictureUri","thumbnailUri","label",true, List.of("fr"));
        userPreference.setUserId("otherUserId");
        userPreference.setProfilePictureUri("otherProfilePictureUri");
        userPreference.setProfileThumbnailUri("otherProfileThumbnailUri");
        userPreference.setTableLabel("otherTableLabel");
        userPreference.setNotifiedByEmail(false);
        userPreference.setLanguagesSpoken(List.of("en"));



        assertEquals("otherUserId", userPreference.getUserId());
        assertEquals("otherProfilePictureUri",userPreference.getProfilePictureUri());
        assertEquals("otherProfileThumbnailUri", userPreference.getProfileThumbnailUri());
        assertEquals("otherTableLabel",userPreference.getTableLabel());
        assertFalse(userPreference.isNotifiedByEmail());
        assertEquals(List.of("en"), userPreference.getLanguagesSpoken());
    }


    @Test
    void equalsTest(){
        EqualsVerifier.simple().forClass(UserPreference.class).verify();
    }

}