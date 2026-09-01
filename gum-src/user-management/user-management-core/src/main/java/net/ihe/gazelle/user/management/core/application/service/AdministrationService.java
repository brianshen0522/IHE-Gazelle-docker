package net.ihe.gazelle.user.management.core.application.service;

/**
 * Service interface for administration tasks related to user management.
 */
public interface AdministrationService {

    /**
     * Purge inactive users
     * Remove all users that have been registered for more than the specified limit and that have never logged in
     * @param  limitDays the number of days after which inactivated users will be purged.
     */
    void purgeInactiveUsers(int limitDays);
}
