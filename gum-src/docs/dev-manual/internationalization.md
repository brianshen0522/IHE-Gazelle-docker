# Internationalization

Gazelle is an international project, so we must be able to manage different languages in our applications for a better user experience.

In the GUM projects, we have different resources where the language can be updated like *interfaces* and *email*.

## Interface

GUM is composed of 3 different microservices. Keycloak, GUM REST APIs and user-interface. 

The user interface is managed by user-management module so all the internalization system is in the associated [Next-JS project](https://gitlab.inria.fr/gazelle/private/kereval/gazelle-user-interface).

## Emails

For emails, it's a bit more complex because both Keycloak and GUM API module can send emails so there are 2 different solutions for the internationalization of the mails.

### In Keycloak

In Keycloak we based our solution on Keycloak implementation. Thanks to `EmailTemplateProvider`, we can directly add `.ftl` template with property files for translation in `keycloak-theme` module. 

Keycloak will directly replace the occurrences of `msg("translationKey")` in the `.ftl` template in the correct language.

The translation files must match the pattern "messages_<locale>.properties".

Example of content for the file `messages_en.properties` : 

```properties
net.ihe.gazelle.gum.accountCreatedSubject=A Gazelle account has been created for you
net.ihe.gazelle.gum.accountBlockedSubject=Your Gazelle Account is temporarily blocked
net.ihe.gazelle.gum.accountInactiveSubject=Your Gazelle account is not active yet
[...]
```

Keycloak base the locale (the language) to use on different criteria. Refer to the following documentation for more details : 

[https://www.keycloak.org/docs/latest/server_admin/#_user_locale_selection](https://www.keycloak.org/docs/latest/server_admin/#_user_locale_selection)

### In GUM REST API module

For our GUM microservice, we implemented an i18n solution based on Keycloak one. A FreeMarkerTemplate extension is available for Quarkus project. 

Using this extension we reproduce the Keycloak functioning with `.ftl` templates and `.properties` files.

For the moment the implementation is localized in the `net.ihe.gazelle.interlay.translation` package of the `user-management-core` module.

If one day, other application need to internationalize some resource like email or report, we will need to move this implementation in a dedicated library.


In UserController we can see that to find the Locale, we start by searching the header `Accept-language` which is a standardized header for browser to indicate to the server the language of the requested pages.

If no locale are found in the header, we search in cookies, a cookie name `KEYCLOAK_LOCALE` which is a Keycloak cookie to give information on the locale of the user.

If no locale are found in the cookies, we take the default locale value present in the JVM.
Refer to the JVM documentation to understand how the locale of the JVM is set but it depends on the OS of the system. (https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Locale.html)

## To be improved

- For the moment, properties files containing translations need to be fill in manually by a developer and any translation update need a release of GUM to be effective.

- If at least one translation is missing, the template will not be able to proceed and so the resource could not be build. An exception will be raised by FreeMarkerTemplate.

- The integration with a solution like `Crowdin` could give to translator the possibility to translate elements more easily and the integration in code source is also more simple.

If you have any other questions regarding the internationalization system of GUM don't hesitate to contact the developer (Valentin Lorand & Nicolas Ronceray).

