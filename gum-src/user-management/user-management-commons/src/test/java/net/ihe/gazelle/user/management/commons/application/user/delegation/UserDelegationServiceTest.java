package net.ihe.gazelle.user.management.commons.application.user.delegation;

import net.ihe.gazelle.user.management.api.application.user.delegation.UserDelegationService;
import net.ihe.gazelle.user.management.api.domain.organization.Organization;
import net.ihe.gazelle.user.management.api.domain.user.DelegatedUser;
import net.ihe.gazelle.user.management.api.domain.user.User;
import net.ihe.gazelle.user.management.commons.application.exception.ErrorMessage;
import net.ihe.gazelle.user.management.commons.application.user.edit.UserEditDAO;
import net.ihe.gazelle.user.management.commons.application.user.lookup.UserLookupDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.NoSuchElementException;
import java.util.Set;

import static net.ihe.gazelle.user.management.api.domain.user.GazelleDefaultGroup.GAZELLE_ADMIN;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
class UserDelegationServiceTest {

    public static final String TEST_USER_ID = "userId";
    public static final String TEST_USER_ORGANIZATION = "organizationId";
    public static final String TEST_USER_FIRSTNAME = "firstName";
    public static final String TEST_USER_LASTNAME = "lastName";
    public static final String TEST_USER_EMAIL = "registerUser@test.fr";
    @Mock
    private UserDelegationDAO userDelegationDAO;
    @Mock
    private UserEditDAO userEditDAO;
    @Mock
    private UserLookupDAO userLookupDAO;
    private UserDelegationService userDelegationService;

    @BeforeEach
    void init() {
        userDelegationService = new UserDelegationServiceImpl(userDelegationDAO, userLookupDAO, userEditDAO);
    }

    @Test
    void testGetDelegatedUser() {
        // Prepare data
        Set<String> groupIds = Set.of(GAZELLE_ADMIN.getName());
        Organization organization = new Organization(TEST_USER_ORGANIZATION);
        organization.setName("KEREVAL");
        User user = new User(TEST_USER_ID, TEST_USER_FIRSTNAME, TEST_USER_LASTNAME, TEST_USER_EMAIL, organization.getId(), groupIds);
        user.setActivated(false);
        DelegatedUser delegatedUser = new DelegatedUser(user, "externalId", "idpId");

        // Prepare mocks
        when(userDelegationDAO.getDelegatedUser("externalId", "idpId")).thenReturn(delegatedUser);

        // Perform asserts
        DelegatedUser resultUser = userDelegationService.getDelegatedUser("externalId", "idpId");
        assertEquals(TEST_USER_ID, resultUser.getId());
        assertEquals(TEST_USER_ORGANIZATION, resultUser.getOrganizationId());
        assertEquals(TEST_USER_FIRSTNAME, resultUser.getFirstName());
        assertEquals(TEST_USER_LASTNAME, resultUser.getLastName());
        assertFalse(resultUser.isActivated());
    }

    @Test
    void testGetDelegatedUserParamNull() {
        Exception externalIdNull = assertThrows(IllegalArgumentException.class,
                () -> userDelegationService.getDelegatedUser(null, "idpId"));
        Exception idpIdNull = assertThrows(IllegalArgumentException.class,
                () -> userDelegationService.getDelegatedUser("externalId", null));

        assertTrue(externalIdNull.getMessage().toLowerCase().contains("externalid"));
        assertTrue(idpIdNull.getMessage().toLowerCase().contains("idpid"));
    }

    @Test
    void testGetDelegatedUserById() {
        // Prepare data
        Set<String> groupIds = Set.of(GAZELLE_ADMIN.getName());
        Organization organization = new Organization(TEST_USER_ORGANIZATION);
        organization.setName("KEREVAL");
        User user = new User(TEST_USER_ID, TEST_USER_FIRSTNAME, TEST_USER_LASTNAME, TEST_USER_EMAIL, organization.getId(), groupIds);
        user.setActivated(false);
        DelegatedUser delegatedUser = new DelegatedUser(user, "externalId", "idpId");

        // Prepare mocks
        when(userDelegationDAO.getDelegatedUserById(TEST_USER_ID)).thenReturn(delegatedUser);

        // Perform asserts
        DelegatedUser resultUser = userDelegationService.getDelegatedUserById(TEST_USER_ID);
        assertEquals(TEST_USER_ID, resultUser.getId());
        assertEquals(TEST_USER_ORGANIZATION, resultUser.getOrganizationId());
        assertEquals(TEST_USER_FIRSTNAME, resultUser.getFirstName());
        assertEquals(TEST_USER_LASTNAME, resultUser.getLastName());
        assertFalse(resultUser.isActivated());
    }

    @Test
    void testGetDelegatedUserByIdNull() {
        Exception userIdNull = assertThrows(IllegalArgumentException.class,
                () -> userDelegationService.getDelegatedUserById(null));
        assertTrue(userIdNull.getMessage().toLowerCase().contains("userid"));
    }

    @Test
    void testGetDelegatedUserNotExistInGum() {
        // Prepare data
        Set<String> groupIds = Set.of("operator");
        Organization organization = new Organization(TEST_USER_ORGANIZATION);
        organization.setName("ORGA1");
        User user = new User(TEST_USER_ID, TEST_USER_FIRSTNAME, TEST_USER_LASTNAME, TEST_USER_EMAIL, organization.getId(), groupIds);
        user.setActivated(false);

        // Prepare mocks
        when(userDelegationDAO.getDelegatedUser("externalId", "idpId")).thenThrow(new IllegalArgumentException(ErrorMessage.USER_NOT_FOUND.getMessage()));
        assertThrows(IllegalArgumentException.class, () -> userDelegationDAO.getDelegatedUser("externalId", "idpId"));
    }

