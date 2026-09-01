package net.ihe.gazelle.user.management.core.interlay.transactional;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import net.ihe.gazelle.user.management.api.domain.user.User;
import net.ihe.gazelle.user.management.commons.application.user.registration.UserRegistrationDAO;
import net.ihe.gazelle.user.management.commons.interlay.dao.user.UserRegistrationDAOImpl;

import java.util.List;

/**
 * Transactional wrapper for UserRegistrationDAO, ensuring that all operations are executed within a transaction context.
 */
@RequestScoped
public class UserRegistrationDAOTransactional implements UserRegistrationDAO {

    private final UserRegistrationDAO userRegistrationDAO;

    /**
     * Constructor that initializes the UserRegistrationDAO with an EntityManager, allowing for database interactions.
     * @param entityManager the EntityManager to be used for database operations
     */
    @Inject
    public UserRegistrationDAOTransactional(EntityManager entityManager) {
        this.userRegistrationDAO = new UserRegistrationDAOImpl(entityManager);
    }

    @Override
    @Transactional
    public User registerUser(User user) {
        return userRegistrationDAO.registerUser(user);
    }

    @Override
    @Transactional
    public User activateUserWithActivationCode(String activationCode) {
        return userRegistrationDAO.activateUserWithActivationCode(activationCode);
    }

    @Override
    public boolean isEmailAlreadyExist(String email) {
        return userRegistrationDAO.isEmailAlreadyExist(email);
    }

    @Override
    public List<User> getActiveAdminsOfOrganization(String organizationId) {
        return userRegistrationDAO.getActiveAdminsOfOrganization(organizationId);
    }

    @Override
    public int getAllUsersCount() {
        return userRegistrationDAO.getAllUsersCount();
    }

    @Override
    @Transactional
    public void rollbackUserRegistration(String userId) {
        userRegistrationDAO.rollbackUserRegistration(userId);
    }
}
