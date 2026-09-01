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

package net.ihe.gazelle.itb.gateway.business.reporting;

import java.util.Objects;

/**
 * ITB callback aggregate containing system/session/report and enriched artifacts.
 */
public class ItbReporting {

    private ItbSystem system;
    private ItbTestSession testSession;
    private String testReport;
    private ItbResult result;
    private byte[] pdfReport;
    private String logs;

    /**
     * Creates an empty reporting payload.
     */
    public ItbReporting() {
    }

    /**
     * Creates a reporting payload.
     *
     * @param system ITB system information
     * @param testSession ITB session information
     * @param testReport raw ITB test report XML
     */
    public ItbReporting(ItbSystem system, ItbTestSession testSession, String testReport) {
        this.system = system;
        this.testSession = testSession;
        this.testReport = testReport;
    }

    /**
     * Returns ITB system information.
     *
     * @return system information
     */
    public ItbSystem getSystem() {
        return system;
    }

    /**
     * Sets ITB system information.
     *
     * @param system system information
     */
    public void setSystem(ItbSystem system) {
        this.system = system;
    }

    /**
     * Returns ITB session information.
     *
     * @return session information
     */
    public ItbTestSession getTestSession() {
        return testSession;
    }

    /**
     * Sets ITB session information.
     *
     * @param testSession session information
     */
    public void setTestSession(ItbTestSession testSession) {
        this.testSession = testSession;
    }

    /**
     * Returns raw ITB report payload.
     *
     * @return report payload
     */
    public String getTestReport() {
        return testReport;
    }

    /**
     * Sets raw ITB report payload.
     *
     * @param testReport report payload
     */
    public void setTestReport(String testReport) {
        this.testReport = testReport;
    }

   /**
    * Returns interpreted ITB result.
    *
    * @return execution result
    */
   public ItbResult getResult() {
      return result;
   }

   /**
    * Sets interpreted ITB result.
    *
    * @param result execution result
    * @return current reporting object
    */
   public ItbReporting setResult(ItbResult result) {
      this.result = result;
      return this;
   }

   /**
    * Returns retrieved PDF report bytes.
    *
    * @return PDF bytes
    */
   public byte[] getPdfReport() {
      return pdfReport;
   }

   /**
    * Sets PDF report bytes.
    *
    * @param pdfReport PDF bytes
    * @return current reporting object
    */
   public ItbReporting setPdfReport(byte[] pdfReport) {
      this.pdfReport = pdfReport;
      return this;
   }

   /**
    * Returns ITB execution logs.
    *
    * @return logs text
    */
   public String getLogs() {
      return logs;
   }

   /**
    * Sets ITB execution logs.
    *
    * @param logs logs text
    * @return current reporting object
    */
   public ItbReporting setLogs(String logs) {
      this.logs = logs;
      return this;
   }

   @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItbReporting that = (ItbReporting) o;
        return Objects.equals(system, that.system) &&
                Objects.equals(testSession, that.testSession) &&
                Objects.equals(testReport, that.testReport);
    }

    @Override
    public int hashCode() {
        return Objects.hash(system, testSession, testReport);
    }

    @Override
    public String toString() {
        return "ItbReporting{" +
                "system=" + system +
                ", testSession=" + testSession +
                ", testReport='" + (testReport != null ? "présent" : "absent") + '\'' +
                '}';
    }

}
