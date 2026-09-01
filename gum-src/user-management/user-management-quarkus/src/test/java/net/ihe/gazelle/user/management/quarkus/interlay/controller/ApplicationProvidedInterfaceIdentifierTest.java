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

import net.ihe.gazelle.servicemetadata.api.business.Binding;
import net.ihe.gazelle.servicemetadata.api.business.HttpRestBinding;
import net.ihe.gazelle.servicemetadata.api.business.ProvidedInterface;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApplicationProvidedInterfaceIdentifierTest {

    @Test
    void testGetProvidedInterface() {
        ApplicationProvidedInterfaceIdentifier identifier = new ApplicationProvidedInterfaceIdentifier();
        ProvidedInterface providedInterface = identifier.getProvidedInterface();

        assertEquals("User Management API", providedInterface.getInterfaceName());
        assertEquals("1.0", providedInterface.getInterfaceVersion());

        assertEquals(1, providedInterface.getBindings().size());
        Binding binding = providedInterface.getBindings().getFirst();
        HttpRestBinding httpRestBinding = (HttpRestBinding) binding;
        assertTrue(httpRestBinding.getServiceUrl().endsWith("/gum/rest"));

    }
}
