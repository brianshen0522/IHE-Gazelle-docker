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

import net.ihe.gazelle.security.business.Authz;
import net.ihe.gazelle.security.business.GazelleIdentity;
import net.ihe.gazelle.security.business.UnauthorizedException;
import net.ihe.gazelle.servicemetadata.api.business.Service;
import net.ihe.gazelle.servicemetadata.api.business.ServiceValidator;
import net.ihe.gazelle.serviceregistry.api.business.DeployedService;
import net.ihe.gazelle.serviceregistry.api.business.ServiceId;
import net.ihe.gazelle.serviceregistry.api.business.ServiceRegistryDescriptor;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

import static net.ihe.gazelle.serviceregistry.api.business.DeployedService.Status.AVAILABLE;
import static net.ihe.gazelle.serviceregistry.api.business.DeployedService.Status.UNKNOWN;
import static net.ihe.gazelle.serviceregistry.business.permission.ServiceRegistryPermissionStore.PERMISSION_SERVICE_READ;
import static net.ihe.gazelle.serviceregistry.business.permission.ServiceRegistryPermissionStore.PERMISSION_SERVICE_REGISTER;

/**
 * ServiceRegistration handles the registration and management of services in the service registry. It provides methods
 * to connect, disconnect, self-register, register, unregister, and purge expired self-registered services.
 */
public class ServiceRegistration {

    private static final String DOES_NOT_EXIST = " does not exist.";

    private final ServiceValidator serviceValidator = new ServiceValidator();
    private final ServiceRegistrationDAO serviceRegistrationDAO;
    private final RegistrationConfiguration configuration;
    private final Authz authz;

    /**
     * Constructs a ServiceRegistration instance with the specified DAO and configuration.
     *
     * @param serviceRegistrationDAO the DAO for service registration operations
     * @param configuration          the configuration for service registration
     * @param authz                  the authorization service for permission checks
     */
    public ServiceRegistration(ServiceRegistrationDAO serviceRegistrationDAO, RegistrationConfiguration configuration, Authz authz) {
        this.serviceRegistrationDAO = serviceRegistrationDAO;
        this.configuration = configuration;
        this.authz = authz;
    }

    /**
     * {@link #connectService(Service, GazelleIdentity)} and {@link #disconnectService(ServiceId)} are intented to provide registration
     * and availability information based on an establised network connection with service-registry. When calling this
     * method, the service will be marked self-registered and AVAILABLE. If the network connection is lost, the service
     * should be disconnected using {@link #disconnectService(ServiceId)}.
     *
     * @param service  the metadata of the self-registered service.
     * @param identity the identity of the caller attempting to register the service.
     * @throws UnauthorizedException if the identity is not authorized to register the service
     */
    public void connectService(Service service, GazelleIdentity identity) {
        authz.assertAuthorized(identity, PERMISSION_SERVICE_REGISTER);
        doRegister(
                new DeployedService(service)
                        .setSelfRegistered(true)
                        .setStatus(AVAILABLE)
        );
    }

    /**
     * Disconnects a service by marking it as UNREACHABLE. This method must be called if the registration has been done
     * via {@link #connectService(Service, GazelleIdentity)} and when the service no longer maintains an open-connection with
     * Service-Registry.
     *
     * @param serviceId the ID of the disconnected service.
     * @throws NoSuchElementException if the service with the given ID does not exist
     */
    public void disconnectService(ServiceId serviceId) {
        synchronized (serviceRegistrationDAO) {
            if (isServiceRegistered(serviceId)) {
                DeployedService service = serviceRegistrationDAO.read(serviceId);
                service.setStatus(DeployedService.Status.UNREACHABLE);
                service.resetLastUpdate();
                serviceRegistrationDAO.update(service);
            } else {
                throw new NoSuchElementException(serviceId + DOES_NOT_EXIST);
            }
        }
    }

    /**
     * Use this method when a service is registered using infrastructure descriptors (such as a config file). This
     * registration strategy does not provide availability information. So it sets its status to UNKNOWN and
     * self-registered to false.
     *
     * @param service the service to register.
     */
    public void register(Service service) {
        doRegister(
                new DeployedService(service)
                        .setSelfRegistered(false)
                        .setStatus(UNKNOWN)
        );
    }

