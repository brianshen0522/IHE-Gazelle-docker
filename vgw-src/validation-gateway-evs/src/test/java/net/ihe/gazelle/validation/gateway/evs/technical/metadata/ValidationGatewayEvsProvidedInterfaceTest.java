package net.ihe.gazelle.validation.gateway.evs.technical.metadata;

import net.ihe.gazelle.servicemetadata.api.business.HttpRestBinding;
import net.ihe.gazelle.servicemetadata.api.business.ProvidedInterface;
import net.ihe.gazelle.servicemetadata.api.business.SecuredMethod;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

class ValidationGatewayEvsProvidedInterfaceTest {

    private static final String SERVICE_REST_URL = "https://localhost/validation-gateway/rest";

    @Test
    void exposesValidationGatewayEvsProvidedInterface() {
        System.setProperty("gzl.service.rest.interface.baseUrl", SERVICE_REST_URL);
        ProvidedInterface providedInterface = new ValidationGatewayEvsProvidedInterface().getProvidedInterface();
        HttpRestBinding binding = (HttpRestBinding) providedInterface.getBindings().iterator().next();

        assertThat(providedInterface.getInterfaceName(), is("EVS Client API"));
        assertThat(providedInterface.getInterfaceVersion(), is("2.0"));
        assertThat(binding.getServiceUrl(), is("https://localhost/validation-gateway/evs/rest"));
        assertThat(binding.getSecuredMethods(), hasSize(2));
        assertThat(binding.getSecuredMethods(), containsInAnyOrder(SecuredMethod.OIDC, SecuredMethod.M2M));
    }
}
