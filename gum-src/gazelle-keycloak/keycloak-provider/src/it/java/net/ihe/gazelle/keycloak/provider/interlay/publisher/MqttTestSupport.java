package net.ihe.gazelle.keycloak.provider.interlay.publisher;

import net.ihe.gazelle.keycloak.core.interlay.publisher.MqttOrganizationEventPublisherPahoImpl;
import net.ihe.gazelle.user.management.commons.application.organization.management.OrganizationEventPublisher;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;

import static net.ihe.gazelle.keycloak.core.interlay.publisher.OrganizationManagementEvents.ORGANIZATION_MANAGEMENT_TOPIC;

final class MqttTestSupport {

    private MqttTestSupport() {
    }

    static MqttClient connectSubscriber(String topic, BlockingQueue<String> messages) throws MqttException {
        MqttClient subscriber = new MqttClient(brokerUrl(), "subscriber-" + UUID.randomUUID(), new MemoryPersistence());
        MqttConnectOptions connectOptions = new MqttConnectOptions();
        connectOptions.setCleanSession(true);
        subscriber.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                // no-op for tests
            }

            @Override
            public void messageArrived(String subscribedTopic, MqttMessage message) {
                try {
                    messages.put(new String(message.getPayload(), StandardCharsets.UTF_8));
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(exception);
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                // no-op for tests
            }
        });
        subscriber.connect(connectOptions);
        subscriber.subscribe(topic, 1);
        return subscriber;
    }

    static String brokerUrl() {
        String host = System.getenv().getOrDefault("MQTT_HOST", "localhost");
        String port = System.getenv().getOrDefault("MQTT_PORT", "1884");
        return "tcp://" + host + ":" + port;
    }

    static String brokerUsername() {
        return System.getenv().getOrDefault("MQTT_USERNAME", "");
    }

    static String brokerPassword() {
        return System.getenv().getOrDefault("MQTT_PASSWORD", "");
    }

    static OrganizationEventPublisher getOrganizationEventPublisher() throws MqttException {
        return new MqttOrganizationEventPublisherPahoImpl(
                MqttTestSupport.brokerUrl(),
                "publisher-" + UUID.randomUUID(),
                ORGANIZATION_MANAGEMENT_TOPIC,
                MqttTestSupport.brokerUsername(),
                MqttTestSupport.brokerPassword(),
                1,
                false
        );
    }
}

