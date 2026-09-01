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

package net.ihe.gazelle.maestro.spi.business.recording;

import java.util.function.BiConsumer;

/**
 * Registry used to subscribe to and publish future reference resolution events.
 */
public interface FutureReferenceCallbackRegistry {

   /**
    * Registers a callback that will be called with (referenceName, resolvedReferenceId).
    *
    * @param referenceName logical name of the planned reference
    * @param callback callback invoked when the reference is resolved
    */
   void registerCallback(String referenceName, BiConsumer<String, String> callback);

   /**
    * Notifies that a future reference has been resolved.
    *
    * @param referenceName logical reference name
    * @param referenceId resolved id/value
    */
   void notifyResolved(String referenceName, String referenceId);
}
