package net.ihe.gazelle.validation.gateway.evs.technical.service;

import net.ihe.gazelle.security.business.GazelleIdentity;
import net.ihe.gazelle.search.api.Range;
import net.ihe.gazelle.validation.gateway.business.ProfileSearchCriteria;
import net.ihe.gazelle.validation.gateway.business.SearchProfileService;
import net.ihe.gazelle.validation.gateway.business.ValidationProfileWithService;
import net.ihe.gazelle.validation.gateway.evs.business.service.ValidationProfileService;
import net.ihe.gazelle.validation.gateway.evs.technical.dto.ValidationServiceProfileDTO;
import net.ihe.gazelle.validation.gateway.evs.technical.dto.ValidatorDTO;
import net.ihe.gazelle.validation.v2.api.business.profile.ValidationProfile;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class ValidationProfileServiceImpl implements ValidationProfileService {

    private final SearchProfileService searchProfileService;

    public ValidationProfileServiceImpl(SearchProfileService searchProfileService) {
        this.searchProfileService = searchProfileService;
    }

    @Override
    public List<ValidationServiceProfileDTO> listProfiles(String serviceName, GazelleIdentity identity) {
        ProfileSearchCriteria criteria = new ProfileSearchCriteria();
        if (serviceName != null && !serviceName.isBlank()) {
            criteria.setValidationService(serviceName);
        }
        var query = new net.ihe.gazelle.search.api.SearchQuery<>(criteria, new Range(0, Integer.MAX_VALUE), null);
        List<ValidationProfileWithService> profiles = searchProfileService.search(query, identity).objects();
        List<ValidationServiceProfileDTO> response = new ArrayList<>();
        for (ValidationProfileWithService entry : profiles) {
            response.add(toProfile(entry));
        }
        return response;
    }

    @Override
    public ValidationServiceProfileDTO toProfile(ValidationProfileWithService entry) {
        ValidationProfile profile = entry.getProfile();
        ValidationServiceProfileDTO dto = new ValidationServiceProfileDTO();
        dto.setServiceName(entry.getValidationService());
        ValidatorDTO validator = new ValidatorDTO();
        validator.setKeyword(profile.getProfileID());
        validator.setName(profile.getProfileName());
        validator.setDomain(profile.getDomain());
        dto.setValidator(validator);
        return dto;
    }
}
