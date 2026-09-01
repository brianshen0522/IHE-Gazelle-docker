package net.ihe.gazelle.keycloak.provider.interlay.events;

import net.ihe.gazelle.keycloak.core.application.event.GazelleEventException;
import net.ihe.gazelle.keycloak.provider.interlay.notification.UserCreationNotificationSender;
import net.ihe.gazelle.keycloak.provider.interlay.notification.UserCreationNotificationSenderImpl;
import net.ihe.gazelle.keycloak.provider.interlay.service.AccountCreationEventService;
import net.ihe.gazelle.keycloak.provider.interlay.service.AccountCreationEventServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.email.EmailException;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AccountCreationEventServiceTest {

    private final AccountCreationEventService accountCreationEventService = new AccountCreationEventServiceImpl();

    @Test
    void testEventAccountCreationByAdmin_OK() {
        UserCreationNotificationSender mockSender = mock(UserCreationNotificationSenderImpl.class);
        assertThrows(IllegalArgumentException.class, () -> accountCreationEventService.eventAccountCreationByAdmin(null));
        assertDoesNotThrow(() -> accountCreationEventService.eventAccountCreationByAdmin(mockSender));
    }

    @Test
    void testThrowGazelleEventExceptionAccountCreationByAdmin() throws EmailException {
        UserCreationNotificationSender mockSender = mock(UserCreationNotificationSenderImpl.class);
        doThrow(EmailException.class).when(mockSender).sendAccountCreatedByAdmin();
        assertThrows(GazelleEventException.class, () -> accountCreationEventService.eventAccountCreationByAdmin(mockSender));
    }

}