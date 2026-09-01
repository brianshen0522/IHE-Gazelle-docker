package net.ihe.gazelle.user.management.commons.application.user.registration;

import net.ihe.gazelle.security.business.Authz;
import net.ihe.gazelle.security.business.AuthzImpl;
import net.ihe.gazelle.security.business.GazelleIdentity;
import net.ihe.gazelle.security.business.UnauthorizedException;
import net.ihe.gazelle.security.technical.PermissionStoreSPIProvider;
import net.ihe.gazelle.user.management.api.application.ConflictException;
import net.ihe.gazelle.user.management.api.application.configuration.ApplicationConfig;
import net.ihe.gazelle.user.management.api.application.organization.OrganizationLookupService;
import net.ihe.gazelle.user.management.api.application.organization.OrganizationManagementService;
import net.ihe.gazelle.user.management.api.application.user.delegation.UserDelegationService;
import net.ihe.gazelle.user.management.api.application.user.edit.UserEditException;
import net.ihe.gazelle.user.management.api.application.user.edit.UserEditService;
import net.ihe.gazelle.user.management.api.application.user.lookup.UserLookupService;
import net.ihe.gazelle.user.management.api.application.user.registration.ConsentService;
import net.ihe.gazelle.user.management.api.application.user.registration.UserRegistrationException;
import net.ihe.gazelle.user.management.api.application.user.registration.UserRegistrationService;
import net.ihe.gazelle.user.management.api.domain.organization.Organization;
import net.ihe.gazelle.user.management.api.domain.user.User;
import net.ihe.gazelle.user.management.commons.ConfigurationsMock;
import net.ihe.gazelle.user.management.commons.application.exception.GazelleDAOException;
import net.ihe.gazelle.user.management.commons.application.user.edit.UserEditDAO;
import net.ihe.gazelle.user.management.commons.application.user.edit.UserEditEmailManager;
import net.ihe.gazelle.user.management.commons.application.user.edit.UserEditServiceImpl;
import net.ihe.gazelle.user.management.commons.application.user.login.HashPasswordServiceProvider;
import net.ihe.gazelle.user.management.commons.application.user.lookup.UserLookupDAO;
import net.ihe.gazelle.user.management.commons.application.user.lookup.UserLookupServiceImpl;
import net.ihe.gazelle.user.management.commons.interlay.provider.HashPasswordServiceSPIProvider;
import net.ihe.gazelle.user.management.commons.interlay.utils.MockedGazelleIdentity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;

import static net.ihe.gazelle.user.management.api.domain.user.GazelleDefaultGroup.*;
import static net.ihe.gazelle.user.management.commons.application.exception.ErrorMessage.EMAIL_NOT_VALID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
class UserRegistrationServiceTest {

   private static final String RANDOM_PASSWORD = "ThisIsARandomPassword!4";
   public static final String ORGANIZATION_ID = "organizationId";

   @Mock
    private UserRegistrationDAO userRegistrationDAO;
    @Mock
    private OrganizationManagementService organizationManagementService;
    @Mock
    private OrganizationLookupService organizationLookupService;
    @Mock
    private UserLookupDAO userLookupDAO;
    @Mock
    private UserEditDAO userEditDAO;
    @Mock
    private ConsentDAO consentDAO;
    @Mock
    private UserRegistrationEmailManager emailManager;
    @Mock
    private UserEditEmailManager editEmailManager;
    @Mock
    private UserDelegationService userDelegationService;
    private UserRegistrationService userRegistrationService;

    private final GazelleIdentity mockIdentity = new MockedGazelleIdentity(Set.of(GAZELLE_ADMIN.getName()));
    private final Authz authz = new AuthzImpl(new PermissionStoreSPIProvider());
    private final Organization organization = new Organization(ORGANIZATION_ID, "shortname","name");

