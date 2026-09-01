package net.ihe.gazelle.user.management.api.interlay.user;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserSummarySearchResponseTest {

    @Test
    void testUserSearchResponse() {
        UserSummaryResource userResource = new UserSummaryResource();
        userResource.setFirstName("toto");
        UserSummarySearchResponse userSearchResponse = new UserSummarySearchResponse(List.of(userResource), 0, 100);
        assertEquals("toto", userSearchResponse.users().getFirst().getFirstName());
        assertEquals(0, userSearchResponse.offset());
        assertEquals(100, userSearchResponse.limit());
    }
}
