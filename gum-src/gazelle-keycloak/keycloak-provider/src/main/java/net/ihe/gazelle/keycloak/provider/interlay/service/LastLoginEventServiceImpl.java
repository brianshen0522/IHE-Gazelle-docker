package net.ihe.gazelle.keycloak.provider.interlay.service;

import net.ihe.gazelle.user.management.api.application.user.login.UserLoginService;
import org.keycloak.models.UserModel;

import java.sql.Timestamp;
import java.time.Instant;

public class LastLoginEventServiceImpl implements LastLoginEventService {

    @Override
    public void eventLoginSuccessful(UserModel user, UserLoginService userLoginService) {
        if (user == null || userLoginService == null)
            throw new IllegalArgumentException("One of the parameters is null");

        userLoginService.updateLastLoginTimestampForUserId(user.getUsername(), Timestamp.from(Instant.now()));
    }

}
