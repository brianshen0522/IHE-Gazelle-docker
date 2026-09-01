package net.ihe.gazelle.user.management.commons.application.user.preference;

import net.ihe.gazelle.security.business.Authz;
import net.ihe.gazelle.security.business.GazelleIdentity;
import net.ihe.gazelle.user.management.api.application.configuration.ApplicationConfig;
import net.ihe.gazelle.user.management.api.application.user.preference.UserPreferenceService;
import net.ihe.gazelle.user.management.api.application.user.preference.UserPreferenceServiceException;
import net.ihe.gazelle.user.management.api.domain.user.User;
import net.ihe.gazelle.user.management.api.domain.user.UserPreference;
import net.ihe.gazelle.user.management.api.interlay.user.UserPreferenceResource;
import net.ihe.gazelle.user.management.commons.application.exception.ErrorMessage;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.NoSuchElementException;

import static net.ihe.gazelle.user.management.commons.application.permission.GUMPermissionStore.*;

public class UserPreferenceServiceImpl implements UserPreferenceService {

    private static final String DEFAULT_PROFILE_PICTURE_PATH = "src/main/resources/images/default_picture.jpg";
    private static final String DEFAULT_PROFILE_THUMBNAIL_PATH = "src/main/resources/images/default_thumbnail.jpg";
    public static final String LANGUAGES_SPOKEN = "languagesSpoken";
    private static final String REST_USERS = "/rest/v2/users/";
    public static final String ERROR_GETTING_USER_PREFERENCES = "Error getting user preferences with user id %s";
    public static final String CANNOT_BE_NULL = " cannot be null";
    public static final String PICTURE_TYPE_NORMAL = "normal";
    public static final String PICTURE_TYPE_THUMBNAIL = "thumbnail";
    public static final String USER_ID = "User id";
    private final UserPreferenceDAO userPreferenceDAO;
    private final Authz authzService;
    private final ApplicationConfig applicationConfig;
    private final ImageTransformationService imageTransformationService;

    public UserPreferenceServiceImpl(UserPreferenceDAO userPreferenceDAO, ApplicationConfig applicationConfig, Authz authorizationService, ImageTransformationService imageTransformationService) {
        this.userPreferenceDAO = userPreferenceDAO;
        this.applicationConfig = applicationConfig;
        this.authzService = authorizationService;
        this.imageTransformationService = imageTransformationService;
    }

    @Override
    public UserPreference getUserPreferenceByUserId(String userId, GazelleIdentity identity) {
        User existingUser = userPreferenceDAO.getUserFromUserId(userId);
        authzService.assertAuthorized(identity, ALL_USER_READ, userId, getUserOrga(existingUser));
        if (userId == null)
            throw new IllegalArgumentException("userId cannot be null");
        if (existingUser == null)
            throw new NoSuchElementException(ErrorMessage.USER_NOT_FOUND.getMessage());

        try {
            return convertToUserPreference(userPreferenceDAO.getUserPreferenceByUserId(userId));
        } catch (NoSuchElementException _) {
            return generateDefaultUserPreferences(userId);
        } catch (Exception e) {
            throw new UserPreferenceServiceException(String.format(ERROR_GETTING_USER_PREFERENCES, userId), e);
        }
    }

    private UserPreference generateDefaultUserPreferences(String userId) {
        UserPreferenceResource defaultUserPreferenceResource = new UserPreferenceResource(userId,
                "", false, List.of());
        return convertToUserPreference(userPreferenceDAO.createUserPreference(defaultUserPreferenceResource));
    }

