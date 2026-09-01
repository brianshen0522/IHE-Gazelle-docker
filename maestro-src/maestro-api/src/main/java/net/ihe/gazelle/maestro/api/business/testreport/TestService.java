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
import java.io.Serializable;
import java.util.Objects;

/**
 * A structure for the service providing the test report.
 * Business rules <br>
 * all mandatory <br>
 * serviceIdentification.version mandatory (version "unknown" is accepted)
 */
public class TestService implements Serializable {

   @Serial
   private static final long serialVersionUID = 4438062754526037863L;

   /**
    * The identification of the service providing the test report.
    */
   private EntityIdentification serviceIdentification;

   /**
    * A disclaimer to be added to the test report.
    */
   private String disclaimer;

   /**
    * Default constructor
    */
   public TestService() {
   }

   /**
    * Constructs an instance of {@code TestService} with the specified service identification
    * and disclaimer.
    *
    * @param serviceIdentification the identification of the service providing the test report; must not be null
    * @param disclaimer the disclaimer to be added to the test report; can be null or blank
    */
   public TestService(EntityIdentification serviceIdentification, String disclaimer) {
      setServiceIdentification(serviceIdentification);
      this.disclaimer = disclaimer;
   }

   /**
    * Retrieves the identification of the service providing the test report.
    *
    * @return the service identification, or null if not defined
    */
   public EntityIdentification getServiceIdentification() {
      return serviceIdentification;
   }

   /**
    * Sets the service identification for the test service. If the version of the provided
    * service identification is null, it defaults the version to "unknown".
    *
    * @param serviceIdentification the identification of the service providing the test report; must not be null
    * @return the current instance of {@code TestService} for method chaining
    */
   public TestService setServiceIdentification(EntityIdentification serviceIdentification) {
      if (serviceIdentification.getVersion() == null) {
         serviceIdentification.setVersion("unknown");
      }
      this.serviceIdentification = serviceIdentification;
      return this;
   }

   /**
    * Retrieves the disclaimer associated with the current instance.
    *
    * @return the disclaimer as a String, or null if no disclaimer is set
    */
   public String getDisclaimer() {
      return disclaimer;
   }

   /**
    * Sets the disclaimer associated with the test report.
    *
    * @param disclaimer the disclaimer to be added to the test report; can be null or blank
    * @return the current instance of {@code TestService} for method chaining
    */
   public TestService setDisclaimer(String disclaimer) {
      this.disclaimer = disclaimer;
      return this;
   }

   /**
    * Checks if the service identification is defined for the current instance.
    *
    * @return true if the service identification is not null; false otherwise
    */
   public boolean isServiceIdentificationDefined() {
      return serviceIdentification != null;
   }

   /**
    * Checks whether the service version is defined.
    *
    * @return true if the service version is not null and not blank; false otherwise
    */
   public boolean isServiceVersionDefined() {
      return serviceIdentification.getVersion() != null && !serviceIdentification.getVersion().isBlank();
   }

   /**
    * Checks whether the disclaimer is defined for the current instance.
    *
    * @return true if the disclaimer is not null and not blank; false otherwise
    */
   public boolean isDisclaimerDefined() {
      return disclaimer != null && !disclaimer.isBlank();
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof TestService that)) return false;
      return Objects.equals(serviceIdentification, that.serviceIdentification)
            && Objects.equals(disclaimer, that.disclaimer);
   }

   @Override
   public int hashCode() {
      return Objects.hash(serviceIdentification, disclaimer);
   }
}
