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

package net.ihe.gazelle.maestro.quarkus.configuration;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import net.ihe.gazelle.maestro.api.business.userinteract.UserInteractionHandler;
import net.ihe.gazelle.servicemetadata.api.business.HttpRestBindingBuilder;
import net.ihe.gazelle.servicemetadata.api.business.ProvidedInterfaceBuilder;
import net.ihe.gazelle.servicemetadata.api.business.Service;
import net.ihe.gazelle.servicemetadata.api.business.ServiceBuilder;
import technical.provider.ServiceRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Implementation of {@link ServiceRegistry} that uses a {@link ServiceRegistryDAO}
 * to retrieve services from the database and cache them locally.
 */
@ApplicationScoped
public class ServiceRegistryImpl implements ServiceRegistry {

   private final ServiceRegistryDAO serviceRegistryDAO;

   private List<Service> services;

   /**
    * Creates a new {@code ServiceRegistryImpl} using the specified {@link ServiceRegistryDAO}.
    *
    * @param serviceRegistryDAO the DAO used to load services from the database
    */
   @Inject
   public ServiceRegistryImpl(ServiceRegistryDAO serviceRegistryDAO) {
      this.serviceRegistryDAO = serviceRegistryDAO;
   }

   @Override
   public List<Service> getServices() {
      if (services == null) {
         services = serviceRegistryDAO.getServices();
         addInternalServices();
      }
      return new ArrayList<>(services);
   }

   @Override
   public Service getService(String serviceName) {
      if (serviceName == null) {
         throw new IllegalArgumentException("service name is required");
      }
      return getServices().stream().filter(service -> serviceName.equals(service.getName()))
            .findFirst()
            .orElseThrow(() -> new NoSuchElementException("No service with name " + serviceName));
   }

   private void addInternalServices() {
      services.add(new ServiceBuilder()
            .setVersion("1.0.0")
            .setInstanceId("00000")
            .setReplicaId("001")
            .setName(UserInteractionHandler.SERVICE_NAME)
            .addProvidedInterfaceBuilder(new ProvidedInterfaceBuilder()
                  .setInterfaceVersion("1.0.0")
                  .setInterfaceName(UserInteractionHandler.INTERFACE_NAME)
                  .addBinding(
                        new HttpRestBindingBuilder()
                              .setServiceUrl("http://localhost:8080/maestro")
                  )
            )
            .build()
      );
   }
}
