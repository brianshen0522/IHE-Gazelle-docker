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

package net.ihe.gazelle.serviceregistry.api.technical.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import net.ihe.gazelle.servicemetadata.technical.jaxrs.ServiceDTO;
import net.ihe.gazelle.serviceregistry.api.business.DeployedService;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Data Transfer Object for DeployedService. Used to serialize and deserialize in JSON. This class extends ServiceDTO
 * and adds additional properties specific to deployed services.
 */
@Schema(name = "DeployedService", description = "Represents a service deployed in the Gazelle Test Bed and registered" +
                                                " in the Service Registry.")
@JsonPropertyOrder({"name", "version", "instanceId", "replicaId", "selfRegistered", "status", "description",
      "providedInterfaces", "consumedInterfaces"})
public class DeployedServiceDTO extends ServiceDTO<DeployedService> {

   /**
    * Default constructor initializing a new DeployedService instance. This is used for deserialization purposes.
    */
   public DeployedServiceDTO() {
      super(new DeployedService());
   }

   /**
    * Constructor that initializes the DTO with an existing DeployedService instance. This is used for serialization
    * purposes.
    *
    * @param service the DeployedService instance to initialize the DTO with
    */
   public DeployedServiceDTO(DeployedService service) {
      super(service);
   }

   /**
    * Is the service self-registered?
    *
    * @return true if the service is self-registered, false otherwise
    */
   @Schema(
         description = "Indicates if the service is self-registered in the Service Registry.",
         readOnly = true,
         examples = "true")
   @JsonProperty("selfRegistered")
   public boolean isSelfRegistered() {
      return ((DeployedService) service).isSelfRegistered();
   }

   /**
    * Sets whether the service is self-registered.
    *
    * @param value true if the service is self-registered, false otherwise
    *
    * @return this DTO instance for method chaining
    */
   public DeployedServiceDTO setSelfRegistered(boolean value) {
      ((DeployedService) service).setSelfRegistered(value);
      return this;
   }

   /**
    * Gets the status of the deployed service.
    *
    * @return the status of the deployed service
    */
   @Schema(
         description = "The current status of the deployed service.",
         readOnly = true,
         examples = {"AVAILABLE", "UNREACHABLE", "UNKNOWN"}
   )
   @JsonProperty("status")
   public DeployedService.Status getStatus() {
      return ((DeployedService) service).getStatus();
   }

   /**
    * Sets the status of the deployed service.
    *
    * @param status the new status to set
    *
    * @return this DTO instance for method chaining
    */
   public DeployedServiceDTO setStatus(DeployedService.Status status) {
      ((DeployedService) service).setStatus(status);
      return this;
   }

}
