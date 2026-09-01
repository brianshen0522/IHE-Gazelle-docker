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

package net.ihe.gazelle.serviceregistry.technical.openapi;

import io.quarkus.smallrye.openapi.OpenApiFilter;
import io.smallrye.openapi.internal.models.ExternalDocumentation;
import org.eclipse.microprofile.openapi.OASFilter;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.Operation;
import org.eclipse.microprofile.openapi.models.PathItem;
import org.eclipse.microprofile.openapi.models.Paths;
import org.eclipse.microprofile.openapi.models.info.Info;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * OpenAPI filter for the service lookup API.
 * <p>
 * This filter modifies the OpenAPI specification by removing the root path from endpoints and setting the terms of
 * service URL if defined.
 */
@OpenApiFilter(OpenApiFilter.RunStage.RUNTIME_PER_REQUEST)
public class ServiceLookupOpenAPIFilter implements OASFilter {

   /**
    * Default constructor.
    */
   public ServiceLookupOpenAPIFilter() {
      // Default constructor
   }

   /**
    * Filters the OpenAPI specification.
    *
    * @param openAPI the OpenAPI specification to filter
    */
   @Override
   public void filterOpenAPI(OpenAPI openAPI) {
      Info info = openAPI.getInfo();
      if (info != null) {
         removeDuplicatedSearchController(openAPI);
         addAsyncAPIDocumentation(openAPI);
      }
      OASFilter.super.filterOpenAPI(openAPI);
   }

   private static final String SEARCH_CONTROLLER_TAG = "Service Lookup Controller";

   private void removeDuplicatedSearchController(OpenAPI openAPI) {
      Paths paths = openAPI.getPaths();
      if (paths == null || paths.getPathItems() == null) {
         return;
      }
      List<String> toRemove = paths.getPathItems().entrySet().stream()
            .filter(entry -> hasTag(entry.getValue(), SEARCH_CONTROLLER_TAG))
            .map(Map.Entry::getKey)
            .toList();
      toRemove.forEach(paths::removePathItem);
   }

   private boolean hasTag(PathItem pathItem, String tag) {
      return pathItem.getOperations().values().stream()
            .filter(Objects::nonNull)
            .map(Operation::getTags)
            .filter(Objects::nonNull)
            .anyMatch(tags -> tags.contains(tag));
   }

   private void addAsyncAPIDocumentation(OpenAPI openAPI) {
      openAPI.setExternalDocs(
              new ExternalDocumentation()
                      .description("Service Registration API WEBSOCKET documentation")
                      .url("/service-registry/service-registration-api/index.html")
      );
   }

}
