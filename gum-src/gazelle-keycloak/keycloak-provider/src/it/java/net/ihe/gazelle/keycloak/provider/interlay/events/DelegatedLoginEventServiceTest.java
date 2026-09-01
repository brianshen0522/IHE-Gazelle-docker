package net.ihe.gazelle.keycloak.provider.interlay.events;

import net.ihe.gazelle.keycloak.core.application.event.GazelleEventException;
import net.ihe.gazelle.keycloak.provider.interlay.notification.DelegatedLoginNotificationSender;
import net.ihe.gazelle.keycloak.provider.interlay.notification.DelegatedLoginNotificationSenderImpl;
import net.ihe.gazelle.keycloak.provider.interlay.service.DelegatedLoginEventServiceImpl;
import net.ihe.gazelle.keycloak.provider.mock.DelegatedLoginNotificationSenderMock;
import net.ihe.gazelle.user.management.api.application.user.delegation.UserDelegationService;
import net.ihe.gazelle.user.management.api.domain.user.DelegatedUser;
import net.ihe.gazelle.user.management.commons.ConfigurationsMock;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.email.EmailException;
import org.keycloak.events.Event;
import org.keycloak.events.EventType;
import org.keycloak.models.*;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DelegatedLoginEventServiceTest {

    protected final KeycloakSession session = mock(KeycloakSession.class);
    @Mock
    private RealmModel realm;
    @Mock
    private UserModel user;
    @Mock
    private UserDelegationService userDelegationService;
    @Mock
    private Event event;

    DelegatedLoginEventServiceImpl delegatedLoginEventService = new DelegatedLoginEventServiceImpl(session, new ConfigurationsMock());
    DelegatedLoginNotificationSender delegatedLoginNotificationSender = new DelegatedLoginNotificationSenderMock();

    @Test
    void testEventLoginOnDelegatedAccount_OK() {
        when(user.getUsername()).thenReturn("delegated_user");
        DelegatedUser delegatedUser = new DelegatedUser();
        when(userDelegationService.getDelegatedUserById(eq("delegated_user"))).thenReturn(delegatedUser);
        UserProvider userProvider = mock(UserProvider.class);
        when(session.users()).thenReturn(userProvider);
        FederatedIdentityModel federatedIdentityModel = new FederatedIdentityModel("idp", "delegated_user", "delegated_user");
        Stream<FederatedIdentityModel> federatedIdentityModelStream = Stream.of(federatedIdentityModel);
        when(userProvider.getFederatedIdentitiesStream(realm, user)).thenReturn(federatedIdentityModelStream);
        when(event.getType()).thenReturn(EventType.LOGIN);
        IdentityProviderModel identityProviderModel = new IdentityProviderModel();
        identityProviderModel.setAlias("idp");
        identityProviderModel.setDisplayName("idp");
        when(realm.getIdentityProviderByAlias("idp")).thenReturn(identityProviderModel);
        assertDoesNotThrow(() -> delegatedLoginEventService.eventLoginOnDelegatedAccount(realm, user, event, userDelegationService, delegatedLoginNotificationSender));
        federatedIdentityModelStream.close();

        federatedIdentityModel = new FederatedIdentityModel("idp", "delegated_user", "delegated_user");
        federatedIdentityModelStream = Stream.of(federatedIdentityModel);
        when(userProvider.getFederatedIdentitiesStream(realm, user)).thenReturn(federatedIdentityModelStream);
        when(userDelegationService.getDelegatedUser(eq("delegated_user"), any())).thenReturn(delegatedUser);
        assertDoesNotThrow(() -> delegatedLoginEventService.eventLoginOnDelegatedAccount(realm, user, event, userDelegationService, delegatedLoginNotificationSender));

        Stream<FederatedIdentityModel> streamMock = mock(Stream.class);
        Optional<FederatedIdentityModel> optionalMock = mock(Optional.class);
        when(optionalMock.orElse(null)).thenReturn(null);
        when(streamMock.findFirst()).thenReturn(optionalMock);
        when(userProvider.getFederatedIdentitiesStream(realm, user)).thenReturn(streamMock);
        assertDoesNotThrow(() -> delegatedLoginEventService.eventLoginOnDelegatedAccount(realm, user, event, userDelegationService, delegatedLoginNotificationSender));
    }

    @Test
    void testIllegalArgumentExceptionLoginOnDelegatedAccount() {
        assertThrows(IllegalArgumentException.class, () -> delegatedLoginEventService.eventLoginOnDelegatedAccount(null, user, event, userDelegationService, delegatedLoginNotificationSender));
        assertThrows(IllegalArgumentException.class, () -> delegatedLoginEventService.eventLoginOnDelegatedAccount(realm, null, event, userDelegationService, delegatedLoginNotificationSender));
        assertThrows(IllegalArgumentException.class, () -> delegatedLoginEventService.eventLoginOnDelegatedAccount(realm, user, null, userDelegationService, delegatedLoginNotificationSender));
        assertThrows(IllegalArgumentException.class, () -> delegatedLoginEventService.eventLoginOnDelegatedAccount(realm, user, event, userDelegationService, null));
    }

    @Test
    void testThrowGazelleEventExceptionLoginOnDelegatedAccount() throws EmailException {
        when(user.getUsername()).thenReturn("delegated_user");
        DelegatedUser delegatedUser = new DelegatedUser();
        when(userDelegationService.getDelegatedUserById(eq("delegated_user"))).thenReturn(delegatedUser);
        FederatedIdentityModel federatedIdentityModel = new FederatedIdentityModel("idp", "delegated_user", "delegated_user");
        UserProvider userProvider = mock(UserProvider.class);
        when(session.users()).thenReturn(userProvider);
        IdentityProviderModel identityProviderModel = new IdentityProviderModel();
        identityProviderModel.setAlias("idp");
        identityProviderModel.setDisplayName("idp");
        when(realm.getIdentityProviderByAlias("idp")).thenReturn(identityProviderModel);
        DelegatedLoginNotificationSender mockSender = mock(DelegatedLoginNotificationSenderImpl.class);
        Event mockEvent = mock(Event.class);
        Stream<FederatedIdentityModel> federatedIdentityModelStream = Stream.of(federatedIdentityModel);
        when(userProvider.getFederatedIdentitiesStream(realm, user)).thenReturn(federatedIdentityModelStream);
        when(mockEvent.getType()).thenReturn(EventType.LOGIN_ERROR);
        doThrow(EmailException.class).when(mockSender).notifyDelegatedCannotLoginLocally(anyString(), anyString());
        assertThrows(GazelleEventException.class, () -> delegatedLoginEventService.eventLoginOnDelegatedAccount(realm, user, mockEvent, userDelegationService, mockSender));
    }

}