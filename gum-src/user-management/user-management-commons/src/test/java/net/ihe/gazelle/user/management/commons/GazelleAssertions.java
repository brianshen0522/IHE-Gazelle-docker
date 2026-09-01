package net.ihe.gazelle.user.management.commons;

import org.junit.jupiter.api.AssertionFailureBuilder;
import org.junit.jupiter.api.Assertions;

public class GazelleAssertions extends Assertions {

    public static void assertExceptionContains(String expected, Exception e) {
        String message = e.getMessage();
        if (message == null || !message.contains(expected)) {
            AssertionFailureBuilder.assertionFailure()
                    .message("Exception doesn't contains expected string.")
                    .expected(expected)
                    .actual(message)
                    .buildAndThrow();
        }
    }
}
