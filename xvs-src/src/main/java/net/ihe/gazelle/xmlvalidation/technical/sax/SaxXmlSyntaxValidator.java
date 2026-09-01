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
import net.ihe.gazelle.framework.modelvalidator.business.ValidatorBuilderFactory;
import net.ihe.gazelle.framework.modelvalidator.technical.adapter.bvalid.BValidatorBuilderFactory;
import net.ihe.gazelle.validation.v2.api.business.report.*;
import net.ihe.gazelle.xmlvalidation.business.XmlSyntaxValidator;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;


@RequestScoped
public class SaxXmlSyntaxValidator implements XmlSyntaxValidator {

   public static final String ASSERTION_TYPE = "XML syntax";
   public static final String ASSERTION_DESC = "The document must be a well-formed XML.";
   private final ValidatorBuilderFactory validatorBuilderFactory;

   public SaxXmlSyntaxValidator() {
      this.validatorBuilderFactory = new BValidatorBuilderFactory();
   }

   @Override
   public ValidationSubReportBuilder validate(byte[] content) {

      ValidationSubReportBuilder xmlSubReportBuilder = new ValidationSubReportBuilder(validatorBuilderFactory)
            .setName("XML syntax validation")
            .setStandards(List.of("XML 1.0"));
      try {
         XMLAssertionErrorHandler errorHandler = new XMLAssertionErrorHandler(validatorBuilderFactory,
               ASSERTION_TYPE);
         DocumentBuilder builder = getDocumentBuilder();
         builder.setErrorHandler(errorHandler);
         builder.parse(new ByteArrayInputStream(content));
         if (errorHandler.getErrors().isEmpty()) {
            return xmlSubReportBuilder.addAssertionReport(
                  getAssertionReportBuilder().setResult(ValidationTestResult.PASSED)
            );
         }
         return xmlSubReportBuilder.setAssertionReports(errorHandler.getErrors());
      } catch (SAXException e) {
         return xmlSubReportBuilder.addAssertionReport(
               getAssertionReportBuilder()
                     .setDescription(
                           ASSERTION_DESC + System.lineSeparator() + "The following error has been detected: " + e.getMessage()
                     )
                     .setResult(ValidationTestResult.FAILED)
         );
      } catch (ParserConfigurationException | IOException e) {
         return xmlSubReportBuilder.addUnexpectedError(new UnexpectedErrorBuilder().fromException(
               new XmlSyntaxValidationException("Error while verifying XML syntax", e)
         ));
      }
   }

   private static DocumentBuilder getDocumentBuilder() throws ParserConfigurationException {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      // Disable access to external entities in XML parsing.
      factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
      factory.setValidating(false);
      return factory.newDocumentBuilder();
   }

   private AssertionReportBuilder getAssertionReportBuilder() {
      return new AssertionReportBuilder(validatorBuilderFactory)
            .setAssertionType(ASSERTION_TYPE)
            .setDescription(ASSERTION_DESC)
            .setPriority(RequirementPriority.MANDATORY);
   }
}
