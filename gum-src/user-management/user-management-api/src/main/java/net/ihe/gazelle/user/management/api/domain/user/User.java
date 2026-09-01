package net.ihe.gazelle.user.management.api.domain.user;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Represents a user in the Gazelle User Management system.
 *
 * <p>This class encapsulates all user-related information including personal details,
 * authentication state, group memberships, and organizational affiliation. It serves
 * as the primary user entity throughout the system.</p>
 *
 * <p>Users can be in different states (activated/deactivated) and belong to multiple
 * groups within an organization. The class tracks login statistics and maintains
 * timestamps for various user lifecycle events.</p>
 *
 */
public class User {
    /** The unique identifier for this user */
    private String id;
    /** The user's first name */
    private String firstName;
    /** The user's last name */
    private String lastName;
    /** The user's email address, used for authentication and communication */
    private String email;
    /** Set of group IDs this user belongs to */
    private Set<String> groupIds;
    /** The ID of the organization this user is associated with */
    private String organizationId;
    /** Whether this user account is activated */
    private Boolean activated;
    /** Activation code used for account activation */
    private String activationCode;
    /** Timestamp of the user's last login (Unix timestamp) */
    private long lastLoginTimestamp;
    /** Timestamp when this user was registered (Unix timestamp) */
    private long registrationTimestamp;
    /** Number of times this user has logged in */
    private int loginCounter;
    /** Timestamp of the last update to this user's information (Unix timestamp) */
    private long lastUpdateTimestamp;

    /**
     * Default constructor.
     *
     * <p>Creates a new User instance with an empty set of group IDs.</p>
     */
    public User() {
        this.groupIds = new HashSet<>();
    }

    /**
     * Constructor with user ID.
     *
     * @param id the unique identifier for this user
     */
    public User(String id) {
        this();
        this.id = id;

    }

    /**
     * Constructor with basic user information.
     *
     * @param id the unique identifier for this user
     * @param firstName the user's first name
     * @param lastName the user's last name
     * @param email the user's email address
     */
    public User(String id, String firstName, String lastName, String email) {
        this(id);
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }

    /**
     * Constructor with user information and organization.
     *
     * @param id the unique identifier for this user
     * @param firstName the user's first name
     * @param lastName the user's last name
     * @param email the user's email address
     * @param organizationId the ID of the user's organization
     */
    public User(String id, String firstName, String lastName, String email, String organizationId) {
        this(id, firstName, lastName, email);
        this.setOrganizationId(organizationId);
    }

    /**
     * Constructor with complete user information including groups.
     *
     * @param id the unique identifier for this user
     * @param firstName the user's first name
     * @param lastName the user's last name
     * @param email the user's email address
     * @param organizationId the ID of the user's organization
     * @param groupIds set of group IDs this user belongs to
     */
    public User(String id, String firstName, String lastName, String email, String organizationId, Set<String> groupIds) {
        this(id, firstName, lastName, email, organizationId);
        this.groupIds.addAll(groupIds);
    }

    /**
     * Copy constructor.
     *
     * <p>Creates a new User instance by copying all fields from another User object.</p>
     *
     * @param userToCopy the user to copy from
     */
    public User(User userToCopy) {
        this(
                userToCopy.getId(),
                userToCopy.getFirstName(),
                userToCopy.getLastName(),
                userToCopy.getEmail(),
                userToCopy.getOrganizationId(),
                userToCopy.getGroupIds()
        );
        setActivated(userToCopy.isActivated());
        setActivationCode(userToCopy.getActivationCode());
        setLastLoginTimestamp(userToCopy.getLastLoginTimestamp());
        setRegistrationTimestamp(userToCopy.getRegistrationTimestamp());
        setLoginCounter(userToCopy.getLoginCounter());
        setLastUpdateTimestamp(userToCopy.getLastUpdateTimestamp());
    }

    /**
     * Gets the user's first name.
     *
     * @return the first name, or null if not set
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Sets the user's first name.
     *
     * @param firstName the first name to set
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Gets the user's last name.
     *
     * @return the last name, or null if not set
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Sets the user's last name.
     *
     * @param lastName the last name to set
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Gets the user's unique identifier.
     *
     * @return the user ID
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the user's unique identifier.
     *
     * @param id the user ID to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets the user's email address.
     *
     * @return the email address, or null if not set
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the user's email address.
     *
     * @param email the email address to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Gets a copy of the set of group IDs this user belongs to.
     *
     * <p>Returns a defensive copy to prevent external modification.</p>
     *
     * @return a new HashSet containing the group IDs
     */
    public Set<String> getGroupIds() {
        return new HashSet<>(this.groupIds);
    }

