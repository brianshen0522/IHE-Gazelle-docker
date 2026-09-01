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
 * ITB status info returned for a session.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ItbSessionStatusInformation {

   private String session;
   private Boolean completed;
   private String report;
   private List<String> logs;

   public String getSession() {
      return session;
   }

   public ItbSessionStatusInformation setSession(String session) {
      this.session = session;
      return this;
   }

   public Boolean getCompleted() {
      return completed;
   }

   public ItbSessionStatusInformation setCompleted(Boolean completed) {
      this.completed = completed;
      return this;
   }

   public String getReport() {
      return report;
   }

   public ItbSessionStatusInformation setReport(String report) {
      this.report = report;
      return this;
   }

   public List<String> getLogs() {
      return logs;
   }

   public ItbSessionStatusInformation setLogs(List<String> logs) {
      this.logs = logs;
      return this;
   }
}
