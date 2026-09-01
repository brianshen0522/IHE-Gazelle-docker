package net.ihe.gazelle.user.management.api.interlay.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import net.ihe.gazelle.modelmarshaller.technical.dto.DTO;
import net.ihe.gazelle.user.management.api.domain.user.DelegatedUser;
import net.ihe.gazelle.user.management.api.domain.user.User;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Data Transfer Object for user data in Gazelle User Management.
 * <p>
 * This class encapsulates user information for API communication, including identity, group membership,
 * organization, activation status, login statistics, and delegation details.
 * </p>
 */
@Schema(name = "UserResource", description = "Represent a user")
@JsonPropertyOrder({"id", "firstName", "lastName", "email", "groupIds", "organizationId", "activated",
        "lastLoginTimestamp", "lastUpdateTimestamp", "loginCounter", "consent", "externalId", "idpId", "delegated"})
public class UserResource implements DTO<User> {
    /** The unique identifier for the user. */
    private String id;
    /** The user's first name. */
    private String firstName;
    /** The user's last name. */
    private String lastName;
    /** The user's email address. */
    private String email;
    /** Set of group IDs the user belongs to. */
    private Set<String> groupIds;
    /** The ID of the organization the user belongs to. */
    private String organizationId;
    /** Whether the user account is activated. */
    private Boolean activated;
    /** Timestamp of the user's last login. */
    private long lastLoginTimestamp;
    /** Timestamp of the last update to the user's information. */
    private long lastUpdateTimestamp;
    /** Number of times the user has logged in. */
    private int loginCounter;
    /** Whether the user has given consent. */
    private Boolean consent;
    /** The external identifier for delegated users. */
    private String externalId;
    /** The identity provider identifier for delegated users. */
    private String idpId;

    /**
     * Default constructor.
     * Initializes the set of group IDs.
     */
    public UserResource() {
        this.groupIds = new HashSet<>();
    }

    /**
     * Constructor with user ID.
     * @param id the user ID
     */
    public UserResource(String id) {
        this();
        this.id = id;
    }

    /**
     * Constructs a UserResource from a User domain object.
     * @param user the User domain object
     */
    public UserResource(User user) {
        this();
        this.id = user.getId();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.email = user.getEmail();
        this.activated = user.isActivated();
        this.lastLoginTimestamp = user.getLastLoginTimestamp();
        this.lastUpdateTimestamp = user.getLastUpdateTimestamp();
        this.loginCounter = user.getLoginCounter();
        this.organizationId = user.getOrganizationId();
        if (user.getGroupIds() != null && !user.getGroupIds().isEmpty()) {
            this.groupIds = new HashSet<>(user.getGroupIds());
        }
        if (user instanceof DelegatedUser delegatedUser) {
            this.externalId = delegatedUser.getExternalId();
            this.idpId = delegatedUser.getIdpId();
        }
    }

    /**
     * Constructs a UserResource with specified details.
     * @param firstName the user's first name
     * @param lastName the user's last name
     * @param organizationId the organization ID
     * @param groupId the group ID
     * @param activated the activation status
     */
    public UserResource(String firstName, String lastName, String organizationId, String groupId, Boolean activated) {
        this();
        this.firstName = firstName;
        this.lastName = lastName;
        this.organizationId = organizationId;
        if (groupId != null) {
            this.groupIds.add(groupId);
        }
        this.activated = activated;
    }

    /**
     * Gets the user ID.
     * @return the user ID
     */
    @Schema(
            description = "The user id.",
            examples = {"673ce282-d335-43ff-bfb2-7113bcba2e50"}
    )
    @JsonProperty("id")
    public String getId() {
        return id;
    }

    /**
     * Gets the user's first name.
     * @return the user's first name
     */
    @Schema(
            description = "The user first name.",
            required = true,
            examples = {"John"}
    )
    @JsonProperty("firstName")
    public String getFirstName() {
        return firstName;
    }

    /**
     * Sets the user's first name.
     * @param firstName the new first name
     * @return the updated UserResource object
     */
    public UserResource setFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    /**
     * Gets the user's last name.
     * @return the user's last name
     */
    @Schema(
            description = "The user last name.",
            required = true,
            examples = {"Doe"}
    )
    @JsonProperty("lastName")
    public String getLastName() {
        return lastName;
    }

