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

import java.util.List;

/**
 * Complete the sub report with FailedAssert and SuccessfulReport converted as Gazelle AssertionReport and
 * contextualized within their FiredRule. ActivePattern will be transformed as sub-SubReports as they are intented to
 * group Schematron rules, assert and reports in categories.
 *
 * @see PhSchematronFlatReportMapper
 */
public class PhSchematronTreeReportMapper implements PhSchematronReportMapper {

   private final ValidatorBuilderFactory validatorBuilderFactory;

   public PhSchematronTreeReportMapper(ValidatorBuilderFactory validatorBuilderFactory) {
      this.validatorBuilderFactory = validatorBuilderFactory;
   }

   @Override
   public void aggregateReport(ValidationSubReportBuilder subReportBuilder,
                               SchematronOutputType schematronOutput) {
      SubReportContext subReportContext = new SubReportContext(subReportBuilder, validatorBuilderFactory);
      FiredRule firedRuleContext = null;

      for (Object output : schematronOutput.getActivePatternAndFiredRuleAndFailedAssert()) {
         if (output instanceof ActivePattern activePattern) {
            subReportContext.newActivePatternSubReport(activePattern);
         } else if (output instanceof FiredRule firedRule) {
            firedRuleContext = firedRule;
         } else if (output instanceof FailedAssert failedAssert) {
            subReportContext.collectAssertionBuilder(
                  mapFailedAssert(failedAssert, firedRuleContext)
            );
         } else if (output instanceof SuccessfulReport successfulReport) {
            subReportContext.collectAssertionBuilder(
                  mapSuccessfulReport(successfulReport, firedRuleContext)
            );
         }
      }
      subReportContext.closeActivePatternSubReport();
   }

   private AssertionReportBuilder mapSuccessfulReport(SuccessfulReport successfulReport, FiredRule firedRuleContext) {
      return new AssertionReportBuilder(validatorBuilderFactory)
            .setAssertionID(successfulReport.getId())
            .setAssertionType(getTypeFromContext(firedRuleContext))
            .setDescription(
                  getDescription(
                        successfulReport.getDiagnosticReferenceOrPropertyReferenceOrText()
                  )
            )
            .setFormalExpression(successfulReport.getTest())
            .addSubjectLocation(
                    new SubjectLocationBuilder()
                            .setType(SubjectLocation.XPATH_TYPE)
                            .setValue(successfulReport.getLocation())
            )
            .setPriority(RequirementPriority.MANDATORY)
            .setResult(ValidationTestResult.PASSED);
   }

   private AssertionReportBuilder mapFailedAssert(FailedAssert failedAssert, FiredRule firedRuleContext) {
      return new AssertionReportBuilder(validatorBuilderFactory)
            .setAssertionID(failedAssert.getId())
            .setAssertionType(getTypeFromContext(firedRuleContext))
            .setDescription(getDescription(
                  failedAssert.getDiagnosticReferenceOrPropertyReferenceOrText()
            ))
            .setFormalExpression(failedAssert.getTest())
            .addSubjectLocation(
                    new SubjectLocationBuilder()
                            .setType(SubjectLocation.XPATH_TYPE)
                            .setValue(failedAssert.getLocation())
            )
            .setResult(ValidationTestResult.FAILED)
            .setPriority(getPriority(failedAssert));
   }

   private static String getTypeFromContext(FiredRule firedRuleContext) {
      return firedRuleContext != null ? "Conditional rule on `" + firedRuleContext.getContext() + "`" : null;
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
            description.append("Property: ")
                  .append(getAdditionalDescription(propertyReference.getText().getContent()));
         }
      }
      return description.toString();
   }

   private String getAdditionalDescription(List<Object> content) {
      return content.stream()
            .filter(s -> s instanceof String)
            .map(s -> (String) s)
            .reduce("\n\tAdditional description: ", String::concat);
   }

   private RequirementPriority getPriority(FailedAssert failedAssert) {
      return (failedAssert.getRole() != null && failedAssert.getRole().equals("warning")) || (hasDescriptionHasWarning(failedAssert))?
            RequirementPriority.RECOMMENDED : RequirementPriority.MANDATORY;
   }

   private boolean hasDescriptionHasWarning(FailedAssert failedAssert) {
        String description = getDescription(failedAssert.getDiagnosticReferenceOrPropertyReferenceOrText());
        return description.toLowerCase().contains("warning");
   }

   private static class SubReportContext {

      private final ValidatorBuilderFactory builderFactory;
      private final ValidationSubReportBuilder rootSubReport;
      private ValidationSubReportBuilder activePatternSubReport = null;

      SubReportContext(ValidationSubReportBuilder rootSubReportBuilder, ValidatorBuilderFactory builderFactory) {
         this.builderFactory = builderFactory;
         this.rootSubReport = rootSubReportBuilder;
      }

      void newActivePatternSubReport(ActivePattern activePattern) {
         closeActivePatternSubReport();
         activePatternSubReport = new ValidationSubReportBuilder(builderFactory)
               .setName(getPatternName(activePattern));
      }

      void collectAssertionBuilder(AssertionReportBuilder assertionBuilder) {
         if (activePatternSubReport != null) {
            activePatternSubReport.addAssertionReport(assertionBuilder);
         } else {
            rootSubReport.addAssertionReport(assertionBuilder);
         }
      }

      public void closeActivePatternSubReport() {
         if (activePatternSubReport != null && activePatternSubReport.hasContent()) {
            rootSubReport.addSubReport(activePatternSubReport);
         }
         activePatternSubReport = null;
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
}
