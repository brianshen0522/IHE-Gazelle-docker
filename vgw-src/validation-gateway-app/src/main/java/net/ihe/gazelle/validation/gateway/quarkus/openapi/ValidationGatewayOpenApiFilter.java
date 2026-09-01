package net.ihe.gazelle.validation.gateway.quarkus.openapi;

import io.quarkus.smallrye.openapi.OpenApiFilter;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.openapi.OASFactory;
import org.eclipse.microprofile.openapi.OASFilter;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.Operation;
import org.eclipse.microprofile.openapi.models.PathItem;
import org.eclipse.microprofile.openapi.models.security.OAuthFlow;
import org.eclipse.microprofile.openapi.models.security.SecurityScheme;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@OpenApiFilter(OpenApiFilter.RunStage.RUN)
public class ValidationGatewayOpenApiFilter implements OASFilter {

   private static final String SECURITY_SCHEME_NAME = "Keycloak";

   @Override
   public SecurityScheme filterSecurityScheme(SecurityScheme securityScheme) {
      String ssoUrl = ConfigProvider.getConfig().getValue("gzl.sso.url", String.class);
      String realm = ConfigProvider.getConfig().getOptionalValue("gzl.sso.realm", String.class).orElse("gazelle");
      String oidcBasePath = "/realms/" + realm + "/protocol/openid-connect/";
      if (securityScheme.getType().equals(SecurityScheme.Type.OAUTH2)) {
         OAuthFlow implicitFlow = OASFactory.createOAuthFlow();
         implicitFlow.setAuthorizationUrl(ssoUrl + oidcBasePath + "auth");
         implicitFlow.setScopes(new HashMap<>());
         securityScheme.getFlows().setImplicit(implicitFlow);
         String tokenEndpoint = ssoUrl + oidcBasePath + "token";
         String logoutLink = ssoUrl + oidcBasePath + "logout";
         String schemeDescription = "Authentication uses Bearer JWT from Keycloak.\n\n"
               + "How to retrieve a token:\n"
               + "1. Send a `POST` request to: `" + tokenEndpoint + "`\n"
               + "2. Use `application/x-www-form-urlencoded` with fields:\n"
               + "   - `client_id=OIDC_GAZELLE_CLIENT`\n"
               + "   - `grant_type=password`\n"
               + "   - `user=<your login>`\n"
               + "   - `secret=<your credential>`\n"
               + "3. Read `access_token` from the response and send it as `Authorization: Bearer <access_token>`.\n\n"
               + "Example:\n"
               + "```bash\n"
               + "curl -X POST '" + tokenEndpoint + "' \\\n"
               + "  -H 'Content-Type: application/x-www-form-urlencoded' \\\n"
               + "  --data-urlencode 'client_id=OIDC_GAZELLE_CLIENT' \\\n"
               + "  --data-urlencode 'grant_type=password' \\\n"
               + "  --data-urlencode 'user=<your login>' \\\n"
               + "  --data-urlencode 'secret=<your credential>'\n"
               + "```\n\n"
               + "To logout completely, use Swagger UI logout and then open: `"
               + logoutLink + "`.";
         securityScheme.setDescription(schemeDescription);
      }
      return OASFilter.super.filterSecurityScheme(securityScheme);
   }

   @Override
   public void filterOpenAPI(OpenAPI openAPI) {
      if (openAPI.getInfo() != null) {
         String termOfService = ConfigProvider.getConfig().getValue("gzl.terms.of.service.url", String.class);
         openAPI.getInfo().setTermsOfService(termOfService);
      }
      addSecurityRequirements(openAPI);
      addConditionalEvsSecurityRequirements(openAPI);
      OASFilter.super.filterOpenAPI(openAPI);
   }

   private void addSecurityRequirements(OpenAPI openAPI) {
      if (openAPI == null || openAPI.getPaths() == null) {
         return;
      }
      openAPI.getPaths().getPathItems().forEach((path, pathItem) -> {
         if (pathItem == null || !isProfilePath(path)) {
            return;
         }
         Operation getOperation = pathItem.getGET();
         if (getOperation == null) {
            return;
         }
         List<org.eclipse.microprofile.openapi.models.security.SecurityRequirement> updated =
               withSecurityRequirement(getOperation.getSecurity());
         if (updated != null) {
            getOperation.setSecurity(updated);
         }
      });
   }

   private boolean isProfilePath(String path) {
      return path != null && (path.endsWith("/profiles")
            || path.endsWith("/profiles/{serviceName}/{profileId}"));
   }

   private void addConditionalEvsSecurityRequirements(OpenAPI openAPI) {
      if (openAPI == null || openAPI.getPaths() == null || !isEvsAuthenticationRequired()) {
         return;
      }
      String evsPath = ConfigProvider.getConfig()
            .getOptionalValue("evs.validations.path", String.class)
            .orElseGet(() -> "/evs" + "/rest/validations");
      openAPI.getPaths().getPathItems().forEach((path, pathItem) -> {
         if (pathItem == null || !isEvsPath(path, evsPath)) {
            return;
         }
         applySecurityRequirement(pathItem);
      });
   }

   private boolean isEvsAuthenticationRequired() {
      return ConfigProvider.getConfig().getOptionalValue("evs.api.user-need-to-be-logged-in", Boolean.class)
            .orElse(false);
   }

   private boolean isEvsPath(String path, String evsValidationsPath) {
      return path != null && (path.equals(evsValidationsPath) || path.startsWith(evsValidationsPath + "/"));
   }

   private void applySecurityRequirement(PathItem pathItem) {
      setSecurityRequirement(pathItem.getGET());
      setSecurityRequirement(pathItem.getPUT());
      setSecurityRequirement(pathItem.getPOST());
      setSecurityRequirement(pathItem.getDELETE());
      setSecurityRequirement(pathItem.getOPTIONS());
      setSecurityRequirement(pathItem.getHEAD());
      setSecurityRequirement(pathItem.getPATCH());
      setSecurityRequirement(pathItem.getTRACE());
   }

   private void setSecurityRequirement(Operation operation) {
      if (operation == null) {
         return;
      }
      List<org.eclipse.microprofile.openapi.models.security.SecurityRequirement> updated =
            withSecurityRequirement(operation.getSecurity());
      if (updated != null) {
         operation.setSecurity(updated);
      }
   }

   private List<org.eclipse.microprofile.openapi.models.security.SecurityRequirement> withSecurityRequirement(
         List<org.eclipse.microprofile.openapi.models.security.SecurityRequirement> existing) {
      List<org.eclipse.microprofile.openapi.models.security.SecurityRequirement> requirements =
            existing == null ? new ArrayList<>() : new ArrayList<>(existing);
      boolean alreadyPresent = requirements.stream()
            .anyMatch(requirement -> requirement.getScheme(SECURITY_SCHEME_NAME) != null);
      if (alreadyPresent) {
         return existing == null ? requirements : null;
      }
      org.eclipse.microprofile.openapi.models.security.SecurityRequirement requirement =
            OASFactory.createSecurityRequirement();
      requirement.addScheme(SECURITY_SCHEME_NAME, List.of());
      requirements.add(requirement);
      return requirements;
   }
}
