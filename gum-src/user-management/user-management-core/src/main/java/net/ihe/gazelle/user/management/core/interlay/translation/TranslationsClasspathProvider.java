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

package net.ihe.gazelle.user.management.core.interlay.translation;

import net.ihe.gazelle.user.management.core.application.translation.TranslationsProvider;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

/**
 * This implementation provide translations present in classpath resources of the folder messages/.
 * The property files must respect the following name : messages_{locale}.properties
 * Example of path for an english translation file : messages/messages_en.properties
 */
public class TranslationsClasspathProvider implements TranslationsProvider {

    Map<String, Properties> propertiesMap = new HashMap<>();

    /** Default constructor */
    public TranslationsClasspathProvider() {
        // Nothing to do here
    }

    @Override
    public String getTranslationForMessageKey(String messageKey, Locale locale) {
        Properties properties = getMessagesForLocale(locale);
        return !properties.isEmpty() ? (String) properties.get(messageKey) : null;
    }

    @Override
    public Map<String, String> getTranslationMap(Locale locale) {
        Map<String, String> result = getMapOfTranslationsFromLocale(locale);
        if (result.isEmpty())
            result = getMapOfTranslationsFromLocale(Locale.getDefault());
        if (result.isEmpty())
            result = getMapOfTranslationsFromLocale(Locale.ENGLISH);

        return result;
    }

    private Map<String, String> getMapOfTranslationsFromLocale(Locale locale) {
        Properties properties = getMessagesForLocale(locale);
        return (Map) properties;
    }


    private Properties getMessagesForLocale(Locale locale) {
        Properties result = propertiesMap.get(locale.getLanguage());
        if (result == null) {
            try {
                result = new Properties();
                result.load(this.getClass().getResourceAsStream("/messages/messages" + "_" + locale.getLanguage() + ".properties"));
                propertiesMap.put(locale.getLanguage(), result);
            } catch (IOException e) {
                propertiesMap.put(locale.getLanguage(), null);
                return new Properties();
            }
        }
        return result;
    }
}
