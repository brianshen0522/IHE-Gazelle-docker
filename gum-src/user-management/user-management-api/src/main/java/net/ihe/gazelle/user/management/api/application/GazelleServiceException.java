/*
 * Copyright 2024 IHE International.
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

package net.ihe.gazelle.user.management.api.application;

/**
 * General service exception for the Gazelle User Management system.
 *
 * This runtime exception is used to wrap and handle errors that occur
 * during service operations throughout the Gazelle User Management
 * application. It provides a consistent way to handle service-level
 * errors and can include both error messages and underlying causes.
 *
 */
public class GazelleServiceException extends RuntimeException {

    /**
     * Constructs a new GazelleServiceException with the specified detail message.
     *
     * @param message the detail message explaining the reason for the exception
     */
    public GazelleServiceException(String message) {
        super(message);
    }

    /**
     * Constructs a new GazelleServiceException with the specified detail message and cause.
     *
     * @param message the detail message explaining the reason for the exception
     * @param cause the cause of this exception (which is saved for later retrieval)
     */
    public GazelleServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
