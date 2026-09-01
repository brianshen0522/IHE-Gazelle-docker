package net.ihe.gazelle.keycloak.provider.interlay.publisher;

import net.ihe.gazelle.security.business.Authz;
import net.ihe.gazelle.security.business.AuthzImpl;
import net.ihe.gazelle.security.technical.PermissionStoreSPIProvider;
import net.ihe.gazelle.user.management.api.domain.organization.Organization;
import net.ihe.gazelle.user.management.commons.application.organization.management.OrganizationManagementDAO;
import net.ihe.gazelle.user.management.commons.application.organization.management.OrganizationManagementServiceImpl;
import net.ihe.gazelle.user.management.commons.interlay.utils.MockedGazelleIdentity;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import static net.ihe.gazelle.keycloak.core.interlay.publisher.OrganizationManagementEvents.ORGANIZATION_MANAGEMENT_TOPIC;
import static net.ihe.gazelle.keycloak.provider.interlay.publisher.MqttTestSupport.getOrganizationEventPublisher;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class OrganizationUpdatePublicationIT {

    private MqttClient subscriber;

    @Test
    void testOrganizationPublishesUpdateMessage() throws Exception {
        BlockingQueue<String> messages = new ArrayBlockingQueue<>(1);

        subscriber = MqttTestSupport.connectSubscriber(ORGANIZATION_MANAGEMENT_TOPIC, messages);

        Organization publishedOrganization = new Organization()
                .setId(UUID.randomUUID().toString())
                .setShortname("orga-" + UUID.randomUUID())
                .setName("Updated organization " + UUID.randomUUID());

        OrganizationManagementDAO organizationManagementDAO = Mockito.mock(OrganizationManagementDAO.class);
        when(organizationManagementDAO.updateOrganization(anyString(), any(Organization.class))).thenReturn(publishedOrganization);
        when(organizationManagementDAO.getOrganizationFromId(anyString())).thenReturn(publishedOrganization);

        Authz authz = new AuthzImpl(new PermissionStoreSPIProvider());
        OrganizationManagementServiceImpl service = new OrganizationManagementServiceImpl(
                organizationManagementDAO, getOrganizationEventPublisher(), authz);

        MockedGazelleIdentity identity = new MockedGazelleIdentity(Set.of("org-adm:"+publishedOrganization.getId())).setOrganizationId(publishedOrganization.getId());
        Organization updatePayload = new Organization().setName("Updated organization " + UUID.randomUUID());

        Organization updated = service.updateOrganization(publishedOrganization.getId(), updatePayload, identity);
        assertEquals(publishedOrganization.getId(), updated.getId());

        String payload = messages.poll(10, TimeUnit.SECONDS);
        assertNotNull(payload, "Expected an MQTT message to be published");
        assertTrue(payload.contains("\"type\":\"organization:updated\""), payload);
        assertTrue(payload.contains("\"shortname\":\"" + publishedOrganization.getShortname() + "\""), payload);
        assertTrue(payload.contains("\"name\":\"" + publishedOrganization.getName() + "\""), payload);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (subscriber != null && subscriber.isConnected()) {
            subscriber.disconnect();
        }
        if (subscriber != null) {
            subscriber.close();
        }
    }
}


