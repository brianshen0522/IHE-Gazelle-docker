package net.ihe.gazelle.validation.gateway.migration.output;

import com.kereval.gazelle.datahouse.api.business.record.Item;
import net.ihe.gazelle.validation.gateway.migration.dto.FailedReportsPage;
import net.ihe.gazelle.validation.gateway.migration.dto.MigrationError;
import net.ihe.gazelle.validation.gateway.migration.dto.MigrationCheckpoint;
import net.ihe.gazelle.validation.gateway.migration.dto.MigrationProgress;
import net.ihe.gazelle.validation.gateway.migration.engine.MigrationMode;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface MigrationTargetService {

   String getTargetType();

   TargetHealth checkHealth();

   String recordMigratedItem(Item item, List<InputAttachmentSource> inputAttachments);

   boolean isMigrationCompleted();

   Instant getCompletedAt();

   Optional<CompletedMigrationMetrics> getCompletedMetrics();

   Optional<PersistedMigrationState> readMigrationState();

   void writeMigrationState(PersistedMigrationState state);

   void markCompleted(MigrationProgress progress, MigrationMode mode, MigrationCheckpoint checkpoint);

   String findItemIdByEvsOid(String evsOid);

   Optional<Map<String, Object>> readItemById(String itemId);

   Optional<InputAttachmentDownload> readInputAttachmentByItemId(String itemId);

   default List<InputAttachmentMetadata> readInputAttachmentsByItemId(String itemId) {
      return readInputAttachmentByItemId(itemId)
            .map(download -> List.of(new InputAttachmentMetadata("", download.filename(), download.contentType())))
            .orElse(List.of());
   }

   default Optional<InputAttachmentDownload> readInputAttachmentByItemAndAttachmentId(String itemId, String attachmentId) {
      return readInputAttachmentByItemId(itemId);
   }

   default void recordFailedReport(String oid, MigrationError error) {
      throw new UnsupportedOperationException("recordFailedReport not implemented");
   }

   default void removeFailedReport(String oid) {
      throw new UnsupportedOperationException("removeFailedReport not implemented");
   }

   default FailedReportsPage readFailedReports(int offset, int limit) {
      throw new UnsupportedOperationException("readFailedReports not implemented");
   }

   default long countFailedReports() {
      throw new UnsupportedOperationException("countFailedReports not implemented");
   }

   default Set<String> readAllFailedOids() {
      throw new UnsupportedOperationException("readAllFailedOids not implemented");
   }

   default void clearFailedReports() {
      throw new UnsupportedOperationException("clearFailedReports not implemented");
   }
}
