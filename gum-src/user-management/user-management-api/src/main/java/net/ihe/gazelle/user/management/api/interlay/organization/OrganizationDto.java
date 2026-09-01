package net.ihe.gazelle.user.management.api.interlay.organization;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import net.ihe.gazelle.user.management.api.domain.organization.DelegatedOrganization;
import net.ihe.gazelle.user.management.api.domain.organization.Organization;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.Objects;

/**
 * Resource representation of an organization for API responses in Gazelle User Management.
 * This class is used to transfer organization data between the backend and clients,
 * including external identity provider information if applicable.
 */
@Schema(name = "Organization", description = "Represents an organization in the Gazelle Test Bed")
@JsonPropertyOrder({"id", "shortname", "name", "archived", "externalId", "idpId", "delegated"})
public class OrganizationDto {
    /** Unique identifier for the organization. */
    private String id;
    /** Name of the organization. */
    private String shortname;
    /** Name of the organization. */
    private String name;
    /** Status of the organization. */
    private Boolean archived;
    /** External identifier for the organization in the identity provider system. */
    private String externalId;
    /** Identity provider identifier where this organization is managed. */
    private String idpId;
    /** Timestamp of the last update to the organization's information. */
    private long lastUpdateTimestamp;

    /**
     * Default constructor (required for Jackson and CDI).
     */
    public OrganizationDto() {
        this.archived = false;
    }

    /**
     * Constructor with organization ID.
     * @param id the organization ID
     */
    public OrganizationDto(String id) {
        this();
        this.id = id;
    }

    /**
     * Constructor with organization ID and name.
     * @param id the organization ID
     * @param name the organization name
     */
    public OrganizationDto(String id, String name) {
        this(id);
        this.name = name;
    }

    /**
     * Constructor with organization ID, name, and URL.
     * @param id the organization ID
     * @param name the organization name
     */
    public OrganizationDto(String id, String shortname, String name) {
        this(id, name);
        this.shortname = shortname;
    }

    /**
     * Constructor with all organization and delegation parameters.
     * @param id the organization ID
     * @param name the organization name
     * @param externalId the external identifier in the identity provider
     * @param idpId the identity provider identifier
     */
    public OrganizationDto(String id, String shortname, String name, String externalId, String idpId) {
        this(id, shortname, name);
        this.externalId = externalId;
        this.idpId = idpId;
    }

    /**
     * Copy constructor.
     * Creates a new OrganizationResource by copying all fields from another OrganizationResource.
     * @param organizationToCopy the organization resource to copy
     */
    public OrganizationDto(OrganizationDto organizationToCopy) {
        this.id = organizationToCopy.id;
        this.shortname = organizationToCopy.shortname;
        this.name = organizationToCopy.name;
        setArchived(organizationToCopy.archived);
        this.externalId = organizationToCopy.externalId;
        this.idpId = organizationToCopy.idpId;
        this.lastUpdateTimestamp = organizationToCopy.lastUpdateTimestamp;
    }

    /**
     * Constructor that initializes the OrganizationResource from an Organization domain object.
     * @param organization the organization domain object to initialize from
     */
    public OrganizationDto(Organization organization) {
        this(
                organization.getId(),
                organization.getShortname(),
                organization.getName()
        );
        setArchived(organization.isArchived());
        if (organization instanceof DelegatedOrganization delegatedOrganization) {
            this.externalId = delegatedOrganization.getExternalId();
            this.idpId = delegatedOrganization.getIdpId();
        }
        this.lastUpdateTimestamp = organization.getLastUpdateTimestamp();
    }

    /**
     * Gets the organization ID.
     * @return the organization ID
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the organization ID.
     * @param id the organization ID
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets the organization shortname.
     * @return the organization shortname
     */
    public String getShortname() {
        return shortname;
    }

    /**
     * Sets the organization shortname.
     * @param shortname the organization shortname
     */
    public void setShortname(String shortname) {
        this.shortname = shortname;
    }

    /**
     * Gets the organization name.
     * @return the organization name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the organization name.
     * @param name the organization name
     */
    public void setName(String name) {
        this.name = name;
    }

    public Boolean isArchived() {
            return archived;
    }

    public void setArchived(Boolean archived) {
        this.archived = Objects.requireNonNullElse(archived, false);
    }

    /**
     * Gets the external identifier for the organization in the identity provider system.
     * @return the external identifier
     */
    public String getExternalId() {
        return externalId;
    }

    /**
     * Sets the external identifier for the organization in the identity provider system.
     * @param externalId the external identifier
     */
    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    /**
     * Gets the identity provider identifier where this organization is managed.
     * @return the identity provider identifier
     */
    public String getIdpId() {
        return idpId;
    }

    /**
     * Sets the identity provider identifier where this organization is managed.
     * @param idpId the identity provider identifier
     */
    public void setIdpId(String idpId) {
        this.idpId = idpId;
    }

    /**
     * Indicates whether the organization is delegated (both externalId and idpId are set).
     *
     * @return true if delegated, false otherwise
     */
    @JsonProperty(value = "delegated", access = JsonProperty.Access.READ_ONLY)
    public boolean isDelegated() {
        return externalId != null && idpId != null;
    }

    @Schema(
            description = "The date of organization's last updated",
            examples = {"1780670484940"}
    )
    @JsonProperty(value = "lastUpdateTimestamp")
    public long getLastUpdateTimestamp() {
        return lastUpdateTimestamp;
    }

    public void setLastUpdateTimestamp(long lastUpdateTimestamp) {
        this.lastUpdateTimestamp = lastUpdateTimestamp;
    }

    /**
     * Converts this OrganizationResource to an Organization domain object.
     * @return the corresponding Organization domain object, which may be a DelegatedOrganization if delegation information is present
     */
    public Organization asOrganization() {
        Organization organization = new Organization(this.id);
        organization.setShortname(this.getShortname());
        organization.setName(this.name);
        organization.setArchived(this.archived);
        organization.setLastUpdateTimestamp(this.lastUpdateTimestamp);
        if (isDelegated()) {
            return new DelegatedOrganization(organization, externalId, idpId);
        }
        return organization;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrganizationDto organizationResource = (OrganizationDto) o;
        return Objects.equals(id, organizationResource.id) &&
                Objects.equals(shortname, organizationResource.shortname) &&
                Objects.equals(name, organizationResource.name) &&
                Objects.equals(archived, organizationResource.archived) &&
                Objects.equals(externalId, organizationResource.externalId) &&
                Objects.equals(idpId, organizationResource.idpId) &&
                Objects.equals(lastUpdateTimestamp, organizationResource.lastUpdateTimestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, shortname, name, archived, externalId, idpId, lastUpdateTimestamp);
    }

    @Override
    public String toString() {
        return "OrganizationResource{" + "id='" + id + '\'' +
                ", shortname='" + shortname + '\'' +
                ", name='" + name + '\'' +
                ", archived='" + archived + '\'' +
                ", externalId='" + externalId + '\'' +
                ", idpId='" + idpId + '\'' +
                ", lastUpdateTimestamp='" + lastUpdateTimestamp + '\'' +
                '}';
    }
}
