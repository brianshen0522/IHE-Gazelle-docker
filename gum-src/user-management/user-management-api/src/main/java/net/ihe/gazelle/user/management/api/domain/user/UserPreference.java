package net.ihe.gazelle.user.management.api.domain.user;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents user preferences in the Gazelle User Management system.
 * <p>
 * This class encapsulates customization information such as profile picture,
 * language, notification settings, and other display and communication preferences.
 * </p>
 */
public class UserPreference {

    /** The user ID associated with these preferences. */
    private String userId;
    /** URI of the user's profile picture. */
    private String profilePictureUri;
    /** URI of the user's profile thumbnail. */
    private String profileThumbnailUri;
    /** Custom label for table display. */
    private String tableLabel;
    /** Whether the user wants to receive email notifications. */
    private boolean notifiedByEmail;
    /** List of languages spoken by the user. */
    private List<String> languagesSpoken;

    /**
     * Default constructor (required for CDI and deserialization).
     */
    public UserPreference() {
        // Default constructor for CDI
    }

    /**
     * Full constructor.
     *
     * @param userId the user ID
     * @param profilePictureUri URI of the profile picture
     * @param profileThumbnailUri URI of the profile thumbnail
     * @param tableLabel custom label for tables
     * @param notifiedByEmail true if email notifications are enabled
     * @param languagesSpoken list of spoken languages
     */
    public UserPreference(String userId, String profilePictureUri, String profileThumbnailUri, String tableLabel, boolean notifiedByEmail, List<String> languagesSpoken) {
        this.userId = userId;
        this.profilePictureUri = profilePictureUri;
        this.profileThumbnailUri = profileThumbnailUri;
        this.tableLabel = tableLabel;
        this.notifiedByEmail = notifiedByEmail;
        this.languagesSpoken = languagesSpoken == null
                ? new ArrayList<>()
                : new ArrayList<>(languagesSpoken);
    }

    /**
     * Gets the user ID.
     * @return the user ID
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Sets the user ID.
     * @param userId the user ID
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Gets the URI of the profile picture.
     * @return the profile picture URI
     */
    public String getProfilePictureUri() {
        return profilePictureUri;
    }

    /**
     * Sets the URI of the profile picture.
     * @param profilePictureUri the profile picture URI
     */
    public void setProfilePictureUri(String profilePictureUri) {
        this.profilePictureUri = profilePictureUri;
    }

    /**
     * Gets the URI of the profile thumbnail.
     * @return the profile thumbnail URI
     */
    public String getProfileThumbnailUri() {
        return profileThumbnailUri;
    }

    /**
     * Sets the URI of the profile thumbnail.
     * @param profileThumbnailUri the profile thumbnail URI
     */
    public void setProfileThumbnailUri(String profileThumbnailUri) {
        this.profileThumbnailUri = profileThumbnailUri;
    }

    /**
     * Gets the custom label for table display.
     * @return the table label
     */
    public String getTableLabel() {
        return tableLabel;
    }

    /**
     * Sets the custom label for table display.
     * @param tableLabel the table label
     */
    public void setTableLabel(String tableLabel) {
        this.tableLabel = tableLabel;
    }

    /**
     * Returns whether the user wants to receive email notifications.
     * @return true if email notifications are enabled, false otherwise
     */
    public boolean isNotifiedByEmail() {
        return notifiedByEmail;
    }

    /**
     * Sets the email notification preference.
     * @param notifiedByEmail true to enable email notifications, false to disable
     */
    public void setNotifiedByEmail(boolean notifiedByEmail) {
        this.notifiedByEmail = notifiedByEmail;
    }

    /**
     * Gets the list of languages spoken by the user.
     * @return the list of spoken languages
     */
    public List<String> getLanguagesSpoken() {
        return new ArrayList<>(languagesSpoken);
    }

    /**
     * Sets the list of languages spoken by the user.
     * @param languagesSpoken the list of spoken languages
     */
    public void setLanguagesSpoken(List<String> languagesSpoken) {
        this.languagesSpoken = languagesSpoken == null
                ? new ArrayList<>()
                : new ArrayList<>(languagesSpoken);
    }

    /**
     * Checks equality with another object.
     * @param object the object to compare
     * @return true if equal, false otherwise
     */
    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        UserPreference that = (UserPreference) object;
        return notifiedByEmail == that.notifiedByEmail
                && Objects.equals(userId, that.userId)
                && Objects.equals(profilePictureUri, that.profilePictureUri)
                && Objects.equals(profileThumbnailUri, that.profileThumbnailUri)
                && Objects.equals(tableLabel, that.tableLabel)
                && Objects.equals(languagesSpoken, that.languagesSpoken);
    }

    /**
     * Computes the hash code for this user preference.
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(userId,
                profilePictureUri,
                profileThumbnailUri,
                tableLabel,
                notifiedByEmail,
                languagesSpoken);
    }

    /**
     * Returns a string representation of the user preference.
     * @return string representation
     */
    @Override
    public String toString() {
        return "UserPreference{" +
                "userId='" + userId + '\'' +
                ", profilePictureUri='" + profilePictureUri + '\'' +
                ", profileThumbnailUri='" + profileThumbnailUri + '\'' +
                ", tableLabel='" + tableLabel + '\'' +
                ", notifiedByEmail=" + notifiedByEmail +
                ", languagesSpoken=" + languagesSpoken +
                '}';
    }
}
