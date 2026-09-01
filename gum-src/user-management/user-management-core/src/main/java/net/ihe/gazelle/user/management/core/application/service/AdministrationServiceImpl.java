package net.ihe.gazelle.user.management.core.application.service;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import net.ihe.gazelle.user.management.core.interlay.dao.AdministrationDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

/**
 * Implementation of the AdministrationService interface that provides administrative operations for user management.
 * This service includes operations such as purging inactive users from the system. It uses the AdministrationDAO
 * to perform database operations related to administration tasks.
 */
@RequestScoped
public class AdministrationServiceImpl implements AdministrationService {

    private static final Logger log = LoggerFactory.getLogger(AdministrationServiceImpl.class);

    private final AdministrationDAO administrationDAO;

    /**
     * Creates a new instance of AdministrationServiceImpl with the given dependencies.
     * @param administrationDAO the AdministrationDAO to use for accessing administration-related data in the database
     */
    @Inject
    public AdministrationServiceImpl(AdministrationDAO administrationDAO) {
        this.administrationDAO = administrationDAO;
    }

    @Override
    public void purgeInactiveUsers(int limitDays) {
        Timestamp limitTimestamp = getCurrentTimestampPlusDays(-limitDays);
        int numberOfDeletedUsers = administrationDAO.purgeInactiveAndNonConsentUsers(limitTimestamp);

        log.warn("Purge inactive users done, {} users deleted", numberOfDeletedUsers);
    }

    /**
     * Computes a Timestamp that is the current time plus a specified number of days.
     * @param days the number of days to add to the current time (can be negative to subtract days)
     * @return a Timestamp representing the current time plus the specified number of days
     */
    public static Timestamp getCurrentTimestampPlusDays(int days) {
        // Compute timestamp limit, month ago
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, days);
        Date limitDate = cal.getTime();
        return new Timestamp(limitDate.getTime());
    }
}
