/*
 * Copyright 2025 IHE International.
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

package net.ihe.gazelle.serviceregistry.client.technical.rest;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import net.ihe.gazelle.modelmarshaller.technical.jackson.JacksonSerDes;
import net.ihe.gazelle.modelmarshaller.technical.serialization.TextSerDes;
import net.ihe.gazelle.search.client.business.ServiceUnavailableException;
import net.ihe.gazelle.security.mocks.KeycloakMockResource;
import net.ihe.gazelle.servicemetadata.api.business.Service;
import net.ihe.gazelle.serviceregistry.api.business.DeployedService;
import net.ihe.gazelle.serviceregistry.api.business.TestDataUtil;
import net.ihe.gazelle.serviceregistry.api.technical.dto.DeployedServiceDTO;
import net.ihe.gazelle.serviceregistry.client.technical.ServiceRegistryMock;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static net.ihe.gazelle.serviceregistry.api.business.DeployedService.Status.AVAILABLE;

@QuarkusTest
@QuarkusTestResource(KeycloakMockResource.class)
class ServiceLookupClientTest {

   private static final TextSerDes SERDES = new JacksonSerDes();
   private static ServiceRegistryMock registryMock;

   @BeforeAll
   static void beforeAll() {
      List<Service> services = TestDataUtil.getAllServices();
      List<DeployedServiceDTO> dtos = Arrays.asList(
              new DeployedServiceDTO(new DeployedService(services.get(0))),
              new DeployedServiceDTO(new DeployedService(services.get(1))),
              new DeployedServiceDTO(new DeployedService(services.get(2)).setStatus(AVAILABLE).setSelfRegistered(true)),
              new DeployedServiceDTO(new DeployedService(services.get(3)).setStatus(AVAILABLE).setSelfRegistered(true)),
              new DeployedServiceDTO(new DeployedService(services.get(4)).setStatus(AVAILABLE).setSelfRegistered(true)),
              new DeployedServiceDTO(new DeployedService(services.get(5)).setStatus(AVAILABLE).setSelfRegistered(true)),
              new DeployedServiceDTO(new DeployedService(services.get(6)).setStatus(AVAILABLE).setSelfRegistered(true)),
              new DeployedServiceDTO(new DeployedService(services.get(7)).setStatus(AVAILABLE).setSelfRegistered(true)),
              new DeployedServiceDTO(new DeployedService(services.get(8)).setStatus(AVAILABLE).setSelfRegistered(true))
      );
      // sort by name ignoring case
      dtos.sort(Comparator.comparing(DeployedServiceDTO::getName, String.CASE_INSENSITIVE_ORDER));

      registryMock = new ServiceRegistryMock(
              new Dispatcher() {
                 @Override
                 public MockResponse dispatch(@NotNull RecordedRequest request) {
                    if ("/service-registry/services".equals(request.getPath())) {
                       return new MockResponse()
                               .setResponseCode(200)
                               .setHeader("Content-Type", "application/json")
                               .setHeader("Content-Range", "DeployedServices 1-9/9")
                               .setBody(SERDES.serializeAsString(dtos));
                    } else {
                       return new MockResponse().setResponseCode(404);
                    }
                 }
              }
      );
      registryMock.start();
   }

   @AfterAll
   static void tearDown() {
      registryMock.shutdown();
   }

   @Test
   void testServiceNotFound() {
      String serviceRegistryUrl = registryMock.getUrl();
      ServiceLookupClientImpl client = new ServiceLookupClientImpl(
            serviceRegistryUrl.replace("/service-registry", "/unknown"));
      Assertions.assertThrows(ServiceUnavailableException.class, () -> client.search(null));
   }

   @Test
   void testSearchWithEmptyResult() throws InterruptedException {
      String serviceRegistryUrl = registryMock.getUrl();
      ServiceLookupClientImpl client = new ServiceLookupClientImpl(serviceRegistryUrl);
      var result = client.search(null);
      Assertions.assertNotNull(result, "Search result should not be null");
      Assertions.assertEquals(9, result.totalObjects(), "Total objects should be 9");
      MatcherAssert.assertThat(
            result.objects().stream().map(Service::getName).toList(),
            Matchers.contains(
                  "EVS Client",
                  "HL7v2 Validator",
                  "IUA Simulator",
                  "Maestro",
                  "Maestro",
                  "mCSD Simulator",
                  "MHD Simulator",
                  "Test Management",
                  "XML Validator"
            )
      );
   }

}
