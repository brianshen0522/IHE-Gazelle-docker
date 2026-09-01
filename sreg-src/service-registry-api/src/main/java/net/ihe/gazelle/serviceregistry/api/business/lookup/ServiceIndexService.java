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

package net.ihe.gazelle.serviceregistry.api.business.lookup;

import net.ihe.gazelle.search.api.IndexService;
import net.ihe.gazelle.search.api.IndexedField;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ServiceIndexService defines the indexes availble for searching servives in the Service Registry.
 * It extends the IndexService interface and provides constants for indexing fields.
 */
public class ServiceIndexService implements IndexService {

   /**
    * Criterion name to filter on the name of the service.
    */
   public static final String NAME = "name";
   /**
    * Criterion name to filter on the unique identifier for the service instance.
    */
   public static final String INSTANCE_ID = "instanceId";
   /**
    * Criterion name fo filter whether the service is self-registered or not.
    */
   public static final String SELF_REGISTERED = "selfRegistered";
   /**
    * Criterion name to filter on the status of the service.
    */
   public static final String STATUS = "status";
   /**
    * Criterion name to filter on interface names provided by the service.
    */
   public static final String PROVIDED_INTERFACE = "providedInterface";

   /**
    * Criterion name to filter on interface names consumed by the service.
    */
   public static final String CONSUMED_INTERFACE = "consumedInterface";

   private static final Map<String, IndexedField> INDEXES = new LinkedHashMap<>();
   static {
      INDEXES.put(NAME, new IndexedField(NAME, IndexedField.Type.STRING));
      INDEXES.put(INSTANCE_ID, new IndexedField(INSTANCE_ID, IndexedField.Type.STRING));
      INDEXES.put(SELF_REGISTERED, new IndexedField(SELF_REGISTERED, IndexedField.Type.BOOLEAN));
      INDEXES.put(STATUS, new IndexedField(STATUS, IndexedField.Type.STRING));
      INDEXES.put(PROVIDED_INTERFACE, new IndexedField(PROVIDED_INTERFACE, IndexedField.Type.STRING));
      INDEXES.put(CONSUMED_INTERFACE, new IndexedField(CONSUMED_INTERFACE, IndexedField.Type.STRING));
   }

   /**
    * Default constructor for ServiceIndexService.
    */
   public ServiceIndexService() {
      // Default constructor
   }

   @Override
   public Map<String, IndexedField> getIndexes() {
      return new LinkedHashMap<>(INDEXES);
   }

}
