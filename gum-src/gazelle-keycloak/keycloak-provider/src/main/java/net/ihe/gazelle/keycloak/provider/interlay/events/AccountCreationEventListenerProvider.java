package net.ihe.gazelle.keycloak.provider.interlay.events;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.ihe.gazelle.keycloak.provider.interlay.BasicUserModel;
import net.ihe.gazelle.keycloak.provider.interlay.notification.UserCreationNotificationSender;
import net.ihe.gazelle.keycloak.provider.interlay.notification.UserCreationNotificationSenderImpl;
import net.ihe.gazelle.keycloak.provider.interlay.service.AccountCreationEventService;
import net.ihe.gazelle.keycloak.provider.interlay.service.AccountCreationEventServiceImpl;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.events.admin.OperationType;
import org.keycloak.events.admin.ResourceType;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Event listener provider for handling account creation events in Keycloak.
 *
 * This provider listens to admin events related to user creation and triggers
 * appropriate notifications and processing for the Gazelle User Management system.
 * It extends the base GazelleEventListenerProvider to provide specific handling
 * for account creation scenarios.
 *
 */
public class AccountCreationEventListenerProvider extends GazelleEventListenerProvider {
    private static final Logger log = LoggerFactory.getLogger(AccountCreationEventListenerProvider.class);

    /**
     * JSON object mapper for processing event data.
     */
    private final ObjectMapper mapper;

    /**
     * Constructs a new AccountCreationEventListenerProvider.
     *
     * @param keycloakSession the Keycloak session
     */
    public AccountCreationEventListenerProvider(KeycloakSession keycloakSession) {
        super(keycloakSession);
        this.mapper = new ObjectMapper();
        this.mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public void onEvent(AdminEvent event, boolean includeRepresentation) {
        // If this event is a user creation event
        if (ResourceType.USER.equals(event.getResourceType()) && OperationType.CREATE.equals(event.getOperationType())) {
            log.trace("AccountCreationEventListenerProvider.onEvent()");
            try {
                RealmModel realm = getKeycloakSession().getContext().getRealm();
                BasicUserModel basicUserModel = mapper.readValue(event.getRepresentation(), BasicUserModel.class);
                UserCreationNotificationSender userCreationNotificationSender = new UserCreationNotificationSenderImpl(getKeycloakSession(), realm, basicUserModel);
                AccountCreationEventService accountCreationEventService = new AccountCreationEventServiceImpl();
                // Call service to send email
                accountCreationEventService.eventAccountCreationByAdmin(userCreationNotificationSender);
            } catch (JsonProcessingException e) {
                log.warn("Unable to parse JSON to UserModel", e);
            }
        }
    }
}
