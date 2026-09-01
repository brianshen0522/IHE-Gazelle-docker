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

package net.ihe.gazelle.user.management.core.interlay.email;

import net.ihe.gazelle.user.management.core.interlay.translation.TranslationsClasspathProvider;
import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TranslationsClassPathProviderTest {

    public static final String TRANSLATION_KEYWORD = "net.ihe.gazelle.translation";

    @Test
    void testGetSingleTranslation() {
        TranslationsClasspathProvider translationClassPathProvider = new TranslationsClasspathProvider();

        String translation = translationClassPathProvider.getTranslationForMessageKey(TRANSLATION_KEYWORD, Locale.FRANCE);
        assertEquals("traduction", translation);
    }

    @Test
    void testGetTranslationMap() {
        TranslationsClasspathProvider translationClassPathProvider = new TranslationsClasspathProvider();

        Map<String, String> translations = translationClassPathProvider.getTranslationMap(Locale.FRANCE);
        assertEquals("traduction", translations.get(TRANSLATION_KEYWORD));
    }

}
