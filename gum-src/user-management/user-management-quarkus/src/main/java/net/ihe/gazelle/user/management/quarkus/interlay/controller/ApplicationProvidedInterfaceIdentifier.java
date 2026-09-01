/*
 * Copyright 2026 IHE International.
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

import net.ihe.gazelle.servicemetadata.api.business.HttpRestBindingBuilder;
import net.ihe.gazelle.servicemetadata.api.business.ProvidedInterface;
import net.ihe.gazelle.servicemetadata.api.business.ProvidedInterfaceBuilder;
import net.ihe.gazelle.servicemetadata.api.business.SecuredMethod;
import net.ihe.gazelle.servicemetadata.api.technical.ProvidedInterfaceIdentifier;

import java.util.Objects;
import java.util.Set;

/**
 * Builds the provided interface metadata for the User Management API.
 */
public class ApplicationProvidedInterfaceIdentifier implements ProvidedInterfaceIdentifier {

    private final String gumBaseUrl;

    /**
     * Creates the identifier using the ROOT_TEST_BED_URL environment variable.
     */
    public ApplicationProvidedInterfaceIdentifier() {
        String baseUrl = Objects.requireNonNull(System.getenv("ROOT_TEST_BED_URL"), "Environment variable ROOT_TEST_BED_URL must not be null.");
        gumBaseUrl = baseUrl + "/gum";
    }

    @Override
    public ProvidedInterface getProvidedInterface() {
        return new ProvidedInterfaceBuilder()
                .setInterfaceName("User Management API")
                .setInterfaceVersion("1.0")
                .setBindings(Set.of(
                        new HttpRestBindingBuilder()
                                .setServiceUrl(gumBaseUrl + "/rest")
                                .setSecuredMethods(Set.of(SecuredMethod.M2M, SecuredMethod.OIDC))
                ))
                .build();
    }
}