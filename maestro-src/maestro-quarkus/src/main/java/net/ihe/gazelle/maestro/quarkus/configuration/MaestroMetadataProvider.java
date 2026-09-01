/*
 * Copyright 2025-2026 IHE International.
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

package net.ihe.gazelle.maestro.quarkus.configuration;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Default;
import net.ihe.gazelle.servicemetadata.api.technical.AbstractMetadataService;

/**
 * Default {@link AbstractMetadataService} implementation providing metadata
 * information for the Maestro test automation engine.
 */
@Default
@ApplicationScoped
public class MaestroMetadataProvider extends AbstractMetadataService {

   /**
    * Protected constructor for CDI.
    * Initializes the metadata service with this class type.
    */
   protected MaestroMetadataProvider() {
      super(MaestroMetadataProvider.class);
   }

   @Override
   public String getServiceName() {
      return "Maestro";
   }

   @Override
   public String getServiceDescription() {
      return "The Gazelle test automation engine";
   }
}
