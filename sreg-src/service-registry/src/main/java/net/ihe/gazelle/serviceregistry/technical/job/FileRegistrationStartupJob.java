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

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import net.ihe.gazelle.lang.IORuntimeException;
import net.ihe.gazelle.modelmarshaller.technical.serialization.DeserializationException;
import net.ihe.gazelle.servicemetadata.api.business.Service;
import net.ihe.gazelle.serviceregistry.business.registration.ServiceRegistration;
import net.ihe.gazelle.serviceregistry.technical.dao.FileServiceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.InvalidPathException;
import java.util.List;

/**
 * Startup job that registers services from a file at application startup.
 * This job reads the service definitions from a specified file and registers them using the ServiceRegistration service.
 */
public class FileRegistrationStartupJob {

    private final Logger log;
    private final ServiceRegistration serviceRegistration;
    private final FileServiceRepository fileServiceRepository;

    /**
     * Constructor for FileRegistrationStartupJob.
     *
     * @param serviceRegistration the service registration service used to register services
     * @param fileServiceRepository the repository that provides access to services defined in a file
     */
    @Inject
    public FileRegistrationStartupJob(ServiceRegistration serviceRegistration, FileServiceRepository fileServiceRepository) {
       this(LoggerFactory.getLogger(FileRegistrationStartupJob.class), serviceRegistration, fileServiceRepository);
    }

    /**
     * Constructor for FileRegistrationStartupJob with a custom logger. For testing purposes.
     *
     * @param log the logger to use for logging messages
     * @param serviceRegistration the service registration service used to register services
     * @param fileServiceRepository the repository that provides access to services defined in a file
     */
    FileRegistrationStartupJob(Logger log, ServiceRegistration serviceRegistration, FileServiceRepository fileServiceRepository) {
        this.log = log;
        this.serviceRegistration = serviceRegistration;
        this.fileServiceRepository = fileServiceRepository;
    }

    /**
     * Method that is called at application startup to register services from a file.
     * It reads the services from the file and registers them using the ServiceRegistration service.
     *
     * @param startupEvent the startup event that triggers this method
     */
    public void onStart(@Observes StartupEvent startupEvent) {
        try {
            List<Service> services = fileServiceRepository.getServices();
            registerFileServices(services);
        } catch (InvalidPathException e) {
            log.error("Services file path is invalid.", e);
        } catch (IORuntimeException e) {
            if(e.getMessage().contains("File not found")) {
               log.warn("Services file not found. {}", e.getMessage());
            } else {
               log.error("Error reading services file.", e);
            }
        } catch (DeserializationException e) {
            log.error("Error deserializing services from file.", e);
        } catch (Exception e) {
            log.error("Unexpected error during service registration from file.", e);
        }
    }

    private void registerFileServices(List<Service> services) {
        for (Service service : services) {
            try {
                serviceRegistration.register(service);
                log.debug("Service {} registered successfully.", service.getName());
            } catch (IllegalArgumentException e) {
                log.error("Error registring service {} : {}", service.getName(), e.getMessage());
            }
        }
    }
}
