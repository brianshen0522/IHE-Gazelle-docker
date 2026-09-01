package net.ihe.gazelle.validation.gateway.evs.business.service;

import net.ihe.gazelle.security.business.GazelleIdentity;
import net.ihe.gazelle.validation.gateway.evs.business.model.ValidationCreationResult;
import net.ihe.gazelle.validation.gateway.evs.technical.dto.ValidationDTO;
import net.ihe.gazelle.validation.gateway.evs.technical.dto.ValidationServiceProfileDTO;

import java.util.List;

public class EvsValidationService {

    private final ValidationExecutionService executionService;
    private final ValidationProfileService validationProfileService;

    public EvsValidationService(ValidationExecutionService executionService,
                                ValidationProfileService validationProfileService) {
        this.executionService = executionService;
        this.validationProfileService = validationProfileService;
    }

    public ValidationCreationResult createValidation(ValidationDTO validationRequest,
                                                     GazelleIdentity identity,
                                                     boolean async,
                                                     String baseUri) {
        return executionService.createValidation(validationRequest, identity, async, baseUri);
    }

    public List<ValidationServiceProfileDTO> listProfiles(String serviceName, GazelleIdentity identity) {
        return validationProfileService.listProfiles(serviceName, identity);
    }
}
