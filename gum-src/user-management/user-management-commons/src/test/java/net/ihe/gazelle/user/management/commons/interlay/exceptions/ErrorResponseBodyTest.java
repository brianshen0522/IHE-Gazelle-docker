package net.ihe.gazelle.user.management.commons.interlay.exceptions;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ErrorResponseBodyTest {

    @Test
    void constructorAndGetterTests() {
        ErrorResponseBody errorResponseBody = new ErrorResponseBody("error", "message", 200);

        assertEquals("error", errorResponseBody.getError());
        assertEquals("message", errorResponseBody.getMessage());
        assertEquals(200, errorResponseBody.getCode());
    }

    @Test
    void setterTests() {
        ErrorResponseBody errorResponseBody = new ErrorResponseBody("error", "message", 200);
        errorResponseBody.setError("other error");
        errorResponseBody.setMessage("other message");
        errorResponseBody.setCode(400);

        assertEquals("other error", errorResponseBody.getError());
        assertEquals("other message", errorResponseBody.getMessage());
        assertEquals(400, errorResponseBody.getCode());
    }


    @Test
    void equalsHashCodeTest() {
        EqualsVerifier.simple().forClass(ErrorResponseBody.class).verify();
    }

}