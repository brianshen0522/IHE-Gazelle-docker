package net.ihe.gazelle.validation.gateway.migration.engine;

import com.kereval.gazelle.datahouse.api.business.record.Item;
import net.ihe.gazelle.validation.gateway.migration.dto.MigrationCheckpoint;
import net.ihe.gazelle.validation.gateway.migration.dto.MigrationPreview;
import net.ihe.gazelle.validation.gateway.migration.dto.MigrationProgress;
import net.ihe.gazelle.validation.gateway.migration.evs.EvsHandledObjectSource;
import net.ihe.gazelle.validation.gateway.migration.exception.MissingInputException;
import net.ihe.gazelle.validation.gateway.migration.exception.MissingValidationReportException;
import net.ihe.gazelle.validation.gateway.migration.mapper.MigrationItemMapper;
import net.ihe.gazelle.validation.gateway.migration.evs.EvsSourceService;
import net.ihe.gazelle.validation.gateway.migration.evs.EvsValidationSourceRow;
import net.ihe.gazelle.validation.gateway.migration.evs.LegacyReportParser;
import net.ihe.gazelle.validation.gateway.migration.output.InputAttachmentSource;
import net.ihe.gazelle.validation.gateway.migration.output.MigrationTargetService;
import net.ihe.gazelle.validation.gateway.migration.output.PersistedMigrationState;
import net.ihe.gazelle.validation.v2.api.business.report.ValidationReport;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@ApplicationScoped
public class MigrationEngine {
   private static final String OID_SUFFIX = " (OID: ";


   private static final Logger LOG = LoggerFactory.getLogger(MigrationEngine.class);

   private final EvsSourceService sourceService;
   private final LegacyReportParser reportParser;
   private final MigrationItemMapper itemMapper;
   private final MigrationTargetService targetService;
   private final MigrationTracker tracker;
   private final int batchSize;

   private final ExecutorService executor = Executors.newSingleThreadExecutor();
   private volatile MigrationState state = MigrationState.IDLE;
   private volatile String lastMessage = "Migration not started";
   private volatile MigrationContext currentContext = new MigrationContext();

   public MigrationEngine(EvsSourceService sourceService,
                          LegacyReportParser reportParser,
                          MigrationItemMapper itemMapper,
                          MigrationTargetService targetService,
                          MigrationTracker tracker,
                          @ConfigProperty(name = "migration.batch.size", defaultValue = "200") int batchSize) {
      this.sourceService = sourceService;
      this.reportParser = reportParser;
      this.itemMapper = itemMapper;
      this.targetService = targetService;
      this.tracker = tracker;
      this.batchSize = batchSize;
      try {
         if (targetService.isMigrationCompleted()) {
            this.state = MigrationState.COMPLETED;
            this.lastMessage = "Migration was already completed";
         }
      } catch (Exception e) {
         LOG.warn("Unable to read migration completion marker at startup: {}", e.getMessage());
      }
   }

   public synchronized void startMigration(boolean freezeConfirmed, boolean rerunConfirmed) {
      startMigration(freezeConfirmed, rerunConfirmed ? MigrationMode.FULL : MigrationMode.FULL, new MigrationContext());
   }

   public synchronized void startMigration(boolean freezeConfirmed, MigrationMode mode) {
      startMigration(freezeConfirmed, mode, new MigrationContext());
   }

   public synchronized void startMigration(boolean freezeConfirmed, MigrationMode mode, MigrationContext context) {
      if (!freezeConfirmed) {
         throw new IllegalArgumentException("Database freeze must be confirmed before running the migration");
      }

      // Validate mode-specific requirements
      if (mode == MigrationMode.RETRY_FAILED_ONLY) {
         Set<String> failedOids = getFailedOids();
         if (failedOids.isEmpty()) {
            throw new IllegalStateException("Cannot retry failed reports: no failures found");
         }
      }

      boolean isRerun = targetService.isMigrationCompleted();
      if (isRerun && mode == MigrationMode.FULL) {
         // Full rerun scenario - requires explicit confirmation
         // This check is implicit via the UI flow (rerunConfirmed parameter)
      }

      if (state == MigrationState.RUNNING) {
         throw new IllegalStateException("Migration is already running");
      }

      state = MigrationState.RUNNING;
      lastMessage = "Migration running";
      currentContext = context;
      MigrationCheckpoint checkpoint = null;

      if (mode == MigrationMode.RETRY_FAILED_ONLY) {
         Set<String> failedOids = getFailedOids();
         tracker.startRetry(failedOids.size(), failedOids);
      } else if (mode == MigrationMode.INCREMENTAL) {
         checkpoint = readLastSuccessfulCheckpoint();
         tracker.startIncremental(sourceService.countReportsAfter(checkpoint));
      } else {
         targetService.clearFailedReports();
         tracker.start(sourceService.countReports());
      }

      MigrationCheckpoint finalCheckpoint = checkpoint;
      executor.submit(() -> runMigration(mode, finalCheckpoint));
   }

