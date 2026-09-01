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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ServiceIdTest {

   @Test
   void testServiceIdConstructors() {
      // Test the constructor with DeployedService
      DeployedService service = new DeployedService();
      service.setName("TestService");
      service.setInstanceId("12345");
      service.setReplicaId("67890");
      ServiceId id = new ServiceId(service);
      assertEquals("12345", id.instanceId());
      assertEquals("67890", id.replicaId());

      // Test the constructor with individual parameters
      ServiceId id2 = new ServiceId("12345", "67890");
      assertEquals("12345", id2.instanceId());
      assertEquals("67890", id2.replicaId());
   }

   @Test
   void testConstructorErrors() {
      // Test with null parameters
      Throwable throwable = assertThrows(IllegalArgumentException.class, () -> new ServiceId(null, "abc"));
      assertEquals("Instance ID cannot be null or blank", throwable.getMessage());

      throwable = assertThrows(IllegalArgumentException.class, () -> new ServiceId("    ", "abc"));
      assertEquals("Instance ID cannot be null or blank", throwable.getMessage());

      throwable = assertThrows(IllegalArgumentException.class, () -> new ServiceId("abc", null));
      assertEquals("Replica ID cannot be null or blank", throwable.getMessage());

      throwable = assertThrows(IllegalArgumentException.class, () -> new ServiceId("abc", "   "));
      assertEquals("Replica ID cannot be null or blank", throwable.getMessage());
   }

   @Test
   void testEqualsAndHashCode() {
      ServiceId id1 = new ServiceId("12345", "67890");
      ServiceId id2 = new ServiceId("12345", "67890");
      ServiceId id3 = new ServiceId("54321", "09876");
      assertEquals(id2, id1);
      assertNotEquals(id3, id1);
      assertNotEquals(new Object(), id1);

      assertEquals(id1.hashCode(), id2.hashCode());
      assertNotEquals(id2.hashCode(), id3.hashCode());

      assertEquals(0, id1.compareTo(id2));
      assertTrue(id1.compareTo(id3) < 0);
   }

}

