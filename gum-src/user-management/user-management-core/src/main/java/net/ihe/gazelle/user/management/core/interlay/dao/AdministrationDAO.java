package net.ihe.gazelle.user.management.core.interlay.dao;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import net.ihe.gazelle.user.management.commons.application.user.edit.UserEditDAO;
import net.ihe.gazelle.user.management.commons.interlay.dao.entities.UserEntity;

import java.sql.Timestamp;
import java.util.List;

/**
 * DAO class for administration operations related to user management, such as purging inactive and non-consent users.
 */
@RequestScoped
public class AdministrationDAO {

    private final EntityManager entityManager;
    private final UserEditDAO userEditDAO;

    /**
     * Constructs an AdministrationDAO with the specified EntityManager.
     * @param entityManager the EntityManager used for database operations
     * @param userEditDAO the UserEditDAO used for user edit operations
     */
    @Inject
    public AdministrationDAO(EntityManager entityManager, UserEditDAO userEditDAO) {
        this.entityManager = entityManager;
        this.userEditDAO = userEditDAO;
    }

    /**
     * Purges inactive and non-consent users from the database based on the specified registration timestamp limit.
     * This method deletes users who are disabled, have zero login count, and registered before the given timestamp,
     * as well as users who have not given consent and have zero login count.
     *
     * @param limitRegistrationTimestamp the timestamp used as a threshold for determining which users to purge
     * @return the total number of users deleted from the database
     */
    @Transactional
    public int purgeInactiveAndNonConsentUsers(Timestamp limitRegistrationTimestamp) {
        // Select users eligible for purge in one query to avoid double fetch and duplicates.
        List<UserEntity> usersToDelete = entityManager.createQuery(
                        "SELECT u FROM UserEntity u WHERE u.registrationTimestamp < :limitRegistrationTimestamp " +
                                "AND u.loginCounter = 0 " +
                                "AND ((u.activated = false AND u.activationCode IS NOT NULL) " +
                                "OR u.id NOT IN (SELECT c.user.id FROM ConsentEntity c WHERE c.consent = true))",
                        UserEntity.class)
                .setParameter("limitRegistrationTimestamp", limitRegistrationTimestamp)
                .getResultList();


        for (UserEntity userToDelete : usersToDelete) {
            userEditDAO.deleteUser(userToDelete.getId());
            userEditDAO.archiveOrgaIfNoMembers(userToDelete.getOrganizationId());
        }

        return usersToDelete.size();
    }
}
