package db.migration;

import net.ihe.gazelle.user.management.api.domain.user.User;

/**
 * This class is a wrapper to manipulate users in the user migration context between gazelle and gum databases
 * This class is used for the migration V2_0_0_2__MigrateGazelleUsers
 */
public class GazelleUserWrapper {

    User user;
    String password;
    String[] roles;

    /**
     * Construct an user wrapper.
     * @param user the user to wrap
     * @param password the user password
     * @param roles the user roles
     */
    public GazelleUserWrapper(User user, String password, String[] roles) {
        this.user = user;
        this.password = password;
        this.roles = roles;
    }

    /** Get user business model
     * @return user */
    public User getUser() { return user; }

    /** Set user business model
     * @param user the user to set */
    public void setUser(User user) { this.user = user; }

    /** Get user password
     * @return the password */
    public String getPassword() { return password; }

    /** Set user password
     * @param password the password to set */
    public void setPassword(String password) { this.password = password; }

    /** Get user roles
     * @return An array of roles */
    public String[] getRoles() { return roles; }

    /** Set user roles
     * @param roles Array of roles to set */
    public void setRoles(String[] roles) { this.roles = roles; }
}
