package net.ihe.gazelle.user.management.commons.interlay.dao;

import jakarta.persistence.EntityManager;
import net.ihe.gazelle.user.management.commons.application.user.registration.ConsentDAO;
import net.ihe.gazelle.user.management.commons.interlay.dao.entities.ConsentEntity;
import net.ihe.gazelle.user.management.commons.interlay.dao.entities.ConsentHistoryEntity;
import net.ihe.gazelle.user.management.commons.interlay.dao.entities.UserEntity;

import java.sql.Timestamp;
import java.util.List;

import static net.ihe.gazelle.user.management.commons.interlay.dao.user.UserEditDAOImpl.USER_ID;

/**
 * Implementation of the ConsentDAO interface for managing user consents using JPA.
 */
public class ConsentDAOImpl implements ConsentDAO {

    private static final String OPT_IN = "OPT_IN";
    private final EntityManager em;

    /**
     * Constructor for ConsentDAOImpl, initializing the EntityManager for database operations.
     * @param em the EntityManager to be used for database interactions
     */
    public ConsentDAOImpl(EntityManager em) {
        this.em = em;
    }

    @Override
    public void acceptUserConsent(String userId) {
        List<ConsentEntity> consentEntities = em.createQuery("select u from ConsentEntity u where u.user.id = :userId", ConsentEntity.class)
                .setParameter(USER_ID, userId)
                .getResultList();
        // If no consent exists for the user, create one
        ConsentEntity consentEntity;
        if (consentEntities.isEmpty()) {
            consentEntity = new ConsentEntity();
            UserEntity user = em.find(UserEntity.class,userId);
            consentEntity.setUser(user);
            consentEntity.setConsent(true);
            em.persist(consentEntity);
        } else {
            // If a consent exists, update it
            consentEntity = consentEntities.get(0);
            consentEntity.setConsent(true);
            em.merge(consentEntity);
        }

        // Create a consent history entry
        ConsentHistoryEntity consentHistoryEntity = new ConsentHistoryEntity();
        consentHistoryEntity.setDecision(OPT_IN);
        consentHistoryEntity.setTimestamp(new Timestamp(System.currentTimeMillis()));
        consentHistoryEntity.setConsent(consentEntity);
        em.persist(consentHistoryEntity);
    }

    @Override
    public boolean needToGiveConsent(String userId) {
        List<ConsentEntity> consentEntities = em.createQuery("select c from ConsentEntity c where c.user.id = :userId", ConsentEntity.class)
                .setParameter(USER_ID, userId)
                .getResultList();
        if (consentEntities.isEmpty())
            return true;
        return !consentEntities.get(0).consent();
    }
}
