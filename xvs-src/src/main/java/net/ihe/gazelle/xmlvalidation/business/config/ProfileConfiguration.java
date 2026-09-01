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
package net.ihe.gazelle.xmlvalidation.business.config;



import net.ihe.gazelle.validation.v2.api.business.profile.ValidationProfile;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ProfileConfiguration {

    private ValidationProfile validationProfile;

    private String rootPath;

    private String xsdPath;

    private String schematronPath;

    private String xsltPath;

    private List<String> standards;

    private String schematronVersion;

    private String xsdVersion;

    private List<String> coveredItems;

    private List<String> tags;

    private boolean available = true;

    private boolean cacheEnabled = false;

    public ProfileConfiguration(){
        this.validationProfile = new ValidationProfile();
    }

    public ProfileConfiguration(String rootPath) {
        this();
        this.rootPath = rootPath;
    }

    public List<String> getCoveredItems() {
        return coveredItems;
    }

    public ProfileConfiguration setCoveredItems(List<String> coveredItems) {
        this.coveredItems = coveredItems;
        return this;
    }

    public List<String> getTags() {
        return tags == null ? List.of() : Collections.unmodifiableList(tags);
    }

    public ProfileConfiguration setTags(List<String> tags) {
        this.tags = tags == null ? null : new ArrayList<>(tags);
        return this;
    }

    public String getId() {
        return validationProfile.getProfileID();
    }

    public ValidationProfile getValidationProfile() {
        return validationProfile;
    }

    public ProfileConfiguration setValidationProfile(ValidationProfile validationProfile) {
        this.validationProfile = validationProfile != null ? validationProfile : new ValidationProfile();
        return this;
    }

    public String getRootPath() {
        return rootPath;
    }

    public ProfileConfiguration setRootPath(String rootPath) {
        this.rootPath = rootPath;
        return this;
    }

    public String getXsdAbsolutePath() {
        return rootPath + File.separator + xsdPath;
    }

    public String getXsdPath() {
        return xsdPath;
    }

    public ProfileConfiguration setXsdPath(String xsdPath) {
        this.xsdPath = xsdPath;
        return this;
    }

    public String getSchematronAbsolutePath() {
        return rootPath + File.separator + schematronPath;
    }

    public String getSchematronPath() {
        return schematronPath;
    }

    public ProfileConfiguration setSchematronPath(String schematronPath) {
        this.schematronPath = schematronPath;
        return this;
    }

    public String getXsltAbsolutePath() {
        return rootPath + File.separator + xsltPath;
    }

    public String getXsltPath() {
        return xsltPath;
    }

    public ProfileConfiguration setXsltPath(String xsltPath) {
        this.xsltPath = xsltPath;
        return this;
    }

    public boolean isCacheEnabled() {
        return cacheEnabled;
    }

    public List<String> getStandards() {
        return standards == null ? List.of() : Collections.unmodifiableList(standards);
    }

    public ProfileConfiguration setStandards(List<String> standards) {
        this.standards = standards == null ? null : new ArrayList<>(standards);
        return this;
    }

    public String getSchematronVersion() {
        return schematronVersion;
    }

    public ProfileConfiguration setSchematronVersion(String schematronVersion) {
        this.schematronVersion = schematronVersion;
        return this;
    }

    public String getXsdVersion() {
        return xsdVersion;
    }

    public ProfileConfiguration setXsdVersion(String xsdVersion) {
        this.xsdVersion = xsdVersion;
        return this;
    }

    public boolean getAvailable() {
        return available;
    }

    public ProfileConfiguration setAvailable(boolean available) {
        this.available = available;
        return this;
    }

    public ProfileConfiguration enableCache() {
        this.cacheEnabled = true;
        return this;
    }

    public ProfileConfiguration disableCache() {
        this.cacheEnabled = false;
        return this;
    }

    @Override
    public String toString() {
        return "ProfileConfiguration{" +
                "xsdPath='" + xsdPath + '\'' +
                ", schematronPath='" + schematronPath + '\'' +
                ", xsltPath='" + xsltPath + '\'' +
                ", schematronVersion='" + schematronVersion + '\'' +
                ", xsdVersion='" + xsdVersion + '\'' +
                ", cacheEnabled='" + cacheEnabled + '\'' +
                ", validationProfile{profileID='" + validationProfile.getProfileID() + '\'' +
                                    ", profileName='" + validationProfile.getProfileName() + '\'' +
                                    ", domain='" + validationProfile.getDomain() + '\'' +
                                    '}' +
                '}';
    }
}
