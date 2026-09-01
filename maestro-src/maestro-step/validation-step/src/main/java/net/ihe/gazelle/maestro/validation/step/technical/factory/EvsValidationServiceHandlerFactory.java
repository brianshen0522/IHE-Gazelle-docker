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

package net.ihe.gazelle.maestro.validation.step.technical.factory;

import net.ihe.gazelle.evsapi.client.technical.ValidationClientHttpImpl;
import net.ihe.gazelle.maestro.spi.business.Handler;
import net.ihe.gazelle.maestro.spi.technical.ConfigProvider;
import net.ihe.gazelle.maestro.spi.technical.HandlerContext;
import net.ihe.gazelle.maestro.spi.technical.HandlerFactory;
import net.ihe.gazelle.maestro.validation.step.technical.handler.EvsValidationHandler;
import net.ihe.gazelle.servicemetadata.api.business.ConsumedInterface;
import net.ihe.gazelle.servicemetadata.api.business.ConsumedInterfaceBuilder;
import net.ihe.gazelle.servicemetadata.api.business.HttpRestBinding;
import net.ihe.gazelle.servicemetadata.api.business.ProvidedInterface;

import java.util.List;

/**
 * Factory for creating EVS ValidationHandler instances.
 * Follows the same pattern as ValidationServiceHandlerFactory.
 */
public class EvsValidationServiceHandlerFactory implements HandlerFactory {

    /**
     * Default constructor.
     */
    public EvsValidationServiceHandlerFactory() { /* Default constructor */ }

    @Override
    public Class<? extends Handler> getHandlerType() {
        return EvsValidationHandler.class;
    }

    @Override
    public Handler createHandler(HandlerContext handlerContext, ProvidedInterface providedInterface, ConfigProvider configProvider) {
        if (providedInterface == null) {
            throw new IllegalArgumentException("ProvidedInterface cannot be null");
        }

        if (providedInterface.getBindings() == null || providedInterface.getBindings().isEmpty()) {
            throw new IllegalArgumentException("Provided interface has no bindings configured");
        }

        try {
            HttpRestBinding binding = (HttpRestBinding) providedInterface.getBindings().getFirst();
            String evsRestUrl = binding.getServiceUrl();

            if (evsRestUrl == null || evsRestUrl.isBlank()) {
                throw new IllegalArgumentException("Provided interface has no valid service URL");
            }

            return new EvsValidationHandler(handlerContext.serviceName(), new ValidationClientHttpImpl(evsRestUrl.trim()));

        } catch (ClassCastException e) {
            throw new IllegalArgumentException("First binding is not an HttpRestBinding", e);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to create EVS Validation Handler", e);
        }
    }

    @Override
    public List<ConsumedInterface> getConsumedInterfaces() {
        return List.of(
            new ConsumedInterfaceBuilder()
                    .setInterfaceName("EVS Client API")
                    .setRequired(false)
                    .setSupportedBindings(List.of(HttpRestBinding.TYPE))
                    .setSupportedVersions(List.of("1.0.0"))
                    .build()
        );
    }
}
