/*
 * Copyright 2025 Kereval.
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

package net.ihe.gazelle.maestro.validation.step.technical.factory;

import net.ihe.gazelle.maestro.spi.business.Handler;
import net.ihe.gazelle.maestro.spi.technical.ConfigProvider;
import net.ihe.gazelle.maestro.spi.technical.HandlerContext;
import net.ihe.gazelle.maestro.spi.technical.HandlerFactory;
import net.ihe.gazelle.maestro.validation.step.technical.handler.ValidationServiceHandler;
import net.ihe.gazelle.servicemetadata.api.business.ConsumedInterface;
import net.ihe.gazelle.servicemetadata.api.business.ConsumedInterfaceBuilder;
import net.ihe.gazelle.servicemetadata.api.business.HttpRestBinding;
import net.ihe.gazelle.servicemetadata.api.business.ProvidedInterface;
import net.ihe.gazelle.validation.v2.api.business.ValidationService;
import net.ihe.gazelle.validation.v2.client.SPIValidationServiceFactoryProvider;
import net.ihe.gazelle.validation.v2.client.ValidationServiceClientFactory;
import net.ihe.gazelle.validation.v2.client.ValidationServiceFactoryProvider;

import java.util.List;
import java.util.Objects;

/**
 * Factory for creating ValidationServiceHandler instances.
 * This factory creates handlers using validation service clients resolved through SPI.
 */
public class ValidationServiceHandlerFactory implements HandlerFactory {

    private static final String EVS_CLIENT_API = "EVS Client API";

    private final ValidationServiceFactoryProvider factoryProvider;

    /**
     * Default constructor.
     */
    public ValidationServiceHandlerFactory() {
        this(new SPIValidationServiceFactoryProvider());
    }

    ValidationServiceHandlerFactory(ValidationServiceFactoryProvider factoryProvider) {
        this.factoryProvider = Objects.requireNonNull(factoryProvider, "factoryProvider cannot be null");
    }

    @Override
    public Class<? extends Handler> getHandlerType() {
        return ValidationServiceHandler.class;
    }

    @Override
   public Handler createHandler(HandlerContext handlerContext, ProvidedInterface providedInterface, ConfigProvider configProvider) throws IllegalArgumentException {
     if (providedInterface == null || providedInterface.getBindings() == null || providedInterface.getBindings().isEmpty()) {
          throw new IllegalArgumentException("Service " + (providedInterface != null ? providedInterface.getInterfaceName() : "Validation Service") + " has no bindings");
     }
     ValidationServiceClientFactory factory = resolveFactory(providedInterface);
     try {
          ValidationService validationService = factory.create(providedInterface);
          return new ValidationServiceHandler(validationService);
     } catch (RuntimeException e) {
          throw new IllegalArgumentException("Can't initiate validation handler due to exception while creating client from server url", e);
     }
    }

    @Override
    public List<ConsumedInterface> getConsumedInterfaces() {
        List<ConsumedInterface> consumedInterfaces = new java.util.ArrayList<>();
        for (ValidationServiceClientFactory factory : factoryProvider.getFactories()) {
            String interfaceName = factory.getInterfaceName();
            if (interfaceName == null || EVS_CLIENT_API.equals(interfaceName)) {
                continue;
            }
            consumedInterfaces.add(new ConsumedInterfaceBuilder()
                    .setInterfaceName(interfaceName)
                    .setRequired(false)
                    .setSupportedBindings(List.of(HttpRestBinding.TYPE))
                    .setSupportedVersions(List.of())
                    .build());
        }
        return consumedInterfaces;
    }

    private ValidationServiceClientFactory resolveFactory(ProvidedInterface providedInterface) {
        String interfaceName = providedInterface.getInterfaceName();
        return factoryProvider.getFactories().stream()
                .filter(factory -> factory.getInterfaceName() != null
                        && factory.getInterfaceName().equals(interfaceName)
                        && factory.supports(providedInterface))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "No validation service factory found for interface " + interfaceName));
    }
}
