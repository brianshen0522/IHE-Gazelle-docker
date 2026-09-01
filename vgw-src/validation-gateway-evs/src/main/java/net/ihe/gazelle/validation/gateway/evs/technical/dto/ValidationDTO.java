package net.ihe.gazelle.validation.gateway.evs.technical.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.time.OffsetDateTime;
import java.util.List;

@JsonRootName(value = "validation")
@JsonIgnoreProperties(ignoreUnknown = true)
@JacksonXmlRootElement(localName = "validation", namespace = "http://evsobjects.gazelle.ihe.net/")
@XmlRootElement(namespace = "http://evsobjects.gazelle.ihe.net/", name = "validation")
@XmlAccessorType(XmlAccessType.FIELD)
public class ValidationDTO {

    @JsonProperty("validationType")
    @XmlElement(name = "validationType")
    @JacksonXmlProperty(localName = "validationType")
    private ValidationType validationType;

    @JsonProperty("validationService")
    @XmlElement(namespace = "http://evsobjects.gazelle.ihe.net/", name = "validationService")
    @JacksonXmlProperty(localName = "validationService", namespace = "http://evsobjects.gazelle.ihe.net/")
    private ValidationServiceDTO validationService;

    @JsonProperty("objectType")
    @XmlAttribute(name = "objectType")
    @JacksonXmlProperty(localName = "objectType", isAttribute = true)
    private String objectType;

    @JsonProperty("status")
    @XmlElement(namespace = "http://evsobjects.gazelle.ihe.net/", name = "status")
    @JacksonXmlProperty(localName = "status", namespace = "http://evsobjects.gazelle.ihe.net/")
    private ValidationStatus status;

    @JsonProperty("validationReportRef")
    @XmlElement(name = "validationReportRef")
    @JacksonXmlProperty(localName = "validationReportRef")
    private ValidationReportRefDTO validationReportRef;

    @JsonProperty("oid")
    @XmlAttribute(name = "oid")
    @JacksonXmlProperty(localName = "oid", isAttribute = true)
    private String oid;

    @JsonProperty("object")
    @JsonAlias("objects")
    @XmlElement(name = "object")
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "object")
    private List<HandledObjectDTO> objects;

    @JsonProperty("sharing")
    @XmlElement(name = "sharing")
    @JacksonXmlProperty(localName = "sharing")
    private SharingMetadataDTO sharing;

    @JsonProperty("caller")
    @XmlElement(name = "caller")
    @JacksonXmlProperty(localName = "caller")
    private CallerMetadataDTO caller;

    @JsonProperty("date")
    @XmlAttribute(name = "date")
    @JacksonXmlProperty(localName = "date", isAttribute = true)
    private OffsetDateTime date;

    @JsonProperty("owner")
    @XmlElement(name = "owner")
    @JacksonXmlProperty(localName = "owner")
    private OwnerMetadataDTO owner;

    public ValidationType getValidationType() {
        return validationType;
    }

    public void setValidationType(ValidationType validationType) {
        this.validationType = validationType;
    }

    public ValidationServiceDTO getValidationService() {
        return validationService;
    }

    public void setValidationService(ValidationServiceDTO validationService) {
        this.validationService = validationService;
    }

    public String getObjectType() {
        return objectType;
    }

    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }

    public ValidationStatus getStatus() {
        return status;
    }

    public void setStatus(ValidationStatus status) {
        this.status = status;
    }

    public ValidationReportRefDTO getValidationReportRef() {
        return validationReportRef;
    }

    public void setValidationReportRef(ValidationReportRefDTO validationReportRef) {
        this.validationReportRef = validationReportRef;
    }

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public List<HandledObjectDTO> getObjects() {
        return objects;
    }

    public void setObjects(List<HandledObjectDTO> objects) {
        this.objects = objects;
    }

    public SharingMetadataDTO getSharing() {
        return sharing;
    }

    public void setSharing(SharingMetadataDTO sharing) {
        this.sharing = sharing;
    }

    public CallerMetadataDTO getCaller() {
        return caller;
    }

    public void setCaller(CallerMetadataDTO caller) {
        this.caller = caller;
    }

    public OffsetDateTime getDate() {
        return date;
    }

    public void setDate(OffsetDateTime date) {
        this.date = date;
    }

    public OwnerMetadataDTO getOwner() {
        return owner;
    }

    public void setOwner(OwnerMetadataDTO owner) {
        this.owner = owner;
    }
}
