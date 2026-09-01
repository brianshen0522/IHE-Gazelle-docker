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

import net.ihe.gazelle.maestro.api.business.test.SupportedInput;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a message for user interaction.
 */
public class InteractWithUser implements Message {

   @Serial
   private static final long serialVersionUID = 5192092115743420623L;

   /**
    * Title of the interaction.
    */
   private String interactionTitle;

   /**
    * Message to display to the user.
    */
   private String message;

   /**
    * Timeout in milliseconds.
    */
   private Long timeout;

   /**
    * List of supported inputs.
    */
   private final List<SupportedInput> supportedInputs;

   /**
    * Constructs a default instance of the InteractWithUser class.
    * Initializes the interaction with no title, no message, no timeout,
    * and an empty list of supported inputs.
    */
   public InteractWithUser() {
      this(null, null, null);
   }

   /**
    * Constructs an instance of the InteractWithUser class with the specified title, message,
    * and timeout, and initializes an empty list of supported inputs.
    *
    * @param interactionTitle the title of the user interaction
    * @param message the message to be displayed to the user
    * @param timeout the timeout duration in milliseconds
    */
   public InteractWithUser(String interactionTitle, String message, Long timeout) {
      this.interactionTitle = interactionTitle;
      this.message = message;
      this.timeout = timeout;
      this.supportedInputs = new ArrayList<>();
   }

   /**
    * Retrieves the title of the interaction.
    *
    * @return the interaction title as a String
    */
   public String getInteractionTitle() {
      return interactionTitle;
   }

   /**
    * Sets the title of the user interaction.
    *
    * @param interactionTitle the title to be set for the user interaction
    * @return the current instance of InteractWithUser to allow method chaining
    */
   public InteractWithUser setInteractionTitle(String interactionTitle) {
      this.interactionTitle = interactionTitle;
      return this;
   }

   /**
    * Retrieves the message to be displayed to the user.
    *
    * @return the message as a String
    */
   public String getMessage() {
      return message;
   }

   /**
    * Sets the message to be displayed to the user.
    *
    * @param message the message to be set
    * @return the current instance of InteractWithUser to allow method chaining
    */
   public InteractWithUser setMessage(String message) {
      this.message = message;
      return this;
   }

   /**
    * Retrieves the timeout value for the user interaction.
    *
    * @return the timeout duration in milliseconds as a Long, or null if no timeout is set
    */
   public Long getTimeout() {
      return timeout;
   }

   /**
    * Sets the timeout value for the user interaction.
    *
    * @param timeout the timeout duration in milliseconds, or null if no timeout is to be set
    * @return the current instance of InteractWithUser to allow method chaining
    */
   public InteractWithUser setTimeout(Long timeout) {
      this.timeout = timeout;
      return this;
   }

   /**
    * Retrieves the list of supported inputs for the user interaction.
    *
    * @return a list of SupportedInput objects representing the supported input options
    */
   public List<SupportedInput> getSupportedInputs() {
      return new ArrayList<>(supportedInputs);
   }

   /**
    * Adds a supported input to the list of inputs associated with the interaction.
    * This method allows chaining by returning the current instance of {@code InteractWithUser}.
    *
    * @param supportedInput the {@code SupportedInput} object to be added to the list of supported inputs
    * @return the current instance of {@code InteractWithUser} to allow method chaining
    */
   public InteractWithUser addSupportedInput(SupportedInput supportedInput) {
      this.supportedInputs.add(supportedInput);
      return this;
   }

   /**
    * Adds a list of supported inputs to the current instance of {@code InteractWithUser}.
    * This method appends all items in the provided list to the existing list of supported inputs
    * and returns the current instance to allow method chaining.
    *
    * @param supportedInputs a list of {@code SupportedInput} objects to be added to the supported inputs
    * @return the current instance of {@code InteractWithUser} to allow method chaining
    */
   public InteractWithUser addSupportedInputs(List<SupportedInput> supportedInputs) {
      this.supportedInputs.addAll(supportedInputs);
      return this;
   }
}
