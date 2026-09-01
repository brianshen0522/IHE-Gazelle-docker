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
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

/**
 * MQTT implementation of organization event publishing for Keycloak runtime.
 */
public class MqttOrganizationEventPublisherPahoImpl implements OrganizationEventPublisher {

    private static final Logger LOG = LoggerFactory.getLogger(MqttOrganizationEventPublisherPahoImpl.class);

    private final String topic;
    private final int qos;
    private final boolean retained;
    private final MqttClient client;
    private final MqttConnectOptions connectOptions;

    public MqttOrganizationEventPublisherPahoImpl(String brokerUrl,
                                                  String clientId,
                                                  String topic,
                                                  String username,
                                                  String password,
                                                  int qos,
                                                  boolean retained) throws MqttException {
        this.topic = topic;
        this.qos = qos;
        this.retained = retained;
        this.connectOptions = new MqttConnectOptions();
        connectOptions.setAutomaticReconnect(true);
        connectOptions.setCleanSession(true);

        if (username != null && !username.isBlank()) {
            connectOptions.setUserName(username);
        }
        if (password != null && !password.isBlank()) {
            connectOptions.setPassword(password.toCharArray());
        }

        this.client = new MqttClient(brokerUrl, clientId, new MemoryPersistence());
        LOG.info("Initialized MQTT publisher for broker {} and topic {} (lazy connection)", brokerUrl, topic);
    }

    @Override
    public void publishOrganizationCreateEvent(Organization organization) {
        publish(OrganizationManagementEvents.EVENT_ORGANIZATION_CREATED, organization);
    }

    @Override
    public void publishOrganizationUpdateEvent(Organization organization) {
        publish(OrganizationManagementEvents.EVENT_ORGANIZATION_UPDATED, organization);
    }

    private synchronized void publish(String eventType, Organization organization) {
        if (organization == null) {
            LOG.warn("Skip MQTT organization event {} because organization is null", eventType);
            return;
        }

        try {
            ensureConnected();
            String payload = buildEventMessage(eventType, organization);
            MqttMessage message = new MqttMessage(payload.getBytes(StandardCharsets.UTF_8));
            message.setQos(qos);
            message.setRetained(retained);
            client.publish(topic, message);
        } catch (MqttException exception) {
            LOG.error("Failed to publish MQTT organization event {}", eventType, exception);
        }
    }

    private void ensureConnected() throws MqttException {
        if (!client.isConnected()) {
            LOG.debug("Connecting to MQTT broker before publishing event");
            client.connect(connectOptions);
        }
    }

    private static String buildEventMessage(String eventType, Organization organization) {
        return "{\"type\":\"" + escapeJson(eventType) + "\","
                + "\"shortname\":\"" + escapeJson(organization.getShortname()) + "\","
                + "\"name\":\"" + escapeJson(organization.getName()) + "\"}";
    }

    private static String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}

