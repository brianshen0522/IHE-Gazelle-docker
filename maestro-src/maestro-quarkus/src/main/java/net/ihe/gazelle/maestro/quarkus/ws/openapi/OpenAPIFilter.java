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

import io.quarkus.smallrye.openapi.OpenApiFilter;
import io.smallrye.openapi.internal.models.ExternalDocumentation;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.openapi.OASFilter;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.PathItem;
import org.eclipse.microprofile.openapi.models.security.OAuthFlow;
import org.eclipse.microprofile.openapi.models.security.SecurityScheme;

import java.util.HashMap;
import java.util.List;

/**
 * This class is used to modify the openapi file generated during the build on runtime.
 * It allows us to set variable that we can only get during runtime (ex: environment variable)
 */
@OpenApiFilter(OpenApiFilter.RunStage.RUN)
public class OpenAPIFilter implements OASFilter {

   private static final String TAG_TO_REMOVE = "Simulation Callback API";
   private static final String REALMS_GAZELLE_PROTOCOL_OPENID_CONNECT = "/realms/gazelle/protocol/openid-connect/";

   private static final String ROOT_PATH = "/maestro";
   @SuppressWarnings("java:S1075")
   private static final String TEST_RUN_PATH = "/v1/test/run";
   @SuppressWarnings("java:S1075")
   private static final String TEST_SUITE_RUN_PATH = "/v1/test-suite/run";

   /**
    * Default constructor
    */
   public OpenAPIFilter() {
      // Empty
   }

   @Override
   public void filterOpenAPI(OpenAPI openAPI) {
      //In this method, we retrieve the term of service path to make sure we have the right url depending of the environment this
      //deployed on.
      if (openAPI.getInfo() != null) {
         String termOfService = ConfigProvider.getConfig().getValue("gzl.terms.of.service.url", String.class);
         String maestroUrl = ConfigProvider.getConfig().getValue("gzl.maestro.url", String.class);
         openAPI.getInfo().setTermsOfService(termOfService);
         openAPI.setExternalDocs(new ExternalDocumentation()
               .url(maestroUrl + "/test-run-stream-api/index.html")
               .description("Test Run Stream API"));
         removeEndpoints(openAPI, List.of("/simulation/v1/report", "/itb/report"));
         replaceEndpoints(openAPI, List.of(TEST_RUN_PATH, TEST_SUITE_RUN_PATH));
      }
      OASFilter.super.filterOpenAPI(openAPI);
   }

   @Override
   public SecurityScheme filterSecurityScheme(SecurityScheme securityScheme) {
      //In this method, we retrieve the SSO url to make sure we have the right url depending of the environment this
      //deployed on.
      String ssoUrl = ConfigProvider.getConfig().getValue("gzl.sso.url", String.class);
      if (securityScheme.getType().equals(SecurityScheme.Type.OAUTH2)) {
         OAuthFlow implicitFlow = new io.smallrye.openapi.internal.models.security.OAuthFlow();
         implicitFlow.setAuthorizationUrl(ssoUrl + REALMS_GAZELLE_PROTOCOL_OPENID_CONNECT + "auth");
         implicitFlow.setScopes(new HashMap<>());
         securityScheme.getFlows().setImplicit(implicitFlow);
         String logoutLink = ssoUrl + REALMS_GAZELLE_PROTOCOL_OPENID_CONNECT + "logout";
         String schemeDescription = "Authorize yourself by logging in Keycloak. To logout completely, use the button " +
               "below and then please follow this link" +
               " <a href=" + logoutLink + ">" + logoutLink + "</a>.";
         securityScheme.setDescription(schemeDescription);
      }
      return OASFilter.super.filterSecurityScheme(securityScheme);
   }

   private void removeEndpoints(OpenAPI openAPI, List<String> endpoints) {
      openAPI.getPaths().removePathItem("/simulation-mock/rest/services");

      openAPI.setTags(
            openAPI.getTags().stream()
                  .filter(tag -> !TAG_TO_REMOVE.equals(tag.getName()))
                  .toList()
      );

      for (String endpoint : endpoints) {
         openAPI.getPaths().removePathItem(ROOT_PATH + endpoint);
         openAPI.getPaths().removePathItem(ROOT_PATH + endpoint);
      }
   }

   private void replaceEndpoints(OpenAPI openAPI, List<String> endpoints) {
      for (String endpoint : endpoints) {
         PathItem pathItem = openAPI.getPaths().getPathItem(ROOT_PATH + endpoint);
         if (pathItem != null) {
            openAPI.getPaths().addPathItem(endpoint, openAPI.getPaths().getPathItem(ROOT_PATH + endpoint));
            openAPI.getPaths().removePathItem(ROOT_PATH + endpoint);
         }
      }
   }
}
