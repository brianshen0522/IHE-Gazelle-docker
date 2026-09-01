# Step Output Dissection

The dissection API lets a step describe how its outputs must be persisted in Datahouse, without exposing recorder internals.
A step provides a `StepOutputPlanDissector` that mutates the provided `StepOutputPlan`.

## High-level flow

1. The step executes and fills a `StepRunReport` with inline outputs (JSON, binaries, etc.).
2. The recorder creates `StepOutputPlan(rootItemPlan, stepRunReport, datahouseUrl)` and calls:
   `StepOutputPlanDissector#dissect(StepOutputPlan)`.
3. The dissector appends references with `stepOutputPlan.addReferencePlan(...)`.
4. The recorder persists referenced items/attachments, applies mutations, then stores the final root report item.

## Core concepts

* `ItemPlan<T>`: Datahouse item to persist from a business object.
* `AttachmentPlan`: binary attachment to upload.
* `ReferencePlan`: reference descriptor (item ID, attachment, URL).
* `FutureReferencePlan<T>`: `ReferencePlan` + optional mutation on containing object after persistence.
* `StepOutputPlan`: bridge between a `StepRunReport` and the root `ItemPlan<TestReport>`.

## Main helper methods

* `StepOutputPlan#addReferencePlan(...)`: attaches step-level references to the root report plan.
* `StepOutputPlan#replaceOutput(...)`: replaces an output in `StepRunReport` index-safely.
* `ItemPlan#withReference(...)`: adds item/attachment child references with optional mutation.
* `ItemPlan#addDetachedAttachment(...)`: convenience for detached attachment references.

## Example pattern

```java
@Override
public void dissect(StepOutputPlan stepOutputPlan) {
   if (stepOutputPlan == null || stepOutputPlan.getStepRunReport() == null) {
      return;
   }

   StepRunReport stepRunReport = stepOutputPlan.getStepRunReport();
   ByteArrayProperty reportProperty = stepRunReport.getOutput("report");
   if (reportProperty == null) {
      return;
   }

   ItemPlan<MyReport> reportPlan = new ItemPlan<>("REPORT_TYPE", parse(reportProperty), new MyMarshaller());

   stepOutputPlan.addReferencePlan(new FutureReferencePlan<>(
         ReferencePlan.forItem("report", reportPlan),
         (currentStep, refName, refId) -> StepOutputPlan.replaceOutput(
               "report",
               output -> new ByteArrayItemProperty((ByteArrayProperty) output)
                     .setValue(null)
                     .setItemType("REPORT_TYPE")
                     .setReference(stepOutputPlan.getDatahouseUrl() + "/items/" + refId),
               currentStep
         )
   ));
}
```

## Tips

* Always guard against `null` / missing outputs and just `return` when nothing must be persisted.
* Keep mutation lambdas focused on the object they mutate.
* When replacing binary outputs, preserve useful metadata (filename, mimeType) in `ByteArrayItemProperty`.
