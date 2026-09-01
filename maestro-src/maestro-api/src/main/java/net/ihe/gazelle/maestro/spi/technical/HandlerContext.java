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

/**
 * Context information provided to a handler, including the service name and session ID.
 *
 * @param serviceName The name of the service that handle step execution
 * @param sessionId   The id of the internal session
 */
public record HandlerContext(String serviceName, String sessionId) {

    /**
     * Creates a new {@code HandlerContext} with the specified service name and no session ID.
     *
     * @param serviceName the name of the service
     */
    public HandlerContext(String serviceName) {
        this(serviceName, null);
    }

}
