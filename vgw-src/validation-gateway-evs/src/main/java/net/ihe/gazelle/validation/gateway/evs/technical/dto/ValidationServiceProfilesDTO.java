package net.ihe.gazelle.validation.gateway.evs.technical.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.List;

@JacksonXmlRootElement(localName = "validationServiceProfiles")
@XmlRootElement(name = "validationServiceProfiles")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ValidationServiceProfilesDTO {

    @JsonProperty("profiles")
    @XmlElement(name = "validationServiceProfile")
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "validationServiceProfile")
    private List<ValidationServiceProfileDTO> profiles;

    public ValidationServiceProfilesDTO() {
    }

    public ValidationServiceProfilesDTO(List<ValidationServiceProfileDTO> profiles) {
        this.profiles = profiles;
    }

    public List<ValidationServiceProfileDTO> getProfiles() {
        return profiles;
    }

    public void setProfiles(List<ValidationServiceProfileDTO> profiles) {
        this.profiles = profiles;
    }
}
