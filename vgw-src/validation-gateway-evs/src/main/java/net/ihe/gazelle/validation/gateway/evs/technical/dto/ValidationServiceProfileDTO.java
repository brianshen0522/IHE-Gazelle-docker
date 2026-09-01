package net.ihe.gazelle.validation.gateway.evs.technical.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "validationServiceProfile")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ValidationServiceProfileDTO {

    @JsonProperty("serviceName")
    @XmlElement(name = "serviceName")
    private String serviceName;

    @JsonProperty("validator")
    @XmlElement(name = "validator")
    private ValidatorDTO validator;

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public ValidatorDTO getValidator() {
        return validator;
    }

    public void setValidator(ValidatorDTO validator) {
        this.validator = validator;
    }
}
