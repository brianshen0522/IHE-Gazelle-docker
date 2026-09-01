package net.ihe.gazelle.user.management.api.application.user;

import net.ihe.gazelle.user.management.api.application.user.lookup.SortOrder;
import net.ihe.gazelle.user.management.api.application.user.lookup.UserQueryParams;
import net.ihe.gazelle.user.management.api.application.user.lookup.UserSearchResult;
import net.ihe.gazelle.user.management.api.domain.user.User;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import java.util.List;

import static net.ihe.gazelle.user.management.api.application.user.lookup.UserQueryParams.*;
import static org.junit.jupiter.api.Assertions.*;

class UserQueryParamsTest {

    @Test
    void testSortOrder() {
        SortOrder sortOrder = SortOrder.ASC;
        assertEquals("ASC", sortOrder.name());
        sortOrder = SortOrder.DESC;
        assertEquals("DESC", sortOrder.name());
        assertEquals(2, SortOrder.values().length);
        assertEquals(SortOrder.ASC, SortOrder.getByString("ASC"));
        assertEquals(SortOrder.DESC, SortOrder.getByString("DESC"));
        assertNotEquals(SortOrder.ASC, SortOrder.getByString("DESC"));
    }

    @Test
    void testUserQueryParams() {
        UserQueryParams userQueryParams = UserQueryParams.nullQuery();
        assertNull(userQueryParams.firstName());
        userQueryParams = userQueryParams.setSearch("search");
        userQueryParams = userQueryParams.setAttribute(ATTR_FIRST_NAME,"firstName");
        userQueryParams = userQueryParams.setAttribute(ATTR_LAST_NAME,"lastName");
        userQueryParams = userQueryParams.setAttribute(ATTR_EMAIL,"email");
        userQueryParams = userQueryParams.setAttribute(ATTR_ORGANIZATION_ID,"orgaId");
        assertEquals("search", userQueryParams.search());
        assertEquals("firstName", userQueryParams.firstName());
        assertEquals("lastName", userQueryParams.lastName());
        assertEquals("email", userQueryParams.email());
        assertEquals("orgaId", userQueryParams.organizationId());

        userQueryParams = new UserQueryParams("search", "firstName", "lastName", "email", "group", "organizationId", true,true, "externalId", "idpId");
        assertEquals("group", userQueryParams.group());
        assertEquals("organizationId", userQueryParams.organizationId());
        assertTrue(userQueryParams.activated());
        assertTrue(userQueryParams.delegated());
        assertEquals("externalId", userQueryParams.externalId());
        assertEquals("idpId", userQueryParams.idpId());
    }

    @Test
    void testUserQueryParams2() {
        UserQueryParams userQueryParams = new UserQueryParams("search", "firstName", "lastName", "email", "group", "organizationId", true,true, "externalId", "idpId");

        UserQueryParams clone = UserQueryParams.clone(userQueryParams);
        assertEquals("search", clone.search());
        assertEquals("firstName", clone.firstName());
        assertEquals("lastName", clone.lastName());
        assertEquals("email", clone.email());
        assertEquals("group", clone.group());
        assertEquals("organizationId", clone.organizationId());
        assertTrue(clone.activated());
        assertTrue(clone.delegated());
        assertEquals("externalId", clone.externalId());
        assertEquals("idpId", clone.idpId());
    }

    @Test
    void testUserSearchResult() {
        User user = new User();
        user.setActivated(true);
        UserSearchResult userSearchResult = new UserSearchResult(List.of(user), 0, 100, 10L);
        assertTrue(userSearchResult.users().get(0).isActivated());
        assertEquals(0, userSearchResult.offset());
        assertEquals(100, userSearchResult.limit());
        assertEquals(10, userSearchResult.count());
    }

    @Test
    void testUserQueryParamEquals() {
        EqualsVerifier.simple().forClass(UserQueryParams.class).verify();
        UserQueryParams userQueryParams1 = UserQueryParams.nullQuery().setAttribute("firstName", "equalsFistName");
        UserQueryParams userQueryParams2 = UserQueryParams.nullQuery().setAttribute("lastName", "equalsLastName");
        UserQueryParams userQueryParams3 = new UserQueryParams(null,"equalsFistName",null,null,null,null,null,null,null,null);
        assertNotEquals(userQueryParams1,userQueryParams2);
        assertEquals(userQueryParams1,userQueryParams3);
        assertNotEquals(userQueryParams2,userQueryParams3);
    }

    @Test
    void testUserQueryParamSetter() {
        UserQueryParams userQueryParams = UserQueryParams.nullQuery().setAttribute("badAttribute", "yoyoyo");
        assertEquals(UserQueryParams.nullQuery(), userQueryParams);

        userQueryParams = UserQueryParams.nullQuery().setAttribute(ATTR_ACTIVATED, "oui")
                .setAttribute(ATTR_DELEGATED, "non")
                .setAttribute(ATTR_EXTERNAL_ID, 1)
                .setAttribute(ATTR_IDP_ID, 1);
        assertEquals(UserQueryParams.nullQuery(), userQueryParams);
    }
}