    @Test
    void testCreateDelegatedUser() {
        // Prepare data
        Set<String> groupIds = Set.of("org:KEREVAL");
        Organization organization = new Organization(TEST_USER_ORGANIZATION);
        organization.setName("KEREVAL");
        User newUser = new User(TEST_USER_ID, TEST_USER_FIRSTNAME, TEST_USER_LASTNAME, TEST_USER_EMAIL, organization.getId(), groupIds);
        newUser.setActivated(false);
        DelegatedUser delegatedUser = new DelegatedUser(newUser, "externalId", "idpId");

        // Prepare mocks
        when(userDelegationDAO.createDelegatedUser(newUser, "externalId", "idpId")).thenReturn(delegatedUser);

        // Perform asserts
        DelegatedUser user = userDelegationService.createDelegatedUser(newUser, "externalId", "idpId");
        assertNotNull(user.getId());
        assertEquals(newUser.getOrganizationId(), user.getOrganizationId());
        assertEquals(newUser.getFirstName(), user.getFirstName());
        assertEquals(newUser.getLastName(), user.getLastName());
    }

    @Test
    void testTransformUserIntoDelegatedUser() {
        // Prepare data
        Set<String> groupIds = Set.of("org:KEREVAL");
        Organization organization = new Organization(TEST_USER_ORGANIZATION);
        organization.setName("KEREVAL");
        User newUser = new User(TEST_USER_ID, TEST_USER_FIRSTNAME, TEST_USER_LASTNAME, TEST_USER_EMAIL, organization.getId(), groupIds);
        newUser.setActivated(true);
        DelegatedUser delegatedUser = new DelegatedUser(newUser, "externalId", "idpId");

        when(userDelegationDAO.transformUserIntoDelegatedUser(TEST_USER_EMAIL, "externalId", "idpId")).thenReturn(delegatedUser);

        // Perform asserts
        DelegatedUser resultUser = userDelegationService.transformUserIntoDelegatedUser(TEST_USER_EMAIL, "externalId", "idpId");
        assertEquals(TEST_USER_ID, resultUser.getId());
        assertEquals(TEST_USER_ORGANIZATION, resultUser.getOrganizationId());
        assertEquals(TEST_USER_FIRSTNAME, resultUser.getFirstName());
        assertEquals(TEST_USER_LASTNAME, resultUser.getLastName());
        assertTrue(resultUser.isActivated());
    }

    @Test
    void testIsDelegatedUserFromId() {
        DelegatedUser delegatedUser = new DelegatedUser("delegated");

        when(userDelegationDAO.getDelegatedUserById("delegated")).thenReturn(delegatedUser);
        when(userDelegationDAO.getDelegatedUserById("not_delegated")).thenThrow(new NoSuchElementException());

        assertThrows(IllegalArgumentException.class, () -> userDelegationService.isUserDelegatedFromId(null));
        assertTrue(userDelegationService.isUserDelegatedFromId("delegated"));
        assertFalse(userDelegationService.isUserDelegatedFromId("not_delegated"));
    }

    @Test
    void testIsDelegatedUserFromEmail() {
        User baseUser = new User("delegated");
        baseUser.setEmail("delegated@mail.com");
        DelegatedUser delegatedUser = new DelegatedUser(baseUser, "externalId", "idpId");

        User notDelegatedUser = new User("not_delegated");
        notDelegatedUser.setEmail("notdelegated@mail.com");

        when(userLookupDAO.getUserByEmail("delegated@mail.com")).thenReturn(baseUser);
        when(userLookupDAO.getUserByEmail("notdelegated@mail.com")).thenReturn(notDelegatedUser);
        when(userDelegationDAO.getDelegatedUserById("delegated")).thenReturn(delegatedUser);
        when(userDelegationDAO.getDelegatedUserById("not_delegated")).thenThrow(new NoSuchElementException());

        assertThrows(IllegalArgumentException.class, () -> userDelegationService.isUserDelegatedFromEmail(null));
        assertTrue(userDelegationService.isUserDelegatedFromEmail("delegated@mail.com"));
        assertFalse(userDelegationService.isUserDelegatedFromEmail("notdelegated@mail.com"));
    }

    @Test
    void testActivateDelegatedUser() {
        String userId = "delegated_user";
        doNothing().when(userEditDAO).updateActivatedStatusOfUser(userId, true);
        assertDoesNotThrow(() -> userDelegationService.activateDelegatedUser(userId));
    }

    @Test
    void testIsDelegatedUserExistingBadParam() {
        assertThrows(IllegalArgumentException.class, () -> userDelegationService.isDelegatedUserExisting(null, "idpId"));
        assertThrows(IllegalArgumentException.class, () -> userDelegationService.isDelegatedUserExisting("externalId", null));
    }

    @Test
    void testIsDelegatedUserExisting() {
        when(userDelegationDAO.isDelegatedUserExisting("externalId", "idpId")).thenReturn(true);

        assertTrue(userDelegationService.isDelegatedUserExisting("externalId", "idpId"));
        assertFalse(userDelegationService.isDelegatedUserExisting("badExternalId", "badIdpId"));
    }

}
