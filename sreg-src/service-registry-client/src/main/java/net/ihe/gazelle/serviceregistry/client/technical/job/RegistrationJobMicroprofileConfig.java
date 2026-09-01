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

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Default;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.Optional;

/**
 * Implementation of the RegistrationJobConfig interface using MicroProfile Config.
 * This class provides access to configuration properties related to service registration.
 */
@Default
@ApplicationScoped
public class RegistrationJobMicroprofileConfig implements RegistrationJobConfig {

   /**
    * Name of the property that indicates whether the service registry is enabled.
    */
      public static final String GZL_SERVICE_REGISTRY_ENABLED = "gzl.service.registry.enabled";
   /**
    * Name of the property that specifies the URL of the service registry.
    */
   public static final String GZL_SERVICE_REGISTRY_URL = "gzl.service.registry.url";
   /**
    * Name of the property that specifies the interval in seconds for keeping the service registry connection alive.
    */
   public static final String GZL_SERVICE_REGISTRY_KEEPALIVE_INTERVAL_SECONDS = "gzl.service.registry.keepalive.interval.seconds";


   @ConfigProperty(name = GZL_SERVICE_REGISTRY_ENABLED, defaultValue = "true")
   Boolean registrationEnabled;

   @ConfigProperty(name = GZL_SERVICE_REGISTRY_URL)
   Optional<String> serviceRegistryUrl;

   @ConfigProperty(name = GZL_SERVICE_REGISTRY_KEEPALIVE_INTERVAL_SECONDS, defaultValue = "60")
   Long keepAliveIntervalSeconds;

   /**
    * Default constructor.
    */
   public RegistrationJobMicroprofileConfig() {
      // No need for initialization, it's done by MicroProfile Config
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public boolean isServiceRegistryEnabled() {
      return registrationEnabled;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public String getServiceRegistryUrl() {
      return serviceRegistryUrl.orElse(null);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public long getServiceRegistryKeepAliveIntervalSeconds() {
      return keepAliveIntervalSeconds;
   }

}
