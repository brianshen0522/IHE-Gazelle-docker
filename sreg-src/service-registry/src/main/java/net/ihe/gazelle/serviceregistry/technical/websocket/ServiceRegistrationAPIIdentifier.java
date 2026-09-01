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

package net.ihe.gazelle.serviceregistry.technical.websocket;

import net.ihe.gazelle.servicemetadata.api.business.*;
import net.ihe.gazelle.servicemetadata.api.technical.ProvidedInterfaceIdentifier;
import net.ihe.gazelle.serviceregistry.api.business.ServiceRegistrationIdentifier;
import org.eclipse.microprofile.config.ConfigProvider;

import java.util.Set;

import static net.ihe.gazelle.serviceregistry.technical.config.ServiceRegistryMicroprofileConfiguration.GZL_SERVICE_REGISTRY_URL;

/**
 * Identifier of the provided Service Registration API.
 * This class provides the WebSocket URL for the Service Registration API.
 */
public class ServiceRegistrationAPIIdentifier implements ProvidedInterfaceIdentifier {

    private final String serviceRegistryUrl;
    private final String webSocketUrl;

    /**
     * Default constructor.
     * Initializes the WebSocket URL from the configuration.
     */
    public ServiceRegistrationAPIIdentifier() {
        // Web Socket URL only works locally for now.
        this(ConfigProvider.getConfig()
                .getOptionalValue(GZL_SERVICE_REGISTRY_URL, String.class)
                .orElseThrow(() -> new IllegalStateException(
                        "Service registry URL not configured. Please set the '"
                                + GZL_SERVICE_REGISTRY_URL + "' property."
                ))
        );
    }

    /**
     * Constructor with Service Registry URL parameter
     *
     * @param serviceRegistryUrl URL of Service Registry
     */
    public ServiceRegistrationAPIIdentifier(String serviceRegistryUrl) {
        this.serviceRegistryUrl = serviceRegistryUrl;
        this.webSocketUrl = serviceRegistryUrl.replaceAll("^http", "ws");
    }

    @Override
    public ProvidedInterface getProvidedInterface() {
        return new ProvidedInterfaceBuilder()
                .setInterfaceName(ServiceRegistrationIdentifier.INTERFACE_NAME)
                .setInterfaceVersion(ServiceRegistrationIdentifier.INTERFACE_VERSION)
                .setBindings(Set.of(
                        new WebSocketBindingBuilder()
                                .setWebSocketUrl(webSocketUrl)
                                .setSecuredMethods(Set.of(SecuredMethod.OIDC, SecuredMethod.M2M)),
                        new HttpRestBindingBuilder()
                                .setServiceUrl(serviceRegistryUrl)
                                .setSecuredMethods(Set.of(SecuredMethod.OIDC, SecuredMethod.M2M))
                ))
                .build();
    }

}
