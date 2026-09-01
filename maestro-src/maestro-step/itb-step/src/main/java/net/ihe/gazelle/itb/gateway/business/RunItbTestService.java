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

package net.ihe.gazelle.itb.gateway.business;


import net.ihe.gazelle.itb.gateway.business.reporting.ItbReporting;
import net.ihe.gazelle.itb.gateway.technical.client.model.ItbInput;
import net.ihe.gazelle.itb.gateway.technical.client.model.ItbInputMappingEntry;
import net.ihe.gazelle.itb.gateway.technical.client.model.ItbStartRequest;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Service to execute an ITB test suite.
 */
public class RunItbTestService {

   private static final String TYPE_STRING = "string";
   private static final String TYPE_NUMBER = "number";
   private static final String TYPE_BINARY = "binary";

   private final ItbClient itbClient;
   private final ItbSessionStore sessionStore;
   private final ItbReportingEnrichmentService enrichmentService;

   /**
    * Creates a service to launch ITB tests and await asynchronous reports.
    *
    * @param itbClient ITB client
    * @param sessionStore ITB callback session store
    */
   public RunItbTestService(ItbClient itbClient, ItbSessionStore sessionStore) {
      this.itbClient = itbClient;
      this.sessionStore = sessionStore;
      this.enrichmentService = new ItbReportingEnrichmentService(itbClient);
   }

   /**
    * Executes a start request in asynchronous mode.
    *
    * @param request start request to execute
    * @return future completed when ITB callback is received
    */
   public CompletableFuture<ItbReporting> runTests(ItbStartRequest request) {
      return runTests(request, ItbExecutionMode.ASYNC, 0L);
   }

   /**
    * Executes a start request in asynchronous or synchronous mode.
    *
    * @param request start request to execute
    * @param executionMode execution strategy
    * @param timeoutMs timeout in milliseconds used for synchronous execution
    * @return pending future in async mode, completed future in sync mode
    */
   public CompletableFuture<ItbReporting> runTests(ItbStartRequest request, ItbExecutionMode executionMode, long timeoutMs) {
      ItbStartRequest validRequest = validateRequest(request);
      if (executionMode == ItbExecutionMode.SYNC) {
         if (timeoutMs <= 0) {
            throw new RunItbTestServiceException("Sync execution requires a timeout greater than 0");
         }
         ItbReporting itbReporting = itbClient.startTestAndWait(validRequest, timeoutMs);
         return CompletableFuture.completedFuture(enrichmentService.enrich(itbReporting));
      }
      String sessionId = itbClient.startTest(validRequest);
      CompletableFuture<ItbReporting> future = new CompletableFuture<>();
      sessionStore.add(sessionId, future);
      return future;
   }

   private ItbStartRequest validateRequest(ItbStartRequest request) {
      if (request == null) {
         throw new RunItbTestServiceException("No test request content");
      }
      if (isBlank(request.getSystem())) {
         throw new RunItbTestServiceException("System ID is missing");
      }
      if (isBlank(request.getActor())) {
         throw new RunItbTestServiceException("Actor ID is missing");
      }
      if (isEmpty(request.getTestSuite()) && isEmpty(request.getTestCase())) {
         throw new RunItbTestServiceException("No tests indicated");
      }

      validateIdentifiers(request.getTestSuite(), "test suite");
      validateIdentifiers(request.getTestCase(), "test case");

      ItbStartRequest validatedRequest = new ItbStartRequest(request);
      validatedRequest.setInputMapping(validateInputMapping(request.getInputMapping()));
      return validatedRequest;
   }

   private List<ItbInputMappingEntry> validateInputMapping(List<ItbInputMappingEntry> inputMapping) {
      if (inputMapping == null || inputMapping.isEmpty()) {
         return inputMapping;
      }
      List<ItbInputMappingEntry> validatedEntries = new ArrayList<>();
      for (ItbInputMappingEntry entry : inputMapping) {
         if (entry == null) {
            throw new RunItbTestServiceException("Input mapping entry is missing");
         }
         validateIdentifiers(entry.getTestSuite(), "input mapping test suite");
         validateIdentifiers(entry.getTestCase(), "input mapping test case");

         ItbInput validatedInput = validateInput(entry.getInput());
         validatedEntries.add(new ItbInputMappingEntry()
               .setTestSuite(entry.getTestSuite())
               .setTestCase(entry.getTestCase())
               .setInput(validatedInput));
      }
      return validatedEntries;
   }

