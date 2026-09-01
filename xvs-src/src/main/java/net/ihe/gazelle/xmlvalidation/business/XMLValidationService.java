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
package net.ihe.gazelle.xmlvalidation.business;

import net.ihe.gazelle.framework.modelvalidator.business.ValidatorBuilderFactory;
import net.ihe.gazelle.servicemetadata.api.business.MetadataService;
import net.ihe.gazelle.validation.v2.api.business.BadInputIdException;
import net.ihe.gazelle.validation.v2.api.business.ValidationService;
import net.ihe.gazelle.validation.v2.api.business.profile.ValidationProfile;
import net.ihe.gazelle.validation.v2.api.business.report.*;
import net.ihe.gazelle.validation.v2.api.business.request.ValidationRequest;
import net.ihe.gazelle.validation.v2.api.business.request.validator.ValidationRequestValidator;
import net.ihe.gazelle.xmlvalidation.business.config.ProfileConfiguration;
import net.ihe.gazelle.xmlvalidation.business.config.ProfileConfigurationService;
import net.ihe.gazelle.xmlvalidation.business.config.ValidationServiceConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;


public class XMLValidationService implements ValidationService {

    public static final String CONTEXT = "Context";

    private final ValidatorBuilderFactory validatorBuilderFactory;

   private final ProfileConfigurationService profileConfigurationService;

   private final XmlSyntaxValidator xmlSyntaxValidator;

   private final XsdValidator xsdValidator;

   private final SchematronValidator schematronValidator;

   private final MetadataService metadataService;

   private final ValidationServiceConfiguration validationServiceConfiguration;

   private final AtomicReference<List<ValidationProfile>> cachedValidationProfiles = new AtomicReference<>();
   private final AtomicReference<List<ProfileConfiguration>> cachedProfileConfigurationsRef = new AtomicReference<>();

   public XMLValidationService(ProfileConfigurationService profileConfigurationService,
                               XmlSyntaxValidator xmlSyntaxValidator,
                               XsdValidator xsdValidator,
                               SchematronValidator schematronValidator,
                               ValidatorBuilderFactory validatorBuilderFactory,
                               ValidationServiceConfiguration  validationServiceConfiguration,
                               MetadataService metadataService) {
      this.profileConfigurationService = profileConfigurationService;
      this.xsdValidator = xsdValidator;
      this.schematronValidator = schematronValidator;
      this.xmlSyntaxValidator = xmlSyntaxValidator;
      this.validatorBuilderFactory = validatorBuilderFactory;
      this.validationServiceConfiguration = validationServiceConfiguration;
      this.metadataService = metadataService;
   }


   @Override
   public ValidationReport validate(ValidationRequest validationRequest) {
      assertRequestIsValid(validationRequest);

      ProfileConfiguration profileConfiguration = profileConfigurationService.getProfileConfiguration(
            validationRequest.getValidationProfileId());

      // validate only first item
      if(validationRequest.getInputs().size() != 1) {
         throw new BadInputIdException("XMl Validation Service validates only one item at a time.");
      }
      byte[] content = validationRequest.getInputs().getFirst().getContent();

      ValidationReportBuilder reportBuilder = getValidationReportBuilder(profileConfiguration);
      ValidationSubReportBuilder xmlSubReport = xmlSyntaxValidator.validate(content);
      reportBuilder.addSubReport(xmlSubReport);

      if (isPASSED(xmlSubReport)) {
         reportBuilder.addSubReport(
               xsdValidator.validate(content, profileConfiguration)
         );
         if (isSchematronPathDefined(profileConfiguration))
             reportBuilder.addSubReport(
                   schematronValidator.validate(content, profileConfiguration)
             );
      }

      return reportBuilder.build();
   }

    @Override
    public List<ValidationProfile> getValidationProfiles() {
        List<ProfileConfiguration> profileConfigurations = profileConfigurationService.getProfileConfigurations();
        List<ProfileConfiguration> configsRef = cachedProfileConfigurationsRef.get();
        List<ValidationProfile> cached = cachedValidationProfiles.get();
        if (cached != null && configsRef == profileConfigurations) {
            return cached;
        }
        synchronized (this) {
            configsRef = cachedProfileConfigurationsRef.get();
            cached = cachedValidationProfiles.get();
            if (cached != null && configsRef == profileConfigurations) {
                return cached;
            }
            List<ValidationProfile> mappedProfiles = profileConfigurations.stream()
                    .filter(ProfileConfiguration::getAvailable)
                    .map(this::toValidationProfile)
                    .toList();
            cachedProfileConfigurationsRef.set(profileConfigurations);
            cachedValidationProfiles.set(mappedProfiles);
            return mappedProfiles;
        }
    }


