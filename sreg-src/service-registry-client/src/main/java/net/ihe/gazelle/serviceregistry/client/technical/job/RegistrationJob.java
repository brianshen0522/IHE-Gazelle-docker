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

package net.ihe.gazelle.serviceregistry.client.technical.job;

import io.quarkus.runtime.ShutdownEvent;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.ObservesAsync;
import net.ihe.gazelle.m2m.client.technical.job.M2MRegisteredEvent;
import net.ihe.gazelle.servicemetadata.api.business.MetadataService;
import net.ihe.gazelle.serviceregistry.client.business.ServiceRegistrationClient;
import net.ihe.gazelle.serviceregistry.client.business.ServiceRegistrationClient.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * RegistrationJob is a CDI bean responsible for maintaining the registration of the service with service
 * registry.
 * <p>
 * It connects to the service registry at application startup, registers the service metadata, and maintains the
 * connection and the registration by sending keep-alive messages at regular intervals. It will automatically shut down
 * when the application stops.
 * <p>
 * This job logs the registration events raised by the client into the server logs.
 */
public class RegistrationJob {

   private static final Logger LOG = LoggerFactory.getLogger(RegistrationJob.class);

   private final MetadataService metadataService;
   private final RegistrationJobConfig config;
   private final ServiceRegistrationClient registrationClient;
   private ScheduledExecutorService scheduler = null;
   private ServiceRegistrationClient.RegistrationSession registrationSession = null;

   /**
    * Constructor for RegistrationJob.
    *
    * @param metadataService       the service providing metadata about the service to be registered
    * @param config                the configuration for the registration job
    * @param registrationClient    the client used to register the service with the service registry
    */
   public RegistrationJob(MetadataService metadataService, RegistrationJobConfig config, ServiceRegistrationClient registrationClient) {
      this.metadataService = metadataService;
      this.config = config;
      this.registrationClient = registrationClient;
      if (config.isServiceRegistryEnabled()) {
         scheduler = Executors.newSingleThreadScheduledExecutor();
      }
   }

   /**
    * Starts the service registration job.
    * <p>
    * This method is called automatically by the CDI container when the application starts. It connects to the service
    * registry and registers the service metadata.
    *
    * @param event the m2m registered event the job is listening to.
    */
   public void onStart(@ObservesAsync M2MRegisteredEvent event) {
      if (config.isServiceRegistryEnabled()) {
         long keepAlive = config.getServiceRegistryKeepAliveIntervalSeconds();

         scheduler.scheduleWithFixedDelay(
               new RunnableCDIWrapper(this::connectOrKeepAlive),
               0, keepAlive, TimeUnit.SECONDS
         );
      } else {
         LOG.warn("Service registration disabled");
      }
   }

   /**
    * Stops the service registration job.
    * <p>
    * This method is called automatically by the CDI container when the application stops. It shuts down the scheduler
    * and closes the registration session.
    *
    * @param shutdownEvent the shutdown event the job is listening to.
    */
   public void onStop(@Observes ShutdownEvent shutdownEvent) {
      if (config.isServiceRegistryEnabled()) {
         scheduler.shutdown();
         if(registrationSession != null) {
            registrationSession.close();
         }
      }
   }

   private void connectOrKeepAlive() {
      if (registrationSession == null) {
         registrationSession = registrationClient.connectAndRegister(
               metadataService.getMetadata(),
               this::consumeRegistrationEvent
         );
         LOG.info("Connected to Service Registry {} as {} ({}, {})",
               config.getServiceRegistryUrl(),
               metadataService.getMetadata().getName(),
               metadataService.getMetadata().getInstanceId(),
               metadataService.getMetadata().getReplicaId());
      } else {
         registrationClient.keepAlive(registrationSession);
      }
   }

   private void consumeRegistrationEvent(Event event) {
      switch (event.level()) {
         case DEBUG -> LOG.debug(event.message(), event.throwable());
         case INFO -> LOG.info(event.message(), event.throwable());
         case WARN -> LOG.warn(event.message(), event.throwable());
         case ERROR -> LOG.error(event.message(), event.throwable());
      }
   }

}
