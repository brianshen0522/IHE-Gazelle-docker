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

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * A structure for the counters of a test.
 * Business rules <br>
 * at least 0 for every int <br>
 * total = sum(passed, failed, undefined)
 */
public class TestCounters implements Serializable {

   @Serial
   private static final long serialVersionUID = 5953325041092171103L;

   /**
    * The total number of test in the test.
    */
   private int total;

   /**
    * The number of passed test.
    */
   private int passed;

   /**
    * The number of failed test.
    */
   private int failed;

   /**
    * The number of undefined test.
    */
   private int undefined;

   /**
    * The number of unexpected errors.
    */
   private int unexpectedErrors;

   /**
    * Default constructor.
    */
   public TestCounters() {
      total = 0;
      passed = 0;
      failed = 0;
      undefined = 0;
      unexpectedErrors = 0;
   }

   /**
    * Constructs a new instance of {@code TestCounters} by copying the values
    * from another {@code TestCounters} instance.
    *
    * @param copy the {@code TestCounters} instance whose values are to
    *             be copied; if {@code null}, the new instance will be initialized
    *             with default values
    */
   public TestCounters(TestCounters copy) {
      this();
      if (copy != null) {
         this.total = copy.total;
         this.passed = copy.passed;
         this.failed = copy.failed;
         this.undefined = copy.undefined;
         this.unexpectedErrors = copy.unexpectedErrors;
      }
   }

   /**
    * Retrieves the total count value.
    *
    * @return the current total value as an integer
    */
   public int getTotal() {
      return total;
   }

   /**
    * Sets the total count value.
    *
    * @param total the total count value to set as an integer
    * @return the current instance of {@code TestCounters} to allow method chaining
    */
   public TestCounters setTotal(int total) {
      this.total = total;
      return this;
   }

   /**
    * Retrieves the number of passed test cases.
    *
    * @return the count of passed test cases as an integer
    */
   public int getPassed() {
      return passed;
   }

   /**
    * Sets the number of passed test cases.
    *
    * @param passed the number of passed test cases to set
    * @return the current instance of {@code TestCounters} to allow method chaining
    */
   public TestCounters setPassed(int passed) {
      this.passed = passed;
      return this;
   }

   /**
    * Retrieves the number of failed test cases.
    *
    * @return the count of failed test cases as an integer
    */
   public int getFailed() {
      return failed;
   }

   /**
    * Sets the number of failed test cases.
    *
    * @param failed the number of failed test cases to set
    * @return the current instance of {@code TestCounters} to allow method chaining
    */
   public TestCounters setFailed(int failed) {
      this.failed = failed;
      return this;
   }

   /**
    * Retrieves the count of undefined test cases.
    *
    * @return the count of undefined test cases as an integer
    */
   public int getUndefined() {
      return undefined;
   }

   /**
    * Sets the count of undefined test cases.
    *
    * @param undefined the number of undefined test cases to set
    * @return the current instance of {@code TestCounters} to allow method chaining
    */
   public TestCounters setUndefined(int undefined) {
      this.undefined = undefined;
      return this;
   }

   /**
    * Retrieves the count of unexpected errors.
    *
    * @return the count of unexpected errors as an integer
    */
   public int getUnexpectedErrors() {
      return unexpectedErrors;
   }

   /**
    * Sets the number of unexpected errors in the {@code TestCounters} instance.
    *
    * @param unexpectedErrors the number of unexpected errors to set
    * @return the current instance of {@code TestCounters} to allow method chaining
    */
   public TestCounters setUnexpectedErrors(int unexpectedErrors) {
      this.unexpectedErrors = unexpectedErrors;
      return this;
   }

   private void incrementTotal() {
      total++;
   }

   /**
    * Increments the count of passed test cases by 1 and also increments the total count to reflect an updated record.
    */
   public void incrementPassed() {
      passed++;
      incrementTotal();
   }

   /**
    * Increments the count of failed test cases by 1 and also increments the total count to reflect an updated record.
    */
   public void incrementFailed() {
      failed++;
      incrementTotal();
   }

   /**
    * Increments the count of undefined test cases by 1 and also increments the total count to reflect an updated record.
    */
   public void incrementUndefined() {
      undefined++;
      incrementTotal();
   }

   /**
    * Increments the count of unexpected errors by 1.
    */
   public void incrementUnexpectedErrors() {
      unexpectedErrors++;
   }

   /**
    * Increments the count of unexpected errors by a specified value.
    *
    * @param unexpectedErrors the number of unexpected errors to add;
    *                         must be a non-negative integer
    */
   public void incrementUnexpectedErrors(int unexpectedErrors) {
      this.unexpectedErrors += unexpectedErrors;
   }

   /**
    * Adds the counts from the specified {@code TestCounters} instance to the current instance.
    * This includes updating the total, passed, failed, undefined, and unexpected errors counts.
    *
    * @param subCounters the {@code TestCounters} instance from which to retrieve and add count values;
    *                    must not be {@code null}
    */
   public void addNumbersFromSubCounters(TestCounters subCounters) {
      total += subCounters.getTotal();
      passed += subCounters.getPassed();
      failed += subCounters.getFailed();
      undefined += subCounters.getUndefined();
      unexpectedErrors += subCounters.getUnexpectedErrors();
   }

   /**
    * Checks if the total count is equal to the sum of passed, failed, and undefined test case counts.
    * Ensures that the total is non-negative and matches the sum of the individual counts.
    *
    * @return true if the total count equals the sum of passed, failed, and undefined counts
    *         and the total is non-negative; false otherwise
    */
   public boolean isTotalEqualsToSumOfPassedFailedAndUndefined() {
      return total >= 0 && total == passed + failed + undefined;
   }

   /**
    * Determines if the total count is non-negative.
    *
    * @return true if the total count is greater than or equal to 0, indicating a positive or neutral value; false otherwise
    */
   public boolean isPassedPositive() {
      return total >= 0;
   }

   /**
    * Determines if the count of failed test cases is non-negative.
    *
    * @return true if the count of failed test cases is greater than or equal to 0, indicating a positive or neutral value; false otherwise
    */
   public boolean isFailedPositive() {
      return failed >= 0;
   }

   /**
    * Determines if the count of undefined test cases is non-negative.
    *
    * @return true if the count of undefined test cases is greater than or equal to 0,
    *         indicating a positive or neutral value; false otherwise.
    */
   public boolean isUndefinedPositive() {
      return undefined >= 0;
   }

   /**
    * Checks if the count of unexpected errors is non-negative.
    *
    * @return true if the count of unexpected errors is greater than or equal to 0, indicating a positive or neutral value; false otherwise
    */
   public boolean isUnexpectedPositive() {
      return unexpectedErrors >= 0;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof TestCounters that)) return false;
      return total == that.total
            && passed == that.passed
            && failed == that.failed
            && undefined == that.undefined
            && unexpectedErrors == that.unexpectedErrors;
   }

   @Override
   public int hashCode() {
      return Objects.hash(total, passed, failed, undefined, unexpectedErrors);
   }
}
