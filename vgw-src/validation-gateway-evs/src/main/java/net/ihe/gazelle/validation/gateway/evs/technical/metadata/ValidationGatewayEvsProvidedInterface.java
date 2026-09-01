package net.ihe.gazelle.validation.gateway.evs.technical.metadata;

import net.ihe.gazelle.framework.modelvalidator.technical.adapter.bvalid.BValidatorBuilderFactory;
import net.ihe.gazelle.servicemetadata.api.business.HttpRestBindingBuilder;
import net.ihe.gazelle.servicemetadata.api.business.ProvidedInterface;
import net.ihe.gazelle.servicemetadata.api.business.ProvidedInterfaceBuilder;
import net.ihe.gazelle.servicemetadata.api.business.SecuredMethod;
import net.ihe.gazelle.servicemetadata.api.technical.ProvidedInterfaceIdentifier;
import org.eclipse.microprofile.config.ConfigProvider;

import java.util.Set;

public class ValidationGatewayEvsProvidedInterface implements ProvidedInterfaceIdentifier {
    private static final String SERVICE_REST_INTERFACE_BASE_URL = "gzl.service.rest.interface.baseUrl";
    private static final String DEFAULT_SERVICE_REST_INTERFACE_BASE_URL = "http://localhost:8092/validation-gateway/rest";

    @Override
    public ProvidedInterface getProvidedInterface() {
        return new ProvidedInterfaceBuilder(new BValidatorBuilderFactory())
              .setInterfaceName("EVS Client API")
              .setInterfaceVersion("2.0")
              .setBindings(Set.of(
                    new HttpRestBindingBuilder()
                          .setServiceUrl(getEvsRestUrl())
                          .setSecuredMethods(Set.of(SecuredMethod.OIDC, SecuredMethod.M2M))
              ))
              .build();
    }

    private String getEvsRestUrl() {
        String restUrl = ConfigProvider.getConfig()
              .getOptionalValue(SERVICE_REST_INTERFACE_BASE_URL, String.class)
              .orElse(DEFAULT_SERVICE_REST_INTERFACE_BASE_URL);
        String withoutTrailingSlash = restUrl.endsWith("/") ? restUrl.substring(0, restUrl.length() - 1) : restUrl;
        if (withoutTrailingSlash.endsWith("/rest")) {
            return withoutTrailingSlash.substring(0, withoutTrailingSlash.length() - "/rest".length()) + "/evs/rest";
        }
        return withoutTrailingSlash + "/evs/rest";
    }
}
