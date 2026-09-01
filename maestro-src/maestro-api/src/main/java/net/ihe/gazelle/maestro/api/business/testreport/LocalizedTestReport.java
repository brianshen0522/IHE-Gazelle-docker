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

package net.ihe.gazelle.maestro.api.business.testreport;

import java.io.Serial;
import java.util.Objects;

/**
 * A test report with its location.
 */
public class LocalizedTestReport extends TestReport {

   @Serial
   private static final long serialVersionUID = 9186291567338000846L;

   /**
    * The location of the test report.
    */
   private String location;

   /**
    * Constructs a LocalizedTestReport instance with the given test report and location.
    *
    * @param testReport the test report associated with this localized report
    * @param location the location associated with this test report
    */
   public LocalizedTestReport(TestReport testReport, String location) {
      super(testReport);
      this.location = location;
   }

   /**
    * Constructs a LocalizedTestReport instance with the specified location.
    *
    * @param location the location associated with this test report
    */
   public LocalizedTestReport(String location) {
      super();
      this.location = location;
   }

   /**
    * Retrieves the location associated with this test report.
    *
    * @return the location of the test report as a string
    */
   public String getLocation() {
      return location;
   }

   /**
    * Sets the location of the test report.
    *
    * @param location the location to associate with the test report
    */
   public void setLocation(String location) {
      this.location = location;
   }

   @Override
   public boolean equals(Object o) {
      if (o == null || getClass() != o.getClass()) return false;
      if (!super.equals(o)) return false;
      LocalizedTestReport that = (LocalizedTestReport) o;
      return Objects.equals(location, that.location);
   }

   @Override
   public int hashCode() {
      return Objects.hash(super.hashCode(), location);
   }
}
