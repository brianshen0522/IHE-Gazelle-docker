package net.ihe.gazelle.user.management.commons.application.user.login;

import net.ihe.gazelle.user.management.api.domain.user.Credentials;
import net.ihe.gazelle.user.management.commons.application.exception.GazelleDAOException;
import net.ihe.gazelle.user.management.commons.interlay.cipher.MD5HashService;
import net.ihe.gazelle.user.management.commons.interlay.cipher.PBKDF2HashService;
import net.ihe.gazelle.user.management.commons.interlay.provider.HashPasswordServiceSPIProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.sql.Timestamp;
import java.util.NoSuchElementException;

import static net.ihe.gazelle.user.management.commons.GazelleAssertions.assertExceptionContains;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
class UserLoginServiceTest {
    @Mock
    private UserLoginDAO userLoginDAOMock;
    private net.ihe.gazelle.user.management.api.application.user.login.UserLoginService userLoginService;

    @BeforeEach
    void beforeAll() {
        HashPasswordServiceProvider hashPasswordServiceProvider = new HashPasswordServiceSPIProvider();
        userLoginService = new UserLoginServiceImpl(userLoginDAOMock, hashPasswordServiceProvider);
    }

    @Test
    void testValidateCredentials() {
        // Prepare data
        Credentials credentials1 = new MD5HashService().hash("testMD5");
        Credentials credentials2 = new PBKDF2HashService().hash("testPBKDF2");

        // Set up mocks
        when(userLoginDAOMock.getCredentialsForUserId("loginTestUser1")).thenReturn(credentials1);
        when(userLoginDAOMock.getCredentialsForUserId("loginTestUser2")).thenReturn(credentials2);

        // Test
        assertFalse(userLoginService.validatePassword("loginTestUser1", "badPassword"));
        assertTrue(userLoginService.validatePassword("loginTestUser1", "testMD5"));
        assertFalse(userLoginService.validatePassword("loginTestUser2", "badPassword"));
        assertTrue(userLoginService.validatePassword("loginTestUser2","testPBKDF2"));
    }

    @Test
    void testValidateBadCredentials() {
        // No userID
        Exception exceptionUserId = Assertions.assertThrows(IllegalArgumentException.class,
                () -> userLoginService.validatePassword(null, "password"));
        assertExceptionContains("null", exceptionUserId);

        // No credentials
        Exception exceptionCredentials = Assertions.assertThrows(IllegalArgumentException.class,
                () -> userLoginService.validatePassword("loginTestUser1", null));
        assertExceptionContains("null", exceptionCredentials);

        // No password
        Exception exceptionPassword = Assertions.assertThrows(IllegalArgumentException.class,
                () -> userLoginService.validatePassword("loginTestUser1", ""));
        assertExceptionContains("password", exceptionPassword);

        // No hashMethodName
        Credentials credentials = new Credentials("password");
        when(userLoginDAOMock.getCredentialsForUserId("loginTestUser1")).thenReturn(credentials);
        Exception exceptionHashMethod = Assertions.assertThrows(IllegalArgumentException.class,
                () -> userLoginService.validatePassword("loginTestUser1","password"));
        assertExceptionContains("hashMethod", exceptionHashMethod);
    }

    @Test
    void testValidateNoSuchCredentials() {
        Mockito.doThrow(new NoSuchElementException("not found")).when(userLoginDAOMock).getCredentialsForUserId("userWithoutCredentials");
        assertFalse(userLoginService.validatePassword("userWithoutCredentials", "password"));
    }

    @Test
    void updateLastLoginTimestampForUserIdTest() {
        // Prepare data
        Timestamp timestamp = new Timestamp(10000);

        // Prepare mock
        Mockito.doNothing().when(userLoginDAOMock).updateLoginMetricsForUserId("id", timestamp);
        Mockito.doThrow(new GazelleDAOException("not found")).when(userLoginDAOMock).updateLoginMetricsForUserId("badId", timestamp);

        // Perform asserts
        userLoginDAOMock.updateLoginMetricsForUserId("id",timestamp);
        assertThrows(IllegalArgumentException.class, () -> userLoginService.updateLastLoginTimestampForUserId(null, timestamp));
        assertThrows(IllegalArgumentException.class, () -> userLoginService.updateLastLoginTimestampForUserId("id", null));
        assertThrows(GazelleDAOException.class, () -> userLoginService.updateLastLoginTimestampForUserId("badId", timestamp));
    }

    @Test
    void testNeedToChangePassword() {
        when(userLoginDAOMock.needToChangePassword("badUserId")).thenThrow(NoSuchElementException.class);
        when(userLoginDAOMock.needToChangePassword("loginTestUser1")).thenReturn(false);
        when(userLoginDAOMock.needToChangePassword("loginTestUser2")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> userLoginService.needToChangePassword(null));
        assertThrows(NoSuchElementException.class, () -> userLoginService.needToChangePassword("badUserId"));
        assertFalse(userLoginService.needToChangePassword("loginTestUser1"));
        assertTrue(userLoginService.needToChangePassword("loginTestUser2"));
    }
}
