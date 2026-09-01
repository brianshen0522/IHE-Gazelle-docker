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

package net.ihe.gazelle.simulation.technical.ws;

import jakarta.ws.rs.QueryParam;
import net.ihe.gazelle.search.jaxrs.api.QueryBeanParam;
import org.eclipse.microprofile.openapi.annotations.enums.ParameterIn;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;

import static net.ihe.gazelle.simulation.business.search.SequenceIndexService.*;

/**
 * Bean param POJO extension with specific search parameters for simulation sequences.
 */
public class SequenceQueryBeanParam extends QueryBeanParam {

   @Parameter(
         name = SERVICE_NAME,
         in = ParameterIn.QUERY,
         description = "The name of the service in which simulation sequences are searched."
   )
   @QueryParam(SERVICE_NAME)
   private String serviceName;

   @Parameter(
         name = ID,
         in = ParameterIn.QUERY,
         description = "The id of the simulation sequence to search."
   )
   @QueryParam(ID)
   private String id;

   @Parameter(
         name = TRANSACTION,
         in = ParameterIn.QUERY,
         description = "A transaction simulated by the searched simulation sequence."
   )
   @QueryParam(TRANSACTION)
   private String transaction;

   @Parameter(
         name = STANDARD,
         in = ParameterIn.QUERY,
         description = "A standard used by the searched simulation sequence."
   )
   @QueryParam(STANDARD)
   private String standard;

   @Parameter(
         name = SIMULATED_ROLE,
         in = ParameterIn.QUERY,
         description = "The role that the simulator will act as."
   )
   @QueryParam(SIMULATED_ROLE)
   private String simulatedRole;

   @Parameter(
         name = TESTED_ROLE,
         in = ParameterIn.QUERY,
         description = "The role that the system under test will act as."
   )
   @QueryParam(TESTED_ROLE)
   private String testedRole;

   @Parameter(
         name = SHORT_DESCRIPTION,
         in = ParameterIn.QUERY,
         description = "A short description of what this sequence does."
   )
   @QueryParam(SHORT_DESCRIPTION)
   private String shortDescription;

   @Parameter(
         name = RUNNABLE,
         in = ParameterIn.QUERY,
         description = "A boolean value to filter runnable or not sequences."
   )
   @QueryParam(RUNNABLE)
   private String runnable;

   @Parameter(
         name = VALID,
         in = ParameterIn.QUERY,
         description = "A boolean value to filter valid or not sequences."
   )
   @QueryParam(VALID)
   private String valid;

   /**
    * Constructor.
    */
   public SequenceQueryBeanParam() {
      // Empty
   }

   /**
    * Retrieves the name of the service.
    *
    * @return the service name as a String.
    */
   public String getServiceName() {
      return serviceName;
   }

   /**
    * Sets the name of the service.
    *
    * @param serviceName the name of the service to set
    * @return the current instance of SequenceQueryBeanParam for method chaining
    */
   public SequenceQueryBeanParam setServiceName(String serviceName) {
      this.serviceName = serviceName;
      return this;
   }

   /**
    * Retrieves the identifier of the sequence query.
    *
    * @return the identifier as a String.
    */
   public String getId() {
      return id;
   }

   /**
    * Sets the identifier of the sequence query.
    *
    * @param id the identifier to set
    * @return the current instance of SequenceQueryBeanParam for method chaining
    */
   public SequenceQueryBeanParam setId(String id) {
      this.id = id;
      return this;
   }

   /**
    * Retrieves the transaction associated with the sequence query.
    *
    * @return the transaction as a String.
    */
   public String getTransaction() {
      return transaction;
   }

   /**
    * Sets the transaction associated with the sequence query.
    *
    * @param transaction the transaction to set
    * @return the current instance of SequenceQueryBeanParam for method chaining
    */
   public SequenceQueryBeanParam setTransaction(String transaction) {
      this.transaction = transaction;
      return this;
   }

   /**
    * Retrieves the standard associated with the sequence query.
    *
    * @return the standard as a String.
    */
   public String getStandard() {
      return standard;
   }

   /**
    * Sets the standard associated with the sequence query.
    *
    * @param standard the standard to set
    * @return the current instance of SequenceQueryBeanParam for method chaining
    */
   public SequenceQueryBeanParam setStandard(String standard) {
      this.standard = standard;
      return this;
   }

   /**
    * Retrieves the simulated role associated with the sequence query.
    *
    * @return the simulated role as a String.
    */
   public String getSimulatedRole() {
      return simulatedRole;
   }

   /**
    * Sets the simulated role associated with the sequence query.
    *
    * @param simulatedRole the simulated role to set
    * @return the current instance of SequenceQueryBeanParam for method chaining
    */
   public SequenceQueryBeanParam setSimulatedRole(String simulatedRole) {
      this.simulatedRole = simulatedRole;
      return this;
   }

   /**
    * Retrieves the tested role associated with the sequence query.
    *
    * @return the tested role as a String.
    */
   public String getTestedRole() {
      return testedRole;
   }

   /**
    * Sets the tested role associated with the sequence query.
    *
    * @param testedRole the tested role to set
    * @return the current instance of SequenceQueryBeanParam for method chaining
    */
   public SequenceQueryBeanParam setTestedRole(String testedRole) {
      this.testedRole = testedRole;
      return this;
   }

   /**
    * Retrieves the short description associated with the sequence query.
    *
    * @return the short description as a String.
    */
   public String getShortDescription() {
      return shortDescription;
   }

   /**
    * Sets the short description associated with the sequence query.
    *
    * @param shortDescription the short description to set
    * @return the current instance of SequenceQueryBeanParam for method chaining
    */
   public SequenceQueryBeanParam setShortDescription(String shortDescription) {
      this.shortDescription = shortDescription;
      return this;
   }

   /**
    * Retrieves the runnable associated with the sequence query.
    *
    * @return the runnable as a String.
    */
   public String getRunnable() {
      return runnable;
   }

   /**
    * Sets the runnable associated with the sequence query.
    *
    * @param runnable the runnable to set
    * @return the current instance of SequenceQueryBeanParam for method chaining
    */
   public SequenceQueryBeanParam setRunnable(String runnable) {
      this.runnable = runnable;
      return this;
   }

   /**
    * Retrieves the valid status associated with the sequence query.
    *
    * @return the valid status as a String.
    */
   public String getValid() {
      return valid;
   }

   /**
    * Sets the valid status associated with the sequence query.
    *
    * @param valid the valid status to set
    * @return the current instance of SequenceQueryBeanParam for method chaining
    */
   public SequenceQueryBeanParam setValid(String valid) {
      this.valid = valid;
      return this;
   }
}
