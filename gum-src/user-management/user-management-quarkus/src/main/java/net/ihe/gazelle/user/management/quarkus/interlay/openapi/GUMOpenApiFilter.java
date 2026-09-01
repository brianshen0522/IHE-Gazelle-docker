package net.ihe.gazelle.user.management.quarkus.interlay.openapi;

import io.quarkus.smallrye.openapi.OpenApiFilter;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.openapi.OASFilter;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.security.OAuthFlow;
import org.eclipse.microprofile.openapi.models.security.SecurityScheme;

import java.util.HashMap;

/**
 * This class is used to modify the openapi file generated during the build on runtime.
 * It allows us to set variable that we can only get during runtime (ex: environment variable)
 */
@OpenApiFilter(OpenApiFilter.RunStage.RUN)
public class GUMOpenApiFilter implements OASFilter {

    private static final String REALMS_GAZELLE_PROTOCOL_OPENID_CONNECT = "/realms/gazelle/protocol/openid-connect/";

    /**
     * Creates the OpenAPI filter.
     */
    public GUMOpenApiFilter() {
        // Default constructor
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

    @Override
    public void filterOpenAPI(OpenAPI openAPI) {
        //In this method, we retrieve the term of service path to make sure we have the right url depending of the environment this
        //deployed on.
        if (openAPI.getInfo() != null) {
            String termOfService = ConfigProvider.getConfig().getValue("gzl.terms.of.service.url", String.class);
            openAPI.getInfo().setTermsOfService(termOfService);
        }
        OASFilter.super.filterOpenAPI(openAPI);
    }
}
