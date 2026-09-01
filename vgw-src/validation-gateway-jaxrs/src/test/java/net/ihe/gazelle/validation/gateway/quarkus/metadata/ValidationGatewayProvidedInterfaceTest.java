package net.ihe.gazelle.validation.gateway.quarkus.metadata;

import net.ihe.gazelle.servicemetadata.api.business.HttpRestBinding;
import net.ihe.gazelle.servicemetadata.api.business.ProvidedInterface;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

class ValidationGatewayProvidedInterfaceTest {

    private static final String SERVICE_REST_URL = "https://localhost/validation-gateway/rest";

    @Test
    void exposesValidationGatewayRestProvidedInterface() {
        System.setProperty(ValidationGatewayRestProvidedInterface.GZL_SERVICE_REST_INTERFACE_BASE_URL, SERVICE_REST_URL);
        ProvidedInterface providedInterface = new ValidationGatewayRestProvidedInterface().getProvidedInterface();
        HttpRestBinding binding = (HttpRestBinding) providedInterface.getBindings().iterator().next();

        assertThat(providedInterface.getInterfaceName(), is("Validation Gateway API"));
        assertThat(providedInterface.getInterfaceVersion(), notNullValue());
        assertThat(binding.getServiceUrl(), is(SERVICE_REST_URL));
    }
}
