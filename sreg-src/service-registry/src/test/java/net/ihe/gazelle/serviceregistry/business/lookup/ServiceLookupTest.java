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

package net.ihe.gazelle.serviceregistry.business.lookup;

import net.ihe.gazelle.search.api.InvalidRangeException;
import net.ihe.gazelle.search.api.Range;
import net.ihe.gazelle.search.api.SearchQuery;
import net.ihe.gazelle.search.api.SearchResult;
import net.ihe.gazelle.security.business.Authz;
import net.ihe.gazelle.security.business.AuthzImpl;
import net.ihe.gazelle.security.business.Groups;
import net.ihe.gazelle.security.mocks.MockedGazelleIdentity;
import net.ihe.gazelle.security.technical.PermissionStoreSPIProvider;
import net.ihe.gazelle.serviceregistry.api.business.DeployedService;
import net.ihe.gazelle.serviceregistry.api.business.ServiceId;
import net.ihe.gazelle.serviceregistry.api.business.TestDataUtil;
import net.ihe.gazelle.serviceregistry.api.business.lookup.ServiceSearchCriteria;
import net.ihe.gazelle.serviceregistry.business.registration.RegistrationConfiguration;
import net.ihe.gazelle.serviceregistry.business.registration.ServiceRegistration;
import net.ihe.gazelle.serviceregistry.technical.dao.InMemoryServiceRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Set;

