/*
Copyright 2010-2025 IHE International

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package net.ihe.gazelle.xmlvalidation.ws;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.common.http.TestHTTPResourceManager;
import net.ihe.gazelle.framework.modelvalidator.business.ValidatorBuilderFactory;
import net.ihe.gazelle.framework.modelvalidator.technical.adapter.bvalid.BValidatorBuilderFactory;
import net.ihe.gazelle.servicemetadata.api.business.HttpRestBinding;
import net.ihe.gazelle.servicemetadata.api.business.ProvidedInterface;
import net.ihe.gazelle.servicemetadata.api.business.ServiceBuilder;
import net.ihe.gazelle.servicemetadata.technical.jaxrs.ServiceDTO;
import net.ihe.gazelle.servicemetadata.technical.jaxrs.ProvidedInterfaceDTO;
import net.ihe.gazelle.servicemetadata.technical.jaxrs.HttpRestBindingDTO;
import net.ihe.gazelle.xmlvalidation.ws.config.IntegrationConfig;
import org.junit.jupiter.api.Test;


import static io.restassured.RestAssured.when;
import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
@QuarkusTestResource(value = IntegrationConfig.class, restrictToAnnotatedClass = true)
class MetadataApiWSIT{

    protected static ValidatorBuilderFactory validatorBuilderFactory = new BValidatorBuilderFactory();

    private static ServiceDTO expectedService;

    @Test
    void testGetServiceMetadata() {
        String baseUrl = TestHTTPResourceManager.getUri();
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }

        expectedService = new ServiceDTO(new ServiceBuilder(validatorBuilderFactory)
                .setName("XML Validation Service")
                .setVersion("unknown")
                .setInstanceId("unknown")
                .setReplicaId("unknown")
                .addProvidedInterface(
                        new ProvidedInterface()
                                .setInterfaceName("Validation Service API")
                                .setInterfaceVersion("2.0.1")
                                .addBinding(new HttpRestBinding()
                                        .setServiceUrl(baseUrl + "/rest")
                                )
                )
                .build()
        );
        ServiceDTO actual = when()
            .get("/rest/metadata")
        .then()
            .statusCode(200)
                .extract()
                .as(ServiceDTO.class);

        assertEqualServiceDTO(expectedService, actual);
    }




    private static void assertEqualServiceDTO(ServiceDTO expected, ServiceDTO actual) {
        assertEqualServiceObject(expected, actual);
        if(expected.getProvidedInterfaces().get(0) == null ||
                actual.getProvidedInterfaces().get(0) == null) {
            throw new RuntimeException("Provided interface is not a ValidationInterface");
        }
        assertEqualValidationInterface((ProvidedInterfaceDTO) expected.getProvidedInterfaces().get(0),
                (ProvidedInterfaceDTO) actual.getProvidedInterfaces().get(0));
    }

    private static void assertEqualServiceObject(ServiceDTO expected, ServiceDTO actual) {
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getVersion(), actual.getVersion());
        assertEquals(expected.getInstanceId(), actual.getInstanceId());
        assertEquals(expected.getReplicaId(), actual.getReplicaId());
    }

    private static void assertEqualValidationInterface(ProvidedInterfaceDTO expected, ProvidedInterfaceDTO actual) {
        assertEquals(expected.getInterfaceName(), actual.getInterfaceName());
        //fixme: this is a problem if versions change
//        assertEquals(expected.getInterfaceVersion(), actual.getInterfaceVersion());

        assertEquals(expected.getBindings().size(), actual.getBindings().size());
        for(int i = 0; i < expected.getBindings().size(); i++) {
            if(!(expected.getBindings().get(i) instanceof HttpRestBindingDTO) || !(actual.getBindings().get(i) instanceof HttpRestBindingDTO))
                throw new RuntimeException("Expected RestBinding but was " + expected.getBindings().get(i).getClass().getName() + " and " + actual.getBindings().get(i).getClass().getName());
            assertEqualBinding((HttpRestBindingDTO) expected.getBindings().get(i), (HttpRestBindingDTO) actual.getBindings().get(i));
        }
    }

    private static void assertEqualBinding(HttpRestBindingDTO expected, HttpRestBindingDTO actual) {
        assertEquals(expected.getServiceUrl(), actual.getServiceUrl());
    }

}
