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

package net.ihe.gazelle.maestrorecording.adapter;

import net.ihe.gazelle.maestro.api.business.property.ByteArrayItemProperty;
import net.ihe.gazelle.maestro.api.business.property.ByteArrayProperty;
import net.ihe.gazelle.maestro.api.business.property.Property;
import net.ihe.gazelle.maestro.api.business.testreport.StepRunReport;
import net.ihe.gazelle.maestro.spi.business.recording.StepOutputPlan;
import net.ihe.gazelle.maestro.spi.business.recording.StepOutputPlanDissector;
import net.ihe.gazelle.maestro.spi.business.recording.plan.AttachmentPlan;
import net.ihe.gazelle.maestro.spi.business.recording.plan.FutureReferencePlan;
import net.ihe.gazelle.maestro.spi.business.recording.plan.ItemPlan;
import net.ihe.gazelle.maestro.spi.business.recording.plan.ReferencePlan;
import net.ihe.gazelle.modelmarshaller.technical.marshalling.MarshallingException;
import net.ihe.gazelle.modelmarshaller.technical.marshalling.TextMarshaller;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

final class StaticStepOutputDissector implements StepOutputPlanDissector {

    static final String ROOT_OUTPUT_NAME = "static-root-output";
    static final String SIBLING_OUTPUT_NAME = "static-sibling-output";
    static final String ATTACHMENT_OUTPUT_NAME = "static-attachment-output";

    static final String ROOT_ITEM_TYPE = "STATIC_ROOT";
    static final String CHILD_ITEM_TYPE = "STATIC_CHILD";
    static final String GRANDCHILD_ITEM_TYPE = "STATIC_GRAND_CHILD";
    static final String SIBLING_ITEM_TYPE = "STATIC_SIBLING";

    static final String CHILD_REFERENCE_NAME = "child-link";
    static final String GRANDCHILD_REFERENCE_NAME = "grandchild-link";
    static final String SIBLING_REFERENCE_NAME = "sibling-link";

    static final String REPORT_REFERENCE_NAME = "static-root-binding";
    static final String REPORT_REFERENCE_TYPE = "STATIC_STEP_REPORT";

    static final String ATTACHMENT_REFERENCE_NAME = "binary-attachment";
    static final String DETACHED_REFERENCE_NAME = "detached-log";
    static final String ATTACHMENT_TYPE = "STATIC_ATTACHMENT";

    StaticStepOutputDissector() {
    }

    @Override
    public void dissect(StepOutputPlan stepOutputPlan) {
        if (stepOutputPlan == null || stepOutputPlan.getStepRunReport() == null) {
            return;
        }
        buildPlan(stepOutputPlan);
    }

    private static void buildPlan(StepOutputPlan stepOutputPlan) {
        String datahouseUrl = stepOutputPlan.getDatahouseUrl();
        ItemPlan<String> root = item(ROOT_ITEM_TYPE, "{\"level\":0}");
        ItemPlan<String> child = item(CHILD_ITEM_TYPE, "{\"level\":1}");
        ItemPlan<String> grandchild = item(GRANDCHILD_ITEM_TYPE, "{\"level\":2}");
        ItemPlan<String> sibling = item(SIBLING_ITEM_TYPE, "{\"level\":1}");

        child.withItemReference(GRANDCHILD_REFERENCE_NAME, grandchild);
        root.withItemReference(CHILD_REFERENCE_NAME, child);
        root.withItemReference(SIBLING_REFERENCE_NAME, sibling);

        AttachmentPlan attached = attachment("binary-output.bin", "application/octet-stream", "root-binary");
        AttachmentPlan detached = attachment("logs.txt", "text/plain", "log");
        root.withAttachmentReference(ATTACHMENT_REFERENCE_NAME, attached);
        root.withAttachmentReference(DETACHED_REFERENCE_NAME, detached);

        List<FutureReferencePlan<StepRunReport>> reportPlans = new ArrayList<>();
        reportPlans.add(new FutureReferencePlan<>(
              ReferencePlan.forItem(REPORT_REFERENCE_NAME, root.type(), root),
              (report, refName, itemId) -> StepOutputPlan.replaceOutput(
                    ROOT_OUTPUT_NAME,
                    output -> toItemReference(output, datahouseUrl, itemId, ROOT_ITEM_TYPE),
                    report
              )
        ));
        reportPlans.add(new FutureReferencePlan<>(
              ReferencePlan.forItem(SIBLING_REFERENCE_NAME, sibling.type(), sibling),
              (report, refName, itemId) -> StepOutputPlan.replaceOutput(
                    SIBLING_OUTPUT_NAME,
                    output -> toItemReference(output, datahouseUrl, itemId, SIBLING_ITEM_TYPE),
                    report
              )
        ));
        reportPlans.add(new FutureReferencePlan<>(
              ReferencePlan.forAttachment(ATTACHMENT_REFERENCE_NAME, attached),
              (report, refName, attachmentId) -> StepOutputPlan.replaceOutput(
                    ATTACHMENT_OUTPUT_NAME,
                    output ->
                          new ByteArrayItemProperty(ATTACHMENT_OUTPUT_NAME, null)
                                .setReference("/attachments/" + attachmentId),
                    report
              )
        ));
        reportPlans.add(new FutureReferencePlan<>(
              ReferencePlan.forAttachment(DETACHED_REFERENCE_NAME, detached),
              null
        ));

        for (FutureReferencePlan<StepRunReport> reportPlan : reportPlans) {
            stepOutputPlan.addReferencePlan(reportPlan.referencePlan(), reportPlan.mutation());
        }
    }

    private static ByteArrayItemProperty toItemReference(Property output,
                                                         String datahouseUrl,
                                                         String itemId,
                                                         String itemType) {
        ByteArrayItemProperty replacement = new ByteArrayItemProperty((ByteArrayProperty) output);
        replacement.setValue(null);
        replacement.setItemType(itemType);
        replacement.setReference(datahouseUrl + "/items/" + itemId);
        return replacement;
    }

    private static ItemPlan<String> item(String type, String content) {
        return new ItemPlan<>(type, content, new StringMarshaller());
    }

    private static AttachmentPlan attachment(String filename, String type, String marker) {
        return new AttachmentPlan(type, marker.getBytes(StandardCharsets.UTF_8), filename);
    }

    private static final class StringMarshaller implements TextMarshaller<String> {
        @Override
        public String marshallAsString(String value) throws MarshallingException {
            return value;
        }

        @Override
        public String unmarshall(String payload) {
            throw new UnsupportedOperationException("String marshalling is one-way for recording");
        }

        @Override
        public byte[] marshall(String value) throws MarshallingException {
            return marshallAsString(value).getBytes(StandardCharsets.UTF_8);
        }

        @Override
        public String unmarshall(byte[] payload) {
            throw new UnsupportedOperationException("String marshalling is one-way for recording");
        }
    }
}
