/*
 * Copyright 2022-2025 IHE International.
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

package net.ihe.gazelle.serviceregistry.technical.rest;

import net.ihe.gazelle.servicemetadata.api.business.HttpRestBindingBuilder;
import net.ihe.gazelle.servicemetadata.api.business.ProvidedInterface;
import net.ihe.gazelle.servicemetadata.api.business.ProvidedInterfaceBuilder;
import net.ihe.gazelle.servicemetadata.api.business.SecuredMethod;
import net.ihe.gazelle.servicemetadata.api.technical.ProvidedInterfaceIdentifier;
import net.ihe.gazelle.serviceregistry.api.business.lookup.ServiceLookupIdentifier;
import org.eclipse.microprofile.config.ConfigProvider;

import java.util.Set;

import static net.ihe.gazelle.serviceregistry.technical.config.ServiceRegistryMicroprofileConfiguration.GZL_SERVICE_REGISTRY_URL;

/**
 * Identifier of the provided Service Lookup API.
 */
public class ServiceLookupAPIIdentifier implements ProvidedInterfaceIdentifier {

    private final String registryUrl;

    /**
     * Default constructor. Initializes the registry URL from the configuration.
     */
    public ServiceLookupAPIIdentifier() {
        this(
                ConfigProvider.getConfig()
                        .getOptionalValue(GZL_SERVICE_REGISTRY_URL, String.class)
                        .orElseThrow(() -> new IllegalStateException(
                                "Service registry URL not configured. Please set the '"
                                        + GZL_SERVICE_REGISTRY_URL + "' property."
                        ))
        );
    }

    /**
     * Constructor with Service Registry URL parameter.
     *
     * @param registryUrl URL of Service Registry.
     */
    public ServiceLookupAPIIdentifier(String registryUrl) {
        this.registryUrl = registryUrl;
    }

    @Override
    public ProvidedInterface getProvidedInterface() {
        return new ProvidedInterfaceBuilder()
                .setInterfaceName(ServiceLookupIdentifier.INTERFACE_NAME)
                .setInterfaceVersion(ServiceLookupIdentifier.INTERFACE_VERSION)
                .setBindings(Set.of(
                        new HttpRestBindingBuilder()
                                .setServiceUrl(registryUrl)
                                .setSecuredMethods(Set.of(SecuredMethod.M2M))
                ))
                .build();
    }

}
