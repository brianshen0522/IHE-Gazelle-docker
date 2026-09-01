package net.ihe.gazelle.user.management.api.interlay.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Data Transfer Object for user preferences in Gazelle User Management.
 * <p>
 * This class encapsulates user preference information such as table label, email notification setting,
 * and spoken languages for API communication.
 * </p>
 */
@Schema(name = "UserPreferenceResource", description = "Represent a user preferences")
@JsonPropertyOrder({"userId", "tableLabel", "notifiedByEmail", "languagesSpoken"})
public class UserPreferenceResource {
    /** The user ID associated with these preferences. */
    private String userId;
    /** Custom label for table display. */
    private String tableLabel;
    /** Whether the user wants to receive email notifications. */
    private boolean notifiedByEmail;
    /** List of languages spoken by the user. */
    private List<String> languagesSpoken;

    /**
     * Default constructor.
     */
    public UserPreferenceResource() {
    }

    /**
     * Full constructor.
     * @param userId the user ID
     * @param tableLabel custom label for tables
     * @param notifiedByEmail true if email notifications are enabled
     * @param languagesSpoken list of spoken languages
     */
    public UserPreferenceResource(String userId,
                                  String tableLabel, boolean notifiedByEmail, List<String> languagesSpoken) {
        this.userId = userId;
        this.tableLabel = tableLabel;
        this.notifiedByEmail = notifiedByEmail;
        this.languagesSpoken = languagesSpoken;
    }

    /**
     * Gets the user ID.
     * @return the user ID
     */
    @Schema(
            description = "The user id the preference is linked to.",
            required = true,
            examples = {"673ce282-d335-43ff-bfb2-7113bcba2e50"}
    )
    @JsonProperty("userId")
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
     * Gets the custom label for table display.
     * @return the table label
     */
    @Schema(
            description = "The table label.",
            required = true,
            examples = {"D6"}
    )
    @JsonProperty("tableLabel")
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
    @Schema(
            description = "Whether the user wants to receive email notifications.",
            required = true,
            examples = {"false"}
    )
    @JsonProperty("notifiedByEmail")
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
    @Schema(
            description = "The list of languages spoken by the user.",
            required = true,
            examples = {"[en, fr]"}
    )
    @JsonProperty("languagesSpoken")
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

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        UserPreferenceResource that = (UserPreferenceResource) object;
        return notifiedByEmail == that.notifiedByEmail
                && Objects.equals(userId, that.userId)
                && Objects.equals(tableLabel, that.tableLabel)
                && Objects.equals(languagesSpoken, that.languagesSpoken);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId,
                tableLabel,
                notifiedByEmail,
                languagesSpoken);
    }

    @Override
    public String toString() {
        return "UserPreferenceResource{" +
                "userId='" + userId + '\'' +
                ", tableLabel='" + tableLabel + '\'' +
                ", notifiedByEmail=" + notifiedByEmail +
                ", languagesSpoken=" + languagesSpoken +
                '}';
    }

}