import static net.ihe.gazelle.serviceregistry.api.business.DeployedService.Status.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ServiceLookupTest {

   private final Authz authz = new AuthzImpl(new PermissionStoreSPIProvider());
   private final MockedGazelleIdentity mockedIdentity = new MockedGazelleIdentity(Set.of(Groups.ROLE_ADMIN));
   private ServiceLookup serviceLookup;
   private ServiceRegistration registration;
   private InMemoryServiceRepository dao;
   private RegistrationUtil registrationUtil;

   @BeforeEach
   void setUp() {
      dao = new InMemoryServiceRepository();
      RegistrationConfiguration config = new RegistrationConfiguration() {
         @Override
         public Duration getSelfRegistrationTimeout() {
            return Duration.ofHours(72);
         }

         @Override
         public Duration getHeartbeatTimeout() {
            return Duration.ofMinutes(5);
         }
      };
      registration = new ServiceRegistration(dao, config, authz);
      serviceLookup = new ServiceLookupImpl(dao, authz);
      registrationUtil = new RegistrationUtil(registration);
   }

   @AfterEach
   void tearDown() {
      dao.dropAll();
   }

   @Test
   void testNoServices() {
      var result = serviceLookup.search(new SearchQuery<>(null, null, null), mockedIdentity);
      assertThat(result.objects(), empty());
      var result2 = serviceLookup.search(null, mockedIdentity);
      assertThat(result2.objects(), empty());
   }

   @Test
   void testInvalidRange() {
      final SearchQuery<ServiceSearchCriteria> query = new SearchQuery<>(
            new ServiceSearchCriteria(),
            new Range().setOffset(-1).setLimit(0), // Invalid range
            null
      );

      assertThrows(InvalidRangeException.class, () -> {
         serviceLookup.search(query, mockedIdentity);
      });
   }

   @Test
   void testGetAll() {
      registrationUtil.registerAllServices();
      SearchResult<DeployedService> result = serviceLookup.search(new SearchQuery<>(null, null, null), mockedIdentity);
      assertEquals(9, result.objects().size());

      // Assert that services are returned ordered by name ascending and ignoring case.
      // As opposed to `hasItems`, Hamcrest `contains` verifies the size and the order of the collection.
      assertThat(
            result.objects().stream().map(DeployedService::getName).toList(),
            contains(
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

   @Test
   void testPages() {
      registrationUtil.registerAllServices();
      SearchResult<DeployedService> result1 = serviceLookup.search(
            new SearchQuery<>(new ServiceSearchCriteria(), new Range().setOffset(0).setLimit(5), null),
              mockedIdentity
      );
      assertEquals(5, result1.objects().size());
      assertEquals(9, result1.totalObjects());
      assertEquals(0, result1.offset());
      assertEquals(5, result1.limit());

      SearchResult<DeployedService> result2 = serviceLookup.search(
            new SearchQuery<>(new ServiceSearchCriteria(), new Range().setOffset(5).setLimit(5), null),
              mockedIdentity
      );
      assertEquals(4, result2.objects().size());
      assertEquals(9, result2.totalObjects());
      assertEquals(5, result2.offset());
      assertEquals(5, result2.limit());
   }

   @Test
   void testFilterAvailableServices() {
      registrationUtil.registerAllServices();
      registrationUtil.disconnectService(new ServiceId(TestDataUtil.getMaestro2())); // make maestro2 Unreachable
      registrationUtil.disconnectService(new ServiceId(TestDataUtil.getIUASimulator())); // make IUA Simulator Unreachable
      SearchQuery<ServiceSearchCriteria> query = new SearchQuery<>(
            new ServiceSearchCriteria().setStatus(AVAILABLE, UNKNOWN),
            null,
            null
      );
      SearchResult<DeployedService> result = serviceLookup.search(query, mockedIdentity);
      assertEquals(7, result.objects().size());
      assertThat(
            result.objects().stream().map(DeployedService::getName).toList(),
            contains("EVS Client", "HL7v2 Validator", "Maestro", "mCSD Simulator", "MHD Simulator", "Test Management",
                  "XML Validator")
      );
   }

   @Test
   void testFilterByName() {
      registrationUtil.registerAllServices();
      SearchQuery<ServiceSearchCriteria> query = new SearchQuery<>(
            new ServiceSearchCriteria().setName("Simulator"),
            null,
            null
      );
      SearchResult<DeployedService> result = serviceLookup.search(query, mockedIdentity);
      assertEquals(3, result.objects().size());
      assertThat(
            result.objects().stream().map(DeployedService::getName).toList(),
            contains("IUA Simulator", "mCSD Simulator", "MHD Simulator")
      );
   }

   @Test
   void testFilterByNameOR() {
      registrationUtil.registerAllServices();
      SearchQuery<ServiceSearchCriteria> query = new SearchQuery<>(
            new ServiceSearchCriteria().setName("Simulator", "Validator"),
            null,
            null
      );
      SearchResult<DeployedService> result = serviceLookup.search(query, mockedIdentity);
      assertEquals(5, result.objects().size());
      assertThat(
            result.objects().stream().map(DeployedService::getName).toList(),
            contains("HL7v2 Validator", "IUA Simulator", "mCSD Simulator", "MHD Simulator", "XML Validator")
      );
   }

   @Test
   void testFilterByProvidedInterface() {
      registrationUtil.registerAllServices();
      registrationUtil.disconnectService(new ServiceId(TestDataUtil.getIUASimulator())); // make IUA Simulator Unreachable

      // Look for any simulation services
      SearchQuery<ServiceSearchCriteria> query = new SearchQuery<>(
            new ServiceSearchCriteria()
                  .setProvidedInterface("Gazelle Simulation API"),
            null,
            null
      );
      SearchResult<DeployedService> result = serviceLookup.search(query, mockedIdentity);
      assertEquals(3, result.objects().size());
      assertThat(
            result.objects().stream().map(DeployedService::getName).toList(),
            contains("IUA Simulator", "mCSD Simulator", "MHD Simulator")
      );

      // Look for not unreachable simulation services
      query = new SearchQuery<>(
            new ServiceSearchCriteria()
                  .setProvidedInterface("Gazelle Simulation API")
                  .setStatus(AVAILABLE, UNKNOWN),
            null,
            null
      );
      result = serviceLookup.search(query, mockedIdentity);
      assertEquals(2, result.objects().size());
      assertThat(
            result.objects().stream().map(DeployedService::getName).toList(),
            contains("mCSD Simulator", "MHD Simulator")
      );
   }

   @Test
   void testFilterByConsumedInterface() {
      registrationUtil.registerAllServices();
      registrationUtil.disconnectService(new ServiceId(TestDataUtil.getHL7v2Validator())); // make IUA Simulator Unreachable

      // Look for any simulation services
      SearchQuery<ServiceSearchCriteria> query = new SearchQuery<>(
              new ServiceSearchCriteria()
                      .setConsumedInterface("Service Registration API"),
              null,
              null
      );
      SearchResult<DeployedService> result = serviceLookup.search(query, mockedIdentity);
      assertEquals(2, result.objects().size());
      assertThat(
              result.objects().stream().map(DeployedService::getName).toList(),
              contains("HL7v2 Validator","mCSD Simulator")
      );

      // Look for unreachable simulation services
      query = new SearchQuery<>(
              new ServiceSearchCriteria()
                      .setConsumedInterface("Service Registration API")
                      .setStatus(UNREACHABLE),
              null,
              null
      );
      result = serviceLookup.search(query, mockedIdentity);
      assertEquals(1, result.objects().size());
      assertThat(
              result.objects().stream().map(DeployedService::getName).toList(),
              contains("HL7v2 Validator")
      );
   }

}
