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

package net.ihe.gazelle.user.management.core.interlay.publisher;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import net.ihe.gazelle.modelmarshaller.technical.jackson.JacksonSerDes;
import net.ihe.gazelle.modelmarshaller.technical.serialization.TextSerDes;
import net.ihe.gazelle.user.management.api.domain.organization.Organization;
import net.ihe.gazelle.user.management.commons.application.organization.management.OrganizationEventPublisher;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletionStage;

@ApplicationScoped
public class OrganizationManagementEventPublisherImpl implements OrganizationEventPublisher {
    private static final Logger LOG = LoggerFactory.getLogger(OrganizationManagementEventPublisherImpl.class);
    private static final String ORGANIZATION_CREATED_EVENT = "organization:created";
    private static final String ORGANIZATION_UPDATED_EVENT = "organization:updated";
    private final Emitter<String> emitter;
    private final TextSerDes serDes = new JacksonSerDes(new ObjectMapper());

    /**
     * Construct a OrganizationManagementEventPublisher with the given emitter.
     *
     * @param emitter the emitter to use for publishing messages
     */
    @Inject
    public OrganizationManagementEventPublisherImpl(@Channel("organization-management-out") Emitter<String> emitter) {
        this.emitter = emitter;
    }

    @Override
    public void publishOrganizationCreateEvent(Organization organization) {
        LOG.info("Publishing organization creation event into organization-management topic.");
        String organizationEventMessage = buildEventMessageFromOrganization(ORGANIZATION_CREATED_EVENT, organization);
        CompletionStage<Void> stage = emitter.send(organizationEventMessage);
        // blocking wait and throw exception on failure
        stage.whenComplete((n, e) -> {
            if (e != null) {
                LOG.error("Failed to publish message for create organization event", e);
            }
        });
    }

    @Override
    public void publishOrganizationUpdateEvent(Organization organization) {
        LOG.info("Publishing organization update event into organization-management topic.");
        String organizationEventMessage = buildEventMessageFromOrganization(ORGANIZATION_UPDATED_EVENT, organization);
        CompletionStage<Void> stage = emitter.send(organizationEventMessage);
        // blocking wait and throw exception on failure
        stage.whenComplete((n, e) -> {
            if (e != null) {
                LOG.error("Failed to publish message for update organization event", e);
            }
        });
    }

    private String buildEventMessageFromOrganization(String eventType, Organization organization) {
        OrganizationManagementEventDto organizationManagementEventDto = new OrganizationManagementEventDto();
        organizationManagementEventDto.setType(eventType);
        organizationManagementEventDto.setShortname(organization.getShortname());
        organizationManagementEventDto.setName(organization.getName());
        try {
            return serDes.serializeAsString(organizationManagementEventDto);
        } catch (Exception e) {
            throw new OrganizationManagementPublisherException("Failed to serialize organization management event DTO", e);
        }
    }
}
