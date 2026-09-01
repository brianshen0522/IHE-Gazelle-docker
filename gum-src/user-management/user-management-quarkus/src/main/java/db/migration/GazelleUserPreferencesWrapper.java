package db.migration;

import net.ihe.gazelle.user.management.api.domain.user.UserPreference;

/**
 * This class is a wrapper to manipulate user preferences in the user pref migration context between gazelle and gum databases
 * This class is used for the migration V4_0_0_3__MigrateGazelleUserPreferences
 */
public class GazelleUserPreferencesWrapper {

    private UserPreference userPreference;
    private byte[] userPicture;
    private byte[] userThumbnail;
    private String spokenLangagesString;

    /**
     * Creates an empty wrapper.
     */
    public GazelleUserPreferencesWrapper() {
        // Default constructor
    }

    /**
     * Returns the user preference model.
     * @return user preference
     */
    public UserPreference getUserPreference() {
        return userPreference;
    }

    /**
     * Sets the user preference model.
     * @param user user preference
     */
    public void setUserPreference(UserPreference user) {
        this.userPreference = user;
    }

    /**
     * Returns the user profile picture bytes.
     * @return profile picture bytes
     */
    public byte[] getUserPicture() {
        return userPicture;
    }

    /**
     * Sets the user profile picture bytes.
     * @param userPicture profile picture bytes
     */
    public void setUserPhoto(byte[] userPicture) {
        this.userPicture = userPicture;
    }

    /**
     * Returns the user thumbnail bytes.
     * @return thumbnail bytes
     */
    public byte[] getUserThumbnail() {
        return userThumbnail;
    }

    /**
     * Sets the user thumbnail bytes.
     * @param userThumbnail thumbnail bytes
     */
    public void setUserPhotoThumbnail(byte[] userThumbnail) {
        this.userThumbnail = userThumbnail;
    }

    /**
     * Returns the spoken languages as a serialized string.
     * @return spoken languages string
     */
    public String getSpokenLanguagesString() {
        return spokenLangagesString;
    }

    /**
     * Sets the spoken languages string.
     * @param spokenLangagesString spoken languages string
     */
    public void setSpokenLanguagesString(String spokenLangagesString) {
        this.spokenLangagesString = spokenLangagesString;
    }
}
