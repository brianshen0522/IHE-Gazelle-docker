package net.ihe.gazelle.user.management.api.application.user.lookup;

import java.util.Objects;

/**
 * Query parameters used to search and filter users.
 * @param search the search term to filter users by name or email
 * @param firstName the first name to filter users
 * @param lastName the last name to filter users
 * @param email the email to filter users
 * @param group the group to filter users
 * @param organizationId the organization ID to filter users
 * @param activated the activation status to filter users
 * @param delegated the delegation status to filter users
 * @param externalId the external ID to filter users
 * @param idpId the identity provider ID to filter users
 */
public record UserQueryParams(String search, String firstName, String lastName, String email, String group, String organizationId, Boolean activated, Boolean delegated, String externalId, String idpId) {

    /** Attribute names for dynamic attribute updates */
    public static final String ATTR_LAST_NAME = "lastName";
    /** Attribute name for first name. */
    public static final String ATTR_FIRST_NAME = "firstName";
    /** Attribute name for searching. */
    public static final String ATTR_SEARCH = "search";
    /** Attribute name for organization ID. */
    public static final String ATTR_ORGANIZATION_ID = "organizationId";
    /** Attribute name for delegation status. */
    public static final String ATTR_DELEGATED = "delegated";
    /** Attribute name for external ID. */
    public static final String ATTR_EXTERNAL_ID = "externalId";
    /** Attribute name for identity provider ID. */
    public static final String ATTR_IDP_ID = "idpId";
    /** Attribute name for email. */
    public static final String ATTR_EMAIL = "email";
    /** Attribute name for group. */
    public static final String ATTR_GROUP = "group";
    /** Attribute name for activation status. */
    public static final String ATTR_ACTIVATED = "activated";

    /**
     * Returns an empty query with all parameters set to null.
     * @return a null query instance
     */
    public static UserQueryParams nullQuery() {
        return new UserQueryParams(null, null, null, null, null, null, null,null, null, null);
    }

    /**
     * Creates a copy of the provided query parameters.
     * @param queryParams the query parameters to clone
     * @return a cloned query instance
     */
    public static UserQueryParams clone(UserQueryParams queryParams) {
        return new UserQueryParams(queryParams.search, queryParams.firstName, queryParams.lastName, queryParams.email, queryParams.group, queryParams.organizationId, queryParams.activated,queryParams.delegated, queryParams.externalId, queryParams.idpId);
    }

    /**
     * Returns a new query with the search term updated.
     * @param search the search term
     * @return a new query instance
     */
    public UserQueryParams setSearch(String search) {
        return new UserQueryParams(
                search,
                this.firstName,
                this.lastName,
                this.email,
                this.group,
                this.organizationId,
                this.activated,
                this.delegated,
                this.externalId,
                this.idpId);
    }

    /**
     * Returns a new query with the given attribute updated.
     * @param attributeName the attribute name to update
     * @param value the value to set
     * @return a new query instance
     */
    public UserQueryParams setAttribute(String attributeName, Object value) {
        return new UserQueryParams(
                attributeName.equals(ATTR_SEARCH) ? (String) value : this.search,
                attributeName.equals(ATTR_FIRST_NAME) ? (String) value : this.firstName,
                attributeName.equals(ATTR_LAST_NAME) ? (String) value : this.lastName,
                attributeName.equals(ATTR_EMAIL) ? (String) value : this.email,
                attributeName.equals(ATTR_GROUP) ? (String) value : this.group,
                attributeName.equals(ATTR_ORGANIZATION_ID) && value instanceof String stringValue ? stringValue : this.organizationId,
                attributeName.equals(ATTR_ACTIVATED) && value instanceof Boolean booleanValue ? booleanValue : this.activated,
                attributeName.equals(ATTR_DELEGATED) && value instanceof Boolean booleanValue ? booleanValue : this.delegated,
                attributeName.equals(ATTR_EXTERNAL_ID) && value instanceof String stringValue ? stringValue : this.externalId,
                attributeName.equals(ATTR_IDP_ID) && value instanceof String stringValue ? stringValue : this.idpId
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserQueryParams that = (UserQueryParams) o;
        return Objects.equals(search, that.search)
                && Objects.equals(firstName, that.firstName)
                && Objects.equals(lastName, that.lastName)
                && Objects.equals(email, that.email)
                && Objects.equals(group, that.group)
                && Objects.equals(organizationId, that.organizationId)
                && Objects.equals(activated, that.activated)
                && Objects.equals(delegated, that.delegated)
                && Objects.equals(externalId, that.externalId)
                && Objects.equals(idpId, that.idpId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(search, firstName, lastName, email, group, organizationId, activated, delegated, externalId, idpId);
    }
}