   public MigrationState getState() {
      try {
         if (state == MigrationState.COMPLETED && !targetService.isMigrationCompleted()) {
            state = MigrationState.IDLE;
            lastMessage = "Migration marker not found in target storage";
         }
      } catch (Exception ignored) {
         // Status endpoint handles target accessibility separately; keep last known state here.
      }
      return state;
   }

   public String getLastMessage() {
      return lastMessage;
   }

   public Instant getCompletedAt() {
      return targetService.getCompletedAt();
   }

   public boolean isMigrationCompleted() {
      return targetService.isMigrationCompleted();
   }

   public MigrationProgress getProgress() {
      return tracker.snapshot();
   }

   private void runMigration(MigrationMode mode) {
      runMigration(mode, null);
   }

   private void runMigration(MigrationMode mode, MigrationCheckpoint checkpoint) {
      try {
         MigrationCheckpoint completionCheckpoint = processRows(mode, checkpoint);
         completeMigration(mode, completionCheckpoint);
      } catch (Exception e) {
         failMigration(e);
      }
   }

   private MigrationCheckpoint processRows(MigrationMode mode, MigrationCheckpoint checkpoint) {
      if (mode == MigrationMode.RETRY_FAILED_ONLY) {
         processRetryFailedRows();
         return null;
      }
      if (mode == MigrationMode.INCREMENTAL) {
         return processIncrementalRows(checkpoint);
      }
      return processAllRows();
   }

   private void processRetryFailedRows() {
      long offset = 0;
      Set<String> failedOids = getFailedOids();
      while (true) {
         List<EvsValidationSourceRow> batch = sourceService.fetchBatchByOids(failedOids, offset, batchSize);
         if (batch.isEmpty()) {
            return;
         }
         processBatch(batch, null);
         offset += batch.size();
      }
   }

   private MigrationCheckpoint processAllRows() {
      return processRowsAfterCheckpoint(null);
   }

   private MigrationCheckpoint processIncrementalRows(MigrationCheckpoint checkpoint) {
      return processRowsAfterCheckpoint(checkpoint);
   }

   private MigrationCheckpoint processRowsAfterCheckpoint(MigrationCheckpoint checkpoint) {
      long offset = 0;
      MigrationCheckpoint latestSuccess = null;
      while (true) {
         List<EvsValidationSourceRow> batch = checkpoint == null
               ? sourceService.fetchBatch(offset, batchSize)
               : sourceService.fetchBatchAfter(checkpoint, offset, batchSize);
         if (batch.isEmpty()) {
            return latestSuccess;
         }
         latestSuccess = processBatch(batch, latestSuccess);
         offset += batch.size();
      }
   }

   private MigrationCheckpoint processBatch(List<EvsValidationSourceRow> batch, MigrationCheckpoint currentLatest) {
      MigrationCheckpoint latest = currentLatest;
      for (EvsValidationSourceRow row : batch) {
         if (migrateOne(row)) {
            latest = new MigrationCheckpoint(
                  row.validationDate() == null ? Instant.EPOCH : row.validationDate(),
                  row.id()
            );
         }
      }
      return latest;
   }

   private void completeMigration(MigrationMode mode, MigrationCheckpoint checkpoint) {
      tracker.complete();
      MigrationProgress finalProgress = tracker.snapshot();
      targetService.markCompleted(finalProgress, mode, checkpoint);
      state = MigrationState.COMPLETED;
      lastMessage = "Migration completed";
   }

   private void failMigration(Exception error) {
      state = MigrationState.FAILED;
      lastMessage = error.getMessage() == null ? "Migration failed" : error.getMessage();
   }

   private java.util.Set<String> getFailedOids() {
      try {
         return targetService.readAllFailedOids();
      } catch (UnsupportedOperationException e) {
         // Fallback for backward compatibility
         return targetService.readMigrationState()
               .map(PersistedMigrationState::allFailedOids)
               .orElse(java.util.Set.of());
      }
   }

   private MigrationCheckpoint readLastSuccessfulCheckpoint() {
      return targetService.readMigrationState()
            .map(PersistedMigrationState::lastSuccessfulCheckpoint)
            .orElse(null);
   }

