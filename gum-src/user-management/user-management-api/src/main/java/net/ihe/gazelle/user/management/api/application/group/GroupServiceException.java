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

package net.ihe.gazelle.user.management.api.application.group;

/**
 * Exception thrown when an error occurs in the GroupService.
 */
public class GroupServiceException extends RuntimeException {

    /**
     * Instantiates a new UserEditService exception.
     *
     * @param message message
     */
    public GroupServiceException(String message) {
        super(message);
    }

    /**
     * Instantiates a new UserEditService exception.
     *
     * @param message message
     * @param cause   cause
     */
    public GroupServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}