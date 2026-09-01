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

package net.ihe.gazelle.serviceregistry.technical.rest;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.enterprise.context.control.ActivateRequestContext;
import net.ihe.gazelle.lang.InterruptedRuntimeException;
import net.ihe.gazelle.search.api.Range;
import net.ihe.gazelle.search.api.SearchQuery;
import net.ihe.gazelle.search.api.SearchResult;
import net.ihe.gazelle.security.mocks.KeycloakMockResource;
import net.ihe.gazelle.serviceregistry.api.business.DeployedService;
import net.ihe.gazelle.serviceregistry.api.business.TestDataUtil;
import net.ihe.gazelle.serviceregistry.api.business.lookup.ServiceSearchCriteria;
import net.ihe.gazelle.serviceregistry.client.business.ServiceLookupClient;
import net.ihe.gazelle.serviceregistry.client.technical.rest.ServiceLookupClientImpl;
import net.ihe.gazelle.serviceregistry.technical.ITServiceConnector;
import org.junit.jupiter.api.*;

import java.net.URI;

import static net.ihe.gazelle.serviceregistry.api.business.DeployedService.Status.AVAILABLE;
import static net.ihe.gazelle.serviceregistry.api.business.DeployedService.Status.UNKNOWN;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
@Order(1)
@ActivateRequestContext
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@QuarkusTestResource(value = KeycloakMockResource.class, restrictToAnnotatedClass = true)
class ServiceLookupClientIT {

   @TestHTTPResource
   URI registryUri;

   private ITServiceConnector itServiceConnector;

   @BeforeAll
   void init() {
      try {
         itServiceConnector = new ITServiceConnector(
               registryUri.toString(),
               TestDataUtil.getAllServices()
         );
      } catch (InterruptedException e) {
         Thread.currentThread().interrupt();
         throw new InterruptedRuntimeException("Service registration interrupted", e);
      }
   }

   @AfterAll
   void tearDown() {
      if(itServiceConnector != null) {
         itServiceConnector.close();
      }
   }

   @Test
   void testAllServicesLookup() throws InterruptedException {
      ServiceLookupClient client = new ServiceLookupClientImpl(registryUri.toString());
      SearchResult<DeployedService> searchResult = client.search(new SearchQuery<>());
      assertNotNull(searchResult);
      assertEquals(10, searchResult.objects().size());
      assertEquals(0, searchResult.offset());
      assertEquals(10, searchResult.limit());
      assertEquals(10, searchResult.totalObjects());
      assertThat(
            searchResult.objects().stream().map(DeployedService::getName).toList(),
            contains("EVS Client", "HL7v2 Validator", "IUA Simulator", "Maestro", "Maestro", "mCSD Simulator", "MHD Simulator", "Service Registry", "Test Management", "XML Validator")
      );
   }

   @Test
   void testPaginationLookup() throws InterruptedException {
      ServiceLookupClient client = new ServiceLookupClientImpl(registryUri.toString());
      SearchResult<DeployedService> searchResult = client.search(new SearchQuery<>(
            new ServiceSearchCriteria(),
            new Range(0, 5),
            null
      ));
      assertNotNull(searchResult);
      assertEquals(5, searchResult.objects().size());
      assertEquals(0, searchResult.offset());
      assertEquals(5, searchResult.limit());
      assertEquals(10, searchResult.totalObjects());
      assertThat(
            searchResult.objects().stream().map(DeployedService::getName).toList(),
            contains("EVS Client", "HL7v2 Validator", "IUA Simulator", "Maestro", "Maestro")
      );

      SearchResult<DeployedService> searchResult2 = client.search(new SearchQuery<>(
            new ServiceSearchCriteria(),
            new Range(5, 5),
            null
      ));
      assertNotNull(searchResult2);
      assertEquals(5, searchResult2.objects().size());
      assertEquals(5, searchResult2.offset());
      assertEquals(5, searchResult2.limit());
      assertEquals(10, searchResult2.totalObjects());
      assertThat(
            searchResult2.objects().stream().map(DeployedService::getName).toList(),
            contains("mCSD Simulator", "MHD Simulator", "Service Registry", "Test Management", "XML Validator")
      );
   }

   @Test
   void testSimulatorNameLookup() throws InterruptedException {
      ServiceLookupClient client = new ServiceLookupClientImpl(registryUri.toString());
      SearchResult<DeployedService> searchResult = client.search(new SearchQuery<>(
            new ServiceSearchCriteria()
                  .setName("simulator"),
            null,
            null
      ));
      assertNotNull(searchResult);
      assertEquals(3, searchResult.objects().size());
      assertEquals(0, searchResult.offset());
      assertEquals(3, searchResult.limit());
      assertEquals(3, searchResult.totalObjects());
      assertThat(
            searchResult.objects().stream().map(DeployedService::getName).toList(),
            contains("IUA Simulator", "mCSD Simulator", "MHD Simulator")
      );
   }

   @Test
   void testSimuInterfaceLookup() throws InterruptedException {
      ServiceLookupClient client = new ServiceLookupClientImpl(registryUri.toString());
      SearchResult<DeployedService> searchResult = client.search(new SearchQuery<>(
            new ServiceSearchCriteria()
                  .setProvidedInterface("Gazelle Simulation API")
                  .setStatus(AVAILABLE,UNKNOWN),
            null,
            null
      ));
      assertNotNull(searchResult);
      assertEquals(3, searchResult.objects().size());
      assertEquals(0, searchResult.offset());
      assertEquals(3, searchResult.limit());
      assertEquals(3, searchResult.totalObjects());
      assertThat(
            searchResult.objects().stream().map(DeployedService::getName).toList(),
            contains("IUA Simulator", "mCSD Simulator", "MHD Simulator")
      );
   }

}
