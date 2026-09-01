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

package net.ihe.gazelle.serviceregistry.technical.rest;

import jakarta.ws.rs.QueryParam;
import net.ihe.gazelle.search.jaxrs.api.QueryBeanParam;
import org.eclipse.microprofile.openapi.annotations.enums.ParameterIn;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;

import static net.ihe.gazelle.serviceregistry.api.business.lookup.ServiceIndexService.*;

/**
 * ServiceQueryBeanParam is a Jakarta-RS bean parameter class used to encapsulate query parameters for service
 * searches.
 */
public class ServiceQueryBeanParam extends QueryBeanParam {

   @Parameter(
         name = NAME,
         in = ParameterIn.QUERY,
         description = "Criterion to filter on the name of the service."
   )
   @QueryParam(NAME)
   private String name;

   @Parameter(
         name = "instanceId",
         in = ParameterIn.QUERY,
         description = "Criterion to filter on the unique identifier for the service instance."
   )
   @QueryParam("instanceId")
   private String instanceId;

   @Parameter(
         name = STATUS,
         in = ParameterIn.QUERY,
         schema = @Schema(type = SchemaType.STRING, enumeration = {"AVAILABLE", "UNREACHABLE", "UNKNOWN"}),
         description = "Criterion to filter on the status of the service."
   )
   @QueryParam(STATUS)
   private String status;

   @Parameter(
         name = "selfRegistered",
         in = ParameterIn.QUERY,
         schema = @Schema(type = SchemaType.BOOLEAN),
         description = "Criterion to filter whether the service is self-registered or not."
   )
   @QueryParam("selfRegistered")
   private String selfRegistered;

   @Parameter(
         name = PROVIDED_INTERFACE,
         in = ParameterIn.QUERY,
         description = "Criterion to filter on the interface names provided by the service."
   )
   @QueryParam(PROVIDED_INTERFACE)
   private String providedInterface;

   @Parameter(
           name = CONSUMED_INTERFACE,
           in = ParameterIn.QUERY,
           description = "Criterion to filter on the interface names consumed by the service."
   )
   @QueryParam(CONSUMED_INTERFACE)
   private String consumedInterface;

   /**
    * Default constructor for ServiceQueryBeanParam.
    */
   public ServiceQueryBeanParam() {
      super(); // Default constructor
   }

   /**
    * Gets the name of the service to look for.
    *
    * @return the name criterion.
    */
   public String getName() {
      return name;
   }

   /**
    * Sets the name of the service to look for.
    *
    * @param name the name criterion to set.
    *
    * @return the current instance of ServiceQueryBeanParam for method chaining.
    */
   public ServiceQueryBeanParam setName(String name) {
      this.name = name;
      return this;
   }

   /**
    * Gets the criterion to filter on unique identifier.
    *
    * @return the instance ID criterion.
    */
   public String getInstanceId() {
      return instanceId;
   }

   /**
    * Sets the criterion to filter on unique identifier.
    *
    * @param instanceId the instance ID criterion to set.
    *
    * @return the current instance of ServiceQueryBeanParam for method chaining.
    */
   public ServiceQueryBeanParam setInstanceId(String instanceId) {
      this.instanceId = instanceId;
      return this;
   }

   /**
    * Gets the status criterion.
    *
    * @return the status criterion.
    */
   public String getStatus() {
      return status;
   }

   /**
    * Sets the status criterion.
    *
    * @param status the status criterion to set.
    *
    * @return the current instance of ServiceQueryBeanParam for method chaining.
    */
   public ServiceQueryBeanParam setStatus(String status) {
      this.status = status;
      return this;
   }

   /**
    * Gets the self-registered criterion.
    *
    * @return the self-registered criterion.
    */
   public String getSelfRegistered() {
      return selfRegistered;
   }

   /**
    * Sets the self-registered criterion.
    *
    * @param selfRegistered the self-registered criterion to set.
    *
    * @return the current instance of ServiceQueryBeanParam for method chaining.
    */
   public ServiceQueryBeanParam setSelfRegistered(String selfRegistered) {
      this.selfRegistered = selfRegistered;
      return this;
   }

   /**
    * Gets the criterion to filter on provided interface names.
    *
    * @return the provided interface criterion.
    */
   public String getProvidedInterface() {
      return providedInterface;
   }

   /**
    * Sets the criterion to filter on provided interface names.
    *
    * @param providedInterface the provided interface criterion to set.
    *
    * @return the current instance of ServiceQueryBeanParam for method chaining.
    */
   public ServiceQueryBeanParam setProvidedInterface(String providedInterface) {
      this.providedInterface = providedInterface;
      return this;
   }

   /**
    * Gets the criterion to filter on consumed interface names.
    *
    * @return the consumed interface criterion.
    */
   public String getConsumedInterface() {
      return consumedInterface;
   }

   /**
    * Sets the criterion to filter on consumed interface names.
    *
    * @param consumedInterface the consumed interface criterion to set.
    *
    * @return the current instance of ServiceQueryBeanParam for method chaining.
    */
   public ServiceQueryBeanParam setConsumedInterface(String consumedInterface) {
      this.consumedInterface = consumedInterface;
      return this;
   }
}
