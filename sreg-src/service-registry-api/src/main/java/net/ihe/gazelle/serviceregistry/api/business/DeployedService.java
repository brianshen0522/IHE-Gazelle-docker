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

package net.ihe.gazelle.serviceregistry.api.business;

import net.ihe.gazelle.servicemetadata.api.business.Service;

import java.time.Instant;
import java.util.Objects;

import static net.ihe.gazelle.serviceregistry.api.business.DeployedService.Status.UNKNOWN;

/**
 * Represents a service that has been deployed and registered in the service registry.
 */
public class DeployedService extends Service {

   /**
    * Enum representing the status of a deployed service.
    */
   public enum Status {
      /**
       * The service is up and running.
       */
      AVAILABLE,

      /**
       * The service is not reachable, possibly due to network issues or downtime.
       */
      UNREACHABLE,

      /**
       * The service is in an unknown state, typically used when the service has been manually registered or its status
       * is indeterminate.
       */
      UNKNOWN
   }

   private Status status = UNKNOWN;
   private Instant lastUpdate = Instant.now();
   private boolean selfRegistered = false;

   /**
    * Default constructor.
    */
   public DeployedService() {
      super();
   }

   /**
    * Constructs a DeployedService from an existing Service instance.
    *
    * @param service the Service instance to copy
    */
   public DeployedService(Service service) {
      this.setInstanceId(service.getInstanceId())
            .setReplicaId(service.getReplicaId())
            .setName(service.getName())
            .setDescription(service.getDescription())
            .setVersion(service.getVersion())
            .setConsumedInterfaces(service.getConsumedInterfaces())
            .setProvidedInterfaces(service.getProvidedInterfaces());
   }

   /**
    * Copy constructor.
    *
    * @param service the DeployedService instance to copy
    */
   public DeployedService(DeployedService service) {
      this.setStatus(service.getStatus())
            .setLastUpdate(service.getLastUpdate())
            .setSelfRegistered(service.isSelfRegistered())
            .setInstanceId(service.getInstanceId())
            .setReplicaId(service.getReplicaId())
            .setName(service.getName())
            .setDescription(service.getDescription())
            .setVersion(service.getVersion())
            .setConsumedInterfaces(service.getConsumedInterfaces())
            .setProvidedInterfaces(service.getProvidedInterfaces());
   }

   /**
    * Gets the status of the deployed service.
    *
    * @return the status of the service
    */
   public Status getStatus() {
      return status;
   }

   /**
    * Sets the status of the deployed service.
    *
    * @param status the new status to set
    *
    * @return this DeployedService instance for method chaining
    */
   public DeployedService setStatus(Status status) {
      this.status = status != null ? status : UNKNOWN;
      return this;
   }

   /**
    * Gets the last update timestamp of the deployed service.
    *
    * @return the last update timestamp
    */
   public Instant getLastUpdate() {
      return lastUpdate;
   }

   /**
    * Sets the last update timestamp of the deployed service.
    *
    * @param lastUpdate the new last update timestamp to set
    *
    * @return this DeployedService instance for method chaining
    */
   public DeployedService setLastUpdate(Instant lastUpdate) {
      this.lastUpdate = lastUpdate != null ? lastUpdate : Instant.now();
      return this;
   }

   /**
    * Resets the last update timestamp to the current time.
    *
    * @return this DeployedService instance for method chaining
    */
   public DeployedService resetLastUpdate() {
      this.lastUpdate = Instant.now();
      return this;
   }

   /**
    * Checks if the service is self-registered (as opposed to manual declarative registration).
    *
    * @return true if the service is self-registered, false otherwise
    */
   public boolean isSelfRegistered() {
      return selfRegistered;
   }

   /**
    * Sets whether the service is self-registered (as opposed to manual declarative registration).
    *
    * @param selfRegistered true if the service is self-registered, false otherwise
    *
    * @return this DeployedService instance for method chaining
    */
   public DeployedService setSelfRegistered(boolean selfRegistered) {
      this.selfRegistered = selfRegistered;
      return this;
   }

   @Override
   public boolean valueEquals(Object o) {
      if (!(o instanceof DeployedService service)) {
         return false;
      } else {
         return super.valueEquals(service) &&
                Objects.equals(this.status, service.status) &&
                Objects.equals(this.lastUpdate, service.lastUpdate) &&
                this.selfRegistered == service.selfRegistered;
      }
   }

   @Override
   public boolean equals(Object o) {
      return super.equals(o) && valueEquals(o);
   }

   @Override
   public int hashCode() {
      return Objects.hash(super.hashCode(), status, lastUpdate, selfRegistered);
   }
}
