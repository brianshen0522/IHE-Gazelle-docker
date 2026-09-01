/*
 * Copyright 2026 IHE International.
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

package net.ihe.gazelle.maestro.spi.business.recording.plan;

import net.ihe.gazelle.security.business.acl.AccessControlList;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PersistencePlanTest {

   @Test
   void shouldExposeConfiguredRootAndAcl() {
      ItemPlan<String> root = new ItemPlan<>("type", "payload", null);
      AccessControlList acl = new AccessControlList();

      PersistencePlan<String> plan = new PersistencePlan<>(root, acl);
      ItemPlan<String> exposedRoot = plan.getRootItem();

      assertNotSame(root, exposedRoot);
      assertEquals(root, exposedRoot);
      assertSame(acl, plan.getAcl());
   }

   @Test
   void equalsAndHashCodeShouldUseRootAndAcl() {
      ItemPlan<String> root = new ItemPlan<>("type", "payload", null);
      AccessControlList acl = new AccessControlList();

      PersistencePlan<String> left = new PersistencePlan<>(root, acl);
      PersistencePlan<String> right = new PersistencePlan<>(root, acl);

      assertEquals(left, right);
      assertEquals(left.hashCode(), right.hashCode());
   }
}
