package net.ihe.gazelle.user.management.api.application.user.search;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserSearchCriteriaTest {

    @Test
    void criteriaCreationTest(){
        UserSearchCriteria criteria = new UserSearchCriteria();

        criteria.setActivatedParam(Boolean.TRUE);
        criteria.setDelegatedParam(Boolean.FALSE);
        criteria.setOrganizationNameParam("organizationName");
        criteria.setGroupParam("role:my_role");
        criteria.setEmailParam("user@mail.com");
        criteria.setFirstNameParam("firstName");
        criteria.setLastNameParam("lastName");
        criteria.setSearchParam("search");

        assertEquals(Boolean.TRUE, criteria.getActivatedParam().getFirstValue());
        assertEquals(Boolean.FALSE, criteria.getDelegatedParam().getFirstValue());
        assertEquals("organizationName", criteria.getOrganizationNameParam().getFirstValue());
        assertEquals("role:my_role", criteria.getGroupParam().getFirstValue());
        assertEquals("user@mail.com", criteria.getEmailParam().getFirstValue());
        assertEquals("firstName", criteria.getFirstNameParam().getFirstValue());
        assertEquals("lastName", criteria.getLastNameParam().getFirstValue());
        assertEquals("search", criteria.getSearchParam().getFirstValue());

    }

}