package net.ihe.gazelle.user.management.api.interlay.user;

/**
 * Data Transfer Object for group ID in Gazelle User Management.
 * <p>
 * This class is used to transfer a group identifier between API layers.
 * </p>
 */
public class GroupIdResource {
    /** The unique identifier for the group. */
    private String groupId;

    /**
     * Default constructor (required for Jackson and CDI).
     */
    public GroupIdResource() {
        // Empty constructor needed for Jackson.
    }

    /**
     * Constructor with group ID.
     * @param groupId the group identifier
     */
    public GroupIdResource(String groupId) {
        this.groupId = groupId;
    }

    /**
     * Gets the group identifier.
     * @return the group ID
     */
    public String getGroupId() {
        return groupId;
    }

    /**
     * Sets the group identifier.
     * @param groupId the group ID
     */
    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }
}
