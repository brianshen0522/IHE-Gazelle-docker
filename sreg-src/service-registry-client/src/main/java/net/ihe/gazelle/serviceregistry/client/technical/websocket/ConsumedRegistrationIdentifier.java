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

package net.ihe.gazelle.serviceregistry.client.technical.websocket;

import net.ihe.gazelle.servicemetadata.api.business.ConsumedInterface;
import net.ihe.gazelle.servicemetadata.api.business.ConsumedInterfaceBuilder;
import net.ihe.gazelle.servicemetadata.api.business.WebSocketBinding;
import net.ihe.gazelle.servicemetadata.api.technical.ConsumedInterfaceIdentifier;
import net.ihe.gazelle.serviceregistry.api.business.ServiceRegistrationIdentifier;
import net.ihe.gazelle.serviceregistry.client.technical.job.RegistrationJobMicroprofileConfig;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;

import java.util.List;

/**
 * This class implements the ConsumedInterfaceIdentifier for the Service Registration API.
 * It checks if the API is required by the service based on the configuration.
 */
public class ConsumedRegistrationIdentifier implements ConsumedInterfaceIdentifier {

   private final boolean required;

   /**
    * Default constructor.
    */
   public ConsumedRegistrationIdentifier() {
      Config config = ConfigProvider.getConfig();
      required = config
            .getOptionalValue(RegistrationJobMicroprofileConfig.GZL_SERVICE_REGISTRY_ENABLED, Boolean.class)
            .orElse(true);
   }

   @Override
   public ConsumedInterface getConsumedInterface() {
      return new ConsumedInterfaceBuilder()
            .setInterfaceName(ServiceRegistrationIdentifier.INTERFACE_NAME)
            .setRequired(required)
            .setSupportedVersions(List.of(ServiceRegistrationIdentifier.INTERFACE_VERSION))
            .setSupportedBindings(List.of(WebSocketBinding.TYPE))
            .build();
   }
}
