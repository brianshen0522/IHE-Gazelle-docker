package net.ihe.gazelle.keycloak.provider.interlay.events;

import net.ihe.gazelle.keycloak.core.application.event.GazelleEventException;
import net.ihe.gazelle.keycloak.provider.interlay.notification.BlockedUserNotificationSender;
import net.ihe.gazelle.keycloak.provider.interlay.notification.BlockedUserNotificationSenderImpl;
import net.ihe.gazelle.keycloak.provider.interlay.service.BlockedAccountEventService;
import net.ihe.gazelle.keycloak.provider.interlay.service.BlockedAccountEventServiceImpl;
import net.ihe.gazelle.keycloak.provider.mock.BlockedUserNotificationSenderMock;
import net.ihe.gazelle.user.management.commons.ConfigurationsMock;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.email.EmailException;
import org.keycloak.events.Event;
import org.keycloak.events.EventType;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.services.managers.BruteForceProtector;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BlockedAccountEventServiceTest {

    protected final KeycloakSession session = mock(KeycloakSession.class);
    @Mock
    private RealmModel realm;
    @Mock
    private UserModel user;
    @Mock
    private Event event;

    BlockedAccountEventService blockedAccountEventService = new BlockedAccountEventServiceImpl(session, new ConfigurationsMock());
    private final BlockedUserNotificationSenderImpl mockSender = mock(BlockedUserNotificationSenderImpl.class);


    @Test
    void testEventLoginOnBlockedAccount_OK() {
        assertThrows(IllegalArgumentException.class, () -> blockedAccountEventService.eventLoginOnBlockedAccount(null, null, null, null));
        when(event.getType()).thenReturn(EventType.LOGIN_ERROR);
        BruteForceProtector protector = mock(BruteForceProtector.class);
        when(session.getProvider(BruteForceProtector.class)).thenReturn(protector);
        when(protector.isTemporarilyDisabled(session, realm, user)).thenReturn(true);
        BlockedUserNotificationSender mockSender = new BlockedUserNotificationSenderMock();

        assertDoesNotThrow(() -> blockedAccountEventService.eventLoginOnBlockedAccount(realm, user, event, mockSender));
    }

    @Test
    void testThrowGazelleEventExceptionLoginOnBlockedAccount() throws EmailException {
        when(event.getType()).thenReturn(EventType.LOGIN_ERROR);
        BruteForceProtector protector = mock(BruteForceProtector.class);
        when(session.getProvider(BruteForceProtector.class)).thenReturn(protector);
        when(protector.isTemporarilyDisabled(session, realm, user)).thenReturn(true);
        doThrow(EmailException.class).when(mockSender).sendBlockedAccount(any(), any(), any());
        assertThrows(GazelleEventException.class, () -> blockedAccountEventService.eventLoginOnBlockedAccount(realm, user, event, mockSender));
    }

    @Test
    void testLoginSuccessLoginOnBlockedAccount() {
        Event mockEvent = mock(Event.class);
        when(mockEvent.getType()).thenReturn(EventType.LOGIN);
        assertDoesNotThrow(() -> blockedAccountEventService.eventLoginOnBlockedAccount(realm, user, event, mockSender));
        when(mockEvent.getType()).thenReturn(EventType.IDENTITY_PROVIDER_LOGIN);
        assertDoesNotThrow(() -> blockedAccountEventService.eventLoginOnBlockedAccount(realm, user, event, mockSender));
    }
}