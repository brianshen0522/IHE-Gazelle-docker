/*
 * Copyright 2025-2026 IHE International.
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

package net.ihe.gazelle.simulation.business.search;

import net.ihe.gazelle.search.api.IndexedField;
import net.ihe.gazelle.search.api.IndexedField.Type;

import java.util.Map;

/**
 * Implementation of the {@link SequenceIndexService} interface.
 * <p>
 * Basically store a static map of Indexes and {@link IndexedField}.
 */
public class SequenceIndexServicesImpl implements SequenceIndexService {

   private static final Map<String, IndexedField> INDEXES = Map.of(
           SERVICE_NAME, new IndexedField(SERVICE_NAME, Type.STRING),
           ID, new IndexedField(ID, Type.STRING),
           TRANSACTION, new IndexedField(TRANSACTION, Type.STRING),
           STANDARD, new IndexedField(STANDARD, Type.STRING),
           SIMULATED_ROLE, new IndexedField(SIMULATED_ROLE, Type.STRING),
           TESTED_ROLE, new IndexedField(TESTED_ROLE, Type.STRING),
           SHORT_DESCRIPTION, new IndexedField(SHORT_DESCRIPTION, Type.STRING),
           RUNNABLE, new IndexedField(RUNNABLE, Type.BOOLEAN),
           VALID, new IndexedField(VALID, Type.BOOLEAN)
   );

   /**
    * Constructor.
    */
   public SequenceIndexServicesImpl() {
      // Empty
   }

    /**
     * Get the sequence indexes.
     *
     * @return a map of indexes and index fields
     */
   @Override
   public Map<String, IndexedField> getIndexes() {
      return INDEXES;
   }

}
