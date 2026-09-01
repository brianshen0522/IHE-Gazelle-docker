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

import java.util.function.BiConsumer;

/**
 * Callback registry wrapper that scopes reference names with a namespace.
 */
class NamespacedFutureReferenceCallbackRegistry implements FutureReferenceCallbackRegistry {

   private final FutureReferenceCallbackRegistry delegate;
   private final String namespace;

   NamespacedFutureReferenceCallbackRegistry(FutureReferenceCallbackRegistry delegate, String namespace) {
      this.delegate = delegate;
      this.namespace = namespace;
   }

   @Override
   public void registerCallback(String referenceName, BiConsumer<String, String> callback) {
      delegate.registerCallback(scoped(referenceName), (ignoredScopedName, referenceId) -> callback.accept(referenceName, referenceId));
   }

   @Override
   public void notifyResolved(String referenceName, String referenceId) {
      delegate.notifyResolved(scoped(referenceName), referenceId);
   }

   private String scoped(String referenceName) {
      return namespace + ":" + referenceName;
   }
}
