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

/**
 * ITB session info returned by start endpoint.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ItbSessionCreationInformation {

   private String session;
   private Boolean completed;

   public String getSession() {
      return session;
   }

   public ItbSessionCreationInformation setSession(String session) {
      this.session = session;
      return this;
   }

   public Boolean getCompleted() {
      return completed;
   }

   public ItbSessionCreationInformation setCompleted(Boolean completed) {
      this.completed = completed;
      return this;
   }
}
