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

import java.util.List;

/**
 * ITB input mapping item for request/test suite/test case scoped inputs.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ItbInputMappingEntry {

   private ItbInput input;
   private List<String> testSuite;
   private List<String> testCase;

   public ItbInput getInput() {
      return input;
   }

   public ItbInputMappingEntry setInput(ItbInput input) {
      this.input = input;
      return this;
   }

   public List<String> getTestSuite() {
      return testSuite;
   }

   public ItbInputMappingEntry setTestSuite(List<String> testSuite) {
      this.testSuite = testSuite;
      return this;
   }

   public List<String> getTestCase() {
      return testCase;
   }

   public ItbInputMappingEntry setTestCase(List<String> testCase) {
      this.testCase = testCase;
      return this;
   }
}