    @Override
    public UserPreference updateUserPreferenceByUserId(String userId, UserPreferenceResource userPreferenceResource, GazelleIdentity identity) {
        User existingUser = userPreferenceDAO.getUserFromUserId(userId);
        authzService.assertAuthorized(identity, USER_UPDATE, userId, getUserOrga(existingUser));
        if (userId == null || userPreferenceResource == null)
            throw new IllegalArgumentException((userId == null ? "userId" : "preference") + CANNOT_BE_NULL);
        if (existingUser == null)
            throw new NoSuchElementException(ErrorMessage.USER_NOT_FOUND.getMessage());

        assertUserIsActivated(existingUser);

        try {
            //if there is no preferences to update, default ones are generated before update
            getUserPreferenceByUserId(userId, identity);

            return convertToUserPreference(userPreferenceDAO.updateUserPreferenceByUserId(userId, userPreferenceResource));
        } catch (Exception e) {
            throw new UserPreferenceServiceException(String.format("Failed to update preference with user id %s", userId), e);
        }
    }

    @Override
    public Object getUserPreferenceByPreferenceName(String userId, String preferenceName, GazelleIdentity identity) {
        User existingUser = userPreferenceDAO.getUserFromUserId(userId);
        authzService.assertAuthorized(identity, ALL_USER_READ, userId, getUserOrga(existingUser));
        if (userId == null || preferenceName == null) {
            throw new IllegalArgumentException((userId == null ? "userId" : "preferenceName") + CANNOT_BE_NULL);
        }
        try {
            //if no preferences are found default ones will be created
            getUserPreferenceByUserId(userId, identity);

            if (LANGUAGES_SPOKEN.equals(preferenceName)) {
                return userPreferenceDAO.getLanguagesSpokenForUserId(userId);
            }
            return userPreferenceDAO.getUserPreferenceByPreferenceName(userId, preferenceName);
        } catch (NoSuchElementException e) {
            throw e;
        } catch (Exception e) {
            throw new UserPreferenceServiceException(String.format(ERROR_GETTING_USER_PREFERENCES, userId), e);
        }
    }

    @Override
    public byte[] getProfilePicture(String userId, String format, GazelleIdentity identity) {
        User existingUser = userPreferenceDAO.getUserFromUserId(userId);
        authzService.assertAuthorized(identity, ALL_USER_READ, userId, getUserOrga(existingUser));
        if (userId == null || format == null)
            throw new IllegalArgumentException((userId == null ? USER_ID : "Query parameter format") + CANNOT_BE_NULL);
        if (!List.of(PICTURE_TYPE_NORMAL, PICTURE_TYPE_THUMBNAIL).contains(format))
            throw new IllegalArgumentException("Bad profile image format: " + format);
        //if no preferences are found default ones will be created
        getUserPreferenceByUserId(userId, identity);

        if (PICTURE_TYPE_NORMAL.equals(format)) {
            byte[] userPicture = userPreferenceDAO.getProfilePictureForUserIdBytes(userId);
            if (userPicture != null && userPicture.length > 0)
                return userPicture;
            else return getDefaultProfilePictureBytes();
        } else if (PICTURE_TYPE_THUMBNAIL.equals(format)) {
            byte[] userPicture = userPreferenceDAO.getProfileThumbnailForUserIdBytes(userId);
            if (userPicture != null && userPicture.length > 0)
                return userPicture;
            else return getDefaultProfileThumbnailBytes();
        }
        throw new UserPreferenceServiceException(String.format("Invalid parameter format %s", format));
    }

    @Override
    public byte[] updateProfilePicture(String userId, byte[] profilePicture, GazelleIdentity identity) {
        User existingUser = userPreferenceDAO.getUserFromUserId(userId);
        authzService.assertAuthorized(identity, USER_UPDATE, userId, getUserOrga(existingUser));
        if (userId == null || profilePicture == null)
            throw new IllegalArgumentException((userId == null ? USER_ID : "Picture") + CANNOT_BE_NULL);
        if (existingUser == null)
            throw new NoSuchElementException(ErrorMessage.USER_NOT_FOUND.getMessage());

        // Will create default preferences if not exist
        getUserPreferenceByUserId(userId, identity);
        assertUserIsActivated(existingUser);

        byte[] transformedImage = imageTransformationService.transformImageToJpeg(profilePicture);
        byte[] updatedImage = userPreferenceDAO.updateUserProfilePicture(userId, transformedImage);
        userPreferenceDAO.updateUserProfileThumbnail(userId, imageTransformationService.generateThumbnail(transformedImage));
        return updatedImage;
    }

