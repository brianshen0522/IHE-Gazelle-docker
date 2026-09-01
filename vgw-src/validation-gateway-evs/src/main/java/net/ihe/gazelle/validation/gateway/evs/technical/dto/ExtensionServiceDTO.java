package net.ihe.gazelle.validation.gateway.evs.technical.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "extensionService")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExtensionServiceDTO {

    @JsonProperty("profile")
    @JacksonXmlProperty(localName = "profile")
    private String profile;

    @JsonProperty("validator")
    @XmlAttribute(name = "validator")
    @JacksonXmlProperty(localName = "validator", isAttribute = true)
    private String validator;

    @JsonProperty("name")
    @XmlAttribute(name = "name")
    @JacksonXmlProperty(localName = "name", isAttribute = true)
    private String name;

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public String getValidator() {
        return validator;
    }

    public void setValidator(String validator) {
        this.validator = validator;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
