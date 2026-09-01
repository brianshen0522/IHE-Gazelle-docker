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

package net.ihe.gazelle.serviceregistry.api.business;

import net.ihe.gazelle.servicemetadata.api.business.Service;
import org.junit.jupiter.api.Test;

import static net.ihe.gazelle.serviceregistry.api.business.DeployedService.Status.AVAILABLE;
import static net.ihe.gazelle.serviceregistry.api.business.DeployedService.Status.UNREACHABLE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class DeployedServiceTest {

   @Test
   void testDeployedServiceCopyConstructor() {
      // Create an original DeployedService instance
      DeployedService original = new DeployedService(
            new Service()
                  .setName("Original Service")
                  .setVersion("1.0")
                  .setInstanceId("12345")
                  .setReplicaId("67890")
      ).setStatus(AVAILABLE)
            .setSelfRegistered(true)
            .resetLastUpdate();

      // Create a copy using the copy constructor
      DeployedService copy = new DeployedService(original);

      // Verify that the copy has the same properties as the original
      assertEquals(original, copy);
   }

   @Test
   void testEqualsAndHashCode() {
      DeployedService service1 = new DeployedService(
            new Service()
                  .setName("Test Service")
                  .setVersion("1.0")
                  .setInstanceId("abc123")
                  .setReplicaId("replica1")
      ).setStatus(AVAILABLE)
            .setSelfRegistered(true)
            .resetLastUpdate();

      DeployedService service2 = new DeployedService(
            new Service()
                  .setName("Test Service")
                  .setVersion("1.0")
                  .setInstanceId("abc123")
                  .setReplicaId("replica1")
      ).setStatus(AVAILABLE)
            .setSelfRegistered(true)
            .setLastUpdate(service1.getLastUpdate());

      DeployedService service3 = new DeployedService(
            new Service()
                  .setName("Another Service")
                  .setVersion("1.0")
                  .setInstanceId("xyz789")
                  .setReplicaId("replica2")
      ).setStatus(UNREACHABLE)
            .setSelfRegistered(false);

      assertEquals(service1, service2);
      assertNotEquals(service1, service3);
      assertEquals(service1.hashCode(), service2.hashCode());
      assertNotEquals(service1.hashCode(), service3.hashCode());

      assertNotEquals(null, service1);
   }

}
