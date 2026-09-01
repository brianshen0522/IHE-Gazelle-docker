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

package net.ihe.gazelle.maestro.engine.business;

import java.io.Serial;

/**
 * Exception thrown when an error occurs during the recording of a test report.
 */
public class TestReportRecordingException extends RuntimeException {

   @Serial
   private static final long serialVersionUID = 3719443258062405706L;

   /**
    * Creates a new {@code TestReportRecordingException} with the specified message.
    *
    * @param message the detail message
    */
   public TestReportRecordingException(String message) {
      super(message);
   }

   /**
    * Creates a new {@code TestReportRecordingException} with the specified message and cause.
    *
    * @param message the detail message
    * @param cause the cause of the exception
    */
   public TestReportRecordingException(String message, Throwable cause) {
      super(message, cause);
   }

   /**
    * Creates a new {@code TestReportRecordingException} with the specified cause.
    *
    * @param cause the cause of the exception
    */
   public TestReportRecordingException(Throwable cause) {
      super(cause);
   }

}
