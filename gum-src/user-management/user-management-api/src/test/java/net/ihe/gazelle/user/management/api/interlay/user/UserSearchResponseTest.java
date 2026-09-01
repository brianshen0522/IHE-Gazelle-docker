package net.ihe.gazelle.user.management.api.interlay.user;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserSearchResponseTest {

    @Test
    void testUserSearchResponse() {
        UserResource userResource = new UserResource();
        userResource.setActivated(true);
        UserSearchResponse userSearchResponse = new UserSearchResponse(List.of(userResource), 0, 100, 10L);
        assertTrue(userSearchResponse.users().get(0).isActivated());
        assertEquals(0, userSearchResponse.offset());
        assertEquals(100, userSearchResponse.limit());
        assertEquals(10, userSearchResponse.count());
    }
}
