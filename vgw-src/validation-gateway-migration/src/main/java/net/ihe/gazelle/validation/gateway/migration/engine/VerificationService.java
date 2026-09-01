package net.ihe.gazelle.validation.gateway.migration.engine;

import net.ihe.gazelle.validation.gateway.migration.dto.VerificationResult;
import net.ihe.gazelle.validation.gateway.migration.evs.EvsSourceService;
import net.ihe.gazelle.validation.gateway.migration.output.InputAttachmentMetadata;
import net.ihe.gazelle.validation.gateway.migration.output.InputAttachmentDownload;
import net.ihe.gazelle.validation.gateway.migration.output.MigrationTargetService;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class VerificationService {

   private final EvsSourceService sourceService;
   private final MigrationTargetService targetService;

   public VerificationService(EvsSourceService sourceService, MigrationTargetService targetService) {
      this.sourceService = sourceService;
      this.targetService = targetService;
   }

   public List<VerificationResult> spotCheck(int count) {
      int effectiveCount = Math.max(1, Math.min(count, 50));
      return sourceService.fetchRandom(effectiveCount).stream().map(row -> {
         String itemId = targetService.findItemIdByEvsOid(row.oid());
         boolean hasInput = false;
         if (itemId != null) {
            // Check if the migrated item has an input attachment
            hasInput = targetService.readInputAttachmentByItemId(itemId).isPresent();
         }
         return new VerificationResult(row.oid(), itemId != null, itemId, hasInput);
      }).toList();
   }

   public Map<String, Object> readMigratedItem(String itemId) {
      return targetService.readItemById(itemId)
            .orElseThrow(() -> new IllegalArgumentException("Item not found for id " + itemId));
   }

   public InputAttachmentDownload readMigratedInputAttachment(String itemId) {
      return targetService.readInputAttachmentByItemId(itemId)
            .orElseThrow(() -> new IllegalArgumentException("Input attachment not found for item id " + itemId));
   }

   public List<InputAttachmentMetadata> readMigratedInputAttachments(String itemId) {
      return targetService.readInputAttachmentsByItemId(itemId);
   }

   public InputAttachmentDownload readMigratedInputAttachment(String itemId, String attachmentId) {
      return targetService.readInputAttachmentByItemAndAttachmentId(itemId, attachmentId)
            .orElseThrow(() -> new IllegalArgumentException(
                  "Input attachment not found for item id " + itemId + " and attachment id " + attachmentId
            ));
   }
}
