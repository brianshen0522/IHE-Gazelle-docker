package net.ihe.gazelle.keycloak.provider.interlay.events;

import net.ihe.gazelle.keycloak.provider.interlay.service.LastLoginEventServiceImpl;
import net.ihe.gazelle.user.management.api.application.user.login.UserLoginService;
import net.ihe.gazelle.user.management.commons.application.exception.GazelleDAOException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.models.UserModel;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LastLoginEventServiceTest {

    @Mock
    private UserModel user;
    @Mock
    private UserLoginService userLoginService;

    LastLoginEventServiceImpl lastLoginEventService = new LastLoginEventServiceImpl();

    @Test
    void testEventLoginSuccessful() {
        // Test with null parameters
        assertThrows(IllegalArgumentException.class, () -> lastLoginEventService.eventLoginSuccessful(null, null));
        when(user.getUsername()).thenReturn("userId");
        assertThrows(IllegalArgumentException.class, () -> lastLoginEventService.eventLoginSuccessful(user, null));

        // Test with a DAO exception
        assertDoesNotThrow(() -> lastLoginEventService.eventLoginSuccessful(user, userLoginService));
        doThrow(new GazelleDAOException("Unable to update user")).when(userLoginService).updateLastLoginTimestampForUserId(eq("userId"), any());
        assertThrows(GazelleDAOException.class, () -> lastLoginEventService.eventLoginSuccessful(user, userLoginService));
    }

}