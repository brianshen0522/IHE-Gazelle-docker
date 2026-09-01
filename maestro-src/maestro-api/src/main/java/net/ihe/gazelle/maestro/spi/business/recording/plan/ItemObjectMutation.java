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

/**
 * Callback applied when a referenced object receives its persisted identifier.
 *
 * @param <T> owner object type being mutated
 */
@FunctionalInterface
public interface ItemObjectMutation<T> {

   /**
    * Applies a reference-based mutation on the owning object.
    *
    * @param object owning object to mutate
    * @param referenceName logical reference name
    * @param referenceId persisted reference identifier
    */
   void mutateReference(T object, String referenceName, String referenceId);
}