    /**
     * Sets the group IDs this user belongs to.
     *
     * <p>Creates a defensive copy of the provided set.</p>
     *
     * @param groupIds the set of group IDs to set
     */
    public void setGroupIds(Set<String> groupIds) {
        if (groupIds == null) {
            this.groupIds = new HashSet<>();
            return;
        }
        this.groupIds = new HashSet<>(groupIds);
    }

    /**
     * Adds a group ID to this user's group memberships.
     *
     * @param groupId the group ID to add
     */
    public void addGroupId(String groupId) {
        this.groupIds.add(groupId);
    }

    /**
     * Checks if this user account is activated.
     *
     * @return true if activated, false if deactivated, null if not set
     */
    public Boolean isActivated() {
        return activated;
    }

    /**
     * Sets the activation status of this user account.
     *
     * @param activated true to activate, false to deactivate
     */
    public void setActivated(Boolean activated) {
        this.activated = activated;
    }

    /**
     * Gets the timestamp of the user's last login.
     *
     * @return the last login timestamp as Unix timestamp
     */
    public long getLastLoginTimestamp() {
        return lastLoginTimestamp;
    }

    /**
     * Sets the timestamp of the user's last login.
     *
     * @param lastLoginTimestamp the last login timestamp as Unix timestamp
     */
    public void setLastLoginTimestamp(long lastLoginTimestamp) {
        this.lastLoginTimestamp = lastLoginTimestamp;
    }

    /**
     * Gets the activation code for this user account.
     *
     * <p>This code is typically used for email-based account activation.</p>
     *
     * @return the activation code, or null if not set
     */
    public String getActivationCode() {
        return activationCode;
    }

    /**
     * Sets the activation code for this user account.
     *
     * @param activationCode the activation code to set
     */
    public void setActivationCode(String activationCode) {
        this.activationCode = activationCode;
    }

    /**
     * Gets the timestamp when this user was registered.
     *
     * @return the registration timestamp as Unix timestamp
     */
    public long getRegistrationTimestamp() {
        return registrationTimestamp;
    }

    /**
     * Sets the timestamp when this user was registered.
     *
     * @param registrationTimestamp the registration timestamp as Unix timestamp
     */
    public void setRegistrationTimestamp(long registrationTimestamp) {
        this.registrationTimestamp = registrationTimestamp;
    }

    /**
     * Gets the number of times this user has logged in.
     *
     * @return the login counter
     */
    public int getLoginCounter() {
        return loginCounter;
    }

    /**
     * Sets the number of times this user has logged in.
     *
     * @param counterLogins the login counter to set
     */
    public void setLoginCounter(int counterLogins) {
        this.loginCounter = counterLogins;
    }

    /**
     * Gets the ID of the organization this user is associated with.
     *
     * @return the organization ID, or null if not set
     */
    public String getOrganizationId() {
        return organizationId;
    }

    /**
     * Sets the ID of the organization this user is associated with.
     *
     * @param organizationId the organization ID to set
     */
    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    /**
     * Gets the timestamp of the last update to this user's information.
     *
     * @return the last update timestamp as Unix timestamp
     */
    public long getLastUpdateTimestamp() {
        return lastUpdateTimestamp;
    }

    /**
     * Sets the timestamp of the last update to this user's information.
     *
     * @param lastUpdateTimestamp the last update timestamp as Unix timestamp
     */
    public void setLastUpdateTimestamp(long lastUpdateTimestamp) {
        this.lastUpdateTimestamp = lastUpdateTimestamp;
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * <p>Two User objects are considered equal if all their fields are equal.</p>
     *
     * @param o the reference object with which to compare
     * @return true if this object is the same as the obj argument; false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(activated, user.activated) &&
                Objects.equals(lastLoginTimestamp, user.lastLoginTimestamp) &&
                Objects.equals(firstName, user.firstName) &&
                Objects.equals(lastName, user.lastName) &&
                Objects.equals(id, user.id) &&
                Objects.equals(email, user.email) &&
                Objects.equals(groupIds, user.groupIds) &&
                Objects.equals(activationCode, user.activationCode) &&
                Objects.equals(registrationTimestamp, user.registrationTimestamp) &&
                Objects.equals(loginCounter, user.loginCounter) &&
                Objects.equals(lastUpdateTimestamp, user.lastUpdateTimestamp) &&
                Objects.equals(organizationId, user.organizationId);
    }

    /**
     * Returns a hash code value for the object.
     *
     * <p>The hash code is computed based on all fields of the User object.</p>
     *
     * @return a hash code value for this object
     */
    @Override
    public int hashCode() {
        return Objects.hash(firstName, lastName, id, email, groupIds, organizationId, activated, lastLoginTimestamp, activationCode, registrationTimestamp, loginCounter, lastUpdateTimestamp);
    }
}
