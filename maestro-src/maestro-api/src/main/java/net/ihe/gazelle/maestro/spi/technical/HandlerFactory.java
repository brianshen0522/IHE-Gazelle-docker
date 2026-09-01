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

package net.ihe.gazelle.maestro.spi.technical;

import net.ihe.gazelle.maestro.spi.business.Handler;
import net.ihe.gazelle.servicemetadata.api.business.ConsumedInterface;
import net.ihe.gazelle.servicemetadata.api.business.ProvidedInterface;

import java.util.List;

/**
 * Factory interface responsible for creating {@link Handler} instances.
 */
public interface HandlerFactory {

    /**
     * Returns the {@link Handler} implementation class associated with this factory.
     *
     * @return the handler implementation class
     */
    Class<? extends Handler> getHandlerType();

    /**
     * Creates a new {@link Handler} instance using the provided context, interface, and configuration.
     *
     * @param handlerContext the context for the handler
     * @param providedInterface the interface provided to the handler
     * @param configProvider the configuration provider
     * @return a new {@code Handler} instance
     * @throws IllegalArgumentException if any argument is invalid
     */
    Handler createHandler(HandlerContext handlerContext, ProvidedInterface providedInterface, ConfigProvider configProvider) throws IllegalArgumentException;

    /**
     * Returns the list of {@link ConsumedInterface} instances consumed by this handler factory.
     *
     * @return a list of consumed interfaces
     */
    List<ConsumedInterface> getConsumedInterfaces();

}
