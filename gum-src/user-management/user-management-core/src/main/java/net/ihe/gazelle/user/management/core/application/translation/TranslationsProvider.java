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

package net.ihe.gazelle.user.management.core.application.translation;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Interface for providing translations of messages based on a key and a locale.
 * This interface defines methods to retrieve specific translations as well as all translations for a given locale.
 */
public interface TranslationsProvider {

    /** List of supported locales for translations */
    List<Locale> SUPPORTED_LOCALES = List.of(Locale.FRENCH, Locale.ENGLISH, Locale.ITALIAN);

    /**
     * Retrieve a translation from the key and the locale (null if not exist)
     * @param messageKey the key of the translation (ex: net.ihe.gazelle.example)
     * @param locale the locale corresponding to the language of the translation
     * @return a string corresponding to the translation, null if the translation does not exist
     */
    String getTranslationForMessageKey(String messageKey, Locale locale);

    /**
     * Retrieve all the available translations for the given locale
     * @param locale the locale corresponding to the language of the translations
     * @return a map containing for each keys the corresponding translation in the given locale
     */
    Map<String,String> getTranslationMap(Locale locale);
}
