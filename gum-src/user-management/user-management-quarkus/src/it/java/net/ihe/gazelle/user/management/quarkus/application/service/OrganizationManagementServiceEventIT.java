package net.ihe.gazelle.user.management.quarkus.application.service;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.reactive.messaging.memory.InMemoryConnector;
import io.smallrye.reactive.messaging.memory.InMemorySink;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import net.ihe.gazelle.security.business.GazelleIdentity;
import net.ihe.gazelle.security.mocks.KeycloakMockResource;
import net.ihe.gazelle.user.management.api.application.organization.OrganizationManagementService;
import net.ihe.gazelle.user.management.api.domain.organization.Organization;
import net.ihe.gazelle.user.management.commons.interlay.utils.MockedGazelleIdentity;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;
import java.util.Set;

import static net.ihe.gazelle.user.management.api.domain.user.GazelleDefaultGroup.GAZELLE_ADMIN;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@QuarkusTestResource(KeycloakMockResource.class)
class OrganizationManagementServiceEventIT {

    @Inject
    OrganizationManagementService organizationManagementService;

    @Inject
    @Any
    Instance<InMemoryConnector> inMemoryConnector;

    GazelleIdentity identity = new MockedGazelleIdentity(Set.of(GAZELLE_ADMIN.getName()));

    private InMemorySink<String> organizationEvents;

    @BeforeEach
    void setUp() {
        organizationEvents = inMemoryConnector.get().sink("organization-management-out");
        organizationEvents.clear();
    }

    @Test
    void createOrganizationPublishesCreateEvent() {
        String suffix = getRandomString();

        Organization organization = new Organization()
                .setName("Organization Name " + suffix)
                .setShortname("orga-" + suffix);

        Organization created = organizationManagementService.createOrganization(organization, identity);

        assertNotNull(created.getId());
        List<? extends Message<String>> messages = organizationEvents.received();
        assertEquals(1, messages.size());
        String payload = messages.getFirst().getPayload();
        assertTrue(payload.contains("\"type\":\"organization:created\""));
        assertTrue(payload.contains("\"shortname\":\"" + created.getShortname() + "\""));
        assertTrue(payload.contains("\"name\":\"" + created.getName() + "\""));
    }

    @Test
    void updateOrganizationPublishesUpdateEvent() {
        String suffix = getRandomString();
        Organization initial = new Organization()
                .setName("Initial Organization " + suffix)
                .setShortname("orga-update-" + suffix);
        Organization created = organizationManagementService.createOrganization(initial, identity);
        organizationEvents.clear();

        Organization updateAttributes = new Organization()
                .setName("Updated Organization " + suffix);

        Organization updated = organizationManagementService.updateOrganization(
                created.getId(),
                updateAttributes,
                identity
        );

        assertEquals(updateAttributes.getName(), updated.getName());
        List<? extends Message<String>> messages = organizationEvents.received();
        assertEquals(1, messages.size());
        String payload = messages.getFirst().getPayload();
        assertTrue(payload.contains("\"type\":\"organization:updated\""));
        assertTrue(payload.contains("\"shortname\":\"" + created.getShortname() + "\""));
        assertTrue(payload.contains("\"name\":\"" + updateAttributes.getName() + "\""));
    }

    private String getRandomString() {
        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 20;
        Random random = new Random();

        String string = random.ints(leftLimit, rightLimit + 1)
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();

        System.out.println("Generated random string: " + string);
        return string;
    }
}

