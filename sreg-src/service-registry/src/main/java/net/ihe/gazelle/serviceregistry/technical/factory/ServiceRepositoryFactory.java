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

package net.ihe.gazelle.serviceregistry.technical.factory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import net.ihe.gazelle.serviceregistry.technical.dao.FileRegistrationConfiguration;
import net.ihe.gazelle.serviceregistry.technical.dao.FileServiceRepository;
import net.ihe.gazelle.serviceregistry.technical.dao.InMemoryServiceRepository;

/**
 * Factory class for producing instances of ServiceRegistrationDAO.
 */
public class ServiceRepositoryFactory {

   private final FileRegistrationConfiguration fileRegistrationConfiguration;

   /**
    * Constructor for ServiceRepositoryFactory.
    *
    * @param fileRegistrationConfiguration the configuration for file-based service registration
    */
   @Inject
   public ServiceRepositoryFactory(FileRegistrationConfiguration fileRegistrationConfiguration) {
      this.fileRegistrationConfiguration = fileRegistrationConfiguration;
   }

   /**
    * Produces a default instance of ServiceRegistrationDAO using an in-memory repository.
    *
    * @return a new instance of InMemoryServiceRepository
    */
   @Default
   @Produces
   @ApplicationScoped
   public InMemoryServiceRepository createServiceRegistrationDAO() {
      return new InMemoryServiceRepository();
   }

   /**
    * Produces a file-based service repository (read-only) using the provided configuration.
    *
    * @return a new instance of FileServiceRepository
    */
   @Default
   @Produces
   @ApplicationScoped
   public FileServiceRepository createFileServiceRepository() {
      return new FileServiceRepository(fileRegistrationConfiguration);
   }

}
