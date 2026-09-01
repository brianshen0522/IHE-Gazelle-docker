package net.ihe.gazelle.user.management.commons.interlay.utils;

import net.ihe.gazelle.user.management.commons.GazelleAssertions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.wildfly.common.Assert.assertTrue;


class CustomSHA1HashServiceTest {

    private final CustomSHA1HashService sha1HashService = new CustomSHA1HashService();

    @Test
    void encodeString() {
        //check if we get the same result when hashing a string two times
        String firstEncoded = sha1HashService.encode("string to encode");
        String secondEncoded = sha1HashService.encode("string to encode");

        Assertions.assertEquals(firstEncoded, secondEncoded);
    }

    @Test
    void checkSize() {
        //size should always be 20
        Assertions.assertEquals(20, sha1HashService.encode("string to encode").length());
        Assertions.assertEquals(20, sha1HashService.encode("this is a long string to test").length());
        Assertions.assertEquals(20, sha1HashService.encode("short").length());
    }

    @Test
    void encodeThrow() {
        Exception exception = Assertions.assertThrows(IllegalArgumentException.class,
                () -> sha1HashService.encode(null));
        GazelleAssertions.assertExceptionContains("hash null", exception);

    }

    @Test
    void verifyThrow() {
        Exception exceptionRaw = Assertions.assertThrows(IllegalArgumentException.class,
                () -> sha1HashService.verify(null, "deb1536f480475f7d593219aa1afd74c"));
        GazelleAssertions.assertExceptionContains("hash null", exceptionRaw);

        Exception exceptionHashed = Assertions.assertThrows(IllegalArgumentException.class,
                () -> sha1HashService.verify("myPassword", null));
        GazelleAssertions.assertExceptionContains("verify null", exceptionHashed);
    }

    @Test
    void verify() {
        assertTrue(sha1HashService.verify("string to encode", "8d0a4f837d621e5626a5"));
    }
}
