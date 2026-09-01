package net.ihe.gazelle.user.management.commons.application.user.preference;

import net.ihe.gazelle.user.management.api.domain.user.User;
import net.ihe.gazelle.user.management.api.interlay.user.UserPreferenceResource;
import net.ihe.gazelle.user.management.commons.application.exception.GazelleDAOException;

import java.util.List;

public interface UserPreferenceDAO {

    /**
     * Get the UserPreference for a user
     *
     * @param userId the id of the user to get its UserPreference
     * @return the UserPreference
     * @throws java.util.NoSuchElementException if the UserPreference is not found
     */
    UserPreferenceResource getUserPreferenceByUserId(String userId);

    /**
     * Update the UserPreference for a user
     *
     * @param userId         the id of the user to update its UserPreference
     * @param userPreference the updated UserPreference
     * @return the UserPreference after the update
     * @throws GazelleDAOException if update fails
     */
    UserPreferenceResource updateUserPreferenceByUserId(String userId, UserPreferenceResource userPreference);

    /**
     * Create a new UserPreference
     *
     * @param userPreferenceResource the UserPreference to create
     * @return the created UserPreference
     * @throws GazelleDAOException if creation fails
     */
    UserPreferenceResource createUserPreference(UserPreferenceResource userPreferenceResource);


    /**
     * Gets user preference by preference name.
     *
     * @param userId         the user id
     * @param preferenceName the preference name
     * @return the user preference by preference name
     * @throws GazelleDAOException if retrieval fails
     */
    Object getUserPreferenceByPreferenceName(String userId, String preferenceName);


    /**
     * Get profile picture bytes for user id.
     *
     * @param userId the user id
     * @return the profile pictures bytes
     * @throws GazelleDAOException if retrieval fails
     */
    byte[] getProfilePictureForUserIdBytes(String userId);


    /**
     * Gets profile thumbnail for user id.
     *
     * @param userId the user id
     * @return the profile thumbnail bytes
     * @throws GazelleDAOException if retrieval fails
     */
    byte[] getProfileThumbnailForUserIdBytes(String userId);

    /**
     * Gets languages spoken for user id.
     *
     * @param userId the user id
     * @return the languages spoken for user id
     * @throws GazelleDAOException if retrieval fails
     */
    List<String> getLanguagesSpokenForUserId(String userId);


    /**
     * Update user profile picture.
     *
     * @param userId         the user id
     * @param profilePicture the new profile picture
     * @return the bytes of the updated image
     */
    byte[] updateUserProfilePicture(String userId, byte[] profilePicture);

    /**
     * Update user profile thumbnail.
     *
     * @param userId           the user id
     * @param profileThumbnail the new profile thumbnail
     * @return the bytes of the updated thumbnail
     */
    byte[] updateUserProfileThumbnail(String userId, byte[] profileThumbnail);

    /**
     * Get the user from the user id (used for authz)
     *
     * @param userId the id of the user
     * @return the organizationId
     */
    User getUserFromUserId(String userId);
}
