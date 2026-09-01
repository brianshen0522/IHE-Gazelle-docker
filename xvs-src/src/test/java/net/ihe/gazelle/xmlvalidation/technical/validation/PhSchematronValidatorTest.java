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
import net.ihe.gazelle.xmlvalidation.business.config.ProfileConfiguration;
import net.ihe.gazelle.xmlvalidation.business.SchematronValidator;
import net.ihe.gazelle.xmlvalidation.mock.ProfileConfigurationServiceMock;
import net.ihe.gazelle.xmlvalidation.technical.phschematron.PhSchematronValidator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PhSchematronValidatorTest {

    protected static SchematronValidator schematronValidator;

    private static ProfileConfiguration profileConfiguration;

    @BeforeAll
    public static void setUp() {
        profileConfiguration = new ProfileConfigurationServiceMock().getProfileConfigurations().get(0);
    }

    protected SchematronValidator getPhSchematronService() {
        if(schematronValidator == null) {
            schematronValidator = new PhSchematronValidator(new BValidatorBuilderFactory());
        }
        return schematronValidator;
    }

    public static ProfileConfiguration getProfileConfiguration() {
        return profileConfiguration;
    }

    @Test
    public void testValidate() throws IOException {
        byte[] content = getClass().getClassLoader()
                .getResourceAsStream("xml/valid01ForSch.xml")
                .readAllBytes();
        ValidationSubReport validationSubReport = getPhSchematronService().validate(content, profileConfiguration).build();
        assertEquals(ValidationTestResult.PASSED, validationSubReport.getSubReportResult());
        assertEquals(4, validationSubReport.getSubCounters().getNumberOfAssertions());
        assertEquals(0, validationSubReport.getSubCounters().getNumberOfFailedWithWarnings());
        assertEquals(0, validationSubReport.getSubCounters().getNumberOfFailedWithInfos());
        assertEquals("Schematron Validation", validationSubReport.getName());
    }

    @Test
    public void testValidateWithInvalidDocument() throws IOException {
        byte[] content = getClass().getClassLoader()
                .getResourceAsStream("xml/invalid01ForSch.xml")
                .readAllBytes();
        ValidationSubReport validationSubReport = getPhSchematronService().validate(content, profileConfiguration).build();
        assertEquals(ValidationTestResult.FAILED, validationSubReport.getSubReportResult());
        assertEquals("Schematron Validation", validationSubReport.getName());
        assertEquals(4, validationSubReport.getSubCounters().getNumberOfAssertions());
        assertEquals(2, validationSubReport.getSubCounters().getNumberOfFailedWithErrors());
    }
}