    @BeforeEach
    void init() {
        ApplicationConfig applicationConfig = new ConfigurationsMock();
        HashPasswordServiceProvider hashPasswordServiceProvider = new HashPasswordServiceSPIProvider();
        UserLookupService userLookupService = new UserLookupServiceImpl(userLookupDAO, authz);
        ConsentService consentService = new ConsentServiceImpl(consentDAO);
        UserEditService userEditService = new UserEditServiceImpl(userEditDAO, hashPasswordServiceProvider, authz,
                editEmailManager, userLookupService, userDelegationService, organizationLookupService);
        userRegistrationService = new UserRegistrationServiceImpl(userEditService, organizationLookupService, organizationManagementService, consentService,
                userRegistrationDAO, applicationConfig, emailManager, authz);
    }

    @Test
    void registerUserWithNewOrganizationNullArgumentsTest() {
        assertThrows(IllegalArgumentException.class, () -> userRegistrationService.registerUser(null, true,
                RANDOM_PASSWORD, RANDOM_PASSWORD, Locale.ENGLISH));
        assertThrows(IllegalArgumentException.class, () -> userRegistrationService.registerUserWithNewOrganization(null, null, true,
                RANDOM_PASSWORD, RANDOM_PASSWORD, Locale.ENGLISH));
        assertThrows(IllegalArgumentException.class, () -> userRegistrationService.registerUser(null, true,
                RANDOM_PASSWORD, RANDOM_PASSWORD, Locale.ENGLISH));

        // Test with bad email
        User user = new User("userId");
        user.setFirstName("firstName");
        user.setLastName("lastName");
        user.setEmail("badEmail");
        assertThrows(IllegalArgumentException.class, () -> userRegistrationService.registerUser(user, true,
                RANDOM_PASSWORD, RANDOM_PASSWORD, Locale.ENGLISH));

        user.setEmail("badEmail@test.fr");
        user.setLastName("99BadLastName");
        assertThrows(IllegalArgumentException.class, () -> userRegistrationService.registerUser(user, true,
                RANDOM_PASSWORD, RANDOM_PASSWORD, Locale.ENGLISH));

        user.setLastName("lastName");
        user.setFirstName("99BadFirstName");
        assertThrows(IllegalArgumentException.class, () -> userRegistrationService.registerUser(user, true,
                RANDOM_PASSWORD, RANDOM_PASSWORD, Locale.ENGLISH));
    }

    @Test
    void createUserBadArgumentsTest() {
        assertThrows(IllegalArgumentException.class, () -> userRegistrationService.createUser(null, mockIdentity, Locale.ENGLISH));
        User user = new User();
        assertThrows(IllegalArgumentException.class, () -> userRegistrationService.createUser(user, mockIdentity, Locale.ENGLISH));
    }

    @Test
    void createUserTest() {
        User user = new User("createdUserId");
        user.setFirstName("createdFirstName");
        user.setLastName("createdLastName");
        user.setOrganizationId(ORGANIZATION_ID);
        user.setEmail("created@user.fr");


        when(userRegistrationDAO.isEmailAlreadyExist("created@user.fr")).thenReturn(false);
        when(organizationLookupService.getOrganizationById(ORGANIZATION_ID)).thenReturn(organization);
        User userCreated = userRegistrationService.createUser(user, mockIdentity, Locale.ENGLISH);
        assertNotNull(userCreated);
        assertTrue(userCreated.getGroupIds().contains("org:organizationId"));
    }

   @Test
   void createUserTestAsOrgaAdmin() {
      User user = new User("createdUserId2");
      user.setFirstName("createdFirstNameTwo");
      user.setLastName("createdLastNameTwo");
      user.setOrganizationId(ORGANIZATION_ID);
      user.setEmail("created2@user.fr");

      when(userRegistrationDAO.isEmailAlreadyExist("created2@user.fr")).thenReturn(false);
      when(organizationLookupService.getOrganizationById(ORGANIZATION_ID)).thenReturn(organization);
      GazelleIdentity orgaAdminIdentity = new MockedGazelleIdentity(Set.of(PREFIX_ORGANIZATION_ADMIN.getName() + ORGANIZATION_ID)).setOrganizationId(ORGANIZATION_ID);
      User userCreated = userRegistrationService.createUser(user, orgaAdminIdentity, Locale.ENGLISH);
      assertNotNull(userCreated);
      assertTrue(userCreated.getGroupIds().contains("org:organizationId"));
   }

