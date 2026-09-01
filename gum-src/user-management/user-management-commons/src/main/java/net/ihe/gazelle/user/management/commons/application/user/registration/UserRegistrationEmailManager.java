package net.ihe.gazelle.user.management.commons.application.user.registration;

import net.ihe.gazelle.user.management.api.domain.user.User;

import java.util.List;
import java.util.Locale;

/**
 * Component used to send email to new users
 */
public interface UserRegistrationEmailManager {

    /**
     * Send email to the user to activate his account
     *
     * @param user             the user concerned by the activation and to send the email
     * @param organizationName the name of the organization where the user will be affiliated
     * @param locale           the locale used to send emails
     * @throws ActivationEmailManagerException if an error occurs during the sending of the email
     */
    void sendMailToActivateUserItself(User user, String organizationName, Locale locale);

    /**
     * Send email to a list of vendors to activate the account of the user
     *
     * @param user                     the user concerned by the activation
     * @param organizationName         the name of the organization where the user will be affiliated
     * @param listOfVendorAdminsEmails the list of emails of the vendor admins to send the email
     * @param locale                   the locale used to send emails
     * @throws ActivationEmailManagerException if an error occurs during the sending of the email
     */
    void sendMailActiveUserToAllVendorAdmin(User user, String organizationName, List<String> listOfVendorAdminsEmails, Locale locale);

    /**
     * Send email to a user created by an admin
     *
     * @param user   the user concerned by the creation
     * @param locale the locale used to send emails
     * @throws ActivationEmailManagerException if an error occurs during the sending of the email
     */
    void sendMailToNewUserCreatedByAdmin(User user, Locale locale);
}
