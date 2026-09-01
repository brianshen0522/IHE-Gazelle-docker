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
package net.ihe.gazelle.xmlvalidation.technical.phschematron;

import com.helger.schematron.svrl.jaxb.ActivePattern;
import com.helger.schematron.svrl.jaxb.DiagnosticReference;
import com.helger.schematron.svrl.jaxb.FailedAssert;
import com.helger.schematron.svrl.jaxb.FiredRule;
import com.helger.schematron.svrl.jaxb.PropertyReference;
import com.helger.schematron.svrl.jaxb.SchematronOutputType;
import com.helger.schematron.svrl.jaxb.SuccessfulReport;
import com.helger.schematron.svrl.jaxb.Text;
import net.ihe.gazelle.framework.modelvalidator.technical.adapter.bvalid.BValidatorBuilderFactory;
import net.ihe.gazelle.validation.v2.api.business.report.AssertionReport;
import net.ihe.gazelle.validation.v2.api.business.report.RequirementPriority;
import net.ihe.gazelle.validation.v2.api.business.report.ValidationSubReport;
import net.ihe.gazelle.validation.v2.api.business.report.ValidationSubReportBuilder;
import net.ihe.gazelle.validation.v2.api.business.report.ValidationTestResult;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PhSchematronFlatReportMapperTest {

    @Test
    void aggregateReportMapsSchematronOutputToAssertions() {
        SchematronOutputType output = new SchematronOutputType();

        ActivePattern activePattern = new ActivePattern();
        activePattern.setId("ap-1");
        activePattern.setName("pattern-name");
        Text activeText = new Text();
        activeText.getContent().add("pattern detail");
        activePattern.setText(activeText);
        output.getActivePatternAndFiredRuleAndFailedAssert().add(activePattern);

        FiredRule firedRule = new FiredRule();
        firedRule.setId("fr-1");
        firedRule.setContext("/ClinicalDocument");
        output.getActivePatternAndFiredRuleAndFailedAssert().add(firedRule);

        FailedAssert failedAssert = new FailedAssert();
        failedAssert.setId("fa-1");
        failedAssert.setLocation("/a/b");
        failedAssert.setRole("warning");
        Text failedText = new Text();
        failedText.getContent().add("failed text");
        failedAssert.getDiagnosticReferenceOrPropertyReferenceOrText().add(failedText);
        DiagnosticReference diagnosticReference = new DiagnosticReference();
        diagnosticReference.setDiagnostic("diag-code");
        diagnosticReference.getContent().add("diag description");
        failedAssert.getDiagnosticReferenceOrPropertyReferenceOrText().add(diagnosticReference);
        PropertyReference propertyReference = new PropertyReference();
        Text propertyText = new Text();
        propertyText.getContent().add("prop value");
        propertyReference.setText(propertyText);
        failedAssert.getDiagnosticReferenceOrPropertyReferenceOrText().add(propertyReference);
        output.getActivePatternAndFiredRuleAndFailedAssert().add(failedAssert);

        SuccessfulReport successfulReport = new SuccessfulReport();
        successfulReport.setId("sr-1");
        Text successText = new Text();
        successText.getContent().add("ok");
        successfulReport.getDiagnosticReferenceOrPropertyReferenceOrText().add(successText);
        output.getActivePatternAndFiredRuleAndFailedAssert().add(successfulReport);

        SuccessfulReport ignoredSuccessfulReport = new SuccessfulReport();
        output.getActivePatternAndFiredRuleAndFailedAssert().add(ignoredSuccessfulReport);

        PhSchematronFlatReportMapper mapper = new PhSchematronFlatReportMapper(new BValidatorBuilderFactory());
        ValidationSubReportBuilder subReportBuilder = new ValidationSubReportBuilder(new BValidatorBuilderFactory())
                .setName("root");
        mapper.aggregateReport(subReportBuilder, output);

        ValidationSubReport report = subReportBuilder.build();
        List<AssertionReport> assertions = report.getAssertionReports();

        assertEquals(4, assertions.size());

        Map<String, AssertionReport> byId = assertions.stream()
                .filter(a -> a.getAssertionID() != null)
                .collect(Collectors.toMap(AssertionReport::getAssertionID, Function.identity()));

        AssertionReport active = byId.get("ap-1");
        assertNotNull(active);
        assertEquals(ValidationTestResult.PASSED, active.getResult());
        assertEquals(RequirementPriority.MANDATORY, active.getPriority());
        assertTrue(active.getDescription().contains("Active pattern: pattern-name (ap-1)"));

        AssertionReport fired = byId.get("fr-1");
        assertNotNull(fired);
        assertEquals(ValidationTestResult.PASSED, fired.getResult());
        assertEquals(RequirementPriority.PERMITTED, fired.getPriority());
        assertEquals("/ClinicalDocument", fired.getDescription());

        AssertionReport failed = byId.get("fa-1");
        assertNotNull(failed);
        assertEquals(ValidationTestResult.FAILED, failed.getResult());
        assertEquals(RequirementPriority.RECOMMENDED, failed.getPriority());
        assertTrue(failed.getDescription().contains("Diagnostic: diag-code"));
        assertTrue(failed.getDescription().contains("Diagnostic description:"));
        assertTrue(failed.getDescription().contains("Property:"));

        AssertionReport success = byId.get("sr-1");
        assertNotNull(success);
        assertEquals(ValidationTestResult.PASSED, success.getResult());
        assertEquals(RequirementPriority.MANDATORY, success.getPriority());
        assertTrue(success.getDescription().contains("Additional description:"));
    }
}
