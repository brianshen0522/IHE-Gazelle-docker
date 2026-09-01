package net.ihe.gazelle.user.management.core.interlay.email;

import freemarker.template.Template;
import freemarker.template.TemplateException;
import io.quarkiverse.freemarker.TemplatePath;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import net.ihe.gazelle.user.management.api.application.configuration.ApplicationConfig;
import net.ihe.gazelle.user.management.api.domain.user.User;
import net.ihe.gazelle.user.management.commons.application.user.registration.ActivationEmailManagerException;
import net.ihe.gazelle.user.management.commons.application.user.registration.UserRegistrationEmailManager;
import net.ihe.gazelle.user.management.commons.interlay.email.ActivationLinkBuilder;
import net.ihe.gazelle.user.management.core.interlay.translation.TranslationResolverMethod;
import net.ihe.gazelle.user.management.core.interlay.translation.TranslationsClasspathProvider;

import java.io.IOException;
import java.io.StringWriter;
import java.text.DateFormat;
import java.util.*;

import static net.ihe.gazelle.user.management.core.interlay.email.UserEditEmailManagerImpl.ERROR_WHILE_PROCESSING_TEMPLATE;
import static net.ihe.gazelle.user.management.core.interlay.email.UserEditEmailManagerImpl.ERROR_WHILE_SENDING_EMAIL;

/**
 * Implementation of the UserRegistrationEmailManager interface responsible for sending activation emails to users and orga admins.
 */
@ApplicationScoped
public class UserRegistrationEmailManagerImpl implements UserRegistrationEmailManager {

    private static final String ACTIVATION_LIMIT = "activationLimit";
    private final Mailer mailer;
    private final ApplicationConfig applicationConfig;
    private final TranslationsClasspathProvider translationsProvider;

    @TemplatePath("text/mailToActivateUserItself.ftl")
    Template textMailToActivateUserItself;
    @TemplatePath("html/mailToActivateUserItself.ftl")
    Template htmlMailToActivateUserItself;
    @TemplatePath("text/mailToActivateUserByVendorAdmin.ftl")
    Template textMailToActivateUserByVendorAdmin;
    @TemplatePath("html/mailToActivateUserByVendorAdmin.ftl")
    Template htmlMailToActivateUserByVendorAdmin;
    @TemplatePath("text/mailToNewUserCreatedByAdmin.ftl")
    Template textMailToNewUserCreatedByAdmin;
    @TemplatePath("html/mailToNewUserCreatedByAdmin.ftl")
    Template htmlMailToNewUserCreatedByAdmin;

    /**
     * Creates a new instance of UserRegistrationEmailManagerImpl with the specified Mailer and ApplicationConfig.
     * @param mailer the Mailer used to send emails
     * @param applicationConfig the ApplicationConfig providing configuration values for email generation
     */
    @Inject
    public UserRegistrationEmailManagerImpl(Mailer mailer, ApplicationConfig applicationConfig) {
        this.mailer = mailer;
        this.applicationConfig = applicationConfig;
        this.translationsProvider = new TranslationsClasspathProvider();
    }

    /**
     * Sends an activation email to the user itself when they register, containing an activation link and information about the account activation limit.
     * @param user             the user concerned by the activation and to send the email
     * @param organizationName the name of the organization where the user will be affiliated
     * @param locale           the locale used to send emails
     */
    public void sendMailToActivateUserItself(User user, String organizationName, Locale locale) {
        Map<String, Object> input = generateParameterMap(user, organizationName);
        input.put("msg", new TranslationResolverMethod(translationsProvider.getTranslationMap(locale)));
        input.put(ACTIVATION_LIMIT, getAccountActivationLimitFormattedDate(locale));

        try {
            StringWriter textStringWriter = new StringWriter();
            StringWriter htmlStringWriter = new StringWriter();
            textMailToActivateUserItself.process(input, textStringWriter);
            htmlMailToActivateUserItself.process(input, htmlStringWriter);

            String emailTitle = translationsProvider.getTranslationForMessageKey("net.ihe.gazelle.gum.newAccountActivation", locale);
            Mail mail = Mail.withText(user.getEmail(), emailTitle, textStringWriter.toString());
            mail.setHtml(htmlStringWriter.toString());
            mailer.send(mail);
        } catch (TemplateException | IOException e) {
            throw new ActivationEmailManagerException(ERROR_WHILE_PROCESSING_TEMPLATE, e);
        } catch (Exception e) {
            throw new ActivationEmailManagerException(ERROR_WHILE_SENDING_EMAIL, e);
        }
    }

