package net.ihe.gazelle.validation.gateway.evs.business.model;

import java.util.List;

public class ValidationRequest {

    private String validationServiceName;
    private String validationServiceValidator;
    private String objectType;
    private List<byte[]> objectContents;

    public String getValidationServiceName() {
        return validationServiceName;
    }

    public ValidationRequest setValidationServiceName(String validationServiceName) {
        this.validationServiceName = validationServiceName;
        return this;
    }

    public String getValidationServiceValidator() {
        return validationServiceValidator;
    }

    public ValidationRequest setValidationServiceValidator(String validationServiceValidator) {
        this.validationServiceValidator = validationServiceValidator;
        return this;
    }

    public String getObjectType() {
        return objectType;
    }

    public ValidationRequest setObjectType(String objectType) {
        this.objectType = objectType;
        return this;
    }

    public List<byte[]> getObjectContents() {
        return objectContents;
    }

    public ValidationRequest setObjectContents(List<byte[]> objectContents) {
        this.objectContents = objectContents;
        return this;
    }
}
