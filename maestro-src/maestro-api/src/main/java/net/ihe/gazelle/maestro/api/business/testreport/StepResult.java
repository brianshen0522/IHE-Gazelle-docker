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

package net.ihe.gazelle.maestro.api.business.testreport;

/**
 * Represents the possible outcomes of a simulation, validation,
 * or processing step.
 * <p>
 * Each enum constant also has a corresponding string constant
 * for use in contexts where only string values are supported.
 * </p>
 */
public enum StepResult {

   /**
    * Indicates that the process or operation has completed successfully,
    * without further qualification of success or failure.
    */
   DONE,

   /**
    * Indicates that the process or operation has passed validation
    * or met the required criteria.
    */
   PASSED,

   /**
    * Indicates that the process or operation has failed validation
    * or did not meet the required criteria.
    */
   FAILED,

   /**
    * Indicates that the outcome of the process or operation
    * could not be determined.
    */
   UNDEFINED;

   /** String constant for {@link #DONE}. */
   public static final String RESULT_DONE = "DONE";

   /** String constant for {@link #PASSED}. */
   public static final String RESULT_PASSED = "PASSED";

   /** String constant for {@link #FAILED}. */
   public static final String RESULT_FAILED = "FAILED";

   /** String constant for {@link #UNDEFINED}. */
   public static final String RESULT_UNDEFINED = "UNDEFINED";
}
