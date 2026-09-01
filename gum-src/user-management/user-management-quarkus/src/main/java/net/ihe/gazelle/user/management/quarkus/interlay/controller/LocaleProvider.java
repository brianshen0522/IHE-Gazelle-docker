/*
 * Copyright 2024 IHE International.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.ihe.gazelle.user.management.quarkus.interlay.controller;

import jakarta.ws.rs.core.HttpHeaders;

import java.util.List;
import java.util.Locale;

import static net.ihe.gazelle.user.management.core.application.translation.TranslationsProvider.SUPPORTED_LOCALES;

/**
 * Resolves the locale to use from HTTP headers and cookies.
 */
public class LocaleProvider {

    /** Name of the cookie storing the Keycloak locale. */
    public static final String LOCALE_COOKIE_NAME = "KEYCLOAK_LOCALE";

    /**
     * Creates a locale provider.
     */
    public LocaleProvider() {
        // Default constructor
    }

    /**
     * Retrieve the locale to use in services from the context (headers or cookies)
     * If the locale can't be found, usage of the default JVM locale.
     *
     * @param headers HTTP headers containing cookies and language preferences
     * @return the locale
     */
    public Locale getLocaleFromHeaders(HttpHeaders headers) {
        String cookieValue = headers.getCookies().containsKey(LOCALE_COOKIE_NAME) ? headers.getCookies().get(LOCALE_COOKIE_NAME).getValue() : null;
        Locale locale = cookieValue != null ? Locale.of(cookieValue) : null;

        if (locale == null) {
            String headerValue = headers.getHeaderString(HttpHeaders.ACCEPT_LANGUAGE);
            if (headerValue != null) {
                List<Locale.LanguageRange> list = Locale.LanguageRange.parse(headerValue);
                locale = Locale.lookup(list,SUPPORTED_LOCALES);
            }
        }

        if (locale == null)
            locale = Locale.getDefault();

        return locale;
    }
}
