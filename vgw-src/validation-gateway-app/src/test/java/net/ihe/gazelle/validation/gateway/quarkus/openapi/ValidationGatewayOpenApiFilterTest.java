package net.ihe.gazelle.validation.gateway.quarkus.openapi;

import org.eclipse.microprofile.openapi.OASFactory;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.Operation;
import org.eclipse.microprofile.openapi.models.PathItem;
import org.eclipse.microprofile.openapi.models.Paths;
import org.eclipse.microprofile.openapi.models.info.Info;
import org.eclipse.microprofile.openapi.models.security.OAuthFlows;
import org.eclipse.microprofile.openapi.models.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.models.security.SecurityScheme;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

class ValidationGatewayOpenApiFilterTest {

   private static final String PROP_SSO_URL = "gzl.sso.url";
   private static final String PROP_SSO_REALM = "gzl.sso.realm";
   private static final String PROP_EVS_AUTH_REQUIRED = "evs.api.user-need-to-be-logged-in";
   private static final String PROP_EVS_PATH = "evs.validations.path";
   private static final String PROP_TERMS_URL = "gzl.terms.of.service.url";

   @Test
   void filterSecuritySchemeUsesConfiguredRealmAndBuildsOidcUrls() {
      withProperties(
            new String[][] {
                  {PROP_SSO_URL, "https://sso.example"},
                  {PROP_SSO_REALM, "custom-realm"}
            },
            () -> {
               ValidationGatewayOpenApiFilter filter = new ValidationGatewayOpenApiFilter();
               SecurityScheme scheme = OASFactory.createSecurityScheme();
               scheme.setType(SecurityScheme.Type.OAUTH2);
               OAuthFlows flows = OASFactory.createOAuthFlows();
               scheme.setFlows(flows);

               SecurityScheme filtered = filter.filterSecurityScheme(scheme);

               assertThat(filtered.getFlows().getImplicit(), notNullValue());
               assertThat(filtered.getFlows().getImplicit().getAuthorizationUrl(),
                     equalTo("https://sso.example/realms/custom-realm/protocol/openid-connect/auth"));
               assertThat(filtered.getDescription(), containsString("https://sso.example/realms/custom-realm/protocol/openid-connect/token"));
               assertThat(filtered.getDescription(), containsString("https://sso.example/realms/custom-realm/protocol/openid-connect/logout"));
            });
   }

   @Test
   void filterOpenApiAddsSecurityToConfiguredEvsPathWhenAuthenticationIsRequired() {
      withProperties(
            new String[][] {
                  {PROP_EVS_AUTH_REQUIRED, "true"},
                  {PROP_EVS_PATH, "/custom/evs/validations"},
                  {PROP_TERMS_URL, "https://example/terms"}
            },
            () -> {
               ValidationGatewayOpenApiFilter filter = new ValidationGatewayOpenApiFilter();
               OpenAPI openAPI = OASFactory.createOpenAPI();
               Info info = OASFactory.createInfo();
               info.setTitle("Validation Gateway");
               openAPI.setInfo(info);

               Paths paths = OASFactory.createPaths();
               PathItem customPathItem = OASFactory.createPathItem();
               customPathItem.setGET(OASFactory.createOperation());
               PathItem unrelatedPathItem = OASFactory.createPathItem();
               unrelatedPathItem.setGET(OASFactory.createOperation());
               paths.addPathItem("/custom/evs/validations", customPathItem);
               paths.addPathItem("/another/path", unrelatedPathItem);
               openAPI.setPaths(paths);

               filter.filterOpenAPI(openAPI);

               assertThat(openAPI.getInfo().getTermsOfService(), equalTo("https://example/terms"));

               Operation customGet = openAPI.getPaths().getPathItem("/custom/evs/validations").getGET();
               List<SecurityRequirement> customSecurity = customGet.getSecurity();
               assertThat(customSecurity, hasSize(1));
               assertThat(customSecurity.getFirst().getScheme("Keycloak"), notNullValue());

               Operation otherGet = openAPI.getPaths().getPathItem("/another/path").getGET();
               assertThat(otherGet.getSecurity(), nullValue());
            });
   }

   @Test
   void filterOpenApiUsesDefaultEvsPathWhenNoCustomPathConfigured() {
      withProperties(
            new String[][] {
                  {PROP_EVS_AUTH_REQUIRED, "true"},
                  {PROP_TERMS_URL, "https://example/terms-default"}
            },
            () -> {
               ValidationGatewayOpenApiFilter filter = new ValidationGatewayOpenApiFilter();
               OpenAPI openAPI = OASFactory.createOpenAPI();
               Info info = OASFactory.createInfo();
               info.setTitle("Validation Gateway");
               openAPI.setInfo(info);

               Paths paths = OASFactory.createPaths();
               PathItem defaultPathItem = OASFactory.createPathItem();
               defaultPathItem.setGET(OASFactory.createOperation());
               paths.addPathItem("/evs/rest/validations", defaultPathItem);
               openAPI.setPaths(paths);

               filter.filterOpenAPI(openAPI);

               Operation getOperation = openAPI.getPaths().getPathItem("/evs/rest/validations").getGET();
               assertThat(getOperation.getSecurity(), hasSize(1));
            });
   }

   private static void withProperties(String[][] entries, Runnable action) {
      String[] previous = new String[entries.length];
      for (int i = 0; i < entries.length; i++) {
         previous[i] = System.getProperty(entries[i][0]);
         System.setProperty(entries[i][0], entries[i][1]);
      }
      try {
         action.run();
      } finally {
         for (int i = 0; i < entries.length; i++) {
            if (previous[i] == null) {
               System.clearProperty(entries[i][0]);
            } else {
               System.setProperty(entries[i][0], previous[i]);
            }
         }
      }
   }
}
