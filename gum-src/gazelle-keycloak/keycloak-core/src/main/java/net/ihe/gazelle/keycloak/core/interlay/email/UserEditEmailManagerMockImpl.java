package net.ihe.gazelle.keycloak.core.interlay.email;

import net.ihe.gazelle.user.management.api.domain.user.User;
import net.ihe.gazelle.user.management.commons.application.user.edit.UserEditEmailManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

public class UserEditEmailManagerMockImpl implements UserEditEmailManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserEditEmailManagerMockImpl.class);


    @Override
    public void sendMailToValidateNewEmail(User user, Locale locale) {
        LOGGER.warn("Email to valid new user email not send because user has been updated in Keycloak");
    }

    @Override
    public void sendMailToOldEmailAddress(User user, String oldEmail, Locale locale) {
        LOGGER.warn("Email to notify email changes not send because user has been updated in Keycloak");
    }

    @Override
    public void sendMailToDeletedUser(User user, Locale locale) {
        LOGGER.warn("Email to notify deleted account not send because user has been deleted from Keycloak");
    }
}
