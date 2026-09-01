package net.ihe.gazelle.user.management.api.domain.organization;

import java.util.Objects;

/**
 * Represents an organization in the Gazelle User Management system.
 * <p>
 * This class encapsulates basic organization information such as ID, name, and URL.
 * </p>
 */
public class Organization {
    /** Unique identifier for the organization. */
    private String id;
    /** Shortname of the organization. */
    private String shortname;
    /** Name of the organization. */
    private String name;
    /** Status of the organization. */
    private Boolean archived = false;
    /** Timestamp of the last update to this organization's information. */
    private long lastUpdateTimestamp;

    /**
     * Default constructor (required for Jackson and CDI).
     */
    public Organization() {
        // Empty constructor needed for Jackson.
    }

    /**
     * Constructor with organization ID.
     * @param id the organization ID
     */
    public Organization(String id) {
        this.id = id;
    }

    /**
     * Constructor with organization shortname, and name.
     * @param shortname the organization ID
     * @param name the organization name
     */
    public Organization(String shortname, String name) {
        this.shortname = shortname;
        this.name = name;
    }


    /**
     * Constructor with organization ID, shortname, and name.
     * @param id the organization ID
     * @param name the organization name
     * @param shortname the organization shortname
     */
    public Organization(String id, String shortname, String name) {
        this(id);
        this.shortname = shortname;
        this.name = name;
    }

    /**
     * Copy constructor.
     * Creates a new Organization by copying all fields from another Organization.
     * @param organizationToCopy the organization to copy
     */
    public Organization(Organization organizationToCopy) {
        this.id = organizationToCopy.id;
        this.name = organizationToCopy.name;
        this.shortname = organizationToCopy.shortname;
        this.archived = organizationToCopy.archived;
        this.lastUpdateTimestamp = organizationToCopy.lastUpdateTimestamp;
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
    public Organization setId(String id) {
        this.id = id;
        return this;
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
    public Organization setShortname(String shortname) {
        this.shortname = shortname;
        return this;
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
    public Organization setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Gets the organization status.
     * @return the organization status
     */
    public Boolean isArchived() {
        return archived;
    }

    /**
     * Sets the organization URL.
     * @param archived the organization status
     */
    public Organization setArchived(Boolean archived) {
        this.archived = archived;
        return this;
    }


    /**
     * Gets the timestamp of the last update to this organization's information.
     *
     * @return the last update timestamp as Unix timestamp
     */
    public long getLastUpdateTimestamp() {
        return lastUpdateTimestamp;
    }

    /**
     * Sets the timestamp of the last update to this organization's information.
     *
     * @param lastUpdateTimestamp the last update timestamp as Unix timestamp
     */
    public void setLastUpdateTimestamp(long lastUpdateTimestamp) {
        this.lastUpdateTimestamp = lastUpdateTimestamp;
    }

    /**
     * Checks equality with another object.
     * @param o the object to compare
     * @return true if equal, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Organization organization = (Organization) o;
        return Objects.equals(id, organization.id) &&
                Objects.equals(shortname, organization.shortname) &&
                Objects.equals(name, organization.name) &&
                Objects.equals(archived, organization.archived) &&
                Objects.equals(lastUpdateTimestamp, organization.lastUpdateTimestamp);
    }

    /**
     * Computes the hash code for this organization.
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(id, shortname, name, archived, lastUpdateTimestamp);
    }

    @Override
    public String toString() {
        return "Organization{" + "id='" + id + '\'' +
                ", shortname='" + shortname + '\'' +
                ", name='" + name + '\'' +
                ", archived='" + archived + '\'' +
                ", lastUpdateTimestamp='" + lastUpdateTimestamp + '\'' +
                '}';
    }
}
