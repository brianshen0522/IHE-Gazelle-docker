package net.ihe.gazelle.validation.gateway.evs.technical.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import jakarta.validation.constraints.Pattern;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "object")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties(ignoreUnknown = true)
public class HandledObjectDTO {

    @JsonProperty("objectType")
    @XmlAttribute(name = "objectType")
    @JacksonXmlProperty(localName = "objectType", isAttribute = true)
    private String objectType;

    @JsonProperty("originalFileName")
    @XmlAttribute(name = "originalFileName")
    @JacksonXmlProperty(localName = "originalFileName", isAttribute = true)
    private String originalFileName;

    @JsonProperty("content")
    @XmlElement(namespace = "http://evsobjects.gazelle.ihe.net/", name = "content")
    @JacksonXmlProperty(localName = "content", namespace = "http://evsobjects.gazelle.ihe.net/")
    @Pattern(regexp = "^(?:[A-Za-z0-9+/]{4})*(?:[A-Za-z0-9+/]{2}==|[A-Za-z0-9+/]{3}=)?$")
    private byte[] content;

    @JsonProperty("ref")
    @XmlAttribute(name = "ref")
    @JacksonXmlProperty(localName = "ref", isAttribute = true)
    private String ref;

    @JsonProperty("role")
    @XmlAttribute(name = "role")
    @JacksonXmlProperty(localName = "role", isAttribute = true)
    private String role;

    public String getObjectType() {
        return objectType;
    }

    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
