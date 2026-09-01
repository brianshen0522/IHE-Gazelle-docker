/*
Copyright 2010-2025 IHE International

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package net.ihe.gazelle.xmlvalidation.technical.config;

import com.fasterxml.jackson.annotation.*;
import net.ihe.gazelle.modelmarshaller.technical.dto.DTO;
import net.ihe.gazelle.validation.v2.api.business.profile.ValidationProfile;
import net.ihe.gazelle.xmlvalidation.business.config.ProfileConfiguration;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;

@JsonRootName("profileConfiguration")
@Schema(name = "ProfileConfiguration")
@JsonTypeName("profileConfiguration")
@JsonPropertyOrder({"profileID", "profileName", "domain", "xsdPath", "schematronPath", "xsltPath", "cacheEnabled", "schematronVersion", "xsdVersion", "standards", "coveredItems", "tags", "available"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProfileConfigurationDTO implements DTO<ProfileConfiguration> {

    private final ProfileConfiguration profileConfiguration;

    public ProfileConfigurationDTO() {
        this.profileConfiguration = new ProfileConfiguration();
    }

    public ProfileConfigurationDTO(ProfileConfiguration profileConfiguration) {
        this.profileConfiguration = profileConfiguration;
    }

    @JsonProperty("profileID")
    public String getProfileID() {
        return this.profileConfiguration.getValidationProfile().getProfileID();
    }

    public ProfileConfigurationDTO setProfileID(String profileID) {
        this.profileConfiguration.getValidationProfile().setProfileID(profileID);
        return this;
    }

    @JsonProperty("profileName")
    public String getProfileName() {
        return this.profileConfiguration.getValidationProfile().getProfileName();
    }

    public ProfileConfigurationDTO setProfileName(String profileName) {
        this.profileConfiguration.getValidationProfile().setProfileName(profileName);
        return this;
    }

    @JsonProperty("domain")
    public String getDomain() {
        return this.profileConfiguration.getValidationProfile().getDomain();
    }

    public ProfileConfigurationDTO setDomain(String domain) {
        this.profileConfiguration.getValidationProfile().setDomain(domain);
        return this;
    }

    @JsonProperty("xsdPath")
    public String getXsdPath() {
        return profileConfiguration.getXsdPath();
    }


    public ProfileConfigurationDTO setXsdPath(String xsdPath) {
        profileConfiguration.setXsdPath(xsdPath);
        return this;
    }

    @JsonProperty("schematronPath")
    public String getSchematronPath() {
        return profileConfiguration.getSchematronPath();
    }

    public ProfileConfigurationDTO setSchematronPath(String schematronPath) {
        profileConfiguration.setSchematronPath(schematronPath);
        return this;
    }

    @JsonProperty("xsltPath")
    public String getXsltPath() {
        return profileConfiguration.getXsltPath();
    }

    public ProfileConfigurationDTO setXsltPath(String xsltPath) {
        profileConfiguration.setXsltPath(xsltPath);
        return this;
    }

    @JsonProperty("cacheEnabled")
    public boolean getCacheEnabled() {
        return profileConfiguration.isCacheEnabled();
    }

    public ProfileConfigurationDTO setCacheEnabled(boolean cacheEnabled) {
        if(cacheEnabled) {
            profileConfiguration.enableCache();
        } else {
            profileConfiguration.disableCache();
        }
        return this;
    }
    @JsonProperty("schematronVersion")
    public String getSchematronVersion() {
        return profileConfiguration.getSchematronVersion();
    }

    @JsonProperty("schematronVersion")
    public ProfileConfigurationDTO setSchematronVersion(String schematronVersion) {
        profileConfiguration.setSchematronVersion(schematronVersion);
        return this;
    }

    @JsonProperty("xsdVersion")
    public String getXsdVersion() {
        return profileConfiguration.getXsdVersion();
    }

    @JsonProperty("xsdVersion")
    public ProfileConfigurationDTO setXsdVersion(String xsdVersion) {
        profileConfiguration.setXsdVersion(xsdVersion);
        return this;
    }


    @JsonProperty("standards")
    public List<String> getStandards() {
        return profileConfiguration.getStandards();
    }

    public ProfileConfigurationDTO setStandards(List<String> standards) {
        profileConfiguration.setStandards(standards);
        return this;
    }

    public ProfileConfigurationDTO setCoveredItems(List<String> coveredItems) {
        profileConfiguration.setCoveredItems(coveredItems);
        return this;
    }

    @JsonProperty
    public List<String> getCoveredItems() {
        return profileConfiguration.getCoveredItems();
    }

    public ProfileConfigurationDTO setTags(List<String> tags) {
        profileConfiguration.setTags(tags);
        return this;
    }

    @JsonProperty
    public List<String> getTags() {
        return profileConfiguration.getTags();
    }

    @JsonProperty("available")
    public boolean getAvailable() {
        return profileConfiguration.getAvailable();
    }

    public ProfileConfigurationDTO setAvailable(boolean available) {
        profileConfiguration.setAvailable(available);
        return this;
    }



    @JsonIgnore
    public ValidationProfile getValidationProfile() {
        return profileConfiguration.getValidationProfile();
    }


    @Override
    public String toString() {
        return "ProfileConfigurationDTO{" +
                "profileConfiguration=" + profileConfiguration +
                '}';
    }

    @JsonIgnore
    @Override
    public ProfileConfiguration getBusinessObject() {
        return profileConfiguration;
    }
}
