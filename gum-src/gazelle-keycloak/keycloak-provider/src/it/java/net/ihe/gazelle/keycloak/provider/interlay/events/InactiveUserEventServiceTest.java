package net.ihe.gazelle.keycloak.provider.interlay.events;

import net.ihe.gazelle.keycloak.core.application.event.GazelleEventException;
import net.ihe.gazelle.keycloak.provider.interlay.model.BasicGroupModel;
import net.ihe.gazelle.keycloak.provider.interlay.model.BasicRoleModel;
import net.ihe.gazelle.keycloak.provider.interlay.notification.InactiveUserNotificationSender;
import net.ihe.gazelle.keycloak.provider.interlay.notification.InactiveUserNotificationSenderImpl;
import net.ihe.gazelle.keycloak.provider.interlay.service.InactiveUserEventService;
import net.ihe.gazelle.keycloak.provider.interlay.service.InactiveUserEventServiceImpl;
import net.ihe.gazelle.keycloak.provider.mock.InactiveUserNotificationSenderMock;
import net.ihe.gazelle.user.management.api.application.user.lookup.UserLookupService;
import net.ihe.gazelle.user.management.commons.ConfigurationsMock;
import net.ihe.gazelle.user.management.commons.GazelleAssertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.email.EmailException;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.UserModel;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class InactiveUserEventServiceTest {
    @Mock
    private RealmModel realm;
    @Mock
    private UserModel user;
    @Mock
    private UserLookupService userLookupService;

    private final InactiveUserEventService inactiveUserEventService = new InactiveUserEventServiceImpl(new ConfigurationsMock());

    @Test
    void testEventLoginOnInactiveAccount() {

        // Disabled user with activation code
        when(user.isEnabled()).thenReturn(false);
        when(userLookupService.getActivationCodeForUserId(any())).thenReturn("activation_code");

        // Test with null parameters
        assertThrows(IllegalArgumentException.class, () -> inactiveUserEventService.eventLoginOnInactiveAccount(null, null, null, null));
        assertThrows(IllegalArgumentException.class, () -> inactiveUserEventService.eventLoginOnInactiveAccount(null, user, userLookupService, null));
        assertThrows(IllegalArgumentException.class, () -> inactiveUserEventService.eventLoginOnInactiveAccount(realm, user, userLookupService, null));

        // Test with group
        when(user.getGroupsStream()).thenReturn(Stream.of(new BasicGroupModel("id","requiredRole")));
        RoleModel roleModel = new BasicRoleModel("id","name");
        when(realm.getRole("org-adm:requiredRole")).thenReturn(roleModel);
        when(user.hasRole(roleModel)).thenReturn(true);
        InactiveUserNotificationSender inactiveUserNotificationSenderMock = new InactiveUserNotificationSenderMock();
        Exception e = assertThrows(GazelleEventException.class,
                () -> inactiveUserEventService.eventLoginOnInactiveAccount(realm, user, userLookupService, inactiveUserNotificationSenderMock));
        GazelleAssertions.assertExceptionContains("activation_code", e);

        // Test with no group
        when(user.getGroupsStream()).thenReturn(Stream.of(new BasicGroupModel("id","requiredRole2")));
        RoleModel roleModel2 = new BasicRoleModel("id2","name2");
        when(realm.getRole("org-adm:requiredRole2")).thenReturn(roleModel2);
        when(user.hasRole(roleModel2)).thenReturn(false);
        assertDoesNotThrow(() -> inactiveUserEventService.eventLoginOnInactiveAccount(realm, user, userLookupService, inactiveUserNotificationSenderMock));
    }


    @Test
    void testThrowGazelleEventExceptionLoginOnInactiveAccount() throws EmailException {
        InactiveUserNotificationSender inactiveUserNotificationSenderMock = mock(InactiveUserNotificationSenderImpl.class);
        when(userLookupService.getActivationCodeForUserId(any())).thenReturn("activation_code");
        when(user.getGroupsStream()).thenReturn(Stream.of(new BasicGroupModel("id","name")));
        when(realm.getRole("org-adm:name")).thenReturn(new BasicRoleModel("id2","name2"));
        when(user.hasRole(any())).thenReturn(true);
        doThrow(EmailException.class).when(inactiveUserNotificationSenderMock).sendInactiveAccountVendorAdmin(anyString(), anyString());
        assertThrows(GazelleEventException.class, () -> inactiveUserEventService.eventLoginOnInactiveAccount(realm, user, userLookupService, inactiveUserNotificationSenderMock));
    }
}