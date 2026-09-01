package net.ihe.gazelle.keycloak.core.interlay.email;

import net.ihe.gazelle.user.management.api.domain.user.User;
import net.ihe.gazelle.user.management.commons.application.user.registration.UserRegistrationEmailManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Locale;

/**
 * Mock implementation of UserRegistrationEmailManager for testing purposes.
 * This implementation does not send any emails and logs warnings instead.
 */
public class UserRegistrationEmailManagerMockImpl implements UserRegistrationEmailManager {

    private final Logger log = LoggerFactory.getLogger(UserRegistrationEmailManagerMockImpl.class);

    @Override
    public void sendMailToActivateUserItself(User user, String organizationName, Locale locale) {
        // We do nothing here because user has been created in Keycloak
        log.warn("Email to activate user itself not send because user has been created in Keycloak");
    }

    @Override
    public void sendMailActiveUserToAllVendorAdmin(User user, String organizationName, List<String> listOfVendorAdminsEmails, Locale locale) {
        // We do nothing here because user has been created in Keycloak
        log.warn("Emails to activate user by vendor admins not send because user has been created in Keycloak");
    }

    @Override
    public void sendMailToNewUserCreatedByAdmin(User user, Locale locale) {
        log.warn("Email for new user not sent because user has been created from Keycloak");
    }
}
