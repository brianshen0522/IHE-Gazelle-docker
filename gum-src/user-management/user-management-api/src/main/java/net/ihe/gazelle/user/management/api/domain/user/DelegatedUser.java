package net.ihe.gazelle.user.management.api.domain.user;

import java.util.Objects;

/**
 * Class representing a user that is managed by an external identity provider.
 *
 * This class extends the base User class to add delegation-specific
 * information such as external identifiers and identity provider references.
 * It is used when users are authenticated and managed through external
 * systems like Keycloak or other OIDC providers.
 *
 */
public class DelegatedUser extends User {

    /**
     * The external identifier for this user in the identity provider system.
     */
    private String externalId;

    /**
     * The identity provider identifier where this user is managed.
     */
    private String idpId;

    /**
     * Default constructor.
     *
     * Creates an empty DelegatedUser instance.
     */
    public DelegatedUser() {
    }

    /**
     * Constructor with user ID.
     *
     * @param id the user identifier
     */
    public DelegatedUser(String id) {
        super(id);
    }

    /**
     * Constructor that creates a delegated user from an existing user.
     *
     * @param user the base user to copy from
     * @param externalId the external identifier in the identity provider
     * @param idpId the identity provider identifier
     */
    public DelegatedUser(User user, String externalId, String idpId) {
        super(user);
        this.externalId = externalId;
        this.idpId = idpId;
    }

    /**
     * Gets the external identifier for this user.
     *
     * @return the external identifier, or null if not set
     */
    public String getExternalId() {
        return externalId;
    }

    /**
     * Sets the external identifier for this user.
     *
     * @param externalId the external identifier to set
     * @return this DelegatedUser instance for method chaining
     */
    public DelegatedUser setExternalId(String externalId) {
        this.externalId = externalId;
        return this;
    }

    /**
     * Gets the identity provider identifier.
     *
     * @return the identity provider identifier, or null if not set
     */
    public String getIdpId() {
        return idpId;
    }

    /**
     * Sets the identity provider identifier.
     *
     * @param idpId the identity provider identifier to set
     * @return this DelegatedUser instance for method chaining
     */
    public DelegatedUser setIdpId(String idpId) {
        this.idpId = idpId;
        return this;
    }

    /**
     * Converts this delegated user to a regular User instance.
     *
     * Creates a new User object with all the base user properties
     * copied from this delegated user, excluding delegation-specific fields.
     *
     * @return a new User instance with copied properties
     */
    public User asUser() {
        return new User(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DelegatedUser user)) return false;
        if (!super.equals(o)) return false;
        return Objects.equals(isActivated(), user.isActivated()) &&
                Objects.equals(getLastLoginTimestamp(), user.getLastLoginTimestamp()) &&
                Objects.equals(getFirstName(), user.getFirstName()) &&
                Objects.equals(getLastName(), user.getLastName()) &&
                Objects.equals(getId(), user.getId()) &&
                Objects.equals(getEmail(), user.getEmail()) &&
                Objects.equals(getGroupIds(), user.getGroupIds()) &&
                Objects.equals(getActivationCode(), user.getActivationCode()) &&
                Objects.equals(getRegistrationTimestamp(), user.getRegistrationTimestamp()) &&
                Objects.equals(getLoginCounter(), user.getLoginCounter()) &&
                Objects.equals(getLastUpdateTimestamp(), user.getLastUpdateTimestamp()) &&
                Objects.equals(getOrganizationId(), user.getOrganizationId()) &&
                Objects.equals(externalId, user.externalId) &&
                Objects.equals(idpId, user.idpId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), externalId, idpId);
    }
}
