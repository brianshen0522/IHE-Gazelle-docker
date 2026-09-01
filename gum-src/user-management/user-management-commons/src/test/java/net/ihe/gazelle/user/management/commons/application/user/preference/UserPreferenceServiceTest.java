package net.ihe.gazelle.user.management.commons.application.user.preference;

import net.ihe.gazelle.security.business.Authz;
import net.ihe.gazelle.security.business.AuthzImpl;
import net.ihe.gazelle.security.business.UnauthorizedException;
import net.ihe.gazelle.security.technical.PermissionStoreSPIProvider;
import net.ihe.gazelle.user.management.api.application.configuration.ApplicationConfig;
import net.ihe.gazelle.user.management.api.application.user.lookup.UserLookupService;
import net.ihe.gazelle.user.management.api.application.user.preference.UserPreferenceService;
import net.ihe.gazelle.user.management.api.application.user.preference.UserPreferenceServiceException;
import net.ihe.gazelle.user.management.api.domain.user.User;
import net.ihe.gazelle.user.management.api.domain.user.UserPreference;
import net.ihe.gazelle.user.management.api.interlay.user.UserPreferenceResource;
import net.ihe.gazelle.user.management.commons.ConfigurationsMock;
import net.ihe.gazelle.user.management.commons.application.exception.GazelleDAOException;
import net.ihe.gazelle.user.management.commons.interlay.utils.MockedGazelleIdentity;
import net.ihe.gazelle.user.management.commons.interlay.utils.ScalrImageService;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static net.ihe.gazelle.user.management.api.domain.user.GazelleDefaultGroup.GAZELLE_ADMIN;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
class UserPreferenceServiceTest {

    private static final String REST_USERS = "/rest/v2/users/";
    @Mock
    UserPreferenceDAO userPreferenceDAOMock;
    UserPreferenceService userPreferenceService;
    @Mock
    UserLookupService userLookupService;

    Authz authorizationService = new AuthzImpl(new PermissionStoreSPIProvider());
    ApplicationConfig applicationConfig = new ConfigurationsMock();

    private final MockedGazelleIdentity mockedGazelleIdentity = new MockedGazelleIdentity(Set.of(GAZELLE_ADMIN.getName()));
    private final UserPreferenceResource expectedUserPreferenceResource = new UserPreferenceResource("id",
            "table", true, List.of("french"));

    @BeforeEach
    void setUp() {
        userPreferenceService = new UserPreferenceServiceImpl(userPreferenceDAOMock, applicationConfig, authorizationService, new ScalrImageService());
    }

    @Test
    void testGetUserPreference() {
        String userId = "id";
        when(userPreferenceDAOMock.getUserPreferenceByUserId(userId)).thenReturn(expectedUserPreferenceResource);
        when(userPreferenceDAOMock.getUserFromUserId(userId)).thenReturn(generateUser(userId,true));
        UserPreference actualUserPreference = userPreferenceService.getUserPreferenceByUserId(userId, mockedGazelleIdentity);
        UserPreference expectedUserPreference = new UserPreference(userId,
                getProfilePictureUri(userId),
                getProfileThumbnailUri(userId),
                "table",
                true,
                List.of("french"));
        assertEquals(expectedUserPreference, actualUserPreference);
    }

    @Test
    void testGetUserPreferenceCreateDefault() {
        String userId = "idDefault";
        UserPreferenceResource defaultUserPreferenceResource = new UserPreferenceResource(userId,
                "",
                false,
                List.of());

        UserPreference expectedUserPreference = new UserPreference(userId,
                getProfilePictureUri(defaultUserPreferenceResource.getUserId()),
                getProfileThumbnailUri(defaultUserPreferenceResource.getUserId()),
                "",
                false,
                List.of());

        when(userPreferenceDAOMock.getUserFromUserId(userId)).thenReturn(generateUser(userId,true));
        when(userPreferenceDAOMock.getUserPreferenceByUserId(userId)).thenThrow(NoSuchElementException.class);
        when(userPreferenceDAOMock.createUserPreference(defaultUserPreferenceResource)).thenReturn(defaultUserPreferenceResource);
        UserPreference actualUserPreference = userPreferenceService.getUserPreferenceByUserId(userId, mockedGazelleIdentity);

        assertEquals(expectedUserPreference, actualUserPreference);
    }


