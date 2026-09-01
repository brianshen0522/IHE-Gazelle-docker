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

import net.ihe.gazelle.search.api.IndexService;
import net.ihe.gazelle.search.api.IndexedField;
import net.ihe.gazelle.search.api.IndexedField.Type;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ServiceIndexServiceTest {

   private final IndexService indexService = new ServiceIndexService();

   @Test
   void testIndexes() {
      assertEquals(6, indexService.getIndexedFields().size(),
            "Only 5 indexed fields expected, do not forget to update this test if you add/remove a new indexed field");
      assertIsIndexedField(ServiceIndexService.NAME, Type.STRING);
      assertIsIndexedField(ServiceIndexService.INSTANCE_ID, Type.STRING);
      assertIsIndexedField(ServiceIndexService.SELF_REGISTERED, Type.BOOLEAN);
      assertIsIndexedField(ServiceIndexService.STATUS, Type.STRING);
      assertIsIndexedField(ServiceIndexService.PROVIDED_INTERFACE, Type.STRING);
      assertIsIndexedField(ServiceIndexService.CONSUMED_INTERFACE, Type.STRING);
   }

   private void assertIsIndexedField(String fieldName, Type expectedType) {
      IndexedField field = assertDoesNotThrow(() -> indexService.getIndexedField(fieldName));
      assertEquals(expectedType, field.getFieldType(),
            "Field type for '" + fieldName + "' should be " + expectedType);
   }

}
