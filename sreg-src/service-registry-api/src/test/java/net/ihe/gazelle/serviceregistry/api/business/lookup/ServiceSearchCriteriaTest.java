/*
 * Copyright 2022-2025 IHE International.
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

package net.ihe.gazelle.serviceregistry.api.business.lookup;

import net.ihe.gazelle.search.api.SearchParameter;
import net.ihe.gazelle.serviceregistry.api.business.DeployedService;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.junit.jupiter.api.Assertions.*;

class ServiceSearchCriteriaTest {

   @Test
   void testNameCriteria() {
      ServiceSearchCriteria criteria = new ServiceSearchCriteria();
      criteria.setName("AnotherService", "YetAnotherService");

      assertCriteria(criteria.getName(),
            ServiceIndexService.NAME,
            new String[]{"AnotherService", "YetAnotherService"}
      );
   }

   @Test
   void testInstanceIdCriteria() {
      ServiceSearchCriteria criteria = new ServiceSearchCriteria();
      criteria.setInstanceId("12345", "67890");

      // Verify that multiple instance IDs are set correctly
      assertCriteria(criteria.getInstanceId(),
            ServiceIndexService.INSTANCE_ID,
            new String[]{"12345", "67890"}
      );
   }

   @Test
   void testSelfRegisteredCriteria() {
      ServiceSearchCriteria criteria = new ServiceSearchCriteria();
      criteria.setSelfRegistered(true);

      // Verify that self-registered is set correctly
      assertCriteria(criteria.getSelfRegistered(),
            ServiceIndexService.SELF_REGISTERED,
            new Boolean[]{true}
      );
   }

   @Test
   void testStatusCriteria() {
      ServiceSearchCriteria criteria = new ServiceSearchCriteria();
      criteria.setStatus(DeployedService.Status.AVAILABLE, DeployedService.Status.UNKNOWN);

      // Verify that multiple statuses are set correctly
      assertCriteria(criteria.getStatus(),
            ServiceIndexService.STATUS,
            new Object[]{DeployedService.Status.AVAILABLE, DeployedService.Status.UNKNOWN}
      );
   }

   @Test
   void testGetSearchParameters() {
      ServiceSearchCriteria criteria = new ServiceSearchCriteria()
            .setSelfRegistered(true)
            .setStatus(DeployedService.Status.AVAILABLE);

      // Verify that the search parameters are created correctly
      assertEquals(2, criteria.getSearchParameters().size(), "Should have 2 search parameters");
      assertCriteria(criteria.getSelfRegistered(), ServiceIndexService.SELF_REGISTERED, new Boolean[]{true});
      assertCriteria(criteria.getStatus(), ServiceIndexService.STATUS, new Object[]{DeployedService.Status.AVAILABLE});

      assertNull(criteria.getName(), "Name criteria should be null when not set");
      assertNull(criteria.getInstanceId(), "Instance ID criteria should be null when not set");
      assertNull(criteria.getProvidedInterface(), "Provided interface should be null when not set");
      assertNull(criteria.getConsumedInterface(), "Consumed interface should be null when not set");
   }

   @Test
   void testProvidedInterfaceCriteria() {
      ServiceSearchCriteria criteria = new ServiceSearchCriteria();
      criteria.setProvidedInterface("Simulation API", "Validation API");

      // Verify that multiple provided interfaces are set correctly
      assertCriteria(criteria.getProvidedInterface(),
            ServiceIndexService.PROVIDED_INTERFACE,
            new String[]{"Simulation API", "Validation API"}
      );
   }

   @Test
   void testConsumedInterfaceCriteria() {
      ServiceSearchCriteria criteria = new ServiceSearchCriteria();
      criteria.setConsumedInterface("Simulation API", "Validation API");

      // Verify that multiple consumed interfaces are set correctly
      assertCriteria(criteria.getConsumedInterface(),
              ServiceIndexService.CONSUMED_INTERFACE,
              new String[]{"Simulation API", "Validation API"}
      );
   }

   private void assertCriteria(SearchParameter parameter, String expectedName, Object[] expectedValues) {
      assertEquals(expectedName, parameter.getName(),
            "Criteria name should be '" + expectedName + "'");
      assertEquals(expectedValues.length, parameter.getValues().size(),
            "Should have " + expectedValues.length + " values");
      assertEquals(expectedValues[0], parameter.getFirstValue(), "First value should be '" + expectedValues[0] + "'");
      assertThat(parameter.getValues(), contains(expectedValues));
      assertFalse(parameter.isNegated());
   }

}
