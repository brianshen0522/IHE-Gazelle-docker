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

package net.ihe.gazelle.itb.gateway.technical.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * ITB start request payload.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ItbStartRequest {

   private String system;
   private String actor;
   private List<String> testSuite;
   private List<String> testCase;
   private List<ItbInputMappingEntry> inputMapping;
   private Boolean waitForCompletion;
   private Long maximumWaitTime;

   public ItbStartRequest() {
      // default constructor
   }

   public ItbStartRequest(ItbStartRequest source) {
      if (source == null) {
         throw new IllegalArgumentException("Source ItbStartRequest cannot be null");
      }
      this.system = source.system;
      this.actor = source.actor;
      this.testSuite = source.testSuite == null ? null : new ArrayList<>(source.testSuite);
      this.testCase = source.testCase == null ? null : new ArrayList<>(source.testCase);
      this.inputMapping = source.inputMapping == null ? null : new ArrayList<>(source.inputMapping);
      this.waitForCompletion = source.waitForCompletion;
      this.maximumWaitTime = source.maximumWaitTime;
   }

   public String getSystem() {
      return system;
   }

   public ItbStartRequest setSystem(String system) {
      this.system = system;
      return this;
   }

   public String getActor() {
      return actor;
   }

   public ItbStartRequest setActor(String actor) {
      this.actor = actor;
      return this;
   }

   public List<String> getTestSuite() {
      return testSuite;
   }

   public ItbStartRequest setTestSuite(List<String> testSuite) {
      this.testSuite = testSuite;
      return this;
   }

   public List<String> getTestCase() {
      return testCase;
   }

   public ItbStartRequest setTestCase(List<String> testCase) {
      this.testCase = testCase;
      return this;
   }

   public List<ItbInputMappingEntry> getInputMapping() {
      return inputMapping;
   }

   public ItbStartRequest setInputMapping(List<ItbInputMappingEntry> inputMapping) {
      this.inputMapping = inputMapping;
      return this;
   }

   public Boolean getWaitForCompletion() {
      return waitForCompletion;
   }

   public ItbStartRequest setWaitForCompletion(Boolean waitForCompletion) {
      this.waitForCompletion = waitForCompletion;
      return this;
   }

   public Long getMaximumWaitTime() {
      return maximumWaitTime;
   }

   public ItbStartRequest setMaximumWaitTime(Long maximumWaitTime) {
      this.maximumWaitTime = maximumWaitTime;
      return this;
   }
}