    private boolean isSchematronPathDefined(ProfileConfiguration profileConfiguration) {
       return profileConfiguration.getSchematronPath() != null && !profileConfiguration.getSchematronPath().isEmpty();
   }

   private static boolean isPASSED(ValidationSubReportBuilder xmlSubReport) {
      return ValidationTestResult.PASSED.equals(xmlSubReport.build().getSubReportResult());
   }

   private void assertRequestIsValid(ValidationRequest validationRequest) {
      ValidationRequestValidator validator = new ValidationRequestValidator(validatorBuilderFactory);
      validator.assertValid(validationRequest);
   }


    private ValidationProfile toValidationProfile(ProfileConfiguration profileConfig) {
       ValidationProfile validationProfile = profileConfig.getValidationProfile();
       validationProfile.setVersion(profileConfig.getSchematronVersion());
       List<String> combinedStandards = profileConfig.getStandards() != null ? profileConfig.getStandards() : new ArrayList<>();
       validationProfile.setStandards(combinedStandards);
       List<String> coveredItems = profileConfig.getCoveredItems();
       if (coveredItems != null && !coveredItems.isEmpty()) {
          validationProfile.setCoveredItems(new ArrayList<>(coveredItems));
       }
       List<String> tags = profileConfig.getTags();
       if (tags != null && !tags.isEmpty()) {
          validationProfile.setTags(new ArrayList<>(tags));
       }
       return validationProfile;
    }

   private ValidationReportBuilder getValidationReportBuilder(ProfileConfiguration profileConfiguration) {
      return new ValidationReportBuilder(validatorBuilderFactory)
            .setDisclaimer("This report is generated by PhSchematron Web Service for testing purpose only")
            .setValidationMethod(new ValidationMethodBuilder()
                  .setValidationProfileID(profileConfiguration.getValidationProfile().getProfileID())
                  .setValidationProfileName(profileConfiguration.getValidationProfile().getProfileName())
                  .setValidationServiceName(metadataService.getMetadata().getName())
                  .setValidationServiceVersion(metadataService.getMetadata().getVersion())
                  .setValidationProfileVersion(profileConfiguration.getSchematronVersion())
            )
            .addAdditionalMetadata(getAdditionalMetadata(profileConfiguration));
   }

   private List<MetadataBuilder> getAdditionalMetadata(ProfileConfiguration profileConfiguration) {
       List<MetadataBuilder> metadataBuilders = new ArrayList<>();
       metadataBuilders.add(
               new MetadataBuilder()
                       .setName("Schematron processing engine name")
                       .setValue(validationServiceConfiguration.getSchematronEngineName())
       );
       metadataBuilders.add(
               new MetadataBuilder()
                       .setName("Schematron processing engine version")
                       .setValue(validationServiceConfiguration.getSchematronEngineVersion())
       );
       String contextValue = buildContextValue(profileConfiguration);
       if (!contextValue.isEmpty()) {
           metadataBuilders.add(
                   new MetadataBuilder()
                           .setName(CONTEXT)
                           .setValue(contextValue)
           );
       }
       return metadataBuilders;
   }

   private String buildContextValue(ProfileConfiguration profileConfiguration) {
       List<String> contextParts = new ArrayList<>();
       ValidationProfile validationProfile = profileConfiguration.getValidationProfile();
       if (validationProfile != null) {
           String domain = validationProfile.getDomain();
           if (domain != null && !domain.isEmpty()) {
               contextParts.add(domain);
           }
       }

       List<String> standards = profileConfiguration.getStandards();
       if (standards != null && !standards.isEmpty()) {
           contextParts.add(String.join(", ", standards));
       }

       List<String> coveredItems = profileConfiguration.getCoveredItems();
       if (coveredItems != null && !coveredItems.isEmpty()) {
           contextParts.add(String.join(", ", coveredItems));
       }

       return String.join(" / ", contextParts);
   }
}
