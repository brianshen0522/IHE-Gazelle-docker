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

import net.ihe.gazelle.validation.v2.api.business.report.ValidationSubReport;
import net.ihe.gazelle.validation.v2.api.business.report.ValidationTestResult;
import net.ihe.gazelle.xmlvalidation.business.XmlSyntaxValidator;
import net.ihe.gazelle.xmlvalidation.technical.sax.SaxXmlSyntaxValidator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class WellFormedValidationTest {

    private static XmlSyntaxValidator xmlSyntaxValidator;

    @BeforeAll
    public static void init() {
        xmlSyntaxValidator = new SaxXmlSyntaxValidator();
    }


    @Test
    public void testWellFormedValidation() {
        ValidationSubReport validationSubReport = xmlSyntaxValidator.validate("<a><b></b></a>".getBytes(StandardCharsets.UTF_8)).build();
        assertEquals(ValidationTestResult.PASSED, validationSubReport.getSubReportResult());
        assertEquals(1, validationSubReport.getAssertionReports().size());
        assertEquals(ValidationTestResult.PASSED, validationSubReport.getAssertionReports().get(0).getResult());
        assertEquals("The document must be a well-formed XML.",
                validationSubReport.getAssertionReports().get(0).getDescription());
        assertEquals("XML syntax", validationSubReport.getAssertionReports().get(0).getAssertionType());
    }

    @Test
    public void testNotWellFormedValidation() {
        ValidationSubReport validationSubReport = xmlSyntaxValidator.validate("<a><b></a>".getBytes(StandardCharsets.UTF_8)).build();
        assertEquals(ValidationTestResult.FAILED, validationSubReport.getSubReportResult());
        assertEquals(1, validationSubReport.getAssertionReports().size());
        assertEquals(ValidationTestResult.FAILED, validationSubReport.getAssertionReports().get(0).getResult());
        assertThat(
              validationSubReport.getAssertionReports().get(0).getDescription(),
              containsString("The document must be a well-formed XML.")
        );
    }

    @Test
    public void testEmptyDocument(){
        ValidationSubReport validationSubReport = xmlSyntaxValidator.validate("".getBytes(StandardCharsets.UTF_8)).build();
        assertEquals(ValidationTestResult.FAILED, validationSubReport.getSubReportResult());
        assertEquals(1, validationSubReport.getAssertionReports().size());
        assertEquals(ValidationTestResult.FAILED, validationSubReport.getAssertionReports().get(0).getResult());
        assertThat(
              validationSubReport.getAssertionReports().get(0).getDescription(),
              containsString("The document must be a well-formed XML.")
        );
    }
}