    /**
     * Sends activation emails to all vendor admins of the organization when a new user registers, containing an activation link and information about the account activation limit.
     * @param user                        the user concerned by the activation and to send the email
     * @param organizationName            the name of the organization where the user will be affiliated
     * @param listOfVendorAdminsEmails    the list of vendor admins emails to send the email
     * @param locale                      the locale used to send emails
     */
    @Override
    public void sendMailActiveUserToAllVendorAdmin(User user, String organizationName, List<String> listOfVendorAdminsEmails, Locale locale) {
        listOfVendorAdminsEmails.forEach(vendorAdmin -> sendMailToActivateUserByVendorAdmin(vendorAdmin, user, organizationName, locale));
    }

    /**
     * Sends an email to a user when an admin creates an account for them, containing an activation link and information about the account activation limit.
     * @param user  the user concerned by the activation and to send the email
     * @param locale the locale used to send emails
     */
    @Override
    public void sendMailToNewUserCreatedByAdmin(User user, Locale locale) {
        Map<String, Object> input = generateParameterMap(user, null);
        input.put("msg", new TranslationResolverMethod(translationsProvider.getTranslationMap(locale)));

        String resetPasswordPath = "/realms/gazelle/login-actions/reset-credentials?client_id=gazelle-account";
        input.put("resetPasswordUrl", applicationConfig.getSSOBaseUrl() + resetPasswordPath);
        input.put(ACTIVATION_LIMIT, getAccountActivationLimitFormattedDate(locale));

        try {
            StringWriter textStringWriter = new StringWriter();
            StringWriter htmlStringWriter = new StringWriter();
            textMailToNewUserCreatedByAdmin.process(input, textStringWriter);
            htmlMailToNewUserCreatedByAdmin.process(input, htmlStringWriter);

            String emailTitle = translationsProvider.getTranslationForMessageKey("net.ihe.gazelle.gum.newAccountCreated", locale);
            Mail mail = Mail.withText(user.getEmail(), emailTitle, textStringWriter.toString());
            mail.setHtml(htmlStringWriter.toString());
            mailer.send(mail);
        } catch (TemplateException | IOException e) {
            throw new ActivationEmailManagerException(ERROR_WHILE_PROCESSING_TEMPLATE, e);
        } catch (Exception e) {
            throw new ActivationEmailManagerException(ERROR_WHILE_SENDING_EMAIL, e);
        }
    }

    private void sendMailToActivateUserByVendorAdmin(String emailVendorAdmin, User user, String organizationName, Locale locale) {
        Map<String, Object> input = generateParameterMap(user, organizationName);
        input.put("msg", new TranslationResolverMethod(translationsProvider.getTranslationMap(locale)));
        input.put(ACTIVATION_LIMIT, getAccountActivationLimitFormattedDate(locale));

        try {
            StringWriter textStringWriter = new StringWriter();
            StringWriter htmlStringWriter = new StringWriter();
            textMailToActivateUserByVendorAdmin.process(input, textStringWriter);
            htmlMailToActivateUserByVendorAdmin.process(input, htmlStringWriter);

            String emailTitle = translationsProvider.getTranslationForMessageKey("net.ihe.gazelle.gum.newAccountActivation", locale);
            Mail mail = Mail.withText(emailVendorAdmin, emailTitle, textStringWriter.toString());
            mail.setHtml(htmlStringWriter.toString());
            mailer.send(mail);
        } catch (TemplateException | IOException e) {
            throw new ActivationEmailManagerException(ERROR_WHILE_PROCESSING_TEMPLATE, e);
        } catch (Exception e) {
            throw new ActivationEmailManagerException(ERROR_WHILE_SENDING_EMAIL, e);
        }
    }

    private Map<String, Object> generateParameterMap(User user, String organizationName) {
        Map<String, Object> input = new HashMap<>();

        // Manage activation url (Use GUM UI if possible otherwise use directly the rest api)
        ActivationLinkBuilder activationLinkBuilder = new ActivationLinkBuilder(applicationConfig);

        // Fill the map
        input.put("firstname", user.getFirstName());
        input.put("lastname", user.getLastName());
        input.put("email", user.getEmail());
        input.put("organizationName", organizationName);
        input.put("activationUrl", activationLinkBuilder.buildActivationLink(user.getActivationCode()));
        return input;
    }

    private String getAccountActivationLimitFormattedDate(Locale locale) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, applicationConfig.getPurgeInactivatedUsersDaysLimit());
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.LONG, locale);
        return dateFormat.format(cal.getTime());
    }
}
