package net.ihe.gazelle.validation.gateway.evs.technical.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "validationService")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ValidationServiceDTO {

    @JsonProperty("extensions")
    @XmlElement(name = "extensions")
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "extensions")
    private List<ExtensionServiceDTO> extensions;

    @JsonProperty("validator")
    @XmlAttribute(name = "validator")
    @JacksonXmlProperty(localName = "validator", isAttribute = true)
    private String validator;

    @JsonProperty("name")
    @XmlAttribute(name = "name")
    @JacksonXmlProperty(localName = "name", isAttribute = true)
    private String name;

    public List<ExtensionServiceDTO> getExtensions() {
        return extensions;
    }

    public void setExtensions(List<ExtensionServiceDTO> extensions) {
        this.extensions = extensions;
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
