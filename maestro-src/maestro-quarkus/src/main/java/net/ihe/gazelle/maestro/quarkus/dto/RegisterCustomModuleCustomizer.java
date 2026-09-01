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

package net.ihe.gazelle.maestro.quarkus.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.jackson.ObjectMapperCustomizer;
import jakarta.inject.Singleton;
import net.ihe.gazelle.modelmarshaller.technical.jackson.ObjectMapperBuilder;

/**
 * Registers a custom module to apply Gazelle-specific settings to the Jackson {@link ObjectMapper}.
 * This ensures consistent serialization and deserialization behavior across the application.
 */
@Singleton
public class RegisterCustomModuleCustomizer implements ObjectMapperCustomizer {

   /**
    * Default constructor
    */
   public RegisterCustomModuleCustomizer() {
      // Empty
   }

   /**
    * Customizes the given {@link ObjectMapper} by applying Gazelle-specific settings.
    *
    * @param mapper the {@link ObjectMapper} to customize
    */
   @Override
   public void customize(ObjectMapper mapper) {
      ObjectMapperBuilder.applyGazelleSettings(mapper);
   }
}