    @Test
    void testGetUserPreferenceIdNotFound() {
        assertThrows(NoSuchElementException.class, () -> userPreferenceService.getUserPreferenceByUserId("notFound", mockedGazelleIdentity));
    }

    @Test
    void testGetPreferenceUserIdNull() {
        assertThrows(IllegalArgumentException.class, () -> userPreferenceService.getUserPreferenceByUserId(null, mockedGazelleIdentity));
    }

    @Test
    void testGetUserPreferenceExceptionThrown() {
        String userId = "id";
        when(userPreferenceDAOMock.getUserFromUserId(userId)).thenReturn(generateUser(userId,true));
        when(userPreferenceDAOMock.getUserPreferenceByUserId(userId)).thenThrow(GazelleDAOException.class);
        assertThrows(UserPreferenceServiceException.class, () -> userPreferenceService.getUserPreferenceByUserId(userId, mockedGazelleIdentity));
    }

    @Test
    void testUpdatePreference() {
        String userId = "id";
        UserPreferenceResource inputUserPreferenceResource = new UserPreferenceResource(userId,
                "A38", true, List.of("fr", "en", "it"));
        UserPreferenceResource persistedUserPreferenceResource = new UserPreferenceResource(userId,
                "table", false, List.of("fr", "en"));
        UserPreferenceResource updatedUserPreferenceResource = new UserPreferenceResource(userId,
                "A38", true, List.of("fr", "en", "it"));
        UserPreference expectedUserPreference = new UserPreference(userId,
                getProfilePictureUri(userId),
                getProfileThumbnailUri(userId),
                "A38", true, List.of("fr", "en", "it"));

        when(userPreferenceDAOMock.getUserFromUserId(userId)).thenReturn(generateUser(userId,true));
        when(userPreferenceDAOMock.getUserPreferenceByUserId(userId)).thenReturn(persistedUserPreferenceResource);
        when(userPreferenceDAOMock.updateUserPreferenceByUserId(userId, inputUserPreferenceResource)).thenReturn(updatedUserPreferenceResource);
        UserPreference actualUserPreference = userPreferenceService.updateUserPreferenceByUserId(userId, inputUserPreferenceResource, mockedGazelleIdentity);

        assertEquals(expectedUserPreference, actualUserPreference);
    }

    @Test
    void testUpdatePreferenceOnDisabledUser() {
        String userId = "id";
        UserPreferenceResource inputUserPreferenceResource = new UserPreferenceResource(userId,
                "A38", true, List.of("fr", "en", "it"));

        when(userPreferenceDAOMock.getUserFromUserId(userId)).thenReturn(generateUser(userId,false));
        assertThrows(IllegalStateException.class,
                () -> userPreferenceService.updateUserPreferenceByUserId(userId, inputUserPreferenceResource, mockedGazelleIdentity));
    }


    @Test
    void testUpdateUserPreferenceWhenNotExist() {
        String userId = "noPref";
        UserPreferenceResource inputUserPreferenceResource = new UserPreferenceResource(userId,
                "table", true, List.of("fr"));

        UserPreferenceResource defaultUserPreferenceResource = new UserPreferenceResource(userId,
                null,
                false,
                List.of());

        UserPreferenceResource updatedUserPreferenceResource = new UserPreferenceResource(userId,
                "table",
                true,
                List.of("fr"));

        UserPreference expectedUserPreference = new UserPreference(userId,
                getProfilePictureUri(userId),
                getProfileThumbnailUri(userId),
                "table", true, Collections.singletonList("fr"));

        when(userPreferenceDAOMock.getUserFromUserId(userId)).thenReturn(generateUser(userId,true));
        when(userPreferenceDAOMock.getUserPreferenceByUserId(userId)).thenThrow(NoSuchElementException.class);
        when(userPreferenceDAOMock.createUserPreference(any())).thenReturn(defaultUserPreferenceResource);
        when(userPreferenceDAOMock.updateUserPreferenceByUserId(userId, inputUserPreferenceResource)).thenReturn(updatedUserPreferenceResource);

        UserPreference actualUserPreference = userPreferenceService.updateUserPreferenceByUserId(userId, inputUserPreferenceResource, mockedGazelleIdentity);

        assertEquals(expectedUserPreference, actualUserPreference);
    }

