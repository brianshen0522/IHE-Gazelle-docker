/*
 * Copyright 2026 IHE International.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.ihe.gazelle.maestro.validation.step.technical.record;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.ihe.gazelle.maestro.api.business.property.ByteArrayProperty;
import net.ihe.gazelle.maestro.api.business.property.Property;
import net.ihe.gazelle.maestro.api.business.property.PropertyBindingPayload;
import net.ihe.gazelle.maestro.api.business.testreport.StepRunReport;
import net.ihe.gazelle.maestro.spi.business.recording.StepOutputPlan;
import net.ihe.gazelle.maestro.spi.business.recording.StepOutputPlanDissector;
import net.ihe.gazelle.maestro.spi.business.recording.plan.ItemPlan;
import net.ihe.gazelle.maestro.validation.step.business.ValidationStepDefinition;
import net.ihe.gazelle.modelmarshaller.technical.marshalling.MarshallingException;
import net.ihe.gazelle.modelmarshaller.technical.marshalling.TextMarshaller;
import net.ihe.gazelle.validation.v2.api.business.report.ValidationReport;
import net.ihe.gazelle.validation.v2.api.technical.dto.report.ValidationReportDTO;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * Dissects validation step outputs for recording purposes.
 * This class processes validation reports and extracts attachments and references.
 */
public class ValidationStepOutputDissector implements StepOutputPlanDissector {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private static final String VALIDATION_INPUT_TYPE = "VALIDATION_INPUT";
    private static final List<String> ACCEPTED_TARGET_KINDS = List.of("validationInput");
    private final ValidationInputReferenceBindingService inputReferenceBindingService;

    /**
     * Default constructor.
     */
    public ValidationStepOutputDissector() {
        this.inputReferenceBindingService = new ValidationInputReferenceBindingService(OBJECT_MAPPER);
    }

    @Override
    public void dissect(StepOutputPlan stepOutputPlan) {
        if (stepOutputPlan == null || stepOutputPlan.getStepRunReport() == null) {
            return;
        }
        StepRunReport stepRunReport = stepOutputPlan.getStepRunReport();
        PropertyBindingPayload propertyBindingPayload = inputReferenceBindingService.consumePropertyBinding(stepRunReport);
        List<Property> outputs = stepRunReport.getOutputs();
        if (outputs == null || outputs.isEmpty()) {
            return;
        }
        ByteArrayProperty validationReportProperty = stepRunReport.getOutput(ValidationStepDefinition.REPORT);
        if (validationReportProperty == null) {
            return;
        }
        byte[] validationReportBytes = validationReportProperty.getValue();
        ValidationReport validationReport = deserializeReport(validationReportBytes);

        ItemPlan<ValidationReport> validationReportPlan = new ItemPlan<>(
              ValidationStepDefinition.REPORT,
              validationReport,
              new ValidationReportMarshaller()
        );

        Map<String, ValidationInputReferenceBindingService.BindingInfo> bindingInfoByTargetId =
              inputReferenceBindingService.toBindingInfoByTargetId(propertyBindingPayload, ACCEPTED_TARGET_KINDS);
        inputReferenceBindingService.registerInputPersistence(
              stepOutputPlan,
              validationReportPlan,
              validationReport,
              bindingInfoByTargetId,
              VALIDATION_INPUT_TYPE
        );

        stepOutputPlan.addItemReferencePlan(ValidationStepDefinition.REPORT, validationReportPlan);

    }

    private ValidationReport deserializeReport(byte[] content) {
        try {
            return OBJECT_MAPPER.readValue(content, ValidationReport.class);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to deserialize ValidationReport from JSON", e);
        }
    }

    private static final class ValidationReportMarshaller implements TextMarshaller<ValidationReport> {
        @Override
        public String marshallAsString(ValidationReport report) throws MarshallingException {
            try {
                return OBJECT_MAPPER.writeValueAsString(new ValidationReportDTO(report));
            } catch (IOException e) {
                throw new MarshallingException(e.getMessage());
            }
        }

        @Override
        public ValidationReport unmarshall(String payload) {
            throw new UnsupportedOperationException("ValidationReport marshalling is one-way for recording");
        }

        @Override
        public byte[] marshall(ValidationReport report) throws MarshallingException {
            return marshallAsString(report).getBytes(StandardCharsets.UTF_8);
        }

        @Override
        public ValidationReport unmarshall(byte[] payload) {
            throw new UnsupportedOperationException("ValidationReport marshalling is one-way for recording");
        }
    }
}