    /**
     * Unregisters a service by its ServiceId. This method removes the service from the registry.
     *
     * @param serviceId the ID of the service to unregister
     * @throws NoSuchElementException if the service with the given ID does not exist
     */
    public void unregister(ServiceId serviceId) {
        synchronized (serviceRegistrationDAO) {
            if (isServiceRegistered(serviceId)) {
                serviceRegistrationDAO.delete(serviceId);
                return;
            }
        }
        throw new NoSuchElementException(serviceId + DOES_NOT_EXIST);
    }

    /**
     * Purges expired self-registered services. This method removes services that are self-registered, unavailable
     * (either UNKNOWN or UNREACHABLE) and have not been updated within the configured timeout period.
     */
    public void purgeExpiredSelfRegistered() {
        synchronized (serviceRegistrationDAO) {
            Instant now = Instant.now();
            List<DeployedService> selfRegisteredServices = serviceRegistrationDAO.getSelfRegisteredServices();
            selfRegisteredServices.stream()
                    .filter(unavailable())
                    .filter(notServiceRegistry())
                    .filter(expired(now, configuration.getSelfRegistrationTimeout()))
                    .map(ServiceId::new)
                    .forEach(serviceRegistrationDAO::delete);
        }
    }

    /**
     * Monitors service heartbeats and downgrades services that have stopped sending heartbeats.
     * Self-registered services (both WebSocket and REST) that are currently AVAILABLE but haven't
     * updated their heartbeat within the configured timeout will be marked as UNREACHABLE.
     * <p>
     * This method should be called periodically to ensure service availability status remains accurate.
     * Services marked as UNREACHABLE can later be purged by {@link #purgeExpiredSelfRegistered()}.
     */
    public void monitorServiceHeartbeat() {
        synchronized (serviceRegistrationDAO) {
            Instant now = Instant.now();
            List<DeployedService> selfRegisteredServices = serviceRegistrationDAO.getSelfRegisteredServices();
            selfRegisteredServices.stream()
                    .filter(available())
                    .filter(notServiceRegistry())
                    .filter(expired(now, configuration.getHeartbeatTimeout()))
                    .forEach(service -> {
                        // Create a new copy with updated state instead of mutating the retrieved service object.
                        // This prevents index corruption in InMemoryServiceRepository where indexed fields
                        // (like status) would change while the object remains in the indexes.
                        DeployedService updated = new DeployedService(service)
                                .setStatus(DeployedService.Status.UNREACHABLE)
                                .setLastUpdate(Instant.now());
                        serviceRegistrationDAO.update(updated);
                    });
        }
    }

    /**
     * Get the service by its ServiceId. This method retrieves the service metadata from the registry.
     *
     * @param serviceId the ID of the service to retrieve
     * @param identity  the identity of the caller attempting to access the service information, used for authorization checks
     * @return the DeployedService associated with the given ServiceId
     * @throws NoSuchElementException if the service with the given ID does not exist
     */
    public DeployedService getService(ServiceId serviceId, GazelleIdentity identity) {
        authz.assertAuthorized(identity, PERMISSION_SERVICE_READ);
        if (isServiceRegistered(serviceId)) {
            return serviceRegistrationDAO.read(serviceId);
        } else {
            throw new NoSuchElementException(serviceId + DOES_NOT_EXIST);
        }
    }

    void doRegister(DeployedService service) {
        serviceValidator.assertValid(service);
        synchronized (serviceRegistrationDAO) {
            if (!isServiceRegistered(new ServiceId(service))) {
                serviceRegistrationDAO.create(service);
            } else {
                serviceRegistrationDAO.update(service);
            }
        }
    }

    boolean isServiceRegistered(ServiceId serviceId) {
        return serviceRegistrationDAO.isServiceRegistered(serviceId);
    }

    private static Predicate<DeployedService> expired(Instant instant, Duration timeout) {
        return service -> instant.isAfter(service.getLastUpdate().plus(timeout));
    }

    private static Predicate<DeployedService> available() {
        return service -> AVAILABLE.equals(service.getStatus());
    }

    private static Predicate<DeployedService> unavailable() {
        return service -> !AVAILABLE.equals(service.getStatus());
    }

    private static Predicate<DeployedService> notServiceRegistry() {
        return service -> !ServiceRegistryDescriptor.SERVICE_REGISTRY_NAME.equals(service.getName());
    }

}
