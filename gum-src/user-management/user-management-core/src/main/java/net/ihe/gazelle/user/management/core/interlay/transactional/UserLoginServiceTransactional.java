package net.ihe.gazelle.user.management.core.interlay.transactional;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import net.ihe.gazelle.user.management.api.application.user.login.UserLoginService;
import net.ihe.gazelle.user.management.commons.application.user.login.HashPasswordServiceProvider;
import net.ihe.gazelle.user.management.commons.application.user.login.UserLoginDAO;
import net.ihe.gazelle.user.management.commons.application.user.login.UserLoginServiceImpl;

import java.sql.Timestamp;

/**
 * Transactional implementation of UserLoginService that delegates to a non-transactional implementation.
 */
@RequestScoped
public class UserLoginServiceTransactional implements UserLoginService {

    private final UserLoginService userLoginService;

    /**
     * Creates a new UserLoginServiceTransactional with the given UserLoginDAO and HashPasswordServiceProvider.
     * @param userLoginDAO the UserLoginDAO to use for database operations
     * @param hashPasswordServiceProvider the HashPasswordServiceProvider to use for password hashing
     */
    @Inject
    public UserLoginServiceTransactional(UserLoginDAO userLoginDAO, HashPasswordServiceProvider hashPasswordServiceProvider) {
        this.userLoginService = new UserLoginServiceImpl(userLoginDAO, hashPasswordServiceProvider);
    }

    @Override
    public boolean validatePassword(String userId, String password) {
        return userLoginService.validatePassword(userId, password);
    }

    @Override
    @Transactional
    public void updateLastLoginTimestampForUserId(String userId, Timestamp timestamp) {
        userLoginService.updateLastLoginTimestampForUserId(userId, timestamp);
    }

    @Override
    public boolean needToChangePassword(String userId) {
        return userLoginService.needToChangePassword(userId);
    }
}
