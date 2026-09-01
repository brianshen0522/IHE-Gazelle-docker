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

import freemarker.template.SimpleScalar;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;

import java.util.List;
import java.util.Map;

/**
 * This class implements FreeMarker model that gives the possibility to implement custom methods callable in .ftl templates
 * The exec method with return the value corresponding to the key in a map, an exception is raised is the param doesn't match any key of the map
 */
public class TranslationResolverMethod implements TemplateMethodModelEx {

    private final Map<String, String> translations;

    /**
     * Constructor of the TranslationResolverMethod class, takes a map of translations as parameter
     * @param translations a map of translations where the key is the code to translate and the value is the translation to return when the code is requested
     */
    public TranslationResolverMethod(Map<String, String> translations) {
        this.translations = translations;
    }

    @Override
    public Object exec(List arguments) throws TemplateModelException {
        if (arguments.size() != 1) {
            throw new TemplateModelException("Wrong number of arguments");
        }
        String code = ((SimpleScalar) arguments.getFirst()).getAsString();
        if (code == null || code.isEmpty()) {
            throw new TemplateModelException("Invalid key value '" + code + "', not found in given translations");
        }
        return translations.get(code);
    }
}