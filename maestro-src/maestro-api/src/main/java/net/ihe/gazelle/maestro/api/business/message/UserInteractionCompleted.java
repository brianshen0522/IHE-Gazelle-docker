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

package net.ihe.gazelle.maestro.api.business.message;

import net.ihe.gazelle.maestro.api.business.property.Property;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

/**
 * Message sent by the producer to notify a subscriber that the user interaction has been completed.
 */
public class UserInteractionCompleted implements Message {

   @Serial
   private static final long serialVersionUID = 8043799892319471672L;

   /**
    * The session id of the execution
    */
   private String sessionId;

   /**
    * The inputs provided by the user
    */
   private final List<Property> inputs;

   /**
    * Default constructor
    */
   public UserInteractionCompleted() {
      this(null);
   }

   /**
    * Constructs an instance of {@code UserInteractionCompleted} with the provided session ID.
    *
    * @param sessionId the session ID associated with the user interaction.
    *                  This ID is used to uniquely identify the execution session.
    */
   public UserInteractionCompleted(String sessionId) {
      this.sessionId = sessionId;
      this.inputs = new ArrayList<>();
   }

   /**
    * Retrieves the session ID associated with the execution.
    *
    * @return the session ID as a string, or null if no session ID is set.
    */
   public String getSessionId() {
      return sessionId;
   }

   /**
    * Sets the session ID associated with the execution.
    *
    * @param sessionId the session ID to be set. This ID is used to uniquely identify the execution session.
    * @return the current instance of {@code UserInteractionCompleted} for method chaining.
    */
   public UserInteractionCompleted setSessionId(String sessionId) {
      this.sessionId = sessionId;
      return this;
   }

   /**
    * Retrieves the list of inputs provided by the user. Each input is
    * represented as an instance of the {@code Property} class.
    *
    * @return a new list containing all inputs provided by the user. Modifications
    * to this list will not affect the original input list.
    */
   public List<Property> getInputs() {
      return new ArrayList<>(inputs);
   }

   /**
    * Adds a user-provided input to the list of inputs associated with this instance
    * of {@code UserInteractionCompleted}. Each input is represented as an instance
    * of {@code Property}.
    *
    * @param input the {@code Property} instance representing the user-provided input
    *              to be added. Must not be null.
    * @return the current instance of {@code UserInteractionCompleted} for method chaining.
    */
   public UserInteractionCompleted addInput(Property input) {
      this.inputs.add(input);
      return this;
   }

   /**
    * Adds a list of user-provided inputs to the current list of inputs associated with
    * this instance of {@code UserInteractionCompleted}. Each input is represented as
    * an instance of {@code Property}.
    *
    * @param inputs the list of {@code Property} instances representing the user-provided
    *               inputs to be added. Must not be null.
    * @return the current instance of {@code UserInteractionCompleted} for method chaining.
    */
   public UserInteractionCompleted addInputs(List<Property> inputs) {
      this.inputs.addAll(inputs);
      return this;
   }
}

