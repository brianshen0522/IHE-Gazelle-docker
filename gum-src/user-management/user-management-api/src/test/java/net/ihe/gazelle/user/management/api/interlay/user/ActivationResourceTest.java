package net.ihe.gazelle.user.management.api.interlay.user;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ActivationResourceTest {

    @Test
    void testActivationResource() {
        ActivationResource activationResource = new ActivationResource();
        assertNull(activationResource.getUserId());
        activationResource = new ActivationResource("userId");
        assertEquals("userId", activationResource.getUserId());
        activationResource.setUserId("newId");
        assertEquals("newId", activationResource.getUserId());
    }
}