    @Test
    void testUpdatePreferenceNullParameter() {
        UserPreferenceResource userPreferenceResource = new UserPreferenceResource("", "", true, Collections.emptyList());
        assertThrows(IllegalArgumentException.class,
                () -> userPreferenceService.updateUserPreferenceByUserId(null,userPreferenceResource,mockedGazelleIdentity));
        assertThrows(IllegalArgumentException.class,
                () -> userPreferenceService.updateUserPreferenceByUserId("id", null, mockedGazelleIdentity));
    }

    @Test
    void testUpdateUserPreferenceExceptionThrown() {
        String userId = "userId";
        when(userPreferenceDAOMock.getUserFromUserId(userId)).thenReturn(generateUser(userId,true));
        UserPreferenceResource userPreferenceResource = new UserPreferenceResource();
        assertThrows(UserPreferenceServiceException.class, () -> userPreferenceService.updateUserPreferenceByUserId(userId, userPreferenceResource, mockedGazelleIdentity));
    }

    @Test
    void testGetUserPreferenceByPreferenceName() {
        String userId = expectedUserPreferenceResource.getUserId();
        when(userPreferenceDAOMock.getUserFromUserId(userId)).thenReturn(generateUser(userId,true));
        when(userPreferenceDAOMock.getUserPreferenceByUserId(userId)).thenReturn(expectedUserPreferenceResource);
        when(userPreferenceDAOMock.getUserPreferenceByPreferenceName(userId, "table_label")).thenReturn(expectedUserPreferenceResource.getTableLabel());
        when(userPreferenceDAOMock.getUserPreferenceByPreferenceName(userId, "notified_by_email")).thenReturn(expectedUserPreferenceResource.isNotifiedByEmail());
        when(userPreferenceDAOMock.getLanguagesSpokenForUserId(userId)).thenReturn(expectedUserPreferenceResource.getLanguagesSpoken());
        //This is to mock the fact that the preference exists


        assertEquals(expectedUserPreferenceResource.getTableLabel(), userPreferenceService.getUserPreferenceByPreferenceName(userId, "table_label", mockedGazelleIdentity));
        assertEquals(expectedUserPreferenceResource.isNotifiedByEmail(), userPreferenceService.getUserPreferenceByPreferenceName(userId, "notified_by_email", mockedGazelleIdentity));
        assertEquals(expectedUserPreferenceResource.getLanguagesSpoken(), userPreferenceService.getUserPreferenceByPreferenceName(userId, UserPreferenceServiceImpl.LANGUAGES_SPOKEN, mockedGazelleIdentity));
    }

    @Test
    void testGetUserPreferencesImages() {
        String userId = expectedUserPreferenceResource.getUserId();
        when(userPreferenceDAOMock.getUserFromUserId(userId)).thenReturn(generateUser(userId,true));
        when(userPreferenceDAOMock.getUserPreferenceByUserId(userId)).thenReturn(expectedUserPreferenceResource);

        byte[] decodedProfilePicture = "profilePictureBytes".getBytes(StandardCharsets.UTF_8);
        when(userPreferenceDAOMock.getProfilePictureForUserIdBytes(userId)).thenReturn(decodedProfilePicture);
        assertArrayEquals(decodedProfilePicture, userPreferenceService.getProfilePicture(userId, "normal", mockedGazelleIdentity));

        byte[] decodedProfileThumbnail = "profileThumbnailBytes".getBytes(StandardCharsets.UTF_8);
        when(userPreferenceDAOMock.getProfileThumbnailForUserIdBytes(userId)).thenReturn(decodedProfileThumbnail);
        assertArrayEquals(decodedProfileThumbnail, userPreferenceService.getProfilePicture(userId, "thumbnail", mockedGazelleIdentity));
    }