   private ItbInput validateInput(ItbInput input) {
      if (input == null) {
         throw new RunItbTestServiceException("Input mapping entry input is missing");
      }
      if (isBlank(input.getName())) {
         throw new RunItbTestServiceException("Input name is missing");
      }

      if (input.getItem() != null && !input.getItem().isEmpty()) {
         String inputType = isBlank(input.getType()) ? "map" : input.getType().trim().toLowerCase();
         if (!"map".equals(inputType)) {
            throw new RunItbTestServiceException("Wrong type for input " + input.getName() + " : " + inputType);
         }
         List<ItbInput> validatedItems = new ArrayList<>();
         for (ItbInput child : input.getItem()) {
            validatedItems.add(validateInput(child));
         }
         return new ItbInput()
               .setName(input.getName())
               .setType("map")
               .setItem(validatedItems);
      }

      if (input.getValue() == null) {
         throw new RunItbTestServiceException(input.getName() + " input value is missing");
      }

      String type = normalizeType(input.getType(), input.getValue());
      String embeddingMethod = input.getEmbeddingMethod();

      switch (type) {
         case TYPE_STRING -> validateTextInput(input.getName(), input.getValue());
         case TYPE_NUMBER -> validateNumberInput(input.getName(), input.getValue());
         case TYPE_BINARY -> {
            validateBinaryInput(input.getName(), input.getValue());
            if (isBlank(embeddingMethod)) {
               embeddingMethod = "BASE64";
            }
         }
         default -> throw new RunItbTestServiceException("Wrong type for input " + input.getName() + " : " + type);
      }

      return new ItbInput()
            .setName(input.getName())
            .setType(type)
            .setEmbeddingMethod(embeddingMethod)
            .setValue(input.getValue());
   }

   private String normalizeType(String inputType, String value) {
      if (isBlank(inputType)) {
         return inferTypeFromValue(value);
      }
      String normalized = inputType.trim().toLowerCase();
      return switch (normalized) {
         case "text", "uuid", "apikey", TYPE_STRING -> TYPE_STRING;
         case "integer", "float", TYPE_NUMBER -> TYPE_NUMBER;
         case "file", TYPE_BINARY -> TYPE_BINARY;
         default -> normalized;
      };
   }

   private String inferTypeFromValue(String value) {
      if (value != null && isBase64(value)) {
         return TYPE_BINARY;
      }
      if (value != null) {
         try {
            Double.parseDouble(value);
            return TYPE_NUMBER;
         } catch (NumberFormatException ignored) {
            // no-op
         }
      }
      return TYPE_STRING;
   }

   private void validateTextInput(String inputName, String value) {
      if (value.isEmpty()) {
         throw new RunItbTestServiceException(inputName + " input value is empty");
      }
   }

   private void validateNumberInput(String inputName, String value) {
      try {
         Double.parseDouble(value);
      } catch (NumberFormatException e) {
         throw new RunItbTestServiceException("Wrong format for number input : " + inputName);
      }
   }

   private void validateBinaryInput(String inputName, String value) {
      if (!isBase64(value)) {
         throw new RunItbTestServiceException("Wrong format for binary input : " + inputName);
      }
   }

   private boolean isBase64(String value) {
      if (value == null || value.isBlank()) {
         return false;
      }
      try {
         Base64.getDecoder().decode(value);
         return true;
      } catch (IllegalArgumentException e) {
         return false;
      }
   }

   private void validateIdentifiers(List<String> ids, String label) {
      if (ids == null) {
         return;
      }
      for (String id : ids) {
         if (isBlank(id)) {
            throw new RunItbTestServiceException("A " + label + " identifier is missing");
         }
      }
   }

   private boolean isEmpty(List<?> values) {
      return values == null || values.isEmpty();
   }

   private boolean isBlank(String value) {
      return value == null || value.isBlank();
   }
}
