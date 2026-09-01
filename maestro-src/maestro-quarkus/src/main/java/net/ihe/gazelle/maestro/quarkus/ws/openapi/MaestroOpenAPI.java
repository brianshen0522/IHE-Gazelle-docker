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

package net.ihe.gazelle.maestro.quarkus.ws.openapi;

import jakarta.ws.rs.core.Application;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.info.License;

/**
 * Configuration class for OpenAPI documentation of the Maestro API.
 */
@OpenAPIDefinition(
      info = @Info(
            title = "Maestro API", description = "This API allows to trigger test execution.", license = @License(
            name = "Under Apache 2.0 license",
            url = "https://www.apache.org/licenses/LICENSE-2.0.html"), version = "")
)
public class MaestroOpenAPI extends Application {

   /**
    * Default constructor
    */
   public MaestroOpenAPI() {
      // Empty
   }
}
