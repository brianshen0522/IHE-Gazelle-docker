package net.ihe.gazelle.user.management.quarkus.interlay.dao;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import net.ihe.gazelle.user.management.api.domain.user.UserPreference;
import net.ihe.gazelle.user.management.api.interlay.user.UserPreferenceResource;
import net.ihe.gazelle.user.management.commons.application.user.preference.UserPreferenceDAO;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for UserPreferenceDAO.
 * <p>
 * See src/it/resources/db/migration/R__Init_db_test_values_UserPreferenceDAOIT.sql to know what data is available
 * or to add data for this test.
 */
@QuarkusTest
@TestTransaction
class UserPreferenceDAOIT {

    @Inject
    UserPreferenceDAO userPreferenceDAO;
    private final byte[] imageEncodedBytes = getImageEncodedBytes("src/it/resources/images/img.jpg");
    private final byte[] thumbnailEncodedBytes = getImageEncodedBytes("src/it/resources/images/thumbnail.jpg");

    private final UserPreference persistedUserPreference = new UserPreference("150",
            "path/to/profile/picture",
            "path/to/profile/thumbnail",
            "table",
            true,
            List.of("fr"));
    private final UserPreferenceResource persistedUserPreferenceResource = new UserPreferenceResource("150",
            "table",
            true,
            List.of("fr"));


    @Test
    void getUserPreference() {
        UserPreferenceResource actualUserPreference = userPreferenceDAO.getUserPreferenceByUserId("150");
        assertEquals(persistedUserPreferenceResource, actualUserPreference);
    }

    @Test
    void getUserPreferenceNotFound() {
        assertThrows(NoSuchElementException.class, () -> userPreferenceDAO.getUserPreferenceByUserId("uid"));
    }

    @Test
    void updateUserPreferencesUserNotFound() {
        UserPreferenceResource userPreferenceResource =
                new UserPreferenceResource("151",
                        "round_table",
                        false,
                        List.of("fr", "en", "it"));
        UserPreferenceResource actual = userPreferenceDAO.updateUserPreferenceByUserId("151", userPreferenceResource);
        assertNotNull(actual);
        assertNotEquals(persistedUserPreferenceResource, actual);
    }

    @Test
    void updateUserPictureForUserId() {
        byte[] updatedUserProfilePicture = userPreferenceDAO.updateUserProfilePicture("151", imageEncodedBytes);
        assertNotNull(updatedUserProfilePicture);
        assertArrayEquals(imageEncodedBytes, updatedUserProfilePicture);

        updatedUserProfilePicture = userPreferenceDAO.updateUserProfileThumbnail("151", thumbnailEncodedBytes);
        assertNotNull(updatedUserProfilePicture);
        assertArrayEquals(thumbnailEncodedBytes, updatedUserProfilePicture);
    }

    @Test
    void updateEmptyUserPictureForUserId() {
        byte[] updatedUserProfilePicture = userPreferenceDAO.updateUserProfilePicture("151", new byte[0]);
        assertNotNull(updatedUserProfilePicture);
        assertEquals(0, updatedUserProfilePicture.length);

        updatedUserProfilePicture = userPreferenceDAO.updateUserProfileThumbnail("151", new byte[0]);
        assertNotNull(updatedUserProfilePicture);
        assertEquals(0, updatedUserProfilePicture.length);
    }

    @Test
    void createUserPreference() {
        UserPreferenceResource expectedUserPreferenceResource = new UserPreferenceResource("1024",
                "table", true, Collections.singletonList("en"));
        UserPreferenceResource createdUserPreference = userPreferenceDAO.createUserPreference(expectedUserPreferenceResource);

        assertEquals(expectedUserPreferenceResource, createdUserPreference);
    }

    @Test
    void getUserPreferenceByPreferenceName() {
        String userId = persistedUserPreference.getUserId();

        assertEquals(persistedUserPreference.getTableLabel(), userPreferenceDAO.getUserPreferenceByPreferenceName(userId, "tableLabel"));
        assertEquals(persistedUserPreference.isNotifiedByEmail(), userPreferenceDAO.getUserPreferenceByPreferenceName(userId, "notifiedByEmail"));
    }

    @Test
    void getUserProfilePictureByUserId() {
        String userId = persistedUserPreference.getUserId();
        assertArrayEquals(imageEncodedBytes, userPreferenceDAO.getProfilePictureForUserIdBytes(userId));
    }

    @Test
    void getUserProfileThumbnailByUserId() {
        String userId = persistedUserPreference.getUserId();
        assertArrayEquals(thumbnailEncodedBytes, userPreferenceDAO.getProfileThumbnailForUserIdBytes(userId));
    }


    @Test
    void getLanguagesSpokenForUserId() {
        String userId = persistedUserPreference.getUserId();
        assertEquals(persistedUserPreference.getLanguagesSpoken(), userPreferenceDAO.getLanguagesSpokenForUserId(userId));
    }

    @Test
    void getUserFromUserId() {
        assertNull(userPreferenceDAO.getUserFromUserId("nonExistingUser"));
        String userId = persistedUserPreference.getUserId();
        assertEquals("orgaId", userPreferenceDAO.getUserFromUserId(userId).getOrganizationId());
    }

    private byte[] getImageEncodedBytes(String pathname) {
        try {
            return FileUtils.readFileToByteArray(new File(pathname));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
