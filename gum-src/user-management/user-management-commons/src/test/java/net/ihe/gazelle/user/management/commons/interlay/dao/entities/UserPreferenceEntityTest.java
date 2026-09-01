package net.ihe.gazelle.user.management.commons.interlay.dao.entities;

import net.ihe.gazelle.user.management.api.interlay.user.UserPreferenceResource;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.wildfly.common.Assert.assertFalse;
import static org.wildfly.common.Assert.assertTrue;

class UserPreferenceEntityTest {

    private static final String PROFILE_PICTURE = "profile";
    private static final String PROFILE_THUMBNAIL = "thumbnail";

    UserPreferenceResource userPreferenceResource = new UserPreferenceResource("userId",
            "table",
            true,
            List.of("fr", "en"));

    @Test
    void testConstructorAndGetter(){
       UserPreferenceEntity userPreferenceEntity = new UserPreferenceEntity(userPreferenceResource);
        UserEntity user = new UserEntity();
        user.setId("userId");
        userPreferenceEntity.setUser(user);
        userPreferenceEntity.setProfileThumbnail(PROFILE_THUMBNAIL.getBytes(StandardCharsets.UTF_8));
        userPreferenceEntity.setProfilePicture(PROFILE_PICTURE.getBytes(StandardCharsets.UTF_8));

        assertEquals(userPreferenceResource.getTableLabel(), userPreferenceEntity.getTableLabel());
        assertEquals(userPreferenceResource.isNotifiedByEmail(), userPreferenceEntity.isNotifiedByEmail());
        assertArrayEquals(PROFILE_PICTURE.getBytes(StandardCharsets.UTF_8), userPreferenceEntity.getProfilePicture());
        assertArrayEquals(PROFILE_THUMBNAIL.getBytes(StandardCharsets.UTF_8), userPreferenceEntity.getProfileThumbnail());
        assertEquals(String.join(",",userPreferenceResource.getLanguagesSpoken()), userPreferenceEntity.getLanguagesSpoken());
        assertEquals(userPreferenceResource.getUserId(), userPreferenceEntity.getUser().getId());
    }

    @Test
    void testSetters(){
        UserPreferenceEntity userPreferenceEntity = new UserPreferenceEntity(userPreferenceResource);
        UserEntity user = new UserEntity();
        user.setId("userId");
        userPreferenceEntity.setUser(user);
        userPreferenceEntity.setProfileThumbnail(PROFILE_THUMBNAIL.getBytes(StandardCharsets.UTF_8));
        userPreferenceEntity.setProfilePicture(PROFILE_PICTURE.getBytes(StandardCharsets.UTF_8));


        userPreferenceEntity.setTableLabel("otherTable");
        userPreferenceEntity.setLanguagesSpoken("fr,en");
        userPreferenceEntity.setNotifiedByEmail(false);
        userPreferenceEntity.setProfilePicture("otherProfilePicture".getBytes(StandardCharsets.UTF_8));
        userPreferenceEntity.setProfileThumbnail("otherProfileThumbnail".getBytes(StandardCharsets.UTF_8));



        assertEquals("otherTable", userPreferenceEntity.getTableLabel());
        assertEquals(false, userPreferenceEntity.isNotifiedByEmail());
        assertEquals("fr,en", userPreferenceEntity.getLanguagesSpoken());
        assertArrayEquals("otherProfilePicture".getBytes(StandardCharsets.UTF_8), userPreferenceEntity.getProfilePicture());
        assertArrayEquals("otherProfileThumbnail".getBytes(StandardCharsets.UTF_8), userPreferenceEntity.getProfileThumbnail());
    }

    @Test
    void testAsUserPreferenceResource() {
        UserEntity userEntity = new UserEntity();
        userEntity.setId("userId");
        UserPreferenceEntity userPreferenceEntity = new UserPreferenceEntity();
        userPreferenceEntity.setUser(userEntity);
        userPreferenceEntity.setProfileThumbnail(PROFILE_THUMBNAIL.getBytes(StandardCharsets.UTF_8));
        userPreferenceEntity.setProfilePicture(PROFILE_PICTURE.getBytes(StandardCharsets.UTF_8));
        userPreferenceEntity.setNotifiedByEmail(true);
        userPreferenceEntity.setLanguagesSpoken("fr,en");
        userPreferenceEntity.setTableLabel("table");

        assertEquals(userPreferenceResource, userPreferenceEntity.asUserPreferenceResource());
    }

    @Test
    void testAsUserPreferenceResourceEmpty() {
        UserPreferenceEntity userPreferenceEntity = new UserPreferenceEntity();
        userPreferenceEntity.setUser(new UserEntity());
        UserPreferenceResource userPrefResource = userPreferenceEntity.asUserPreferenceResource();
        assertFalse(userPrefResource.isNotifiedByEmail());
        assertTrue(userPrefResource.getLanguagesSpoken().isEmpty());
        assertNull(userPrefResource.getTableLabel());
    }

    @Test
    void testUserPreferenceEntityEquals() {
        EqualsVerifier.simple()
                .forClass(UserPreferenceEntity.class)
                .suppress(Warning.SURROGATE_OR_BUSINESS_KEY)
                .verify();
    }
}