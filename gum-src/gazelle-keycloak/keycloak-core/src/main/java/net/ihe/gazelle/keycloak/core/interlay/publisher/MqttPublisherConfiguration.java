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

import net.ihe.gazelle.user.management.commons.application.organization.management.OrganizationEventPublisher;
import org.slf4j.Logger;

import java.util.UUID;

import static net.ihe.gazelle.keycloak.core.interlay.publisher.OrganizationManagementEvents.ORGANIZATION_MANAGEMENT_TOPIC;

/**
 * Resolves MQTT configuration from environment variables and creates the matching publisher.
 */
public final class MqttPublisherConfiguration {

    private static final String MQTT_ENABLED_ENV = "MQTT_ENABLED";
    private static final String MQTT_BROKER_URL_ENV = "MQTT_BROKER_URL";
    private static final String MQTT_CLIENT_ID_ENV = "MQTT_CLIENT_ID";
    private static final String MQTT_QOS_ENV = "MQTT_QOS";
    private static final String MQTT_RETAINED_ENV = "MQTT_RETAINED";
    private static final String MQTT_HOST_ENV = "MQTT_HOST";
    private static final String MQTT_PORT_ENV = "MQTT_PORT";
    private static final String MQTT_USERNAME_ENV = "MQTT_USERNAME";
    private static final String MQTT_PASSWORD_ENV = "MQTT_PASSWORD";
    private static final String MQTT_SSL_ENABLED_ENV = "MQTT_SSL_ENABLED";

    private final boolean enabled;
    private final String brokerUrl;
    private final String topic;
    private final String clientId;
    private final String username;
    private final String password;
    private final int qos;
    private final boolean retained;

    private MqttPublisherConfiguration(boolean enabled,
                                      String brokerUrl,
                                      String topic,
                                      String clientId,
                                      String username,
                                      String password,
                                      int qos,
                                      boolean retained) {
        this.enabled = enabled;
        this.brokerUrl = brokerUrl;
        this.topic = topic;
        this.clientId = clientId;
        this.username = username;
        this.password = password;
        this.qos = qos;
        this.retained = retained;
    }

    public static MqttPublisherConfiguration fromEnvironment() {
        boolean enabled = Boolean.parseBoolean(readEnvOrDefault(MQTT_ENABLED_ENV, "true"));
        return new MqttPublisherConfiguration(
                enabled,
                readBrokerUrl(),
                ORGANIZATION_MANAGEMENT_TOPIC,
                readEnvOrDefault(MQTT_CLIENT_ID_ENV, "keycloak-gum-" + UUID.randomUUID()),
                readEnvOrDefault(MQTT_USERNAME_ENV, ""),
                readEnvOrDefault(MQTT_PASSWORD_ENV, ""),
                parseQos(readEnvOrDefault(MQTT_QOS_ENV, "1")),
                Boolean.parseBoolean(readEnvOrDefault(MQTT_RETAINED_ENV, "false"))
        );
    }

    public boolean isEnabled() {
        return enabled;
    }

    public OrganizationEventPublisher createPublisher(Logger log) {
        try {
            return new MqttOrganizationEventPublisherPahoImpl(brokerUrl, clientId, topic, username, password, qos, retained);
        } catch (Exception exception) {
            log.error("Unable to initialize MQTT organization event publisher; using NoOp fallback", exception);
            return new NoOpOrganizationEventPublisher();
        }
    }

    private static String readBrokerUrl() {
        String explicitBroker = readEnvOrDefault(MQTT_BROKER_URL_ENV, "");
        if (!explicitBroker.isBlank()) {
            return explicitBroker;
        }

        String ssl = readEnvOrDefault(MQTT_SSL_ENABLED_ENV, "false");
        String host = readEnvOrDefault(MQTT_HOST_ENV, "localhost");
        String port = readEnvOrDefault(MQTT_PORT_ENV, "1883");
        if (Boolean.parseBoolean(ssl)) {
            return "ssl://" + host + ":" + port;
        }
        return "tcp://" + host + ":" + port;
    }

    private static String readEnvOrDefault(String envKey, String defaultValue) {
        String envValue = System.getenv(envKey);
        if (envValue == null || envValue.isBlank()) {
            return defaultValue;
        }
        return envValue;
    }

    private static int parseQos(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException _) {
            return 1;
        }
    }
}