   @Test
   void createUserTestAsOrgaAdminOfWrongOrgaThrowUnauthorized() {
      User user = new User("createdUserId2");
      user.setFirstName("createdFirstNameTwo");
      user.setLastName("createdLastNameTwo");
      user.setOrganizationId(ORGANIZATION_ID);
      user.setEmail("created2@user.fr");

      GazelleIdentity orgaAdminIdentity = new MockedGazelleIdentity(Set.of(PREFIX_ORGANIZATION_ADMIN.getName() + "wrongID")).setOrganizationId("wrongID");

      assertThrows(UnauthorizedException.class,
            () -> userRegistrationService.createUser(user, orgaAdminIdentity, Locale.ENGLISH));
      verify(userRegistrationDAO, never()).registerUser(any());
   }

   @Test
   void createUserShouldRejectUnauthorizedRequestedRole() {
      User user = new User("createdUserId");
      user.setFirstName("createdFirstName");
      user.setLastName("createdLastName");
      user.setOrganizationId(ORGANIZATION_ID);
      user.setEmail("created@user.fr");
      user.addGroupId(PROJECT_ADMIN.getName());

      GazelleIdentity tsmIdentity = new MockedGazelleIdentity(Set.of(TESTING_SESSION_MANAGER.getName()));

      when(userRegistrationDAO.isEmailAlreadyExist("created@user.fr")).thenReturn(false);
      when(organizationLookupService.getOrganizationById(ORGANIZATION_ID)).thenReturn(organization);

      assertThrows(UnauthorizedException.class,
            () -> userRegistrationService.createUser(user, tsmIdentity, Locale.ENGLISH));
      verify(userRegistrationDAO, never()).registerUser(any());
   }

   @Test
   void createUserShouldRejectUnauthorizedAutoGrantedRole() {
      User user = new User("createdUserId");
      user.setFirstName("createdFirstName");
      user.setLastName("createdLastName");
      user.setOrganizationId(ORGANIZATION_ID);
      user.setGroupIds(Set.of(GAZELLE_ADMIN.getName()));
      user.setEmail("created@user.fr");

      GazelleIdentity orgaAdminIdentity = new MockedGazelleIdentity(Set.of(PREFIX_ORGANIZATION_ADMIN.getName() + ORGANIZATION_ID))
            .setIdentityId("orgaAdminId")
            .setOrganizationId(ORGANIZATION_ID);

      when(userRegistrationDAO.isEmailAlreadyExist("created@user.fr")).thenReturn(false);
      when(organizationLookupService.getOrganizationById(ORGANIZATION_ID)).thenReturn(organization);

      assertThrows(UnauthorizedException.class,
            () -> userRegistrationService.createUser(user, orgaAdminIdentity, Locale.ENGLISH));
      verify(userRegistrationDAO, never()).registerUser(any());
   }


   @ParameterizedTest
    @MethodSource("provideUserAttributes")
    void registerUserWithNewOrganizationIllegalArgumentsTest(String firstName, String lastName, String email, String organizationId) {
        // Prepare data
        User user = new User("userId");
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setOrganizationId(organizationId);

        // Perform asserts
        assertThrows(IllegalArgumentException.class, () -> userRegistrationService.registerUser(user, true, RANDOM_PASSWORD, RANDOM_PASSWORD, Locale.ENGLISH));
    }

    static Stream<Arguments> provideUserAttributes() {
        return Stream.of(
                Arguments.of(null, "user1 ln", "registerUserIllegalArguments@test.fr", ORGANIZATION_ID),
                Arguments.of("user1 fn", null, "registerUserIllegalArguments@test.fr", ORGANIZATION_ID),
                Arguments.of("user1 fn", "user1 ln", null, ORGANIZATION_ID),
                Arguments.of("user1 fn", "user1 ln", "registerUserIllegalArguments@test.fr", null)
        );
    }

