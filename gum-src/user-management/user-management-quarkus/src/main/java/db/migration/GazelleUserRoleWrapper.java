package db.migration;

/**
 * This class is used for the migration V4_0_0_5__MigrateRolesToGroups
 */
public class GazelleUserRoleWrapper {

    private String userId;
    private String orgaId;
    private String roleName;
    private String roleDescription;

    /** Creates an empty wrapper. */
    public GazelleUserRoleWrapper() {
        // Default constructor
    }

    /** Get userId
     * @return user id
     */
    public String getUserId() { return userId; }

    /** Set userId
     * @param userId user id
     */
    public void setUserId(String userId) { this.userId = userId; }

    /** Get roleName
     * @return role name
     */
    public String getRoleName() { return roleName; }

    /** Set roleName
     * @param roleName role name
     */
    public void setRoleName(String roleName) { this.roleName = roleName; }

    /** Get roleDescription
     * @return role description
     */
    public String getRoleDescription() {return roleDescription; }

    /** Set roleDescription
     * @param roleDescription role description
     */
    public void setRoleDescription(String roleDescription) { this.roleDescription = roleDescription; }

    /** Get orgaId
     * @return organization id
     */
    public String getOrgaId() { return orgaId; }

    /** Set orgaId
     * @param orgaId organization id
     */
    public void setOrganizationId(String orgaId) { this.orgaId = orgaId; }
}
