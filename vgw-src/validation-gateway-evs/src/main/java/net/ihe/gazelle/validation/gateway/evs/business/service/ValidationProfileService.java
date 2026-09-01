package net.ihe.gazelle.validation.gateway.evs.business.service;

import net.ihe.gazelle.security.business.GazelleIdentity;
import net.ihe.gazelle.validation.gateway.business.ValidationProfileWithService;
import net.ihe.gazelle.validation.gateway.evs.technical.dto.ValidationServiceProfileDTO;

import java.util.List;

public interface ValidationProfileService {

    List<ValidationServiceProfileDTO> listProfiles(String serviceName, GazelleIdentity identity);

    ValidationServiceProfileDTO toProfile(ValidationProfileWithService entry);
}
