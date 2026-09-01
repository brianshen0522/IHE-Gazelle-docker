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

import java.util.Comparator;
import java.util.Objects;

/**
 * Represents a unique identifier for a service instance, consisting of an instance ID and a replica ID. This record is
 * immutable and implements Comparable for sorting purposes.
 *
 * @param instanceId the unique identifier for the service instance or cluster (must not be null or blank)
 * @param replicaId  the unique replica identifier amongst an instance cluster (must not be null or blank)
 */
public record ServiceId(String instanceId, String replicaId) implements Comparable<ServiceId> {

   /**
    * Construct a ServiceId from a Service instance.
    *
    * @param service service to extract ID information from.
    */
   public ServiceId(Service service) {
      this(service.getInstanceId(), service.getReplicaId());
   }

   /**
    * Constructs a ServiceId with the specified instance ID and replica ID.
    *
    * @param instanceId the unique identifier for the service instance or cluster (must not be null or blank)
    * @param replicaId  the unique replica identifier amongst an instance cluster (must not be null or blank)
    */
   public ServiceId {
      if (instanceId == null || instanceId.isBlank()) {
         throw new IllegalArgumentException("Instance ID cannot be null or blank");
      }
      if (replicaId == null || replicaId.isBlank()) {
         throw new IllegalArgumentException("Replica ID cannot be null or blank");
      }
   }

   @Override
   public String toString() {
      return "ServiceId{" +
             "instanceId='" + instanceId + '\'' +
             ", replicaId='" + replicaId + '\'' +
             '}';
   }

   @Override
   public int compareTo(ServiceId other) {
      return Comparator.comparing(ServiceId::instanceId, Comparator.naturalOrder())
            .thenComparing(ServiceId::replicaId, Comparator.naturalOrder())
            .compare(this, other);
   }

   @Override
   public boolean equals(Object o) {
      if (!(o instanceof ServiceId(String oInstanceId, String oReplicaId))) {
         return false;
      }
      return Objects.equals(oInstanceId, instanceId) && Objects.equals(oReplicaId, replicaId);
   }

   @Override
   public int hashCode() {
      return Objects.hash(instanceId, replicaId);
   }
}
