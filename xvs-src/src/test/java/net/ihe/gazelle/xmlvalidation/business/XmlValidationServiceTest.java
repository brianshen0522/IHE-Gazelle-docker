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
import net.ihe.gazelle.framework.modelvalidator.technical.adapter.bvalid.BValidatorBuilderFactory;
import net.ihe.gazelle.modelmarshaller.technical.jackson.JacksonSerDes;
import net.ihe.gazelle.modelmarshaller.technical.jackson.ObjectMapperBuilder;
import net.ihe.gazelle.servicemetadata.api.business.MetadataService;
import net.ihe.gazelle.validation.v2.api.business.UnknownValidationProfileException;
import net.ihe.gazelle.validation.v2.api.business.ValidationService;
import net.ihe.gazelle.validation.v2.api.business.report.ValidationReport;
import net.ihe.gazelle.validation.v2.api.business.report.ValidationTestResult;
import net.ihe.gazelle.validation.v2.api.business.request.InputInRequestBuilder;
import net.ihe.gazelle.validation.v2.api.business.request.ValidationRequest;
import net.ihe.gazelle.validation.v2.api.business.request.ValidationRequestBuilder;
import net.ihe.gazelle.validation.v2.api.technical.dto.report.ValidationReportDTO;
import net.ihe.gazelle.xmlvalidation.business.config.ProfileConfigurationService;
import net.ihe.gazelle.xmlvalidation.business.config.ValidationServiceConfiguration;
import net.ihe.gazelle.xmlvalidation.mock.ValidationServiceConfigurationMock;
import net.ihe.gazelle.xmlvalidation.mock.XMLMetadataServiceMock;
import net.ihe.gazelle.xmlvalidation.mock.XsdNamespaceServiceMock;
import net.ihe.gazelle.xmlvalidation.technical.phschematron.PhSchematronValidator;
import net.ihe.gazelle.xmlvalidation.technical.sax.SaxXmlSyntaxValidator;
import net.ihe.gazelle.xmlvalidation.technical.sax.SaxXsdValidator;
import net.ihe.gazelle.xmlvalidation.mock.ProfileConfigurationServiceMock;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class XmlValidationServiceTest {

    private static ValidatorBuilderFactory validatorBuilderFactory;

    private static ValidationService validationService;


    @BeforeAll
    static void init() {
        validatorBuilderFactory = new BValidatorBuilderFactory();
        MetadataService metadataServiceMock = new XMLMetadataServiceMock();
        ValidationServiceConfiguration  validationServiceConfigurationMock = new ValidationServiceConfigurationMock();
        ProfileConfigurationService profileConfigurationService = new ProfileConfigurationServiceMock();
        XmlSyntaxValidator xmlSyntaxValidator = new SaxXmlSyntaxValidator();
        XsdValidator xsdValidator = new SaxXsdValidator(new XsdNamespaceServiceMock(), new BValidatorBuilderFactory());
        SchematronValidator schematronValidator = new PhSchematronValidator(
              new BValidatorBuilderFactory());
        validationService = new XMLValidationService(
              profileConfigurationService, //metadataService,
              xmlSyntaxValidator,
              xsdValidator, schematronValidator,
              validatorBuilderFactory,
                validationServiceConfigurationMock,
                metadataServiceMock

        );
    }

    @Test
    void testValidateValid() throws IOException {
        byte[] content = getClass().getClassLoader()
                .getResourceAsStream("xml/valid01ForSch.xml")
                .readAllBytes();
        ValidationReport validationReport = validationService.validate(constructValidationRequest("valid01", content));

        JacksonSerDes structureMapper = new JacksonSerDes(
                new ObjectMapperBuilder().build()
        );
        System.out.println(structureMapper.serializeAsString(new ValidationReportDTO(validationReport)));

        assertEquals(ValidationTestResult.PASSED, validationReport.getOverallResult());
        assertEquals(3, validationReport.getReports().size());
        assertEquals(ValidationTestResult.PASSED, validationReport.getReports().get(0).getSubReportResult());
        assertEquals("XML syntax validation", validationReport.getReports().get(0).getName());
        assertEquals(1, validationReport.getReports().getFirst().getAssertionReports().size());
        assertEquals(ValidationTestResult.PASSED, validationReport.getReports().get(0).getAssertionReports().get(0).getResult());
        assertEquals("The document must be a well-formed XML.",
                validationReport.getReports().get(0).getAssertionReports().get(0).getDescription());
        assertEquals(ValidationTestResult.PASSED, validationReport.getReports().get(1).getSubReportResult());
        assertEquals("XSD schema validation", validationReport.getReports().get(1).getName());
        assertEquals(1, validationReport.getReports().get(1).getAssertionReports().size());
        assertEquals(ValidationTestResult.PASSED, validationReport.getReports().get(1).getAssertionReports().get(0).getResult());
        assertEquals("The document must be compliant with the XSD Schema UNKNOWN",
                validationReport.getReports().get(1).getAssertionReports().get(0).getDescription());
        assertEquals(ValidationTestResult.PASSED, validationReport.getReports().get(2).getSubReportResult());
        assertEquals("Schematron Validation", validationReport.getReports().get(2).getName());
        assertEquals(4, validationReport.getReports().get(2).getSubCounters().getNumberOfAssertions());

        assertEquals(3, validationReport.getAdditionalMetadata().size());
        assertEquals("Context", validationReport.getAdditionalMetadata().get(2).getName());
        assertEquals("domain1 / xsd1, xsd2", validationReport.getAdditionalMetadata().get(2).getValue());

    }

    @Test
    void testValidateInvalidSch() throws IOException {
        byte[] content = getClass().getClassLoader()
                .getResourceAsStream("xml/valid01.xml")
                .readAllBytes();
        Throwable throwable = assertThrows(UnknownValidationProfileException.class, ()-> validationService
                .validate(constructValidationRequest("notfound", content)));
        assertEquals("Unknown validation profile: notfound", throwable.getMessage());

    }

    @Test
    void testValidateValidWithoutSchFile() throws IOException {
        byte[] content = getClass().getClassLoader()
                .getResourceAsStream("xml/valid01.xml")
                .readAllBytes();
        ValidationReport validationReport = validationService.validate(constructValidationRequest("validWithoutSchFile02", content));

        assertEquals(ValidationTestResult.PASSED, validationReport.getOverallResult());
        assertEquals(2, validationReport.getReports().size());
        assertEquals(ValidationTestResult.PASSED, validationReport.getReports().get(0).getSubReportResult());
        assertEquals("XML syntax validation", validationReport.getReports().get(0).getName());
        assertEquals(1, validationReport.getReports().get(0).getAssertionReports().size());
        assertEquals(ValidationTestResult.PASSED, validationReport.getReports().get(0).getAssertionReports().get(0).getResult());
        assertEquals("The document must be a well-formed XML.",
                validationReport.getReports().get(0).getAssertionReports().get(0).getDescription());
        assertEquals(ValidationTestResult.PASSED, validationReport.getReports().get(1).getSubReportResult());
        assertEquals("XSD schema validation", validationReport.getReports().get(1).getName());
        assertEquals(1, validationReport.getReports().get(1).getAssertionReports().size());
        assertEquals(ValidationTestResult.PASSED, validationReport.getReports().get(1).getAssertionReports().get(0).getResult());
        assertEquals("The document must be compliant with the XSD Schema UNKNOWN",
                validationReport.getReports().get(1).getAssertionReports().get(0).getDescription());
    }

    @Test
    void testValidateInvalidContent(){
        byte[] invalidContent = new byte[5];
        ValidationReport validationReport = validationService.validate(constructValidationRequest("valid01", invalidContent));
        assertEquals(ValidationTestResult.FAILED, validationReport.getOverallResult());
    }

    private ValidationRequest constructValidationRequest(String profileId, byte[] content) {
        return new ValidationRequestBuilder(validatorBuilderFactory)
                .setValidationProfileId(profileId)
                .addInput(new InputInRequestBuilder(validatorBuilderFactory)
                        .setItemId("item1")
                        .setContent(content)
                )
                .build();
    }
}