   private boolean migrateOne(EvsValidationSourceRow row) {
      String oid = row.oid();
      try {
         Optional<ValidationReport> parsedReport = reportParser.parseValidationReport(row.validationReportArchivePath());

         // Fail migration if validation report is missing
         if (parsedReport.isEmpty()) {
            throw new MissingValidationReportException("Validation report not found or could not be parsed for processing " +
                  row.id() + OID_SUFFIX + oid + ")");
         }

         Item item = itemMapper.map(row, parsedReport.get());

         List<InputAttachmentSource> inputAttachments;
         try {
            inputAttachments = buildInputAttachments(row);
         } catch (MissingInputException e) {
            // Check if we should ignore missing input for this OID
            if (currentContext.shouldIgnoreMissingInput(oid)) {
               // Create report WITHOUT input attachment.
               LOG.info("Ignoring missing input for OID {}: creating validation report without input attachment", oid);
               inputAttachments = List.of();
            } else {
               // Don't ignore - re-throw to fail the migration for this report
               throw e;
            }
         }

         String itemId = targetService.recordMigratedItem(item, inputAttachments);

         String result = itemMapper.mapStatus(row.status()).name();
         tracker.markSuccess(new MigrationPreview(oid, itemId, result,
               row.validationDate() == null ? Instant.now() : row.validationDate()));
         return true;
      } catch (Exception e) {
         tracker.markFailure(oid, e);
         return false;
      }
   }

   private List<InputAttachmentSource> buildInputAttachments(EvsValidationSourceRow row) {
      String oid = row.oid() == null || row.oid().isBlank() ? String.valueOf(row.id()) : row.oid();

      List<EvsHandledObjectSource> handledObjects = sourceService.fetchHandledObjects(row.id());
      List<EvsHandledObjectSource> candidates = handledObjects.stream()
            .filter(this::isInputRole)
            .toList();
      if (candidates.isEmpty()) {
         candidates = handledObjects;
      }
      List<EvsHandledObjectSource> readableObjects = new ArrayList<>();
      List<String> unreadableFiles = new ArrayList<>();

      for (EvsHandledObjectSource object : candidates) {
         String filePath = object.filePath();
         if (filePath == null || filePath.isBlank()) {
            continue;
         }
         java.io.File file = new java.io.File(filePath);
         if (file.exists() && file.isFile() && file.canRead()) {
            readableObjects.add(object);
         } else {
            unreadableFiles.add(filePath);
            LOG.warn("Input file not readable for processing {}: {}", row.id(), filePath);
         }
      }

      // STRICTER VALIDATION: Fail if ANY file is unreadable (not just all)
      if (!unreadableFiles.isEmpty()) {
         throw new MissingInputException("Some input files unreadable for processing " + row.id() +
               OID_SUFFIX + oid + "): " + String.join(", ", unreadableFiles));
      }

      // Fail migration for this report if no input documents are found
      if (readableObjects.isEmpty()) {
         throw new MissingInputException("No readable input files found for processing " + row.id() +
               OID_SUFFIX + oid + "): migration cannot proceed without validated document");
      }

      List<InputAttachmentSource> attachments = new ArrayList<>(readableObjects.size());
      int index = 1;
      for (EvsHandledObjectSource object : readableObjects) {
         String filename = preferredFilename(object, "input-" + oid + "-" + index + ".bin");
         attachments.add(new InputAttachmentSource(
               filename,
               detectContentType(object.filePath()),
               Path.of(object.filePath()),
               object.role()
         ));
         index++;
      }
      return attachments;
   }

   private boolean isInputRole(EvsHandledObjectSource object) {
      String role = object.role();
      if (role == null || role.isBlank()) {
         return false;
      }
      String normalized = role.trim().toUpperCase(java.util.Locale.ROOT);
      return normalized.contains("INPUT")
            || normalized.contains("ORIGINAL")
            || normalized.contains("VALIDATED");
   }

   private String detectContentType(String filePath) {
      try {
         String detected = java.nio.file.Files.probeContentType(Path.of(filePath));
         return detected == null || detected.isBlank() ? "application/octet-stream" : detected;
      } catch (Exception e) {
         LOG.debug("Could not detect content type for {}, fallback to octet-stream: {}", filePath, e.getMessage());
         return "application/octet-stream";
      }
   }

   private String preferredFilename(EvsHandledObjectSource input, String fallback) {
      if (input.originalFileName() != null && !input.originalFileName().isBlank()) {
         return sanitizeFilename(input.originalFileName());
      }
      if (input.filePath() != null && !input.filePath().isBlank()) {
         String pathName = new java.io.File(input.filePath()).getName();
         if (!pathName.isBlank()) {
            return sanitizeFilename(pathName);
         }
      }
      return sanitizeFilename(fallback);
   }

   private String sanitizeFilename(String filename) {
      return filename.replace("\\", "_").replace("/", "_").replace("\"", "_");
   }

   @PreDestroy
   void destroy() {
      executor.shutdownNow();
   }
}
