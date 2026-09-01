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
import net.ihe.gazelle.framework.modelvalidator.business.ValidatorBuilderFactory;
import net.ihe.gazelle.validation.v2.api.business.report.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Complete the sub report with all ActivePatterns, FireRules, FailedAssert and SuccessfulReport converted as Gazelle
 * AssertionReport and aggregated at the same root level of SubReport.
 * <p>
 * That technic inherently make the number of assertions growing for every ActivePattern and every FiredRules, even
 * thought they are not technically speaking "assertions".
 *
 * @see PhSchematronTreeReportMapper
 */
public class PhSchematronFlatReportMapper implements PhSchematronReportMapper {

   private final ValidatorBuilderFactory validatorBuilderFactory;

   public PhSchematronFlatReportMapper(ValidatorBuilderFactory validatorBuilderFactory) {
      this.validatorBuilderFactory = validatorBuilderFactory;
   }

   @Override
   public void aggregateReport(ValidationSubReportBuilder subReportBuilder, SchematronOutputType schematronOutput) {
      subReportBuilder.setAssertionReports(asAssertionReports(schematronOutput));
   }

   private List<AssertionReportBuilder> asAssertionReports(SchematronOutputType schematronOutputType) {
      List<AssertionReportBuilder> assertionReportObjectBuilders = new ArrayList<>();
      for (Object assertion : schematronOutputType.getActivePatternAndFiredRuleAndFailedAssert()) {
         if (assertion instanceof FailedAssert failedAssert) {
            assertionReportObjectBuilders.add(mapFailedAssert(failedAssert));
         } else if (assertion instanceof FiredRule firedRule) {
            assertionReportObjectBuilders.add(mapFiredRule(firedRule));
         } else if (assertion instanceof ActivePattern activePattern) {
            assertionReportObjectBuilders.add(mapActivePattern(activePattern));
         } else if (assertion instanceof SuccessfulReport successfulReport && successfulReport.getId() != null) {
            assertionReportObjectBuilders.add(mapSuccessfulReport(successfulReport));
         }
      }
      return assertionReportObjectBuilders;

   }

   private AssertionReportBuilder mapSuccessfulReport(SuccessfulReport successfulReport) {
      return new AssertionReportBuilder(validatorBuilderFactory).setAssertionID(successfulReport.getId())
            .setDescription(getDescription(successfulReport.getDiagnosticReferenceOrPropertyReferenceOrText()))
            .setPriority(RequirementPriority.MANDATORY).setResult(ValidationTestResult.PASSED);
   }

   private AssertionReportBuilder mapActivePattern(ActivePattern activePattern) {
      return new AssertionReportBuilder(validatorBuilderFactory)
            .setAssertionID(activePattern.getId())
            .setDescription(
                  getPatternName(activePattern) +
                        (activePattern.getText() != null ?
                              " " + getAdditionalDescription(activePattern.getText().getContent()) :
                              "")
            )
            .setPriority(RequirementPriority.MANDATORY)
            .setResult(ValidationTestResult.PASSED);
   }

   private AssertionReportBuilder mapFiredRule(FiredRule firedRule) {
      return new AssertionReportBuilder(validatorBuilderFactory)
            .setAssertionID(firedRule.getId())
            .setDescription(firedRule.getContext())
            .setPriority(RequirementPriority.PERMITTED)
            .setResult(ValidationTestResult.PASSED);
   }

   private AssertionReportBuilder mapFailedAssert(FailedAssert failedAssert) {
      return new AssertionReportBuilder(validatorBuilderFactory).setAssertionID(failedAssert.getId())
            .setDescription(getDescription(failedAssert.getDiagnosticReferenceOrPropertyReferenceOrText()))
            .addSubjectLocation(new SubjectLocationBuilder(validatorBuilderFactory)
                  .setType(SubjectLocation.XPATH_TYPE)
                  .setValue(failedAssert.getLocation()))
            .setResult(ValidationTestResult.FAILED)
            .setPriority(getPriority(failedAssert));
   }

   private static RequirementPriority getPriority(FailedAssert failedAssert) {
      return (failedAssert.getRole() != null && failedAssert.getRole()
            .equals("warning")) ? RequirementPriority.RECOMMENDED : RequirementPriority.MANDATORY;
   }

   private String getDescription(List<Object> diagnosticReferenceOrPropertyReferenceOrText) {
      StringBuilder description = new StringBuilder();
      for (Object object : diagnosticReferenceOrPropertyReferenceOrText) {
         if (object instanceof Text text) {
            description.append(getAdditionalDescription(text.getContent()));
         }
         if (object instanceof DiagnosticReference diagnosticReference) {
            if (diagnosticReference.getDiagnostic() != null) {
               description.append("\nDiagnostic: ").append(diagnosticReference.getDiagnostic());
            }
            description.append("\nDiagnostic description: ")
                  .append(getAdditionalDescription(diagnosticReference.getContent()));
         }
         if (object instanceof PropertyReference propertyReference && propertyReference.getText() != null) {
            description.append("Property: ").append(getAdditionalDescription(propertyReference.getText().getContent()));
         }
      }
      return description.toString();
   }

   private String getAdditionalDescription(List<Object> content) {
      return content.stream().filter(s -> s instanceof String).map(s -> (String) s)
            .reduce("\n\tAdditional description: ", String::concat);
   }

   private String getPatternName(ActivePattern activePattern) {
      StringBuilder name = new StringBuilder("Active pattern");
      if (activePattern.getName() != null) {
         name.append(": " + activePattern.getName());
      }
      if (activePattern.getId() != null) {
         name.append(" (" + activePattern.getId() + ")");
      }
      return name.toString();
   }

}
