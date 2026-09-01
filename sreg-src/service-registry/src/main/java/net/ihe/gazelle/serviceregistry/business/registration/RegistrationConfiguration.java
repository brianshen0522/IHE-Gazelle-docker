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

package net.ihe.gazelle.serviceregistry.business.registration;

import java.time.Duration;

/**
 * Configuration for service registration.
 */
public interface RegistrationConfiguration {

   /**
    * Get the duration after which a self-registered service that does not provide signs of life will be unregistered.
    *
    * @return the duration for self-registration timeout
    */
   Duration getSelfRegistrationTimeout();

   /**
    * Get the duration after which a service that does not send heartbeat will be marked as UNREACHABLE.
    * This applies to all self-registered services (WebSocket and REST) and may be used for future
    * healthcheck mechanisms on file-registered services.
    *
    * @return the duration for heartbeat timeout
    */
   Duration getHeartbeatTimeout();

}
