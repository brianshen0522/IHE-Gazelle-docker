package net.ihe.gazelle.user.management.commons.application.user.search;

import net.ihe.gazelle.security.business.*;
import net.ihe.gazelle.security.technical.PermissionStoreSPIProvider;
import net.ihe.gazelle.user.management.api.application.user.search.UserSearchCriteria;
import net.ihe.gazelle.user.management.api.application.user.search.UserSearchIndexServiceImpl;
import net.ihe.gazelle.user.management.api.application.user.search.UserSuggestionService;
import net.ihe.gazelle.user.management.commons.interlay.dao.user.UserSearchDAO;
import net.ihe.gazelle.user.management.commons.interlay.utils.MockedGazelleIdentity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
class UserSuggestionServiceImplTest {


    @Mock
    UserSearchDAO userSearchDAO;
    UserSuggestionService userSuggestionService;
    GazelleIdentity adminIdentity = new MockedGazelleIdentity(Set.of(Groups.ROLE_ADMIN));


    @BeforeEach
    void init() {
        userSuggestionService = new UserSuggestionServiceImpl(userSearchDAO, new UserSearchIndexServiceImpl(), new AuthzImpl(new PermissionStoreSPIProvider()));
    }

    @Test
    void testGetSuggestionsUnauthenticated() {
        UserSearchCriteria userSearchCriteria = new UserSearchCriteria();
        assertThrows(UnauthorizedException.class, () -> userSuggestionService.getSuggestions("firstName", userSearchCriteria, null));

        GazelleIdentity identity = BaseGazelleIdentity.unauthenticatedIdentity();
        assertThrows(UnauthorizedException.class, () -> userSuggestionService.getSuggestions("firstName", userSearchCriteria, identity));
    }

    @Test
    void testGetSuggestionsBadField() {
        UserSearchCriteria userSearchCriteria = new UserSearchCriteria();
        assertThrows(IllegalArgumentException.class, () -> userSuggestionService.getSuggestions("bad field", userSearchCriteria, adminIdentity));
    }

    @Test
    void testGetSuggestions() {
        UserSearchCriteria userSearchCriteria = new UserSearchCriteria();
        List<String> firstNameResults = List.of("John", "Jane", "Joe");
        when(userSearchDAO.getSuggestions("firstName",userSearchCriteria)).thenReturn(firstNameResults);

        List<String> results = userSuggestionService.getSuggestions("firstName", userSearchCriteria, adminIdentity);

        assertEquals(firstNameResults, results);
    }

}