    /**
     * Sets the user's last name.
     * @param lastName the new last name
     * @return the updated UserResource object
     */
    public UserResource setLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    /**
     * Gets the user's email address.
     * @return the user's email address
     */
    @Schema(
            description = "The user email.",
            required = true,
            examples = {"john.doe@example.com"}
    )
    @JsonProperty("email")
    public String getEmail() {
        return email;
    }

    /**
     * Sets the user's email address.
     * @param email the new email address
     * @return the updated UserResource object
     */
    public UserResource setEmail(String email) {
        this.email = email;
        return this;
    }

    /**
     * Gets the set of group IDs the user belongs to.
     * @return the set of group IDs
     */
    @Schema(
            description = "The groups the user is member of.",
            required = true,
            examples = {"[\"org:myOrga\",\n \"role:user\""}
    )
    @JsonProperty("groupIds")
    public Set<String> getGroupIds() {
        return new HashSet<>(groupIds);
    }

    /**
     * Sets the group IDs for the user.
     * @param groupIds the new set of group IDs
     * @return the updated UserResource object
     */
    public UserResource setGroupIds(Set<String> groupIds) {
        if (groupIds != null)
            this.groupIds = new HashSet<>(groupIds);
        else
            this.groupIds = new HashSet<>();
        return this;
    }

    /**
     * Gets the organization ID.
     * @return the organization ID
     */
    @Schema(
            description = "The user organization id.",
            required = true,
            examples = {"b59fc01e-a3f4-457f-836f-d57830bacf71"}
    )
    @JsonProperty("organizationId")
    public String getOrganizationId() {
        return organizationId;
    }

