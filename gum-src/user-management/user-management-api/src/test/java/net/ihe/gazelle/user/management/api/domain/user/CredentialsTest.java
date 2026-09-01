package net.ihe.gazelle.user.management.api.domain.user;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CredentialsTest {

    @Test
    void basicConstructor() {
        Credentials credentials = new Credentials("password");
        assertNull(credentials.getIterations());
        assertNull(credentials.getSalt());
        assertNull(credentials.getHashMethod());
        assertEquals("password", credentials.getPassword());
    }


    @Test
    void constructCredentialsWithSetters() {
        Credentials credentials = new Credentials("password");
        credentials.setPassword("newPassword");
        assertEquals("newPassword", credentials.getPassword());

        credentials.setSalt("salt");
        assertEquals("salt", credentials.getSalt());

        credentials.setIterations(100);
        assertEquals(100, credentials.getIterations());

        credentials.setHashMethod("PBKDF2");
        assertEquals("PBKDF2", credentials.getHashMethod());

    }

    @Test
    void testEquals() {
        EqualsVerifier.simple().forClass(Credentials.class).verify();
    }

}
