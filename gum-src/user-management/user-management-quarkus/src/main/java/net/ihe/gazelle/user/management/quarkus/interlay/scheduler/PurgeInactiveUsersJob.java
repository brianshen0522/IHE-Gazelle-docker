package net.ihe.gazelle.user.management.quarkus.interlay.scheduler;

import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import net.ihe.gazelle.user.management.core.application.service.AdministrationService;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Scheduled job responsible for purging inactive users from the system after a configurable number of days.
 */
@ApplicationScoped
public class PurgeInactiveUsersJob {

    private final AdministrationService administrationService;

    @ConfigProperty(name = "gzl.user.inactivated.purge.after.days")
    int purgeInactivatedUsersAfterDays;

    /**
     * Creates the scheduled job with the required administration service.
     *
     * @param administrationService service handling administration operations, including purging inactive users
     */
    @Inject
    public PurgeInactiveUsersJob(AdministrationService administrationService) {
        this.administrationService = administrationService;
    }

    /**
     * Purge of inactive users executed every morning at 4:00 AM
     */
    @Scheduled(cron = "0 0 4 * * ?")
    public void purgeInactiveUsers() {
        administrationService.purgeInactiveUsers(purgeInactivatedUsersAfterDays);
    }

}