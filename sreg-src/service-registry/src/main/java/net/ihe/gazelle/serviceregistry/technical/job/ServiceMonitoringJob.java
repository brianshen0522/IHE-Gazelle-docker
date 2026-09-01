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

package net.ihe.gazelle.serviceregistry.technical.job;

import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import net.ihe.gazelle.serviceregistry.business.registration.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Scheduled job responsible for monitoring service health and removing expired services.
 *
 * <p>This job ensures that the service registry maintains accurate availability information
 * by performing two critical tasks:</p>
 * <ul>
 *   <li>Monitoring heartbeats (every minute): Identifies self-registered services that have
 *       stopped sending heartbeats and marks them as UNREACHABLE</li>
 *   <li>Purging expired services (every 10 minutes): Removes services that have been
 *       UNREACHABLE beyond the configured timeout period</li>
 * </ul>
 *
 * <p>The job applies to all self-registered services regardless of registration method
 * (WebSocket or REST).</p>
 */
@ApplicationScoped
public class ServiceMonitoringJob {

   private static final Logger LOG = LoggerFactory.getLogger(ServiceMonitoringJob.class);

   private final ServiceRegistration serviceRegistration;

   /**
    * Constructor for ServiceMonitoringJob.
    *
    * @param serviceRegistration the ServiceRegistration instance responsible for managing
    *                            service registration, monitoring, and expiration logic.
    */
   @Inject
   public ServiceMonitoringJob(ServiceRegistration serviceRegistration) {
      this.serviceRegistration = serviceRegistration;
   }

   /**
    * Monitors service heartbeats to detect services that are no longer available.
    *
    * <p>Checks for self-registered services (WebSocket and REST) with expired heartbeats
    * and downgrades their status from AVAILABLE to UNREACHABLE.</p>
    *
    * <p>Runs every minute as scheduled by Quarkus.</p>
    *
    * <p>Any exceptions during execution are caught and logged to prevent the scheduler from stopping.</p>
    */
   @Scheduled(every = "1m", identity = "service-heartbeat-monitor")
   void monitorHeartbeats() {
      try {
         LOG.debug("Monitoring service heartbeats");
         serviceRegistration.monitorServiceHeartbeat();
      } catch (Exception e) {
         LOG.error("Error while monitoring service heartbeats", e);
      }
   }

   /**
    * Purges services that have been UNREACHABLE for too long.
    *
    * <p>Removes self-registered services that have exceeded the configured timeout period
    * in the UNREACHABLE state.</p>
    *
    * <p>Runs every 10 minutes as scheduled by Quarkus.</p>
    *
    * <p>Any exceptions during execution are caught and logged to prevent the scheduler from stopping.</p>
    */
   @Scheduled(every = "10m", identity = "service-purge-job")
   void purgeExpiredServices() {
      try {
         LOG.debug("Purging expired self-registered services");
         serviceRegistration.purgeExpiredSelfRegistered();
      } catch (Exception e) {
         LOG.error("Error while purging expired services", e);
      }
   }
}
