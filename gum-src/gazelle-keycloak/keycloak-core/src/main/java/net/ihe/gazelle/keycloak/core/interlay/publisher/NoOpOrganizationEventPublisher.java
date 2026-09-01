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

package net.ihe.gazelle.keycloak.core.interlay.publisher;

import net.ihe.gazelle.user.management.api.domain.organization.Organization;
import net.ihe.gazelle.user.management.commons.application.organization.management.OrganizationEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Safe fallback used when MQTT publishing is disabled or unavailable.
 */
public class NoOpOrganizationEventPublisher implements OrganizationEventPublisher {

    private static final Logger LOG = LoggerFactory.getLogger(NoOpOrganizationEventPublisher.class);

    @Override
    public void publishOrganizationCreateEvent(Organization organization) {
        LOG.warn("organization:created event not published (NoOp publisher)");
    }

    @Override
    public void publishOrganizationUpdateEvent(Organization organization) {
        LOG.warn("organization:updated event not published (NoOp publisher)");
    }
}

