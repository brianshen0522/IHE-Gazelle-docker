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
package net.ihe.gazelle.serviceregistry.technical.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Default;
import net.ihe.gazelle.serviceregistry.business.registration.RegistrationConfiguration;
import net.ihe.gazelle.serviceregistry.technical.dao.FileRegistrationConfiguration;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Duration;

/**
 * Implementation of {@link RegistrationConfiguration} and {@link FileRegistrationConfiguration} that rely on
 * MicroProfile Config.
 *
 * @author Cédric Eoche-Duval
 */
@Default
@ApplicationScoped
public class ServiceRegistryMicroprofileConfiguration
      implements RegistrationConfiguration, FileRegistrationConfiguration {

   /**
    * The variable name for the URL of the service registry.
    * <p>
    * <i>Using microprofile config, that variable can be set as JVM options or as environment variable in upper case
    * with underscore.</i>
    */
   public static final String GZL_SERVICE_REGISTRY_URL = "gzl.service.registry.url";

   /**
    * The variable name for the number of hours after which a self-registered service that does not provide signs of
    * life will be unregistered.
    * <p>
    * <i>Using microprofile config, that variable can be set as JVM options or as environment variable in upper case
    * with underscore.</i>
    */
   public static final String GZL_SERVICE_REGISTRY_SELF_REGISTRATION_TIMEOUT_HOURS = "gzl.service.registry.self" +
                                                                                     ".registration.timeout.hours";

   /**
    * The variable name for the number of minutes after which a service that does not send heartbeat
    * will be marked as UNREACHABLE.
    * <p>
    * <i>Using microprofile config, that variable can be set as JVM options or as environment variable in upper case
    * with underscore.</i>
    */
   public static final String GZL_SERVICE_REGISTRY_HEARTBEAT_TIMEOUT_MINUTES =
         "gzl.service.registry.heartbeat.timeout.minutes";

   /**
    * The variable name for the file path where the service registry is initialized.
    * <p>
    * <i>Using microprofile config, that variable can be set as JVM options or as environment variable in upper case
    * with underscore.</i>
    */
   public static final String GZL_SERVICE_REGISTRY_FILE_PATH = "gzl.service.registry.file.path";

   /**
    * Default value for the number of hours after which a self-registered service that does not provide signs of life
    * will be unregistered.
    */
   public static final String DEFAULT_SELF_REGISTRATION_TIMEOUT_HOURS = "72";

   /**
    * Default value for the heartbeat timeout in minutes.
    */
   public static final String DEFAULT_HEARTBEAT_TIMEOUT_MINUTES = "5";

   /**
    * Default value for the file path where the service registry is initialized.
    */
   public static final String DEFAULT_FILE_PATH = "/opt/service-registry/services.json";

   @ConfigProperty(
         name = GZL_SERVICE_REGISTRY_SELF_REGISTRATION_TIMEOUT_HOURS,
         defaultValue = DEFAULT_SELF_REGISTRATION_TIMEOUT_HOURS
   )
   Long selfRegistrationTimeoutHours;

   @ConfigProperty(
         name = GZL_SERVICE_REGISTRY_HEARTBEAT_TIMEOUT_MINUTES,
         defaultValue = DEFAULT_HEARTBEAT_TIMEOUT_MINUTES
   )
   Long heartbeatTimeoutMinutes;

   @ConfigProperty(
         name = GZL_SERVICE_REGISTRY_FILE_PATH,
         defaultValue = DEFAULT_FILE_PATH
   )
   String servicesFilePath;

   private Duration selfRegistrationTimeout = null;
   private Duration heartbeatTimeout = null;

   /**
    * Default constructor for CDI.
    */
   public ServiceRegistryMicroprofileConfiguration() {
      // Default constructor for CDI
   }

   @Override
   public Duration getSelfRegistrationTimeout() {
      if (selfRegistrationTimeout == null) {
         selfRegistrationTimeout = Duration.ofHours(selfRegistrationTimeoutHours);
      }
      return selfRegistrationTimeout;
   }

   @Override
   public Duration getHeartbeatTimeout() {
      if (heartbeatTimeout == null) {
         heartbeatTimeout = Duration.ofMinutes(heartbeatTimeoutMinutes);
      }
      return heartbeatTimeout;
   }

   @Override
   public String getServicesFilePath() {
      return servicesFilePath;
   }
}