    /**
     * Sets the organization ID.
     * @param organizationId the new organization ID
     * @return the updated UserResource object
     */
    public UserResource setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
        return this;
    }

    /**
     * Checks if the user account is activated.
     * @return true if activated, false otherwise
     */
    @Schema(
            description = "The status of the user's account.",
            examples = {"true"}
    )
    @JsonProperty("activated")
    public Boolean isActivated() {
        return activated;
    }

    /**
     * Sets the activation status of the user account.
     * @param activated the new activation status
     * @return the updated UserResource object
     */
    public UserResource setActivated(Boolean activated) {
        this.activated = activated;
        return this;
    }

    /**
     * Gets the timestamp of the user's last login.
     * @return the last login timestamp
     */
    @Schema(
            description = "The date of user's last login",
            examples = {"1782386099"}
    )
    @JsonProperty("lastLoginTimestamp")
    public long getLastLoginTimestamp() {
        return lastLoginTimestamp;
    }

    /**
     * Sets the timestamp of the user's last login.
     * @param lastLoginTimestamp the new last login timestamp
     * @return the updated UserResource object
     */
    public UserResource setLastLoginTimestamp(long lastLoginTimestamp) {
        this.lastLoginTimestamp = lastLoginTimestamp;
        return this;
    }

    /**
     * Gets the timestamp of the last update to the user's information.
     * @return the last update timestamp
     */
    @Schema(
            description = "The date of user's last updated",
            examples = {"1780670484940"}
    )
    @JsonProperty("lastUpdateTimestamp")
    public long getLastUpdateTimestamp() {
        return lastUpdateTimestamp;
    }

    /**
     * Sets the timestamp of the last update to the user's information.
     * @param lastUpdateTimestamp the new last update timestamp
     * @return the updated UserResource object
     */
    public UserResource setLastUpdateTimestamp(long lastUpdateTimestamp) {
        this.lastUpdateTimestamp = lastUpdateTimestamp;
        return this;
    }

    /**
     * Gets the consent status of the user.
     * @return the consent status
     */
    @Schema(
            description = "The status of the user's GDPR consent",
            examples = {"true"}
    )
    @JsonProperty("consent")
    public Boolean getConsent() {
        return consent;
    }

    /**
     * Sets the consent status of the user.
     * @param consent the new consent status
     */
    public void setConsent(Boolean consent) {
        this.consent = consent;
    }

    /**
     * Gets the login counter value.
     * @return the login counter value
     */
    @Schema(
            description = "The number of time the user has logged in",
            examples = {"42"}
    )
    @JsonProperty("loginCounter")
    public int getLoginCounter() {
        return loginCounter;
    }

    /**
     * Sets the login counter value.
     * @param loginCounter the new login counter value
     */
    public void setLoginCounter(int loginCounter) {
        this.loginCounter = loginCounter;
    }

    /**
     * Gets the external ID of the user.
     * @return the external ID
     */
    @Schema(
            description = "If the user is delegated, it is the external id of the user",
            examples = {"b59fc01e-a3f4-457f-836f-d57830bacf71"}
    )
    @JsonProperty("externalId")
    public String getExternalId() {
        return externalId;
    }

    /**
     * Sets the external ID of the user.
     * @param externalId the new external ID
     * @return the updated UserResource object
     */
    public UserResource setExternalId(String externalId) {
        this.externalId = externalId;
        return this;
    }

    /**
     * Gets the identity provider ID of the user.
     * @return the identity provider ID
     */
    @Schema(
            description = "If the user is delegated, it is the Identity Provider id",
            examples = {"my-idp"}
    )
    @JsonProperty("idpId")
    public String getIdpId() {
        return idpId;
    }

    /**
     * Sets the identity provider ID of the user.
     * @param idpId the new identity provider ID
     * @return the updated UserResource object
     */
    public UserResource setIdpId(String idpId) {
        this.idpId = idpId;
        return this;
    }

    /**
     * Checks if the user is a delegated user.
     * @return true if the user is delegated, false otherwise
     */
    @Schema(
            description = "The status of the delegation of the user",
            examples = {"false"}
    )
    @JsonProperty("delegated")
    public boolean isDelegated() {
        return externalId != null && idpId != null;
    }

    /**
     * Sets the delegation status of the user.
     * @param delegated the new delegation status
     */
    public void setDelegated(boolean delegated) {
        /* Do nothing here, jackson needs */
    }

    /**
     * Converts this UserResource to a User domain object.
     * @return the User domain object
     */
    public User asUser() {
        return getBusinessObject();
    }

    @Override
    public String toString() {
        return "UserResource{" +
                "id='" + id + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", groupIds=" + groupIds +
                ", organizationId='" + organizationId + '\'' +
                ", activated=" + activated +
                ", lastLoginTimestamp=" + lastLoginTimestamp +
                ", loginCounter=" + loginCounter +
                ", externalId=" + externalId +
                ", idpId=" + idpId +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserResource that = (UserResource) o;
        return lastLoginTimestamp == that.lastLoginTimestamp
                && lastUpdateTimestamp == that.lastUpdateTimestamp
                && loginCounter == that.loginCounter
                && Objects.equals(id, that.id)
                && Objects.equals(firstName, that.firstName)
                && Objects.equals(lastName, that.lastName)
                && Objects.equals(email, that.email)
                && Objects.equals(groupIds, that.groupIds)
                && Objects.equals(organizationId, that.organizationId)
                && Objects.equals(activated, that.activated)
                && Objects.equals(consent, that.consent)
                && Objects.equals(externalId, that.externalId)
                && Objects.equals(idpId, that.idpId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, firstName, lastName, email, groupIds, organizationId, activated, lastLoginTimestamp, lastUpdateTimestamp, loginCounter, consent, externalId, idpId);
    }

    @Override
    @JsonIgnore
    public User getBusinessObject() {
        User user = new User(this.id);
        user.setFirstName(this.firstName);
        user.setLastName(this.lastName);
        user.setEmail(this.email);
        user.setActivated(this.activated);
        user.setLastLoginTimestamp(this.lastLoginTimestamp);
        user.setLastUpdateTimestamp(this.lastUpdateTimestamp);
        user.setLoginCounter(this.loginCounter);
        if (this.organizationId != null)
            user.setOrganizationId(this.organizationId);
        if (this.groupIds != null)
            user.setGroupIds(this.groupIds);
        if (isDelegated()) {
            return new DelegatedUser(user, externalId, idpId);
        }
        return user;
    }
}
