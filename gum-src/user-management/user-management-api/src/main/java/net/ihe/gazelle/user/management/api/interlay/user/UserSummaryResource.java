package net.ihe.gazelle.user.management.api.interlay.user;

import net.ihe.gazelle.user.management.api.domain.user.User;

import java.util.Objects;

/**
 * Data Transfer Object for user summary information in Gazelle User Management.
 * <p>
 * This class encapsulates summary user information for API communication, including identity and organization.
 * </p>
 */
public class UserSummaryResource {
    /** The unique identifier for the user. */
    private String id;
    /** The user's first name. */
    private String firstName;
    /** The user's last name. */
    private String lastName;
    /** The ID of the organization the user belongs to. */
    private String organizationId;

    /**
     * Default constructor.
     */
    public UserSummaryResource() {
    }

    /**
     * Constructor with user ID.
     * @param id the user ID
     */
    public UserSummaryResource(String id) {
        this();
        this.id = id;
    }

    /**
     * Constructs a UserSummaryResource from a User domain object.
     * @param user the User domain object
     */
    public UserSummaryResource(User user) {
        this();
        this.id = user.getId();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.organizationId = user.getOrganizationId();
    }

    /**
     * Constructs a UserSummaryResource with specified details.
     * @param firstName the user's first name
     * @param lastName the user's last name
     * @param organizationId the organization ID
     */
    public UserSummaryResource(String firstName, String lastName, String organizationId) {
        this();
        this.firstName = firstName;
        this.lastName = lastName;
        this.organizationId = organizationId;
    }

    /**
     * Gets the user ID.
     * @return the user ID
     */
    public String getId() {
        return id;
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
     * @return the updated UserSummaryResource object
     */
    public UserSummaryResource setFirstName(String firstName) {
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
     * @return the updated UserSummaryResource object
     */
    public UserSummaryResource setLastName(String lastName) {
        this.lastName = lastName;
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
     * @return the updated UserSummaryResource object
     */
    public UserSummaryResource setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
        return this;
    }

    /**
     * Converts this UserSummaryResource to a User domain object.
     * @return the User domain object
     */
    public User asUser() {
        User user = new User(this.id);
        user.setFirstName(this.firstName);
        user.setLastName(this.lastName);
        if (this.organizationId != null)
            user.setOrganizationId(this.organizationId);
        return user;
    }

    @Override
    public String toString() {
        return "UserResource{" +
                "id='" + id + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", organizationId='" + organizationId + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        UserSummaryResource that = (UserSummaryResource) object;
        return Objects.equals(id, that.id) && Objects.equals(firstName, that.firstName) && Objects.equals(lastName, that.lastName) && Objects.equals(organizationId, that.organizationId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, firstName, lastName, organizationId);
    }
}
