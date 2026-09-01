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
package net.ihe.gazelle.xmlvalidation.technical.sax;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import net.ihe.gazelle.framework.modelvalidator.business.ValidatorBuilderFactory;
import net.ihe.gazelle.validation.v2.api.business.report.*;
import net.ihe.gazelle.xmlvalidation.business.XsdValidator;
import net.ihe.gazelle.xmlvalidation.business.config.ProfileConfiguration;
import net.ihe.gazelle.xmlvalidation.technical.service.XsdNamespaceService;
import net.ihe.gazelle.xmlvalidation.technical.xml.XmlFactorySecurityConfigurer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

@RequestScoped
public class SaxXsdValidator implements XsdValidator {

   public static final String ASSERTION_TYPE = "Schema validation";
   private final ValidatorBuilderFactory validatorBuilderFactory;
    private static final String DESCRIPTION = "The document must be compliant with the XSD Schema ";

    private static final Logger LOG = LoggerFactory.getLogger(SaxXsdValidator.class);

    @Inject
    XsdNamespaceService xsdNamespaceService;

   public SaxXsdValidator(XsdNamespaceService xsdNamespaceService, ValidatorBuilderFactory validatorBuilderFactory) {
      this.validatorBuilderFactory = validatorBuilderFactory;
      this.xsdNamespaceService = xsdNamespaceService;
   }

   @Override
   public ValidationSubReportBuilder validate(byte[] content, ProfileConfiguration profileConfiguration) {

       XMLAssertionErrorHandler errorHandler = new XMLAssertionErrorHandler(validatorBuilderFactory, ASSERTION_TYPE);
      Validator validator;
      try {
         validator = newXSDValidator(profileConfiguration.getXsdAbsolutePath(), errorHandler);
      } catch (SAXException e) {
          LOG.error("Error while loading XSD Schema from path: {}", profileConfiguration.getXsdAbsolutePath(), e);
         return initSubReportBuilder(profileConfiguration).addUnexpectedError(new UnexpectedErrorBuilder().fromException(
               new XSDValidationException("Error while loading XSD Schema", e)
         ));
      }
      try {
         validator.validate(new StreamSource(new ByteArrayInputStream(content)));
         String targetNamespace = xsdNamespaceService.getTargetNamespace(profileConfiguration);
         if (errorHandler.getErrors().isEmpty()) {
             return initSubReportBuilder(profileConfiguration, targetNamespace);
         }
         return initSubReportBuilder(profileConfiguration).setAssertionReports(errorHandler.getErrors()).addAssertionReport(getAssertionReport(targetNamespace));
      } catch(SAXParseException e) {
         return initSubReportBuilder(profileConfiguration).addUnexpectedError(new UnexpectedErrorBuilder().fromException(
               new XSDValidationException("Unexpected error while validating against the XSD Schema: " + saxParseMessage(e))
         ));
      } catch (SAXException | IOException e) {
         return initSubReportBuilder(profileConfiguration).addUnexpectedError(new UnexpectedErrorBuilder().fromException(
               new XSDValidationException("Unexpected error while validating against the XSD Schema", e)
         ));
      }
   }

    private AssertionReportBuilder getAssertionReport(String targetNamespace) {
        return new AssertionReportBuilder(validatorBuilderFactory)
                .setDescription(DESCRIPTION + targetNamespace)
                .setAssertionType(ASSERTION_TYPE)
                .setResult(ValidationTestResult.PASSED)
                .setPriority(RequirementPriority.MANDATORY);
    }

    private String saxParseMessage(SAXParseException e) {
      return "(line: " + e.getLineNumber() + ", column: " + e.getColumnNumber() + ") " + e.getMessage();
   }

   private static Validator newXSDValidator(String xsdPath, XMLAssertionErrorHandler errorHandler) throws SAXException {
      SchemaFactory factory = XmlFactorySecurityConfigurer.newSecureSchemaFactory(XMLConstants.W3C_XML_SCHEMA_NS_URI, LOG);
      // REVIEW: This feature, after changing 'http' with 'https' in xsd import, is blocking the validation xsd if there is declaration DOCTYPE
      //factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
      Schema schema = factory.newSchema(new File(xsdPath));
      Validator validator = schema.newValidator();
      validator.setErrorHandler(errorHandler);
      return validator;
   }

   private ValidationSubReportBuilder initSubReportBuilder(ProfileConfiguration profileConfiguration) {
      return new ValidationSubReportBuilder(validatorBuilderFactory)
            .setName("XSD schema validation")
              .setStandards(profileConfiguration.getStandards());
   }

    private ValidationSubReportBuilder initSubReportBuilder(ProfileConfiguration profileConfiguration, String targetNamespace) {
        return initSubReportBuilder(profileConfiguration)
                .addAssertionReport(getAssertionReport(targetNamespace));
    }
}
