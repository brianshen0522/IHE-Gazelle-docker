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

package net.ihe.gazelle.serviceregistry.technical.dao;

import net.ihe.gazelle.lang.IORuntimeException;
import net.ihe.gazelle.modelmarshaller.technical.jackson.JacksonSerDes;
import net.ihe.gazelle.modelmarshaller.technical.serialization.DeserializationException;
import net.ihe.gazelle.modelmarshaller.technical.serialization.TextSerDes;
import net.ihe.gazelle.servicemetadata.api.business.Service;
import net.ihe.gazelle.servicemetadata.technical.jaxrs.ServiceDTO;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Read-only Repository accessing services stored in a file system.
 * This class reads a JSON file containing service definitions and provides methods to retrieve them.
 */
public class FileServiceRepository {

   private static final TextSerDes SERDES = new JacksonSerDes();

   private final FileRegistrationConfiguration fileRegistrationConfiguration;

   /**
    * Constructor for FileServiceRepository.
    *
    * @param fileRegistrationConfiguration the configuration containing the file path for service registration.
    */
   public FileServiceRepository(FileRegistrationConfiguration fileRegistrationConfiguration) {
      this.fileRegistrationConfiguration = fileRegistrationConfiguration;
   }

   /**
    * Get the list of services to be manually registered from the file system.
    *
    * @return the list of services to be manually registered. May be empty if the file does not exist or is empty.
    *
    * @throws InvalidPathException     if the configured file path is invalid.
    * @throws IORuntimeException       if the file does not exist, or if there is an error reading the file.
    * @throws DeserializationException if there is an error deserializing the services.
    */
   public List<Service> getServices() {
      return getServices(fileRegistrationConfiguration.getServicesFilePath());
   }

   private List<Service> getServices(String filePath) {
      Path path = Path.of(filePath);
      if (Files.exists(path)) {
         try {
            byte[] jsonBytes = Files.readAllBytes(path);
            return Arrays.stream(SERDES.deserialize(jsonBytes, ServiceDTO[].class))
                  .map(ServiceDTO::getBusinessObject)
                  .filter(Objects::nonNull)
                  .toList();
         } catch (IOException e) {
            throw new IORuntimeException("Error reading services from file: " + filePath, e);
         }
      } else {
         throw new IORuntimeException("File not found: " + filePath);
      }
   }
}
