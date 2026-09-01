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
package net.ihe.gazelle.xmlvalidation.mock;

import net.ihe.gazelle.framework.modelvalidator.technical.adapter.bvalid.BValidatorBuilderFactory;

import net.ihe.gazelle.xmlvalidation.business.config.ProfileConfiguration;
import net.ihe.gazelle.xmlvalidation.business.config.ProfileConfigurationService;
import net.ihe.gazelle.validation.v2.api.business.UnknownValidationProfileException;
import net.ihe.gazelle.validation.v2.api.business.profile.ValidationProfileBuilder;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

public class ProfileConfigurationServiceMock implements ProfileConfigurationService {

   private final String tempXsltPath;

   public ProfileConfigurationServiceMock() {
      try {
         tempXsltPath = getTempPath(".xslt");
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

   @Override
   public List<ProfileConfiguration> getProfileConfigurations() {
      return List.of(
            new ProfileConfiguration()
                  .setRootPath(getRootPath())
                  .setSchematronPath("/profiles/domain1/profile1/valid01.sch")
                  .setXsdPath("/profiles/domain1/schema.xsd")
                  .setXsltPath(tempXsltPath)
                  .setValidationProfile(
                        new ValidationProfileBuilder(new BValidatorBuilderFactory())
                              .setProfileID("valid01")
                              .setProfileName("profile1")
                              .setDomain("domain1")
                              .build()
                  )
                    .setSchematronVersion("1.0")
                    .setStandards(List.of("xsd1", "xsd2")),
            new ProfileConfiguration()
                    .setRootPath(getRootPath())
                    .setXsdPath("/profiles/domain1/schema.xsd")
                    .setXsltPath(tempXsltPath)
                    .setValidationProfile(
                            new ValidationProfileBuilder(new BValidatorBuilderFactory())
                                .setProfileID("validWithoutSchFile02")
                                .setProfileName("profile2")
                                .setDomain("domain2")
                                .build()
                    )
                        .setSchematronVersion("2.0")
                        .setStandards(List.of("xsd3"))
      );
   }

   String getRootPath() {
      try {
         URL url = this.getClass().getResource("/index.json");
         return new File(url.toURI()).getParent();
      } catch (URISyntaxException e) {
         throw new RuntimeException(e);
      }
   }

   @Override
   public ProfileConfiguration getProfileConfiguration(String validationProfileId) {
      return getProfileConfigurations().stream()
            .filter(profile -> validationProfileId.equals(profile.getId()))
            .findFirst()
            .orElseThrow(() -> new UnknownValidationProfileException(validationProfileId));
   }

   @Override
   public void reset() {
        // Nothing to reset in the mock
   }

   private String getTempPath(String extension) throws IOException {
      return "/tmp" + System.currentTimeMillis() + extension;

   }
}