    @Test
    void testGetUserPreferencesDefaultImages() {
        String userId = expectedUserPreferenceResource.getUserId();
        when(userPreferenceDAOMock.getUserFromUserId(userId)).thenReturn(generateUser(userId,true));
        when(userPreferenceDAOMock.getUserPreferenceByUserId(userId)).thenReturn(expectedUserPreferenceResource);

        byte[] expectedPicture = getImageEncodedBytes("src/test/resources/images/default_img.jpg");
        when(userPreferenceDAOMock.getProfilePictureForUserIdBytes(userId)).thenReturn(new byte[0]);
        assertArrayEquals(expectedPicture, userPreferenceService.getProfilePicture(userId, "normal", mockedGazelleIdentity));

        byte[] expectedThumbnail = getImageEncodedBytes("src/test/resources/images/default_thumbnail.jpg");
        when(userPreferenceDAOMock.getProfileThumbnailForUserIdBytes(userId)).thenReturn(new byte[0]);
        assertArrayEquals(expectedThumbnail, userPreferenceService.getProfilePicture(userId, "thumbnail", mockedGazelleIdentity));
    }

    @Test
    void testGetUserPreferencesImagesBadQueryParam() {
        String userId = expectedUserPreferenceResource.getUserId();

        assertThrows(IllegalArgumentException.class,
                () -> userPreferenceService.getProfilePicture(userId, "small", mockedGazelleIdentity));
    }

    @Test
    void testGetUserPreferenceByPreferenceNameNullParameter() {
        assertThrows(IllegalArgumentException.class,
                () -> userPreferenceService.getUserPreferenceByPreferenceName(null, "name", mockedGazelleIdentity));
        assertThrows(IllegalArgumentException.class,
                () -> userPreferenceService.getUserPreferenceByPreferenceName("id", null, mockedGazelleIdentity));
    }

    @Test
    void testGetUserPreferenceByPreferenceNameNotFound() {
        assertThrows(NoSuchElementException.class,
                () -> userPreferenceService.getUserPreferenceByPreferenceName("notFound", "name", mockedGazelleIdentity));
    }

    @Test
    void testGetUserPreferenceByPreferenceNameExceptionThrown() {
        when(userPreferenceDAOMock.getUserFromUserId("userId")).thenReturn(generateUser("userId",true));
        assertThrows(UserPreferenceServiceException.class,
                () -> userPreferenceService.getUserPreferenceByPreferenceName("userId", "table_label", mockedGazelleIdentity));
    }

    @Test
    void testGetPictureNullParameter() {
        assertThrows(IllegalArgumentException.class,
                () -> userPreferenceService.getProfilePicture(null, "name", mockedGazelleIdentity));
        assertThrows(IllegalArgumentException.class,
                () -> userPreferenceService.getProfilePicture("id", null, mockedGazelleIdentity));
    }

