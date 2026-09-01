package net.ihe.gazelle.keycloak.provider.interlay.events;

import net.ihe.gazelle.keycloak.core.application.event.GazelleEventException;
import net.ihe.gazelle.keycloak.provider.interlay.notification.UserEditNotificationSender;
import net.ihe.gazelle.keycloak.provider.interlay.notification.UserEditNotificationSenderImpl;
import net.ihe.gazelle.keycloak.provider.interlay.service.UpdatePasswordEventService;
import net.ihe.gazelle.keycloak.provider.interlay.service.UpdatePasswordEventServiceImpl;
import net.ihe.gazelle.keycloak.provider.mock.UserEditNotificationSenderMock;
import org.junit.jupiter.api.Test;
import org.keycloak.email.EmailException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

class UpdatePasswordEventServiceTest {

    UpdatePasswordEventService updatePasswordEventService = new UpdatePasswordEventServiceImpl();
    UserEditNotificationSender userEditNotificationSender = new UserEditNotificationSenderMock();

    @Test
    void testEventUpdatePassword_OK() {
        assertThrows(IllegalArgumentException.class, () -> updatePasswordEventService.eventUpdatePassword(null));
        assertDoesNotThrow(() -> updatePasswordEventService.eventUpdatePassword(userEditNotificationSender));
    }

    @Test
    void testThrowGazelleEventExceptionEventUpdatePassword() throws EmailException {
        UserEditNotificationSenderImpl mockSender = mock(UserEditNotificationSenderImpl.class);
        doThrow(EmailException.class).when(mockSender).notifyPasswordUpdated(anyString());
        assertThrows(GazelleEventException.class, () -> updatePasswordEventService.eventUpdatePassword(mockSender));
    }

}