    @ParameterizedTest
    @MethodSource("provideGroupAttributes")
    void registerGroupIllegalArgumentsTest(String organizationId, String organizationName, String orgaUrl, Class<Throwable> expectedException) {
        // Prepare data
        User user = new User("userId");
        user.setFirstName("firstName");
        user.setLastName("lastName");
        user.setEmail("email@test.fr");
        Organization organization1 = new Organization(organizationId);
        organization1.setName(organizationName);

        // Perform asserts
        assertThrows(expectedException, () -> userRegistrationService.registerUserWithNewOrganization(user, organization1, true,
                "password", "password", Locale.ENGLISH));
    }

    static Stream<Arguments> provideGroupAttributes() {
        return Stream.of(
                Arguments.of(null, "organizationName", "badUrl", IllegalArgumentException.class),
                Arguments.of(ORGANIZATION_ID, null, "badUrl", IllegalArgumentException.class),
                Arguments.of(ORGANIZATION_ID, "organizationName", null, IllegalArgumentException.class),
                Arguments.of("superLongGroupIdIsNotPossible", "organizationName", "badUrl", IllegalArgumentException.class),
                Arguments.of(ORGANIZATION_ID, "organizationName", "badUrl", IllegalArgumentException.class)
        );
    }

    @Test
    void registerUserWithNewOrganizationBadGroupTest() {
        // Prepare data
        User user = new User("userId");
        user.setFirstName("firstName");
        user.setLastName("lastName");
        user.setEmail("email@test.fr");
        Organization organization1 = new Organization(ORGANIZATION_ID);

        Organization organization2 = new Organization("organizationId2");
        organization2.setShortname("orga2Shortname");
        organization2.setName("KEREVAL");

        // Prepare mocks
        when(userRegistrationDAO.isEmailAlreadyExist("email@test.fr")).thenReturn(true);
        assertThrows(UserRegistrationException.class, () -> userRegistrationService.registerUserWithNewOrganization(user, organization1, true,
                RANDOM_PASSWORD, RANDOM_PASSWORD, Locale.ENGLISH));

        when(userRegistrationDAO.isEmailAlreadyExist("email@test.fr")).thenReturn(false);
        when(organizationManagementService.createOrganization(eq(organization2), any())).thenThrow(new ConflictException("Organization with same shortname or name already exists."));
        assertThrows(ConflictException.class, () -> userRegistrationService.registerUserWithNewOrganization(user, organization2, true,
                RANDOM_PASSWORD, RANDOM_PASSWORD, Locale.ENGLISH));

        doThrow(UserRegistrationException.class).when(organizationManagementService).createOrganization(any(), any());
        assertThrows(UserRegistrationException.class, () -> userRegistrationService.registerUserWithNewOrganization(user, organization2, true,
                RANDOM_PASSWORD, RANDOM_PASSWORD, Locale.ENGLISH));
    }

    @ParameterizedTest
    @MethodSource("provideEmailAttributes")
    void registerUserWithNewOrganizationEmailTest(String email, boolean correctEmail) {
        // Prepare data
        User user = new User("userId");
        user.setFirstName("firstName");
        user.setLastName("lastName");
        user.setOrganizationId(ORGANIZATION_ID);
        user.setEmail(email);

        if (correctEmail) {
            when(userRegistrationDAO.isEmailAlreadyExist(email)).thenReturn(false);
            when(organizationLookupService.getOrganizationById(any())).thenReturn(organization);
            assertDoesNotThrow(() -> userRegistrationService.registerUser(user, true, RANDOM_PASSWORD, RANDOM_PASSWORD, Locale.ENGLISH));
        } else {
            Exception e = assertThrows(IllegalArgumentException.class, () -> userRegistrationService.registerUser(user, true, RANDOM_PASSWORD, RANDOM_PASSWORD, Locale.ENGLISH));
            assertEquals(EMAIL_NOT_VALID.getMessage(), e.getMessage());
        }
    }

