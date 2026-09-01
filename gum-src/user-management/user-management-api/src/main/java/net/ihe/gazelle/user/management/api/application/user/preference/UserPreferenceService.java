package net.ihe.gazelle.user.management.api.application.user.preference;

import net.ihe.gazelle.security.business.GazelleIdentity;
import net.ihe.gazelle.user.management.api.domain.user.UserPreference;
import net.ihe.gazelle.user.management.api.interlay.user.UserPreferenceResource;

/**
 * Service used to manage user preferences
 */
public interface UserPreferenceService {

    /**
     * Get the UserPreference for a User
     *
     * @param userId          the id of the user to get the UserPreference
     * @param gazelleIdentity a GazelleIdentity that is allowed to search users
     * @return the UserPreference found
     * @throws IllegalArgumentException         if userId is null
     * @throws java.util.NoSuchElementException if user does not exist
     * @throws UserPreferenceServiceException   if no UserPreference is found
     */
    UserPreference getUserPreferenceByUserId(String userId, GazelleIdentity gazelleIdentity);

    /**
     * Update the UserPreference for a User
     *
     * @param userId          the id of the user to update the UserPreference
     * @param userPreference  the UserPreference containing the update
     * @param gazelleIdentity a GazelleIdentity that is allowed to search users
     * @return the updated UserPreference
     * @throws IllegalArgumentException         if userId or userPreference is null
     * @throws java.util.NoSuchElementException if user does not exist
     * @throws UserPreferenceServiceException   if no Preference is found or if update failed
     */
    UserPreference updateUserPreferenceByUserId(String userId, UserPreferenceResource userPreference, GazelleIdentity gazelleIdentity);

    /**
     * Gets user preference by preference name.
     *
     * @param userId          the user id
     * @param preferenceName  the preference name
     * @param gazelleIdentity a GazelleIdentity that is allowed to search users
     * @return the user preference by preference name
     * @throws IllegalArgumentException         if userId or preferenceName is null
     * @throws java.util.NoSuchElementException if user does not exist
     * @throws UserPreferenceServiceException   if fail to get preference
     */
    Object getUserPreferenceByPreferenceName(String userId, String preferenceName, GazelleIdentity gazelleIdentity);


    /**
     * Get profile picture bytes for a given user.
     *
     * @param userId          the user id
     * @param format            the format of picture (normal or thumbnail)
     * @param gazelleIdentity a GazelleIdentity that is allowed to search users
     * @return the profile picture as byte array
     * @throws IllegalArgumentException         if userId, format or gazelleIdentity is null
     * @throws IllegalArgumentException         if format is unsupported
     * @throws java.util.NoSuchElementException if user does not exist
     */
    byte[] getProfilePicture(String userId, String format, GazelleIdentity gazelleIdentity);

    /**
     * Update profile picture.
     *
     * @param userId          the user id
     * @param profilePicture  the new profile picture
     * @param gazelleIdentity a GazelleIdentity that can search user
     * @return the profile thumbnail as byte array
     * @throws IllegalArgumentException if one of the parameters is null
     * @throws java.util.NoSuchElementException if user does not exist
     */
    byte[] updateProfilePicture(String userId, byte[] profilePicture, GazelleIdentity gazelleIdentity);

    /**
     * Delete user profile picture by replacing it with the default one.
     *
     * @param userId   the user id
     * @param identity the identity with delete right
     * @return the bytes of the default picture
     * @throws IllegalArgumentException if userId or identity is null
     * @throws java.util.NoSuchElementException if the user is not found
     * @throws UserPreferenceServiceException if fail to delete the picture
     */
    byte[] deleteUserProfilePicture(String userId, GazelleIdentity identity);
}
