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

/**
 * Configuration interface for file-based service registration.
 * This interface provides methods to retrieve the file path where the services are stored.
 */
public interface FileRegistrationConfiguration {

   /**
    * Gets the file path where the service registry is initialized.
    *
    * @return the file path as a String
    */
   String getServicesFilePath();

}
