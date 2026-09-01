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

package net.ihe.gazelle.serviceregistry.client.technical.rest;

import net.ihe.gazelle.servicemetadata.api.business.ConsumedInterface;
import net.ihe.gazelle.servicemetadata.api.business.ConsumedInterfaceBuilder;
import net.ihe.gazelle.servicemetadata.api.business.HttpRestBinding;
import net.ihe.gazelle.servicemetadata.api.technical.ConsumedInterfaceIdentifier;
import net.ihe.gazelle.serviceregistry.api.business.lookup.ServiceLookupIdentifier;

import java.util.List;

/**
 * Abstract base class for declaring a service consumer of the Service Lookup API. It provides the contractual interface
 * definition.
 * <p>
 * Child classes should simply extend this class, call the super constructor and provide whether the API is required or
 * not for it to operate.
 */
public abstract class AbstractConsumedServiceLookupIdentifier implements ConsumedInterfaceIdentifier {

   private final boolean required;

   /**
    * Constructor for the AbstractConsumedServiceLookupIdentifier.
    * @param required indicates whether the Service Lookup API is required for the service to operate.f
    */
   protected AbstractConsumedServiceLookupIdentifier(boolean required) {
      this.required = required;
   }

   @Override
   public ConsumedInterface getConsumedInterface() {
      return new ConsumedInterfaceBuilder()
            .setInterfaceName(ServiceLookupIdentifier.INTERFACE_NAME)
            .setRequired(required)
            .setSupportedVersions(List.of(ServiceLookupIdentifier.INTERFACE_VERSION))
            .setSupportedBindings(List.of(HttpRestBinding.TYPE))
            .build();
   }
}
