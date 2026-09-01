package net.ihe.gazelle.user.management.api.domain.organization;

import java.util.Objects;

/**
 * Class representing an organization that is managed by an external identity provider.
 * This class extends the base Organization class to add delegation-specific
 * information such as external identifiers and identity provider references.
 * It is used when organizations are managed through external authentication
 * systems like Keycloak or other OIDC providers.
 */
public class DelegatedOrganization extends Organization {

    /**
     * The external identifier for this organization in the identity provider system.
     */
    private String externalId;

    /**
     * The identity provider identifier where this organization is managed.
     */
    private String idpId;

    /**
     * Default constructor.
     * <p>
     * Creates an empty DelegatedOrganization instance.
     * </p>
     */
    public DelegatedOrganization() {
    }

    /**
     * Constructor with delegation parameters.
     *
     * @param externalId the external identifier in the identity provider
     * @param idpId the identity provider identifier
     */
    public DelegatedOrganization(String externalId, String idpId) {
        this.externalId = externalId;
        this.idpId = idpId;
    }

    /**
     * Constructor with all organization and delegation parameters.
     *
     * @param shortname the organization shortname
     * @param name the organization name
     * @param externalId the external identifier in the identity provider
     * @param idpId the identity provider identifier
     */
    public DelegatedOrganization(String id, String shortname, String name, String externalId, String idpId) {
        super(id, shortname, name);
        this.externalId = externalId;
        this.idpId = idpId;
    }


    /**
     * Constructor that creates a delegated organization from an existing organization.
     *
     * @param organization the base organization to copy from
     * @param externalId the external identifier in the identity provider
     * @param idpId the identity provider identifier
     */
    public DelegatedOrganization(Organization organization, String externalId, String idpId) {
        super(organization);
        this.externalId = externalId;
        this.idpId = idpId;
    }

    /**
     * Gets the external identifier for this organization.
     *
     * @return the external identifier, or null if not set
     */
    public String getExternalId() {
        return externalId;
    }

    /**
     * Sets the external identifier for this organization.
     *
     * @param externalId the external identifier to set
     * @return this DelegatedOrganization instance for method chaining
     */
    public DelegatedOrganization setExternalId(String externalId) {
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
     * @return this DelegatedOrganization instance for method chaining
     */
    public DelegatedOrganization setIdpId(String idpId) {
        this.idpId = idpId;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        DelegatedOrganization that = (DelegatedOrganization) o;
        return Objects.equals(externalId, that.externalId) && Objects.equals(idpId, that.idpId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), externalId, idpId);
    }

    @Override
    public String toString() {
        return "DelegatedOrganization{" + "id='" + getId() + '\'' +
                ", name='" + getName() + '\'' +
                ", externalId='" + externalId + '\'' +
                ", idp='" + idpId + '\'' +
                '}';
    }
}