    private static Stream<Arguments> provideEmailAttributes() {
        return Stream.of(
                Arguments.of("badEmail", false),
                Arguments.of("valid||email@test.fr", true),
                Arguments.of("email@test.fr", true),
                Arguments.of("email+alias@test.fr", true),
                Arguments.of("email.domain+alias@test.fr", true),
                Arguments.of("email@super.company.business", true),
                Arguments.of("email@super.company.business", true),
                Arguments.of("email#valid@it.uk", true),
                Arguments.of("test.kereval@i.cz", true),
                Arguments.of("test.incorrect@icz", false),
                Arguments.of("not€valid@it.uk", false),
                Arguments.of("畖十戈心大@畖十戈心大.難弓", false),
                Arguments.of("畖十戈心大@畖十戈心大.難弓", false),
                Arguments.of("خلد@الدوح.ةقطر", false)
        );
    }

    @Test
    void registerUserWithNewOrganizationTest() {
        // Prepare data
        Set<String> groupIds = Set.of("org:organizationId");
        User newUser = new User("userId", "firstName", "lastName", "registerUser@test.fr", organization.getId(), groupIds);
        newUser.setActivated(false);

        // Prepare mocks
        when(userRegistrationDAO.registerUser(any())).thenReturn(newUser);
        when(userRegistrationDAO.isEmailAlreadyExist(any())).thenReturn(false);
        when(organizationLookupService.getOrganizationById(any())).thenReturn(organization);

        // Perform asserts
        User user = userRegistrationService.registerUser(newUser, true, RANDOM_PASSWORD, RANDOM_PASSWORD, Locale.ENGLISH);
        assertNotNull(user.getId());
        assertEquals(newUser.getOrganizationId(), user.getOrganizationId());
        assertEquals(newUser.getFirstName(), user.getFirstName());
        assertEquals(newUser.getLastName(), user.getLastName());
        Set<String> expectedRoles = Set.of(SUT_OPERATOR.getName(), "org:organizationId");
        assertEquals(expectedRoles, user.getGroupIds());
    }

    @Test
    /*
    Test to verify the email regex allows for an email address domain to contain only one character
     */
    void registerUserWithNewOrganizationMailRegexTest() {
        // Prepare data
        Set<String> groupIds = Set.of("org:organizationId");
        User newUser = new User("userId", "firstName", "lastName", "registerUser@t.fr", organization.getId(), groupIds);
        newUser.setActivated(false);

        // Prepare mocks
        when(userRegistrationDAO.registerUser(any())).thenReturn(newUser);
        when(userRegistrationDAO.isEmailAlreadyExist(any())).thenReturn(false);
        when(organizationLookupService.getOrganizationById(any())).thenReturn(organization);

        // Perform asserts
        User user = userRegistrationService.registerUser(newUser, true, RANDOM_PASSWORD, RANDOM_PASSWORD, Locale.ENGLISH);
        assertNotNull(user.getId());
        assertEquals(newUser.getOrganizationId(), user.getOrganizationId());
        assertEquals(newUser.getFirstName(), user.getFirstName());
        assertEquals(newUser.getLastName(), user.getLastName());
        Set<String> expectedRoles = Set.of(SUT_OPERATOR.getName(), "org:organizationId");
        assertEquals(expectedRoles, user.getGroupIds());
    }

    @Test
    void registerFirstUserTest() {
        User newUser = new User("userId", "firstName", "lastName", "registerVendorAdmin@test.fr", organization.getId(), Set.of());

        // Mock DAO
        when(userRegistrationDAO.registerUser(any())).thenReturn(newUser);

        when(organizationManagementService.createOrganization(any(), any())).thenReturn(organization);
        when(userRegistrationDAO.getAllUsersCount()).thenReturn(0);
        when(userRegistrationDAO.isEmailAlreadyExist(any())).thenReturn(false);

        // Register user
        User user = userRegistrationService.registerUserWithNewOrganization(newUser, organization, true, RANDOM_PASSWORD, RANDOM_PASSWORD, Locale.ENGLISH);
        assertNotNull(newUser.getId(), user.getId());
        assertEquals(newUser.getOrganizationId(), user.getOrganizationId());

        String groupOrgaAdmin = PREFIX_ORGANIZATION_ADMIN.getName() + user.getOrganizationId();
        String groupOrgaMember = PREFIX_ORGANIZATION_MEMBER.getName() + user.getOrganizationId();
        Set<String> expectedRoles = Set.of(SUT_OPERATOR.getName(), groupOrgaAdmin, groupOrgaMember, GAZELLE_ADMIN.getName());
        assertEquals(expectedRoles, user.getGroupIds());
    }

