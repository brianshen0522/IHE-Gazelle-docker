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

package net.ihe.gazelle.maestrorecording.adapter;

import net.ihe.gazelle.maestro.spi.business.recording.FutureReferenceCallbackRegistry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * In-memory registry for future reference resolution callbacks.
 */
class InMemoryFutureReferenceCallbackRegistry implements FutureReferenceCallbackRegistry {

   private final Map<String, List<BiConsumer<String, String>>> callbacksByReferenceName = new HashMap<>();
   private final Map<String, String> resolvedReferenceIds = new HashMap<>();

   @Override
   public void registerCallback(String referenceName, BiConsumer<String, String> callback) {
      if (referenceName == null || referenceName.isBlank() || callback == null) {
         throw new IllegalArgumentException("referenceName and callback must not be null or blank");
      }
      callbacksByReferenceName.computeIfAbsent(referenceName, key -> new ArrayList<>()).add(callback);
      String resolvedReferenceId = resolvedReferenceIds.get(referenceName);
      if (resolvedReferenceId != null) {
         callback.accept(referenceName, resolvedReferenceId);
      }
   }

   @Override
   public void notifyResolved(String referenceName, String referenceId) {
      if (referenceName == null || referenceName.isBlank() || referenceId == null || referenceId.isBlank()) {
         throw new IllegalArgumentException("referenceName and referenceId must not be null or blank");
      }
      resolvedReferenceIds.put(referenceName, referenceId);
      List<BiConsumer<String, String>> callbacks = callbacksByReferenceName.get(referenceName);
      if (callbacks == null) {
         return;
      }
      for (BiConsumer<String, String> callback : callbacks) {
         callback.accept(referenceName, referenceId);
      }
   }
}
