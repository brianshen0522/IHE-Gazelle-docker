/*
 * Copyright 2026 IHE International.
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

package net.ihe.gazelle.user.management.commons.application.organization.management;

import net.ihe.gazelle.user.management.api.domain.organization.Organization;

/**
 * Publisher of events related to organization management.
 */
public interface OrganizationEventPublisher {

    /**
     * Publish an event indicating that an organization has been created.
     *
     * @param organization the organization that was created
     */
    void publishOrganizationCreateEvent(Organization organization);

    /**
     * Publish an event indicating that an organization has been updated.
     *
     * @param organization the organization that was updated
     */
    void publishOrganizationUpdateEvent(Organization organization);

}