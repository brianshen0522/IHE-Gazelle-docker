package net.ihe.gazelle.user.management.api.interlay.user;

import net.ihe.gazelle.user.management.api.domain.user.User;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Data Transfer Object for editing user information in Gazelle User Management.
 * <p>
 * This class encapsulates editable user information for API communication, including identity, group membership,
 * organization, activation status, and consent.
 * </p>
 */
public class UserEditionResource {
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
    /** Whether the user has given consent. */
    private Boolean consent;

    /**
     * Default constructor.
     * Initializes the set of group IDs.
     */
    public UserEditionResource() {
        this.groupIds = new HashSet<>();
    }

    /**
     * Constructs a UserEditionResource from a User domain object.
     * @param user the User domain object
     */
    public UserEditionResource(User user) {
        this();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.email = user.getEmail();
        this.activated = user.isActivated();
        this.organizationId = user.getOrganizationId();
        if (user.getGroupIds() != null && !user.getGroupIds().isEmpty()) {
            this.groupIds = new HashSet<>(user.getGroupIds());
        }
    }

    /**
     * Constructs a UserEditionResource with specified details.
     * @param firstName the user's first name
     * @param lastName the user's last name
     * @param organizationId the organization ID
     * @param groupId the group ID
     * @param activated the activation status
     */
    public UserEditionResource(String firstName, String lastName, String organizationId, String groupId, Boolean activated) {
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
     * Gets the user's first name.
     * @return the user's first name
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Sets the user's first name.
     * @param firstName the new first name
     * @return the updated UserEditionResource object
     */
    public UserEditionResource setFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    /**
     * Gets the user's last name.
     * @return the user's last name
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Sets the user's last name.
     * @param lastName the new last name
     * @return the updated UserEditionResource object
     */
    public UserEditionResource setLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    /**
     * Gets the user's email address.
     * @return the user's email address
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the user's email address.
     * @param email the new email address
     * @return the updated UserEditionResource object
     */
    public UserEditionResource setEmail(String email) {
        this.email = email;
        return this;
    }

    /**
     * Gets the set of group IDs the user belongs to.
     * @return the set of group IDs
     */
    public Set<String> getGroupIds() {
        return new HashSet<>(groupIds);
    }

    /**
     * Sets the group IDs for the user.
     * @param groupIds the new set of group IDs
     * @return the updated UserEditionResource object
     */
    public UserEditionResource setGroupIds(Set<String> groupIds) {
        if (groupIds != null)
            this.groupIds = new HashSet<>(groupIds);
        else
            this.groupIds = new HashSet<>();
        return this;
    }

    /**
     * Gets the organization ID the user belongs to.
     * @return the organization ID
     */
    public String getOrganizationId() {
        return organizationId;
    }

    /**
     * Sets the organization ID for the user.
     * @param organizationId the new organization ID
     * @return the updated UserEditionResource object
     */
    public UserEditionResource setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
        return this;
    }

    /**
     * Checks if the user account is activated.
     * @return true if activated, false otherwise
     */
    public Boolean isActivated() {
        return activated;
    }

    /**
     * Sets the activation status for the user account.
     * @param activated the new activation status
     * @return the updated UserEditionResource object
     */
    public UserEditionResource setActivated(Boolean activated) {
        this.activated = activated;
        return this;
    }

    /**
     * Gets the consent status of the user.
     * @return true if consent is given, false otherwise
     */
    public Boolean getConsent() {
        return consent;
    }

    /**
     * Sets the consent status for the user.
     * @param consent the new consent status
     */
    public void setConsent(Boolean consent) {
        this.consent = consent;
    }

    /**
     * Converts this UserEditionResource to a User domain object.
     * @return the User domain object
     */
    public User asUser() {
        User user = new User();
        user.setFirstName(this.firstName);
        user.setLastName(this.lastName);
        user.setEmail(this.email);
        user.setActivated(this.activated);
        if (this.organizationId != null)
            user.setOrganizationId(this.organizationId);
        if (this.groupIds != null)
            user.setGroupIds(this.groupIds);
        return user;
    }

    @Override
    public String toString() {
        String groupIdsString = groupIds != null ? groupIds.toString() : "null";
        return "UserResource{" +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", groupIds=" + groupIdsString +
                ", organizationId='" + organizationId + '\'' +
                ", activated=" + activated +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserEditionResource that = (UserEditionResource) o;
        return Objects.equals(firstName, that.firstName) && Objects.equals(lastName, that.lastName) && Objects.equals(email, that.email) && Objects.equals(groupIds, that.groupIds) && Objects.equals(organizationId, that.organizationId) && Objects.equals(activated, that.activated) && Objects.equals(consent, that.consent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstName, lastName, email, groupIds, organizationId, activated, consent);
    }
}
