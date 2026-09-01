package net.ihe.gazelle.user.management.api.domain.user;

/**
 * Enumeration of default groups available in the Gazelle system.
 *
 * This enum defines the standard roles and group prefixes used throughout
 * the Gazelle User Management system for authorization and access control.
 * It includes both specific roles and organizational group prefixes.
 *
 */
public enum GazelleDefaultGroup {

    /**
     * Administrator role with full system privileges.
     */
    GAZELLE_ADMIN("role:gazelle_admin"),

    /**
     * Project administrator role for managing specific projects.
     */
    PROJECT_ADMIN("role:project_admin"),

    /**
     * Monitor role for system monitoring and oversight.
     */
    MONITOR("role:monitor"),

    /**
     * SUT (System Under Test) operator role for managing test systems.
     */
    SUT_OPERATOR("role:sut_operator"),

    /**
     * Test designer role for creating and managing test cases.
     */
    TEST_DESIGNER("role:test_designer"),

    /**
     * Testing session manager role for managing test sessions.
     */
    TESTING_SESSION_MANAGER("role:testing_session_manager"),

    /**
     * Late registration role for users who register after deadlines.
     */
    LATE_REGISTRATION("role:late_registration"),

    /**
     * Prefix for organization member groups.
     */
    PREFIX_ORGANIZATION_MEMBER("org:"),

    /**
     * Prefix for organization administrator groups.
     */
    PREFIX_ORGANIZATION_ADMIN ("org-adm:");

    /**
     * The string name/identifier of the group.
     */
    private final String name;

    /**
     * Constructor for enum values.
     *
     * @param name the string identifier for this group
     */
    GazelleDefaultGroup(String name) {
        this.name = name;
    }

    /**
     * Gets the string name/identifier of this group.
     *
     * @return the group name/identifier
     */
    public String getName() {
        return this.name;
    }
}
