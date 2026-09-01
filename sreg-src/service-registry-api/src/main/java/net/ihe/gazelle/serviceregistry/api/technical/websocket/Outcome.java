/*
 * Copyright 2025 IHE International.
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

package net.ihe.gazelle.serviceregistry.api.technical.websocket;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Represents the outcome of a registration via websocket, indicating success or failure along with a message.
 */
@JsonPropertyOrder({"status", "message"})
public class Outcome {

   private Status status;
   private String message;


   /**
    * Default constructor for deserialization purposes.
    */
   public Outcome() {
   }

   /**
    * Constructs an Outcome with the specified status and message.
    *
    * @param status  the status of the outcome (SUCCESS or FAILURE)
    * @param message the message providing additional information about the outcome
    */
   public Outcome(Status status, String message) {
      this.status = status;
      this.message = message;
   }

   /**
    * Outcome status indicating whether the operation was successful or failed.
    */
   public enum Status {
      /**
       * Indicates that the operation was successful.
       */
      SUCCESS,
      /**
       * Indicates that the operation failed.
       */
      FAILURE
   }

   /**
    * Gets the status of the outcome.
    *
    * @return the status of the outcome
    */
   @JsonProperty("status")
   public Status getStatus() {
      return status;
   }

   /**
    * Sets the status of the outcome.
    *
    * @param status the status to set
    *
    * @return the current Outcome instance for method chaining
    */
   public Outcome setStatus(Status status) {
      this.status = status;
      return this;
   }

   /**
    * Gets the message associated with the outcome.
    *
    * @return the message providing additional information about the outcome
    */
   @JsonProperty("message")
   public String getMessage() {
      return message;
   }

   /**
    * Sets the message associated with the outcome.
    *
    * @param message the message to set
    *
    * @return the current Outcome instance for method chaining
    */
   public Outcome setMessage(String message) {
      this.message = message;
      return this;
   }

   /**
    * Creates a new sucessful Outcome instance with the specified message.
    *
    * @param message the message associated with the outcome
    *
    * @return a new Outcome instance with SUCCESS status
    */
   public static Outcome success(String message) {
      return new Outcome(Status.SUCCESS, message);
   }

   /**
    * Creates a new failed Outcome instance with the specified message.
    *
    * @param message the message associated with the outcome
    *
    * @return a new Outcome instance with FAILURE status
    */
   public static Outcome failure(String message) {
      return new Outcome(Status.FAILURE, message);
   }
}
