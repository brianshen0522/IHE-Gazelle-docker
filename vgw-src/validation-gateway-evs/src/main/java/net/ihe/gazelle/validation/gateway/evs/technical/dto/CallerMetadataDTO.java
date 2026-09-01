package net.ihe.gazelle.validation.gateway.evs.technical.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "caller")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CallerMetadataDTO {

    @JsonProperty("entryPoint")
    @XmlAttribute(name = "entryPoint")
    @JacksonXmlProperty(localName = "entryPoint", isAttribute = true)
    private EntryPoint entryPoint;

    @JsonProperty("toolOid")
    @XmlAttribute(name = "toolOid")
    @JacksonXmlProperty(localName = "toolOid", isAttribute = true)
    private String toolOid;

    @JsonProperty("toolObjectId")
    @XmlAttribute(name = "toolObjectId")
    @JacksonXmlProperty(localName = "toolObjectId", isAttribute = true)
    private String toolObjectId;

    @JsonProperty("proxyType")
    @XmlAttribute(name = "proxyType")
    @JacksonXmlProperty(localName = "proxyType", isAttribute = true)
    private String proxyType;

    public EntryPoint getEntryPoint() {
        return entryPoint;
    }

    public void setEntryPoint(EntryPoint entryPoint) {
        this.entryPoint = entryPoint;
    }

    public String getToolOid() {
        return toolOid;
    }

    public void setToolOid(String toolOid) {
        this.toolOid = toolOid;
    }

    public String getToolObjectId() {
        return toolObjectId;
    }

    public void setToolObjectId(String toolObjectId) {
        this.toolObjectId = toolObjectId;
    }

    public String getProxyType() {
        return proxyType;
    }

    public void setProxyType(String proxyType) {
        this.proxyType = proxyType;
    }
}
