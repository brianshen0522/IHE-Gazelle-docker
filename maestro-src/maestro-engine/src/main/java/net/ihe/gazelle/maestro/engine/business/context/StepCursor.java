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

package net.ihe.gazelle.maestro.engine.business.context;

import net.ihe.gazelle.maestro.api.business.test.Step;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * Tracks iteration over the ordered list of {@link Step}.
 */
final class StepCursor {

   private final List<Step> steps;
   private int index;

   /**
    * Creates a new {@code StepCursor} for the given ordered list of steps.
    *
    * @param orderedSteps the ordered list of steps to iterate
    */
   private StepCursor(List<Step> orderedSteps) {
      this.steps = List.copyOf(orderedSteps);
      this.index = -1;
   }

   /**
    * Creates a {@code StepCursor} from the given list of ordered steps.
    *
    * @param orderedSteps the ordered list of steps
    * @return a new {@code StepCursor} instance
    */
   static StepCursor from(List<Step> orderedSteps) {
      return new StepCursor(orderedSteps);
   }

   /**
    * Indicates whether there is a next step available.
    *
    * @return {@code true} if a next step exists, {@code false} otherwise
    */
   boolean hasNext() {
      return index < steps.size() - 1;
   }

   /**
    * Advances to the next step and returns it.
    *
    * @return the next {@link Step}
    * @throws NoSuchElementException if there are no more steps
    */
   Step next() {
      if (!hasNext()) {
         throw new NoSuchElementException("No more steps to execute");
      }
      return steps.get(++index);
   }

   /**
    * Indicates whether the cursor is currently pointing to a step.
    *
    * @return {@code true} if a current step exists, {@code false} otherwise
    */
   boolean hasCurrent() {
      return index > -1;
   }

   /**
    * Returns the current step that the cursor points to.
    *
    * @return the current {@link Step}
    * @throws IllegalStateException if no step has been executed yet
    */
   Step current() {
      if (!hasCurrent()) {
         throw new IllegalStateException("No step has been executed yet");
      }
      return steps.get(index);
   }

   /**
    * Returns the index of the current step.
    *
    * @return the current step index
    */
   int currentIndex() {
      return index;
   }
}

