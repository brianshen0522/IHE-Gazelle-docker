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
 * ITB status request payload.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ItbStatusRequest {

   private List<String> session;
   private Boolean withLogs;
   private Boolean withReports;

   public List<String> getSession() {
      return session;
   }

   public ItbStatusRequest setSession(List<String> session) {
      this.session = session;
      return this;
   }

   public Boolean getWithLogs() {
      return withLogs;
   }

   public ItbStatusRequest setWithLogs(Boolean withLogs) {
      this.withLogs = withLogs;
      return this;
   }

   public Boolean getWithReports() {
      return withReports;
   }

   public ItbStatusRequest setWithReports(Boolean withReports) {
      this.withReports = withReports;
      return this;
   }
}
