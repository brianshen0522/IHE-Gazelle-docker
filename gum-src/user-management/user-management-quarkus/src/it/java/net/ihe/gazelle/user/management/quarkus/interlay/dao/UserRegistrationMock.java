package net.ihe.gazelle.user.management.quarkus.interlay.dao;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

@RequestScoped
public class UserRegistrationMock {

    @Inject
    EntityManager entityManager;

    @Transactional
    public int updateRegistrationDateByUserId(String userId, long registrationDateTimestamp) {
       return entityManager.createQuery("UPDATE UserEntity u SET u.registrationTimestamp = :registrationDateTimestamp" +
                " WHERE u.id = :userId")
                .setParameter("registrationDateTimestamp", registrationDateTimestamp)
                .setParameter("userId", userId)
                .executeUpdate();
    }
}