    @Override
    public byte[] deleteUserProfilePicture(String userId, GazelleIdentity identity) {
        User existingUser = userPreferenceDAO.getUserFromUserId(userId);
        authzService.assertAuthorized(identity, USER_PREFERENCES_DELETE, userId, getUserOrga(existingUser));
        if (userId == null)
            throw new IllegalArgumentException(USER_ID + CANNOT_BE_NULL);
        if (existingUser == null)
            throw new NoSuchElementException(ErrorMessage.USER_NOT_FOUND.getMessage());

        assertUserIsActivated(existingUser);
        try {
            byte[] emptyUserImage = new byte[0];
            userPreferenceDAO.updateUserProfilePicture(userId, emptyUserImage);
            userPreferenceDAO.updateUserProfileThumbnail(userId, emptyUserImage);
            return emptyUserImage;
        } catch (Exception e) {
            throw new UserPreferenceServiceException(String.format(ERROR_GETTING_USER_PREFERENCES, userId), e);
        }
    }

    /**
     * Get the default user profile picture
     *
     * @return an array of bytes representing the picture
     */
    private byte[] getDefaultProfilePictureBytes() {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("images/default_picture.jpg")) {
            if (inputStream == null)
                throw new UserPreferenceServiceException("Default profile picture not found");
            return inputStream.readAllBytes();
        } catch (IOException e) {
            String message = "Failed to retrieve default profile picture located at " + DEFAULT_PROFILE_PICTURE_PATH;
            throw new UserPreferenceServiceException(message, e);
        }
    }

    /**
     * Get the default user profile thumbnail
     *
     * @return an array of bytes representing the picture
     */
    private byte[] getDefaultProfileThumbnailBytes() {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("images/default_thumbnail.jpg")) {
            if (inputStream == null)
                throw new UserPreferenceServiceException("Default profile picture not found");
            return inputStream.readAllBytes();
        } catch (IOException e) {
            String message = "Failed to retrieve default profile picture located at " + DEFAULT_PROFILE_THUMBNAIL_PATH;
            throw new UserPreferenceServiceException(message, e);
        }
    }

    /**
     * Verify that the given existing user is activated
     *
     * @param existingUser the existing user
     * @throws NoSuchElementException if user is not found
     */
    private void assertUserIsActivated(User existingUser) {
        if (Boolean.FALSE.equals(existingUser.isActivated()))
            throw new IllegalStateException("User " + existingUser.getId() + " is not activated");
    }

    private UserPreference convertToUserPreference(UserPreferenceResource userPreferenceResource) {
        UserPreference userPreference = new UserPreference();
        userPreference.setUserId(userPreferenceResource.getUserId());
        userPreference.setLanguagesSpoken(userPreferenceResource.getLanguagesSpoken());
        userPreference.setNotifiedByEmail(userPreferenceResource.isNotifiedByEmail());
        userPreference.setTableLabel(userPreferenceResource.getTableLabel());
        userPreference.setProfilePictureUri(getPictureUriWithQueryParam(userPreferenceResource.getUserId(), PICTURE_TYPE_NORMAL));
        userPreference.setProfileThumbnailUri(getPictureUriWithQueryParam(userPreferenceResource.getUserId(), PICTURE_TYPE_THUMBNAIL));
        return userPreference;
    }

    private String getPictureUriWithQueryParam(String userId, String queryParam) {
        return applicationConfig.getGUMBaseUrl() + REST_USERS + userId + "/preferences/picture?format=" + queryParam;
    }

    private static Object getUserOrga(User existingUser) {
        return existingUser != null ? existingUser.getOrganizationId() : null;
    }
}
