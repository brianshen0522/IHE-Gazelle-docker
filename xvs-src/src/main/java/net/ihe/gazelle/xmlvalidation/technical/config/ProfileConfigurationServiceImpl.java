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

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import net.ihe.gazelle.lang.UnexpectedInternalErrorException;
import net.ihe.gazelle.validation.v2.api.business.UnavailableValidationProfileException;
import net.ihe.gazelle.validation.v2.api.business.UnknownValidationProfileException;
import net.ihe.gazelle.xmlvalidation.business.config.ConfigurationException;
import net.ihe.gazelle.xmlvalidation.business.config.ProfileConfigurationService;
import net.ihe.gazelle.xmlvalidation.business.config.ProfileConfiguration;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ApplicationScoped
public class ProfileConfigurationServiceImpl implements ProfileConfigurationService {

   private static final Logger LOG = LoggerFactory.getLogger(ProfileConfigurationServiceImpl.class);
   private static final ObjectMapper mapper = new ObjectMapper();

   @ConfigProperty(name = "gzl.xmlvalidator.profile.index.path")
   String profileIndexPath;
   String configDirPath;

   private List<ProfileConfiguration> profileConfigurations;
   private Map<String, ErrorHandler> profileErrorHandlers;

   @PostConstruct
   @Override
   public void reset() {
      configDirPath = getDirectoryPath(profileIndexPath);
      profileConfigurations = null;
      profileErrorHandlers = new HashMap<>();
   }

   @Override
   public List<ProfileConfiguration> getProfileConfigurations() {
      if (profileConfigurations == null) {
         profileConfigurations = loadValidationProfilesIndex()
               .filter(this::isValidAndReport)
               .collect(Collectors.toList());
      }
      return profileConfigurations;
   }

   @Override
   public ProfileConfiguration getProfileConfiguration(String validationProfileId) {
      if (profileErrorHandlers.containsKey(validationProfileId)) {
         throw new UnavailableValidationProfileException(profileErrorHandlers.get(validationProfileId).toString());
      } else {
         return getProfileConfigurations().stream()
               .filter(vc -> vc.getId().equals(validationProfileId))
               .findFirst()
               .orElseThrow(() -> new UnknownValidationProfileException(validationProfileId));
      }
   }

   private Stream<ProfileConfiguration> loadValidationProfilesIndex() {
      try {
         List<ProfileConfigurationDTO> profileConfigurationDTOS = readProfileIndex(profileIndexPath);

         return profileConfigurationDTOS.stream()
               .map(dto -> dto.getBusinessObject().setRootPath(configDirPath));
      } catch (IOException | IllegalArgumentException e) {
         throw new UnexpectedInternalErrorException("Unable to load the index file", e);
      }
   }

   private List<ProfileConfigurationDTO> readProfileIndex(String path) throws IOException {
      return mapper.readValue(new File(path), mapper.getTypeFactory()
            .constructCollectionType(List.class, ProfileConfigurationDTO.class));
   }

   private boolean isValidAndReport(ProfileConfiguration profileConfiguration) {
      ErrorHandler profileErrorHandler = validate(profileConfiguration);
      if (profileErrorHandler.hasErrors()) {
         profileErrorHandlers.put(profileConfiguration.getId(), profileErrorHandler);
         LOG.error("Unable to load profile {}\n{}", profileConfiguration.getId(), profileErrorHandler);
         return false;
      } else {
         return true;
      }
   }

   private ErrorHandler validate(ProfileConfiguration profileConfiguration) {
      ErrorHandler handler = new ErrorHandler();
      handler.handle(
            () -> assertNotNull(profileConfiguration.getId(), "profileID must not be null")
      );
      handler.handle(
            () -> assertNotNull(profileConfiguration.getValidationProfile().getProfileName(),
                  "profileName must not be null")
      );
      handler.handle(
            () -> assertNotNull(profileConfiguration.getValidationProfile().getDomain(),
                  "domain should not be null")
      );
      handler.handle(
            () -> assertNotNull(profileConfiguration.getXsdPath(), "xsdPath must not be null")
      );
      handler.handle(
            () -> assertExist(profileConfiguration.getXsdAbsolutePath(),
                  "XSD file not found ")
      );
      handler.handle(
            () -> assertSchFileExistIfPathNotNull(profileConfiguration,
                  "Schematron file not found ")
      );
      return handler;
   }

   private void assertNotNull(Object object, String message) {
      if (object == null) {
         throw new ConfigurationException(message);
      }
   }

   private void assertSchFileExistIfPathNotNull(ProfileConfiguration profileConfiguration, String message) {
      if (profileConfiguration.getSchematronPath() == null || profileConfiguration.getSchematronPath().isEmpty()) {
         return; // Schematron path is not required, so we skip the existence check
      }
      assertExist(profileConfiguration.getSchematronAbsolutePath(), message);
   }

   private void assertExist(String path, String message) {
      File file = new File(path);
      if (!file.exists()) {
         throw new ConfigurationException(message + file.getAbsolutePath());
      }
   }

   private static String getDirectoryPath(String path) {
      return new File(path).getParent();
   }
}
