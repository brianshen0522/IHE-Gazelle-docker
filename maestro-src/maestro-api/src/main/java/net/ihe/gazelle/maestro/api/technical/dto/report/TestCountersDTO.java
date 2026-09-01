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

package net.ihe.gazelle.maestro.api.technical.dto.report;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import net.ihe.gazelle.maestro.api.business.testreport.TestCounters;
import net.ihe.gazelle.modelmarshaller.technical.dto.DTO;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(
      name = "TestCounters",
      description = "The counters for each possible result for a test execution."
)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
      "total",
      "passed",
      "failed",
      "undefined",
      "unexpectedErrors"
})
public class TestCountersDTO implements DTO<TestCounters> {

   @JsonIgnore
   private final TestCounters testCounters;

   public TestCountersDTO() {
      this(new TestCounters());
   }

   public TestCountersDTO(TestCounters testCounters) {
      this.testCounters = new TestCounters(testCounters);
   }

   @Override
   @JsonIgnore
   public TestCounters getBusinessObject() {
      return new TestCounters(testCounters);
   }

   @Schema(
         name = "total",
         description = "The total number of tests executed.",
         required = true
   )
   @JsonProperty(value = "total")
   public int getTotal() {
      return testCounters.getTotal();
   }

   public void setTotal(int total) {
      testCounters.setTotal(total);
   }

   @Schema(
         name = "passed",
         description = "The number of passed tests.",
         required = true
   )
   @JsonProperty(value = "passed")
   public int getPassed() {
      return testCounters.getPassed();
   }

   public void setPassed(int passed) {
      testCounters.setPassed(passed);
   }

   @Schema(
         name = "failed",
         description = "The number of failed tests.",
         required = true
   )
   @JsonProperty(value = "failed")
   public int getFailed() {
      return testCounters.getFailed();
   }

   public void setFailed(int failed) {
      testCounters.setFailed(failed);
   }

   @Schema(
         name = "undefined",
         description = "The number of undefined tests.",
         required = true
   )
   @JsonProperty(value = "undefined")
   public int getUndefined() {
      return testCounters.getUndefined();
   }

   public void setUndefined(int undefined) {
      testCounters.setUndefined(undefined);
   }

   @Schema(
         name = "unexpectedErrors",
         description = "The number of unexpected errors.",
         required = true
   )
   @JsonProperty(value = "unexpectedErrors")
   public int getUnexpectedErrors() {
      return testCounters.getUnexpectedErrors();
   }

   public void setUnexpectedErrors(int unexpectedErrors) {
      testCounters.setUnexpectedErrors(unexpectedErrors);
   }
}
