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

package net.ihe.gazelle.simulation.technical.ws;

import io.quarkus.smallrye.openapi.OpenApiFilter;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.openapi.OASFilter;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.security.OAuthFlow;
import org.eclipse.microprofile.openapi.models.security.SecurityScheme;

import java.util.HashMap;

/**
 * Filter to modify the OpenAPI definition to adapt it to the environment.
 */
@OpenApiFilter(OpenApiFilter.RunStage.RUN)
public class SequenceSearchOpenAPIFilter implements OASFilter {

   private static final String REALMS_GAZELLE_PROTOCOL_OPENID_CONNECT = "/realms/gazelle/protocol/openid-connect/";

   /**
    * Constructor.
    */
   public SequenceSearchOpenAPIFilter() {
      // Empty
   }

   @Override
   public void filterOpenAPI(OpenAPI openAPI) {
      //In this method, we retrieve the term of service path to make sure we have the right url depending of the environment this
      //deployed on.
      if (openAPI.getInfo() != null) {
         String termOfService = ConfigProvider.getConfig().getValue("gzl.terms.of.service.url", String.class);
         openAPI.getInfo().setTitle("Simulation Sequence Search API");
         openAPI.getInfo().setTermsOfService(termOfService);
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
}
