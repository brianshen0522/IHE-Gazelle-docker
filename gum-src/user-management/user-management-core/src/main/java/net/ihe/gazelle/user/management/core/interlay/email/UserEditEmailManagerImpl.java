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
import net.ihe.gazelle.user.management.commons.application.user.edit.UserEditEmailManager;
import net.ihe.gazelle.user.management.commons.application.user.registration.ActivationEmailManagerException;
import net.ihe.gazelle.user.management.commons.interlay.email.ActivationLinkBuilder;
import net.ihe.gazelle.user.management.core.interlay.translation.TranslationResolverMethod;
import net.ihe.gazelle.user.management.core.interlay.translation.TranslationsClasspathProvider;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Implementation of the UserEditEmailManager interface that handles sending emails related to user email changes and account deletion.
 */
@ApplicationScoped
public class UserEditEmailManagerImpl implements UserEditEmailManager {

    /** Error message constant for template processing errors. */
    public static final String ERROR_WHILE_PROCESSING_TEMPLATE = "Error while processing template";
    /** Error message constant for email sending errors. */
    public static final String ERROR_WHILE_SENDING_EMAIL = "Error while sending email";

    private final Mailer mailer;
    private final ApplicationConfig applicationConfig;
    private final TranslationsClasspathProvider translationsProvider;

    @TemplatePath("text/mailToActivateUserAfterUpdateEmail.ftl")
    Template textMailToActivateNewEmail;
    @TemplatePath("html/mailToActivateUserAfterUpdateEmail.ftl")
    Template htmlMailToActivateNewEmail;
    @TemplatePath("text/mailToNotifyOldEmail.ftl")
    Template textMailToNotifyOldEmail;
    @TemplatePath("html/mailToNotifyOldEmail.ftl")
    Template htmlMailToNotifyOldEmail;
    @TemplatePath("text/mailToNotifyDeletedAccount.ftl")
    Template textMailToNotifyDeletedAccount;
    @TemplatePath("html/mailToNotifyDeletedAccount.ftl")
    Template htmlMailToNotifyDeletedAccount;

    /**
     * Creates a new UserEditEmailManagerImpl with the given Mailer and ApplicationConfig.
     * @param mailer the Mailer to use for sending emails
     * @param applicationConfig the ApplicationConfig to use for accessing application configuration settings
     */
    @Inject
    public UserEditEmailManagerImpl(Mailer mailer, ApplicationConfig applicationConfig) {
        this.mailer = mailer;
        this.applicationConfig = applicationConfig;
        this.translationsProvider = new TranslationsClasspathProvider();

    }

    @Override
    public void sendMailToValidateNewEmail(User user, Locale locale) {
        Map<String, Object> input = generateParameterMap(user, user.getEmail(), locale);
        ActivationLinkBuilder activationLinkBuilder = new ActivationLinkBuilder(applicationConfig);
        input.put("activationUrl", activationLinkBuilder.buildActivationLink(user.getActivationCode()));


        try {
            StringWriter textStringWriter = new StringWriter();
            StringWriter htmlStringWriter = new StringWriter();
            textMailToActivateNewEmail.process(input, textStringWriter);
            htmlMailToActivateNewEmail.process(input, htmlStringWriter);
            String emailTitle = translationsProvider.getTranslationForMessageKey("net.ihe.gazelle.gum.newEmailConfirmation", locale);
            Mail mail = Mail.withText(user.getEmail(), emailTitle, textStringWriter.toString());
            mail.setHtml(htmlStringWriter.toString());
            mailer.send(mail);
        } catch (TemplateException | IOException e) {
            throw new ActivationEmailManagerException(ERROR_WHILE_PROCESSING_TEMPLATE, e);
        } catch (Exception e) {
            throw new ActivationEmailManagerException(ERROR_WHILE_SENDING_EMAIL, e);
        }
    }

    @Override
    public void sendMailToOldEmailAddress(User user, String oldEmail, Locale locale) {
        Map<String, Object> input = generateParameterMap(user, oldEmail, locale);

        try {
            StringWriter textStringWriter = new StringWriter();
            StringWriter htmlStringWriter = new StringWriter();
            textMailToNotifyOldEmail.process(input, textStringWriter);
            htmlMailToNotifyOldEmail.process(input, htmlStringWriter);

            String emailTitle = translationsProvider.getTranslationForMessageKey("net.ihe.gazelle.gum.yourEmailHasBeenChanged", locale);
            Mail mail = Mail.withText(oldEmail, emailTitle, textStringWriter.toString());
            mail.setHtml(htmlStringWriter.toString());
            mailer.send(mail);
        } catch (TemplateException | IOException e) {
            throw new ActivationEmailManagerException(ERROR_WHILE_PROCESSING_TEMPLATE, e);
        } catch (Exception e) {
            throw new ActivationEmailManagerException(ERROR_WHILE_SENDING_EMAIL, e);
        }
    }

    @Override
    public void sendMailToDeletedUser(User user, Locale locale) {
        Map<String, Object> input = generateParameterMap(user, user.getEmail(), locale);

        try {
            StringWriter textStringWriter = new StringWriter();
            StringWriter htmlStringWriter = new StringWriter();
            textMailToNotifyDeletedAccount.process(input, textStringWriter);
            htmlMailToNotifyDeletedAccount.process(input, htmlStringWriter);

            String emailTitle = translationsProvider.getTranslationForMessageKey("net.ihe.gazelle.gum.yourAccountHasBeenDeleted", locale);
            Mail mail = Mail.withText(user.getEmail(), emailTitle, textStringWriter.toString());
            mail.setHtml(htmlStringWriter.toString());
            mailer.send(mail);
        } catch (TemplateException | IOException e) {
            throw new ActivationEmailManagerException(ERROR_WHILE_PROCESSING_TEMPLATE, e);
        } catch (Exception e) {
            throw new ActivationEmailManagerException(ERROR_WHILE_SENDING_EMAIL, e);
        }
    }


    private Map<String, Object> generateParameterMap(User user, String targetEmail, Locale locale) {
        Map<String, Object> input = new HashMap<>();
        input.put("firstname", user.getFirstName());
        input.put("lastname", user.getLastName());
        input.put("email", targetEmail);
        input.put("msg", new TranslationResolverMethod(translationsProvider.getTranslationMap(locale)));
        return input;
    }
}