    @Test
    void registerVendorAdminTest() {
        User newUser = new User("userId", "firstName", "lastName", "registerVendorAdmin@test.fr", organization.getId(), Set.of());

        // Mock DAO
        when(userRegistrationDAO.registerUser(any())).thenReturn(newUser);
        when(organizationManagementService.createOrganization(any(), any())).thenReturn(organization);
        when(userRegistrationDAO.getAllUsersCount()).thenReturn(1);
        when(userRegistrationDAO.isEmailAlreadyExist(any())).thenReturn(false);

        // Register user
        User user = userRegistrationService.registerUserWithNewOrganization(newUser, organization, true, RANDOM_PASSWORD, RANDOM_PASSWORD, Locale.ENGLISH);
        assertNotNull(newUser.getId(), user.getId());
        assertEquals(newUser.getOrganizationId(), user.getOrganizationId());
        String groupOrgaAdmin = PREFIX_ORGANIZATION_ADMIN.getName() + user.getOrganizationId();
        String groupOrgaMember = PREFIX_ORGANIZATION_MEMBER.getName() + user.getOrganizationId();
        Set<String> expectedRoles = Set.of(groupOrgaMember, SUT_OPERATOR.getName(), groupOrgaAdmin);
        assertEquals(expectedRoles, user.getGroupIds());
    }

    @Test
    void activateUserTest() {
        // Prepare data
        User user = new User();
        user.setActivationCode("goodActivationCode");
        user.setActivated(true);

        // Prepare mock
        when(userRegistrationDAO.activateUserWithActivationCode("goodActivationCode")).thenReturn(user);
        doThrow(new GazelleDAOException("not activated")).when(userRegistrationDAO).activateUserWithActivationCode("badActivationCode");

        // Perform asserts
        User activatedUser = userRegistrationService.activateUserWithActivationCode("goodActivationCode");
        assertTrue(activatedUser.isActivated());
        assertThrows(IllegalArgumentException.class, () -> userRegistrationService.activateUserWithActivationCode(null));
        assertThrows(UserEditException.class, () -> userRegistrationService.activateUserWithActivationCode("badActivationCode"));
    }

    @Test
    void createUserWithNewOrganizationTest() {
        User user = new User("createdUserId");
        user.setFirstName("createdFirstName");
        user.setLastName("createdLastName");
        user.setEmail("created-with-orga@user.fr");
        user.addGroupId("custom:group");

        Organization newOrganization = new Organization("newOrganizationId", "newshort", "newName");

        when(userRegistrationDAO.isEmailAlreadyExist("created-with-orga@user.fr")).thenReturn(false);
        when(organizationManagementService.createOrganization(eq(newOrganization), any())).thenReturn(newOrganization);
        when(userRegistrationDAO.registerUser(any())).thenAnswer(invocation -> invocation.getArgument(0));

        User createdUser = userRegistrationService.createUserWithNewOrganization(user, newOrganization, mockIdentity, Locale.ENGLISH);

        assertNotNull(createdUser);
        assertEquals("newOrganizationId", createdUser.getOrganizationId());
        assertTrue(createdUser.isActivated());
        assertTrue(createdUser.getGroupIds().contains(SUT_OPERATOR.getName()));
        assertTrue(createdUser.getGroupIds().contains(PREFIX_ORGANIZATION_ADMIN.getName() + "newOrganizationId"));
        assertTrue(createdUser.getGroupIds().contains(PREFIX_ORGANIZATION_MEMBER.getName() + "newOrganizationId"));
        assertTrue(createdUser.getGroupIds().contains("custom:group"));
    }

