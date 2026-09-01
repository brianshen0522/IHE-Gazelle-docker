package net.ihe.gazelle.validation.gateway.evs.business.service;

import net.ihe.gazelle.maestro.api.business.property.ByteArrayProperty;
import net.ihe.gazelle.maestro.api.business.property.Property;
import net.ihe.gazelle.maestro.api.business.property.StringProperty;
import net.ihe.gazelle.maestro.api.business.test.Step;
import net.ihe.gazelle.maestro.api.business.test.Test;
import net.ihe.gazelle.maestro.api.business.testrun.TestRun;
import net.ihe.gazelle.security.business.acl.AccessControlList;
import net.ihe.gazelle.validation.gateway.evs.technical.dto.HandledObjectDTO;
import net.ihe.gazelle.validation.gateway.evs.technical.dto.ValidationDTO;
import net.ihe.gazelle.validation.gateway.evs.technical.dto.ValidationReportRefDTO;
import net.ihe.gazelle.validation.gateway.evs.technical.dto.ValidationStatus;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class TestRunMapper {

    private static final String STEP_TYPE = "VALIDATION";
    private static final String PROPERTY_VALIDATION_SERVICE = "validationService";
    private static final String PROPERTY_VALIDATION_PROFILE = "validationProfile";
    private static final String PROPERTY_CONTENT_TO_VALIDATE = "contentToValidate";
    private static final String DEFAULT_INPUT_ID = "inputFile";

    public String generateOid() {
        return UUID.randomUUID().toString();
    }

    public TestRun toTestRun(String oid, ValidationDTO validationRequest, AccessControlList acl) {
        return toTestRun(oid, validationRequest, acl, null);
    }

    public TestRun toTestRun(String oid,
                             ValidationDTO validationRequest,
                             AccessControlList acl,
                             String profileInputName) {
        Test test = new Test()
              .setId("evs-validation-" + oid)
              .setName("EVS validation");
        Step step = new Step()
              .setName("Validation")
              .setType(STEP_TYPE)
              .setProperties(buildStepProperties(validationRequest, profileInputName));
        test.setSteps(List.of(step));
        return new TestRun()
              .setTest(test)
              .setInputs(buildInputs(validationRequest, profileInputName))
              .setAccessControlList(acl);
    }

    public ValidationDTO normalizeRequest(ValidationDTO validationRequest) {
        ValidationDTO normalized = validationRequest != null ? validationRequest : new ValidationDTO();
        if (normalized.getDate() == null) {
            normalized.setDate(OffsetDateTime.now());
        }
        return normalized;
    }

    public ValidationDTO toValidationResponse(String oid,
                                              ValidationDTO baseValidation,
                                              ValidationStatus status,
                                              String reportLocation) {
        ValidationDTO response = new ValidationDTO();
        response.setOid(oid);
        response.setValidationService(baseValidation.getValidationService());
        response.setValidationType(baseValidation.getValidationType());
        response.setObjectType(baseValidation.getObjectType());
        response.setObjects(baseValidation.getObjects());
        response.setSharing(baseValidation.getSharing());
        response.setOwner(baseValidation.getOwner());
        response.setCaller(baseValidation.getCaller());
        response.setDate(baseValidation.getDate());
        response.setStatus(status);
        if (reportLocation != null) {
            response.setValidationReportRef(new ValidationReportRefDTO(reportLocation));
        }
        return response;
    }

    private List<Property> buildInputs(ValidationDTO validationRequest, String profileInputName) {
        List<Property> inputs = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        List<HandledObjectDTO> objects = validationRequest.getObjects();
        if (objects != null) {
            for (HandledObjectDTO object : objects) {
                if (object != null && object.getContent() != null) {
                    String inputName = resolveInputName(object, profileInputName);
                    if (seen.add(inputName)) {
                        inputs.add(new ByteArrayProperty(resolveInputId(object, inputName), object.getContent()));
                    }
                }
            }
        }
        return inputs;
    }

    private List<Property> buildStepProperties(ValidationDTO validationRequest, String profileInputName) {
        List<Property> properties = new ArrayList<>();
        var validationService = validationRequest.getValidationService();
        if (validationService != null) {
            properties.add(new StringProperty(PROPERTY_VALIDATION_SERVICE, validationService.getName()));
            properties.add(new StringProperty(PROPERTY_VALIDATION_PROFILE, validationService.getValidator()));
        }
        List<HandledObjectDTO> objects = validationRequest.getObjects();
        if (objects != null) {
            for (HandledObjectDTO object : objects) {
                if (object == null || object.getContent() == null) {
                    continue;
                }
                String propertyName = resolveInputName(object, profileInputName);
                if (properties.stream().noneMatch(prop -> propertyName.equals(prop.getName()))) {
                    String inputId = resolveInputId(object, propertyName);
                    ByteArrayProperty content = new ByteArrayProperty(propertyName, "${" + inputId + "}");
                    content.setFileName(object.getOriginalFileName());
                    properties.add(content);
                }
            }
        }
        return properties;
    }

    private String resolveInputName(HandledObjectDTO object, String profileInputName) {
        if (object.getRole() != null && !object.getRole().isBlank()) {
            return object.getRole();
        }
        if (profileInputName != null && !profileInputName.isBlank()) {
            return profileInputName;
        }
        return PROPERTY_CONTENT_TO_VALIDATE;
    }

    private String resolveInputId(HandledObjectDTO object, String fallbackInputId) {
        if (object.getOriginalFileName() != null && !object.getOriginalFileName().isBlank()) {
            return object.getOriginalFileName();
        }
        if (PROPERTY_CONTENT_TO_VALIDATE.equals(fallbackInputId)) {
            return DEFAULT_INPUT_ID;
        }
        return DEFAULT_INPUT_ID + "-" + fallbackInputId;
    }
}