    @Test
    void testUpdateUserProfilePicture() {
        String userId = expectedUserPreferenceResource.getUserId();

        byte[] imageEncodedBytes = getImageEncodedBytes("src/test/resources/images/img.jpg");
        when(userPreferenceDAOMock.getUserFromUserId(userId)).thenReturn(generateUser(userId,true));
        when(userPreferenceDAOMock.getUserPreferenceByUserId(userId)).thenReturn(expectedUserPreferenceResource);

        byte[] imageRotatedBytes = getImageEncodedBytes("src/test/resources/images/img_rotated.jpg");
        when(userPreferenceDAOMock.updateUserProfilePicture(userId, imageRotatedBytes)).thenReturn(imageRotatedBytes);

        byte[] updatedProfilePicture = userPreferenceService.updateProfilePicture(userId, imageRotatedBytes, mockedGazelleIdentity);
        assertFalse(Arrays.equals(imageEncodedBytes, updatedProfilePicture));
        assertArrayEquals(imageRotatedBytes, updatedProfilePicture);

        byte[] thumbnailRotatedBytes = getImageEncodedBytes("src/test/resources/images/thumbnail_rotated.jpg");
        when(userPreferenceDAOMock.getProfileThumbnailForUserIdBytes(userId)).thenReturn(thumbnailRotatedBytes);
        assertArrayEquals(thumbnailRotatedBytes, userPreferenceService.getProfilePicture(userId, "thumbnail", mockedGazelleIdentity));
    }

    @Test
    void testUpdateDisabledUserProfilePicture() {
        String userId = expectedUserPreferenceResource.getUserId();
        byte[] imageEncodedBytes = new byte[1];
        when(userPreferenceDAOMock.getUserFromUserId(userId)).thenReturn(generateUser(userId,false));
        when(userPreferenceDAOMock.getUserPreferenceByUserId(userId)).thenReturn(expectedUserPreferenceResource);

        assertThrows(IllegalStateException.class,
                () -> userPreferenceService.updateProfilePicture(userId, imageEncodedBytes, mockedGazelleIdentity));

    }

    @Test
    void testUpdateUserProfilePictureNullParameter() {
        assertThrows(IllegalArgumentException.class,
                () -> userPreferenceService.updateProfilePicture(null, new byte[128], mockedGazelleIdentity));
        assertThrows(IllegalArgumentException.class,
                () -> userPreferenceService.updateProfilePicture("id", null, mockedGazelleIdentity));
    }

    @Test
    void testDeleteUserProfilePicture() {
        String userId = "userId";
        when(userPreferenceDAOMock.getUserFromUserId(userId)).thenReturn(generateUser(userId,true));
        when(userPreferenceDAOMock.updateUserProfilePicture(userId, new byte[0])).thenReturn(new byte[0]);
        when(userPreferenceDAOMock.updateUserProfileThumbnail(userId, new byte[0])).thenReturn(new byte[0]);

        byte[] actualProfilePicture = userPreferenceService.deleteUserProfilePicture(userId, mockedGazelleIdentity);
        assertArrayEquals(new byte[0], actualProfilePicture);
    }

    @Test
    void testDeleteUserProfilePictureOfDisabledUser() {
        String userId = "userId";
        when(userPreferenceDAOMock.getUserFromUserId(userId)).thenReturn(generateUser(userId,false));

        assertThrows(IllegalStateException.class, () ->
                userPreferenceService.deleteUserProfilePicture(userId, mockedGazelleIdentity));
    }

    @Test
    void testDeleteUserProfilePictureNullParameter() {
        assertThrows(IllegalArgumentException.class,
                () -> userPreferenceService.deleteUserProfilePicture(null, mockedGazelleIdentity));
        assertThrows(UnauthorizedException.class,
                () -> userPreferenceService.deleteUserProfilePicture("user", null));
    }


    @Test
    void testDeleteUserProfilePictureUserNotFoundException() {
        assertThrows(NoSuchElementException.class,
                () -> userPreferenceService.deleteUserProfilePicture("notFound", mockedGazelleIdentity));
    }

    private byte[] getImageEncodedBytes(String pathname) {
        try {
            return FileUtils.readFileToByteArray(new File(pathname));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String getProfileThumbnailUri(String userId) {
        return applicationConfig.getGUMBaseUrl() + REST_USERS + userId + "/preferences/picture?format=thumbnail";
    }

    private String getProfilePictureUri(String userId) {
        return applicationConfig.getGUMBaseUrl() + REST_USERS + userId + "/preferences/picture?format=normal";
    }

    private User generateUser(String userId, boolean activated) {
        User user = new User();
        user.setId(userId);
        user.setActivated(activated);
        return user;
    }
}
