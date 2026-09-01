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


/**
 * Provides {@link Handler} instances based on the context and handler type.
 */
public interface HandlerProvider {

    /**
     * Retrieves a handler of the specified type using the provided context.
     *
     * @param handlerContext the context for the handler
     * @param handlerType the class of the handler to retrieve
     * @return the handler instance
     */
    Handler getHandler(HandlerContext handlerContext, Class<? extends Handler> handlerType);

}
