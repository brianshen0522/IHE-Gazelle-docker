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

package net.ihe.gazelle.maestro.api.business.userinteract;

import net.ihe.gazelle.maestro.spi.business.Handler;

/**
 * The user interaction handler is used to ask additional actions to the user during test execution
 */
public interface UserInteractionHandler extends Handler {

   /**
    * The name of the service associated with user interaction
    */
   String SERVICE_NAME = "User Interaction Service";

   /**
    * The name of the interface of the user interaction
    */
   String INTERFACE_NAME = "User Interaction API";

   /**
    * Displays a message to the user during test execution.
    *
    * @param interactionTitle the title of the interaction
    * @param message the message to display
    * @param timeout the maximum time to wait for user interaction, in milliseconds
    */
   void displayMessage(String interactionTitle, String message, long timeout);
}
