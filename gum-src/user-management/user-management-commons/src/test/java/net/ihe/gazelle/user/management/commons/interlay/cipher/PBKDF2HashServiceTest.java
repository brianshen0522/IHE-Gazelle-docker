package net.ihe.gazelle.user.management.commons.interlay.cipher;

import net.ihe.gazelle.user.management.api.domain.user.Credentials;
import net.ihe.gazelle.user.management.commons.GazelleAssertions;
import net.ihe.gazelle.user.management.commons.application.user.login.HashPasswordService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PBKDF2HashServiceTest {

    private final HashPasswordService pbkdf2HashService = new PBKDF2HashService();

    @Test
    void testHashBadParameter() {
        Exception exception = Assertions.assertThrows(IllegalArgumentException.class,
                () -> pbkdf2HashService.hash(null));

        GazelleAssertions.assertExceptionContains("Password is null", exception);
    }

    @Test
    void testHashPassword() {
        String password = "myPassword";
        Credentials hashedPassword = pbkdf2HashService.hash(password);

        assertEquals("PBKDF2", hashedPassword.getHashMethod());
        assertNotNull(hashedPassword.getSalt());
        assertNotNull(hashedPassword.getIterations());
        assertNotNull(hashedPassword.getPassword());
    }

    @Test
    void testVerifyBadHashPassword() {
        String rawPassword = "myPassword";
        String hashedPassword = "21b30ada3fe1abc219f6e86c3c371f52a866aff82d3a5c1bda3f9b3c921bc74beb551b8bffed1d580528f86a746c84e2e8e8e7f0339c852d0321821a629fc6e7";

        Credentials credentials = new Credentials(hashedPassword);
        credentials.setSalt("kerevalSalt");
        credentials.setIterations(1000);
        assertTrue(pbkdf2HashService.verify(credentials, rawPassword));
    }

    @Test
    void testVerifyBadParameters() {
        Credentials credentials1 = new Credentials("hash");
        Exception exception = Assertions.assertThrows(IllegalArgumentException.class,
                () -> pbkdf2HashService.verify(credentials1,null));
        GazelleAssertions.assertExceptionContains("is null", exception);

        Credentials credentials2 = new Credentials();
        exception = Assertions.assertThrows(IllegalArgumentException.class,
                () -> pbkdf2HashService.verify(credentials2,"myPassword"));
        GazelleAssertions.assertExceptionContains("is null", exception);

        exception = Assertions.assertThrows(IllegalArgumentException.class,
                () -> pbkdf2HashService.verify(credentials1,"myPassword"));
        GazelleAssertions.assertExceptionContains("is null", exception);

        Credentials credentials3 = new Credentials("hash");
        credentials3.setSalt("salt");
        exception = Assertions.assertThrows(IllegalArgumentException.class,
                () -> pbkdf2HashService.verify(credentials3,"myPassword"));
        GazelleAssertions.assertExceptionContains("is null", exception);
    }
}
