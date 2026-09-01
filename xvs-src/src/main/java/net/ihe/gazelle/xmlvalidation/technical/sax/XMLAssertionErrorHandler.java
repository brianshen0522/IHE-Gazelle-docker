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

import net.ihe.gazelle.framework.modelvalidator.business.ValidatorBuilderFactory;
import net.ihe.gazelle.validation.v2.api.business.report.*;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

import java.util.ArrayList;
import java.util.List;

class XMLAssertionErrorHandler implements ErrorHandler {

    private final ValidatorBuilderFactory validatorBuilderFactory;
    private final String assertionType;

    XMLAssertionErrorHandler(ValidatorBuilderFactory validatorBuilderFactory, String assertionType) {
        this.validatorBuilderFactory = validatorBuilderFactory;
       this.assertionType = assertionType;
    }

    private final List<AssertionReportBuilder> errors = new ArrayList<>();

    List<AssertionReportBuilder> getErrors() {
        return errors;
    }

    @Override
    public void warning(SAXParseException exception) {
        errors.add(buildAssertionReport(exception, RequirementPriority.RECOMMENDED));
    }

    @Override
    public void error(SAXParseException exception) {
        errors.add(buildAssertionReport(exception, RequirementPriority.MANDATORY));
    }

    @Override
    public void fatalError(SAXParseException exception) {
        errors.add(buildAssertionReport(exception, RequirementPriority.MANDATORY));
    }

    private AssertionReportBuilder buildAssertionReport(SAXParseException exception, RequirementPriority priority) {
        return new AssertionReportBuilder(validatorBuilderFactory)
                .setAssertionID(exception.getPublicId())
                .setPriority(priority)
                .setAssertionType(assertionType)
                .setResult(ValidationTestResult.FAILED)
                .setDescription(exception.getMessage())
                .setSubjectValue(exception.getLocalizedMessage())
                .addSubjectLocation(new SubjectLocationBuilder()
                        .setValue("line " + exception.getLineNumber() + ", column " + exception.getColumnNumber())
                        .setType(SubjectLocation.LINE_COLUMN_TYPE)
                );
    }
}