    @Test
    void createUserWithNewOrganizationRollbackOnRegisterFailureTest() {
        User user = new User("createdUserId");
        user.setFirstName("createdFirstName");
        user.setLastName("createdLastName");
        user.setEmail("rollback-with-orga@user.fr");

        Organization newOrganization = new Organization("newOrganizationId", "newshort", "newName");

        when(userRegistrationDAO.isEmailAlreadyExist("rollback-with-orga@user.fr")).thenReturn(false);
        when(organizationManagementService.createOrganization(eq(newOrganization), any())).thenReturn(newOrganization);
        doThrow(new GazelleDAOException("register failed")).when(userRegistrationDAO).registerUser(any());

        assertThrows(UserRegistrationException.class,
                () -> userRegistrationService.createUserWithNewOrganization(user, newOrganization, mockIdentity, Locale.ENGLISH));

        verify(userRegistrationDAO).rollbackUserRegistration(anyString());
    }

   @Test
   void createUserWithNewOrgaShouldRejectUnauthorizedRequestedRole() {
      User user = new User("createdUserId");
      user.setFirstName("createdFirstName");
      user.setLastName("createdLastName");
      user.setOrganizationId(ORGANIZATION_ID);
      user.setEmail("created@user.fr");
      user.addGroupId(PROJECT_ADMIN.getName());

      Organization newOrganization = new Organization("newOrganizationId", "newshort", "newName");

      GazelleIdentity tsmIdentity = new MockedGazelleIdentity(Set.of(TESTING_SESSION_MANAGER.getName()));

      when(userRegistrationDAO.isEmailAlreadyExist("created@user.fr")).thenReturn(false);
      when(organizationManagementService.createOrganization(eq(newOrganization), any())).thenReturn(newOrganization);

      assertThrows(UnauthorizedException.class,
            () -> userRegistrationService.createUserWithNewOrganization(user, newOrganization, tsmIdentity, Locale.ENGLISH));

      verify(userRegistrationDAO, never()).registerUser(any());
   }

   @Test
   void createUserWithNewOrgaShouldRejectUnauthorizedAutoGrantedRole() {
      User user = new User("createdUserId");
      user.setFirstName("createdFirstName");
      user.setLastName("createdLastName");
      user.setOrganizationId(ORGANIZATION_ID);
      user.setGroupIds(Set.of(GAZELLE_ADMIN.getName()));
      user.setEmail("created@user.fr");

      Organization newOrganization = new Organization("newOrganizationId", "newshort", "newName");

      GazelleIdentity orgaAdminIdentity = new MockedGazelleIdentity(Set.of(PREFIX_ORGANIZATION_ADMIN.getName() + ORGANIZATION_ID))
            .setIdentityId("orgaAdminId")
            .setOrganizationId(ORGANIZATION_ID);

      when(userRegistrationDAO.isEmailAlreadyExist("created@user.fr")).thenReturn(false);
      when(organizationManagementService.createOrganization(eq(newOrganization), any())).thenReturn(newOrganization);

      assertThrows(UnauthorizedException.class,
            () -> userRegistrationService.createUserWithNewOrganization(user, newOrganization, orgaAdminIdentity, Locale.ENGLISH));
      verify(userRegistrationDAO, never()).registerUser(any());
   }

   @Test
   void createUserWithNewOrgaTestAsOrgaAdmin() {
      User user = new User("createdUserId2");
      user.setFirstName("createdFirstNameTwo");
      user.setLastName("createdLastNameTwo");
      user.setOrganizationId(ORGANIZATION_ID);
      user.setEmail("created2@user.fr");

      Organization newOrganization = new Organization(ORGANIZATION_ID, "newshort", "newName");

      when(userRegistrationDAO.isEmailAlreadyExist("created2@user.fr")).thenReturn(false);
      when(organizationManagementService.createOrganization(eq(newOrganization), any())).thenReturn(newOrganization);
      GazelleIdentity orgaAdminIdentity = new MockedGazelleIdentity(Set.of(PREFIX_ORGANIZATION_ADMIN.getName() + ORGANIZATION_ID)).setOrganizationId(ORGANIZATION_ID);
      User userCreated = userRegistrationService.createUserWithNewOrganization(user, newOrganization, orgaAdminIdentity, Locale.ENGLISH);
      assertNotNull(userCreated);
      assertTrue(userCreated.getGroupIds().contains("org:organizationId"));
   }
}
