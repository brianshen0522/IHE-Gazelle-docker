/*
Copyright 2010-2025 IHE International

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package net.ihe.gazelle.xmlvalidation.technical.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ErrorHandler {

   public interface Assertion {
      void execute() throws Exception;
   }

   private static final String PACKAGE_PART_REGEX = "[a-zA-Z0-9$#~!%._\\-]+";

   public static String constructErrorMessage(Throwable t) {
      StringBuilder builder = new StringBuilder("");
      appendMessages(builder, t);
      return builder.toString();
   }

   private static void appendMessages(StringBuilder builder, Throwable t) {
      if (t != null) {
         builder.append(removePackageInfo(t.getMessage()));
         if (t.getCause() != null) {
            builder.append(System.lineSeparator());
            builder.append("Caused by: ");
            appendMessages(builder, t.getCause());
         }
      }
   }

   private static String removePackageInfo(String message) {
      // FIXME it removes too much, it should leave the exception name.
      int start = 0;
      boolean scanningPackagePrefix = true;
      while (scanningPackagePrefix && start < message.length()) {
         int delimiterIndex = message.indexOf(": ", start);
         if (delimiterIndex >= 0) {
            String packagePart = message.substring(start, delimiterIndex);
            if (packagePart.matches(PACKAGE_PART_REGEX)) {
               start = delimiterIndex + 2;
            } else {
               scanningPackagePrefix = false;
            }
         } else {
            scanningPackagePrefix = false;
         }
      }
      return message.substring(start);
   }

   private final List<String> errors = new ArrayList<>();

   public List<String> getErrors() {
      return Collections.unmodifiableList(errors);
   }

   public ErrorHandler addError(Exception e) {
      errors.add(constructErrorMessage(e));
      return this;
   }

   public ErrorHandler handle(Assertion assertion) {
      try {
         assertion.execute();
      } catch (Exception e) {
         errors.add(constructErrorMessage(e));
      }
      return this;
   }

   public boolean hasErrors() {
      return !errors.isEmpty();
   }

   @Override
   public String toString() {
      return errors.stream().reduce((a, b) -> a + System.lineSeparator() + b).orElse("");
   }
}
