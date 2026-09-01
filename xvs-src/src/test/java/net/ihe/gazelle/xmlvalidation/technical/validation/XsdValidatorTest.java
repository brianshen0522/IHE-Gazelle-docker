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
package net.ihe.gazelle.xmlvalidation.technical.validation;

import net.ihe.gazelle.framework.modelvalidator.technical.adapter.bvalid.BValidatorBuilderFactory;
import net.ihe.gazelle.validation.v2.api.business.report.ValidationSubReport;
import net.ihe.gazelle.validation.v2.api.business.report.ValidationTestResult;
import net.ihe.gazelle.xmlvalidation.business.XsdValidator;
import net.ihe.gazelle.xmlvalidation.business.config.ProfileConfiguration;
import net.ihe.gazelle.xmlvalidation.business.config.ProfileConfigurationService;
import net.ihe.gazelle.xmlvalidation.mock.ProfileConfigurationServiceMock;
import net.ihe.gazelle.xmlvalidation.mock.XsdNamespaceServiceMock;
import net.ihe.gazelle.xmlvalidation.technical.sax.SaxXsdValidator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class XsdValidatorTest {

   private static XsdValidator xsdValidator;

   private static ProfileConfiguration profileConfiguration;

   @BeforeAll
   static void init() {
      xsdValidator = new SaxXsdValidator(new XsdNamespaceServiceMock(), new BValidatorBuilderFactory());
      ProfileConfigurationService profileConfigurationService = new ProfileConfigurationServiceMock();
      profileConfiguration = profileConfigurationService.getProfileConfigurations().getFirst();
   }

   @Test
   void testValidXSDValidation() throws IOException {
      byte[] content = getClass().getClassLoader()
            .getResourceAsStream("xml/valid01.xml")
            .readAllBytes();
      ValidationSubReport validationSubReport = xsdValidator
            .validate(content, profileConfiguration).build();
      assertEquals(ValidationTestResult.PASSED, validationSubReport.getSubReportResult());
      assertEquals(1, validationSubReport.getAssertionReports().size());
      assertEquals(ValidationTestResult.PASSED, validationSubReport.getAssertionReports().get(0).getResult());
      assertEquals("XSD schema validation", validationSubReport.getName());
      assertEquals("The document must be compliant with the XSD Schema UNKNOWN",
            validationSubReport.getAssertionReports().get(0).getDescription());
   }


   @Test
   void testInvalidXSDValidation() {
      byte[] content = "<AAA><CCC/></AAA>".getBytes();
      ValidationSubReport validationSubReport = xsdValidator
            .validate(content, profileConfiguration).build();
      assertEquals(ValidationTestResult.FAILED, validationSubReport.getSubReportResult());
      assertEquals(2, validationSubReport.getAssertionReports().size());
      assertEquals(ValidationTestResult.FAILED, validationSubReport.getAssertionReports().getFirst().getResult());
      assertEquals("XSD schema validation", validationSubReport.getName());
      assertEquals(
            "cvc-complex-type.2.4.a: Invalid content was found starting with element 'CCC'. One of '{BBB}' is " +
                  "expected.",
            validationSubReport.getAssertionReports().get(0).getDescription());
   }

   @Test
   void testEmptyDocument() {
      byte[] content = "".getBytes();
      ValidationSubReport validationSubReport = xsdValidator.validate(
            content,
            profileConfiguration
      ).build();
      assertEquals(ValidationTestResult.UNDEFINED, validationSubReport.getSubReportResult());
      assertEquals(1, validationSubReport.getUnexpectedErrors().size());
      assertEquals(
            "Unexpected error while validating against the XSD Schema: " +
                  "(line: 1, column: 1) Premature end of file.",
            validationSubReport.getUnexpectedErrors().get(0).getMessage()
      );
   }

   @Test
   void testBrokenDocument() {
      byte[] content = "<AAA><BBB/></A".getBytes();
      ValidationSubReport validationSubReport = xsdValidator.validate(
            content,
            profileConfiguration
      ).build();
      assertEquals(ValidationTestResult.UNDEFINED, validationSubReport.getSubReportResult());
      assertEquals(1, validationSubReport.getUnexpectedErrors().size());
      // That is an Unexpected Error as the XML syntax should have been validated by Well-Formed validator.
      assertEquals(
            "Unexpected error while validating against the XSD Schema: " +
                  "(line: 1, column: 14) The element type \"AAA\" must be terminated by the matching end-tag \"</AAA>\".",
            validationSubReport.getUnexpectedErrors().get(0).getMessage()
      );
   }

   @Test
   void testXSDValidationWithLocalIncludedSchema(@TempDir Path tempDir) throws IOException {
      Files.writeString(tempDir.resolve("child.xsd"), """
            <xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema">
              <xs:complexType name="childType">
                <xs:sequence>
                  <xs:element name="value" type="xs:string"/>
                </xs:sequence>
              </xs:complexType>
            </xs:schema>
            """);
      Files.writeString(tempDir.resolve("main.xsd"), """
            <xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema">
              <xs:include schemaLocation="child.xsd"/>
              <xs:element name="root" type="childType"/>
            </xs:schema>
            """);

      ProfileConfiguration configuration = new ProfileConfiguration(tempDir.toString())
            .setXsdPath("main.xsd")
            .setStandards(List.of("xsd-local-include"));
      byte[] content = "<root><value>ok</value></root>".getBytes(StandardCharsets.UTF_8);

      ValidationSubReport validationSubReport = xsdValidator.validate(content, configuration).build();

      assertEquals(ValidationTestResult.PASSED, validationSubReport.getSubReportResult());
      assertEquals(1, validationSubReport.getAssertionReports().size());
      assertEquals(ValidationTestResult.PASSED, validationSubReport.getAssertionReports().getFirst().getResult());
   }
}
