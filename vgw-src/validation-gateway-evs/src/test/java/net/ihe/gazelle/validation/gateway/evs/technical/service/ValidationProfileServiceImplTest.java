package net.ihe.gazelle.validation.gateway.evs.technical.service;

import net.ihe.gazelle.search.api.SearchQuery;
import net.ihe.gazelle.search.api.SearchResult;
import net.ihe.gazelle.security.business.Authz;
import net.ihe.gazelle.security.business.GazelleIdentity;
import net.ihe.gazelle.validation.gateway.business.ProfileSearchCriteria;
import net.ihe.gazelle.validation.gateway.business.SearchProfileService;
import net.ihe.gazelle.validation.gateway.business.ValidationProfileWithService;
import net.ihe.gazelle.validation.gateway.evs.technical.dto.ValidationServiceProfileDTO;
import net.ihe.gazelle.validation.v2.api.business.profile.ValidationProfile;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class ValidationProfileServiceImplTest {

    @Test
    void listProfilesUsesUnboundedRange() {
        CapturingSearchProfileService searchProfileService = new CapturingSearchProfileService();
        ValidationProfileServiceImpl service = new ValidationProfileServiceImpl(searchProfileService);

        service.listProfiles(null, null);

        assertThat(searchProfileService.lastQuery.range().getOffset(), is(0));
        assertThat(searchProfileService.lastQuery.range().getLimit(), is(Integer.MAX_VALUE));
    }

    @Test
    void toProfileMapsLegacyValidatorStructure() {
        ValidationProfileServiceImpl service = new ValidationProfileServiceImpl((SearchProfileService) null);
        ValidationProfile profile = new ValidationProfile()
              .setProfileID("ITI-18_request")
              .setProfileName("ITI-18 Registry Stored Query Request")
              .setDomain("ITI");
        ValidationProfileWithService entry = new ValidationProfileWithService("mock-validation-service", profile);

        ValidationServiceProfileDTO dto = service.toProfile(entry);

        assertThat(dto.getServiceName(), is("mock-validation-service"));
        assertThat(dto.getValidator().getKeyword(), is("ITI-18_request"));
        assertThat(dto.getValidator().getName(), is("ITI-18 Registry Stored Query Request"));
        assertThat(dto.getValidator().getDomain(), is("ITI"));
    }

    private static final class CapturingSearchProfileService extends SearchProfileService {

        private static final Authz ALLOW_ALL_AUTHZ = new Authz() {
            @Override
            public boolean isAuthorized(GazelleIdentity identity, String action, Object... resources) {
                return true;
            }

            @Override
            public <C extends java.util.Collection<? extends Object>> C filterOutUnauthorized(
                  GazelleIdentity identity, String action, C resources) {
                return resources;
            }
        };

        private SearchQuery<ProfileSearchCriteria> lastQuery;

        private CapturingSearchProfileService() {
            super((criteria, identity) -> List.of(), ALLOW_ALL_AUTHZ);
        }

        @Override
        public SearchResult<ValidationProfileWithService> search(SearchQuery<ProfileSearchCriteria> query,
                                                                 GazelleIdentity identity) {
            this.lastQuery = query;
            return new SearchResult<>(List.of(), 0, 0, 0);
        }
    }
}
