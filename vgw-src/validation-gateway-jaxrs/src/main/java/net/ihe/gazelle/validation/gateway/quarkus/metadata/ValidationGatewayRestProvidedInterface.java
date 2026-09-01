package net.ihe.gazelle.validation.gateway.quarkus.metadata;

import net.ihe.gazelle.framework.modelvalidator.technical.adapter.bvalid.BValidatorBuilderFactory;
import net.ihe.gazelle.servicemetadata.api.business.HttpRestBindingBuilder;
import net.ihe.gazelle.servicemetadata.api.business.ProvidedInterface;
import net.ihe.gazelle.servicemetadata.api.business.ProvidedInterfaceBuilder;
import net.ihe.gazelle.servicemetadata.api.technical.ProvidedInterfaceIdentifier;
import net.ihe.gazelle.servicemetadata.api.technical.VersionExtractor;
import org.eclipse.microprofile.config.ConfigProvider;

import java.util.Set;

public class ValidationGatewayRestProvidedInterface implements ProvidedInterfaceIdentifier {

    public static final String GZL_SERVICE_REST_INTERFACE_BASE_URL = "gzl.service.rest.interface.baseUrl";
    private static final String DEFAULT_SERVICE_REST_INTERFACE_BASE_URL = "http://localhost:8092/validation-gateway/rest";

    @Override
    public ProvidedInterface getProvidedInterface() {
        return new ProvidedInterfaceBuilder(new BValidatorBuilderFactory())
              .setInterfaceName("Validation Gateway API")
              .setInterfaceVersion(getVersion())
              .setBindings(Set.of(
                    new HttpRestBindingBuilder().setServiceUrl(getServiceRestUrl())
              ))
              .build();
    }

    private String getServiceRestUrl() {
        return ConfigProvider.getConfig()
              .getOptionalValue(GZL_SERVICE_REST_INTERFACE_BASE_URL, String.class)
              .orElse(DEFAULT_SERVICE_REST_INTERFACE_BASE_URL);
    }

    private String getVersion() {
        return VersionExtractor.getVersion(ValidationGatewayRestProvidedInterface.class);
    }
}
