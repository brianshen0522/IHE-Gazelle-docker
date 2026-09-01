package net.ihe.gazelle.keycloak.provider.interlay.service;

import net.ihe.gazelle.user.management.api.application.user.login.UserLoginService;
import org.keycloak.models.UserModel;

public interface LastLoginEventService {
    void eventLoginSuccessful(UserModel user, UserLoginService userLoginService);
}
