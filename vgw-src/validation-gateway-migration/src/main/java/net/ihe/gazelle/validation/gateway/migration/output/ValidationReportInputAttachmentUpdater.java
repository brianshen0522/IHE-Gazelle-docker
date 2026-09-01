package net.ihe.gazelle.validation.gateway.migration.output;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.ihe.gazelle.validation.v2.api.business.Input;
import net.ihe.gazelle.validation.v2.api.business.report.ValidationReport;
import net.ihe.gazelle.validation.v2.api.technical.dto.report.ValidationReportDTO;

import java.util.List;
import java.util.Set;
import java.util.HashSet;

class ValidationReportInputAttachmentUpdater {

   private final ObjectMapper objectMapper;

   ValidationReportInputAttachmentUpdater(ObjectMapper objectMapper) {
      this.objectMapper = objectMapper;
   }

   String withInputAttachmentReferences(String content, List<InputAttachmentMetadata> attachments) {
      if (content == null || content.isBlank() || attachments == null || attachments.isEmpty()) {
         return content;
      }
      try {
         ValidationReportDTO dto = objectMapper.readValue(content, ValidationReportDTO.class);
         ValidationReport report = dto.getBusinessObject();
         Set<String> usedInputIds = new HashSet<>();
         for (Input existing : report.getInputs()) {
            if (existing.getId() != null && !existing.getId().isBlank()) {
               usedInputIds.add(existing.getId());
            }
         }
         int fallbackIndex = report.getInputs().size() + 1;
         for (InputAttachmentMetadata attachment : attachments) {
            if (attachment == null || attachment.attachmentId() == null || attachment.attachmentId().isBlank()) {
               continue;
            }
            String preferredInputId = sanitizeInputId(attachment.filename(), fallbackIndex);
            boolean assignedToExisting = false;
            for (Input input : report.getInputs()) {
               if ((input.getItemId() == null || input.getItemId().isBlank())
                     && (input.getLocation() == null || input.getLocation().isBlank())) {
                  input.setItemId(attachment.attachmentId());
                  if (input.getId() == null || input.getId().isBlank()) {
                     String unique = uniqueInputId(preferredInputId, usedInputIds);
                     input.setId(unique);
                     usedInputIds.add(unique);
                  }
                  assignedToExisting = true;
                  break;
               }
            }
            if (!assignedToExisting) {
               String unique = uniqueInputId(preferredInputId, usedInputIds);
               report.addInput(new Input().setId(unique).setItemId(attachment.attachmentId()));
               usedInputIds.add(unique);
            }
            fallbackIndex++;
         }
         return objectMapper.writeValueAsString(new ValidationReportDTO(report));
      } catch (Exception e) {
         return content;
      }
   }

   private String sanitizeInputId(String filename, int fallbackIndex) {
      if (filename == null || filename.isBlank()) {
         return "input-" + fallbackIndex;
      }
      return filename.replace("\\", "_").replace("/", "_").replace("\"", "_");
   }

   private String uniqueInputId(String preferred, Set<String> usedInputIds) {
      if (!usedInputIds.contains(preferred)) {
         return preferred;
      }
      int suffix = 2;
      String candidate = preferred + "-" + suffix;
      while (usedInputIds.contains(candidate)) {
         suffix++;
         candidate = preferred + "-" + suffix;
      }
      return candidate;
   }
}
