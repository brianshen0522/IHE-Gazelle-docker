package net.ihe.gazelle.user.management.commons.interlay.cipher;

import net.ihe.gazelle.user.management.api.domain.user.Credentials;
import net.ihe.gazelle.user.management.commons.GazelleAssertions;
import net.ihe.gazelle.user.management.commons.application.user.login.HashPasswordService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class MD5HashServiceTest {

    private final HashPasswordService md5HashService = new MD5HashService();

    private static final String PASSWORD = "myPassword";

    @Test
    void testHashBadParameter() {
        Exception exception = Assertions.assertThrows(IllegalArgumentException.class,
                () -> md5HashService.hash(null));

        GazelleAssertions.assertExceptionContains("Password is null", exception);
    }

    @Test
    void testHashPassword() {
        Credentials passwordEncoded = md5HashService.hash(PASSWORD);

        Assertions.assertEquals("deb1536f480475f7d593219aa1afd74c", passwordEncoded.getPassword());
    }

    @Test
    void testVerifyPassword() {
        String encodedPassword = "deb1536f480475f7d593219aa1afd74c";

        Assertions.assertTrue(md5HashService.verify(new Credentials(encodedPassword), PASSWORD));
    }

    @Test
    void testVerifyBadParameters() {
        Credentials credentials = new Credentials("deb1536f480475f7d593219aa1afd74c");
        Exception exceptionRaw = Assertions.assertThrows(IllegalArgumentException.class,
                () -> md5HashService.verify(credentials, null));

        GazelleAssertions.assertExceptionContains("Password is null", exceptionRaw);

        Credentials credentials1 = new Credentials();
        Exception exceptionHashed = Assertions.assertThrows(IllegalArgumentException.class,
                () -> md5HashService.verify(credentials1, PASSWORD));

        GazelleAssertions.assertExceptionContains("Hashed password is null", exceptionHashed);
    }
}
