package net.ihe.gazelle.user.management.commons.application.user.edit;

import net.ihe.gazelle.user.management.api.domain.user.User;

import java.util.Locale;

public interface UserEditEmailManager {

    /**
     * Send a mail to the user in order to validate his email
     * @param user the concerned user
     * @param locale the locale used for the email
     */
    void sendMailToValidateNewEmail(User user, Locale locale);

    /**
     * Send a mail to old email to inform of a modification
     * @param user the user linked to the modification
     * @param oldEmail the old email (the recipient)
     * @param locale the locale used for the mail
     */
    void sendMailToOldEmailAddress(User user, String oldEmail, Locale locale);

    /**
     * Send a mail to the user in order inform him that his account is deleted
     * @param user the concerned user
     * @param locale the locale used for the email
     */
    void sendMailToDeletedUser(User user, Locale locale);
}
