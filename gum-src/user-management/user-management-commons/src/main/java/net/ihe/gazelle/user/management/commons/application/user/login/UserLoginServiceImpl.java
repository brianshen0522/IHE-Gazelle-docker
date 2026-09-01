package net.ihe.gazelle.user.management.commons.application.user.login;

import net.ihe.gazelle.user.management.api.domain.user.Credentials;
import net.ihe.gazelle.user.management.commons.application.exception.ErrorMessage;

import java.sql.Timestamp;
import java.util.NoSuchElementException;
import java.util.Optional;

public class UserLoginServiceImpl implements net.ihe.gazelle.user.management.api.application.user.login.UserLoginService {

    private final UserLoginDAO userLoginDAO;

    private final HashPasswordServiceProvider hashPasswordServiceProvider;

    public UserLoginServiceImpl(UserLoginDAO userLoginDAO, HashPasswordServiceProvider hashPasswordServiceProvider) {
        this.userLoginDAO = userLoginDAO;
        this.hashPasswordServiceProvider = hashPasswordServiceProvider;
    }

    @Override
    public boolean validatePassword(String userId, String password) {
        if (userId == null) throw new IllegalArgumentException("userId is null");
        if (password == null || password.isBlank()) throw new IllegalArgumentException("password is null");

        // Retrieve the current credentials for the user
        try {
            Credentials currentCredentials = userLoginDAO.getCredentialsForUserId(userId);

            // Retrieve the hash method used for the user
            String hashMethodName = currentCredentials.getHashMethod();

            // Usage of SPI to retrieve the correct implementation corresponding to the hash method stored in Credentials
            Optional<HashPasswordService> hashPasswordService = hashPasswordServiceProvider.getHashPasswordService(hashMethodName);
            if (hashPasswordService.isEmpty())
                throw new IllegalArgumentException("hashMethod is not supported");

            return hashPasswordService.get().verify(currentCredentials, password);

        } catch (NoSuchElementException _) {
            // If no credentials found, the password is not valid
            return false;
        }
    }

    @Override
    public void updateLastLoginTimestampForUserId(String userId, Timestamp timestamp) {
        if (userId == null) throw new IllegalArgumentException(ErrorMessage.USER_ID_IS_NULL.getMessage());
        if (timestamp == null) throw new IllegalArgumentException("timestamp is null");

        userLoginDAO.updateLoginMetricsForUserId(userId, timestamp);
    }


    @Override
    public boolean needToChangePassword(String userId) {
        if (userId == null) throw new IllegalArgumentException(ErrorMessage.USER_ID_IS_NULL.getMessage());

        return userLoginDAO.needToChangePassword(userId);
    }
}
