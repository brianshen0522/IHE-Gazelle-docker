package net.ihe.gazelle.validation.gateway.migration.engine;

import com.kereval.gazelle.datahouse.api.business.record.Item;
import net.ihe.gazelle.validation.gateway.migration.evs.EvsHandledObjectSource;
import net.ihe.gazelle.validation.gateway.migration.evs.EvsSourceService;
import net.ihe.gazelle.validation.gateway.migration.evs.EvsValidationSourceRow;
import net.ihe.gazelle.validation.gateway.migration.evs.LegacyReportParser;
import net.ihe.gazelle.validation.gateway.migration.mapper.MigrationItemMapper;
import net.ihe.gazelle.validation.gateway.migration.dto.FailedReportsPage;
import net.ihe.gazelle.validation.gateway.migration.dto.MigrationCheckpoint;
import net.ihe.gazelle.validation.gateway.migration.dto.MigrationError;
import net.ihe.gazelle.validation.gateway.migration.dto.MigrationErrorType;
import net.ihe.gazelle.validation.gateway.migration.dto.MigrationPreview;
import net.ihe.gazelle.validation.gateway.migration.output.CompletedMigrationMetrics;
import net.ihe.gazelle.validation.gateway.migration.output.InputAttachmentMetadata;
import net.ihe.gazelle.validation.gateway.migration.output.InputAttachmentDownload;
import net.ihe.gazelle.validation.gateway.migration.output.InputAttachmentSource;
import net.ihe.gazelle.validation.gateway.migration.output.MigrationTargetService;
import net.ihe.gazelle.validation.gateway.migration.output.PersistedMigrationState;
import net.ihe.gazelle.validation.gateway.migration.output.TargetHealth;
import net.ihe.gazelle.validation.v2.api.business.report.ValidationReport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class MigrationEngineTest {

   private final List<MigrationEngine> engines = new ArrayList<>();

   @AfterEach
   void tearDown() {
      engines.forEach(MigrationEngine::destroy);
      engines.clear();
   }

   @Test
   void runMigrationFullProcessesBatchAndMarksCompleted(@TempDir Path tempDir) throws Exception {
      Path inputFile = tempDir.resolve("doc.xml");
      Files.writeString(inputFile, "payload");

      StubSourceService source = new StubSourceService();
      source.batches.add(List.of(row("oid-1")));
      source.batches.add(List.of());
      source.handledObjects = List.of(new EvsHandledObjectSource(1, "INPUT", "doc.xml", inputFile.toString()));

      StubLegacyReportParser parser = new StubLegacyReportParser();
      StubTargetService target = new StubTargetService(false);
      MigrationTracker tracker = new MigrationTracker(target);
      MigrationEngine engine = register(new MigrationEngine(source, parser, new MigrationItemMapper("super-admin"), target, tracker, 50));

      invokeRunMigration(engine, MigrationMode.FULL);

      assertThat(target.recordMigratedItemCalls, is(1));
      assertThat(target.markCompletedCalls, is(1));
      assertThat(target.lastMarkedMode, is(MigrationMode.FULL));
      assertThat(engine.getProgress().succeeded(), is(1L));
      assertThat(source.fetchBatchCalls > 0, is(true));
   }

   @Test
   void runMigrationRetryModeUsesFailedOids(@TempDir Path tempDir) throws Exception {
      Path inputFile = tempDir.resolve("doc.xml");
      Files.writeString(inputFile, "payload");

      StubSourceService source = new StubSourceService();
      source.retryBatches.add(List.of(row("oid-retry")));
      source.retryBatches.add(List.of());
      source.handledObjects = List.of(new EvsHandledObjectSource(1, "INPUT", "doc.xml", inputFile.toString()));

      StubLegacyReportParser parser = new StubLegacyReportParser();
      StubTargetService target = new StubTargetService(false);
      target.state = new PersistedMigrationState(
            1,
            0,
            0,
            0,
            Instant.now(),
            false,
            null,
            List.of(),
            List.of(),
            null,
            null,
            Set.of("oid-retry"),
            Map.of()
      );
      // Populate failedReports so readAllFailedOids() returns the OIDs
      target.failedReports.put("oid-retry", new MigrationError("oid-retry", "test error", Instant.now(), null));
      MigrationTracker tracker = new MigrationTracker(target);
      MigrationEngine engine = register(new MigrationEngine(source, parser, new MigrationItemMapper("super-admin"), target, tracker, 50));

      invokeRunMigration(engine, MigrationMode.RETRY_FAILED_ONLY);

      assertThat(source.fetchBatchCalls, is(0L));
      assertThat(source.fetchBatchByOidsCalls > 0, is(true));
      assertThat(source.lastRequestedOids, is(Set.of("oid-retry")));
      assertThat(target.markCompletedCalls, is(1));
      assertThat(target.lastMarkedMode, is(MigrationMode.RETRY_FAILED_ONLY));
   }

   @Test
   void runMigrationIncrementalUsesCheckpointQueries(@TempDir Path tempDir) throws Exception {
      Path inputFile = tempDir.resolve("doc.xml");
      Files.writeString(inputFile, "payload");

      StubSourceService source = new StubSourceService();
      source.incrementalBatches.add(List.of(row(3, "oid-new", Instant.parse("2026-01-03T10:00:00Z"))));
      source.incrementalBatches.add(List.of());
      source.handledObjects = List.of(new EvsHandledObjectSource(1, "INPUT", "doc.xml", inputFile.toString()));

      StubLegacyReportParser parser = new StubLegacyReportParser();
      StubTargetService target = new StubTargetService(false);
      target.state = new PersistedMigrationState(
            2,
            2,
            2,
            0,
            Instant.parse("2026-01-01T00:00:00Z"),
            true,
            Instant.parse("2026-01-01T00:01:00Z"),
            List.of(),
            List.of(),
            new MigrationCheckpoint(Instant.parse("2026-01-02T10:00:00Z"), 2),
            MigrationMode.FULL.name(),
            Set.of(),
            Map.of()
      );
      MigrationTracker tracker = new MigrationTracker(target);
      MigrationEngine engine = register(new MigrationEngine(source, parser, new MigrationItemMapper("super-admin"), target, tracker, 50));

      engine.startMigration(true, MigrationMode.INCREMENTAL, new MigrationContext());
      waitForTerminalState(engine);

      assertThat(source.countReportsAfterCalls, is(1L));
      assertThat(source.fetchBatchAfterCalls > 0, is(true));
      assertThat(source.fetchBatchCalls, is(0L));
      assertThat(target.clearFailedReportsCalls, is(0));
      assertThat(target.lastMarkedMode, is(MigrationMode.INCREMENTAL));
      assertThat(target.lastMarkedCheckpoint, is(new MigrationCheckpoint(Instant.parse("2026-01-03T10:00:00Z"), 3)));
      assertThat(engine.getProgress().total(), is(3L));
      assertThat(engine.getProgress().processed(), is(3L));
      assertThat(engine.getProgress().succeeded(), is(3L));
   }

   @Test
   void runMigrationIncrementalWithoutCheckpointFallsBackToFullQuery(@TempDir Path tempDir) throws Exception {
      Path inputFile = tempDir.resolve("doc.xml");
      Files.writeString(inputFile, "payload");

      StubSourceService source = new StubSourceService();
      source.batches.add(List.of(row("oid-1")));
      source.batches.add(List.of());
      source.handledObjects = List.of(new EvsHandledObjectSource(1, "INPUT", "doc.xml", inputFile.toString()));

      StubLegacyReportParser parser = new StubLegacyReportParser();
      StubTargetService target = new StubTargetService(false);
      MigrationTracker tracker = new MigrationTracker(target);
      MigrationEngine engine = register(new MigrationEngine(source, parser, new MigrationItemMapper("super-admin"), target, tracker, 50));

      engine.startMigration(true, MigrationMode.INCREMENTAL, new MigrationContext());
      waitForTerminalState(engine);

      assertThat(source.fetchBatchCalls > 0, is(true));
      assertThat(source.fetchBatchAfterCalls, is(0L));
      assertThat(target.lastMarkedMode, is(MigrationMode.INCREMENTAL));
      assertThat(target.lastMarkedCheckpoint, is(new MigrationCheckpoint(Instant.parse("2026-01-01T10:00:00Z"), 1)));
      assertThat(engine.getProgress().total(), is(1L));
      assertThat(engine.getProgress().processed(), is(1L));
   }

   @Test
   void previewsAreCumulativeAcrossRuns(@TempDir Path tempDir) throws Exception {
      Path inputFile = tempDir.resolve("doc.xml");
      Files.writeString(inputFile, "payload");

      StubSourceService source = new StubSourceService();
      source.incrementalBatches.add(List.of(row(2, "oid-new", Instant.parse("2026-01-02T10:00:00Z"))));
      source.incrementalBatches.add(List.of());
      source.handledObjects = List.of(new EvsHandledObjectSource(1, "INPUT", "doc.xml", inputFile.toString()));

      StubLegacyReportParser parser = new StubLegacyReportParser();
      StubTargetService target = new StubTargetService(false);
      target.state = new PersistedMigrationState(
            1,
            1,
            1,
            0,
            Instant.parse("2026-01-01T00:00:00Z"),
            true,
            Instant.parse("2026-01-01T00:01:00Z"),
            List.of(new MigrationPreview(
                  "oid-old",
                  "item-old",
                  "PASSED",
                  Instant.parse("2026-01-01T10:00:00Z")
            )),
            List.of(),
            new MigrationCheckpoint(Instant.parse("2026-01-01T10:00:00Z"), 1),
            MigrationMode.FULL.name(),
            Set.of(),
            Map.of()
      );
      MigrationTracker tracker = new MigrationTracker(target);
      MigrationEngine engine = register(new MigrationEngine(source, parser, new MigrationItemMapper("super-admin"), target, tracker, 50));

      engine.startMigration(true, MigrationMode.INCREMENTAL, new MigrationContext());
      waitForTerminalState(engine);

      List<MigrationPreview> previews = engine.getProgress().previews();
      assertThat(previews.size(), is(2));
      assertThat(previews.get(0).evsOid(), is("oid-new"));
      assertThat(previews.get(1).evsOid(), is("oid-old"));
   }

   @Test
   void incrementalRunPreservesExistingRecentErrors(@TempDir Path tempDir) throws Exception {
      Path inputFile = tempDir.resolve("doc.xml");
      Files.writeString(inputFile, "payload");

      StubSourceService source = new StubSourceService();
      source.incrementalBatches.add(List.of(row(2, "oid-new", Instant.parse("2026-01-02T10:00:00Z"))));
      source.incrementalBatches.add(List.of());
      source.handledObjects = List.of(new EvsHandledObjectSource(1, "INPUT", "doc.xml", inputFile.toString()));

      StubLegacyReportParser parser = new StubLegacyReportParser();
      StubTargetService target = new StubTargetService(false);
      MigrationError oldError = new MigrationError(
            "oid-old-error",
            "older failure",
            Instant.parse("2026-01-01T09:00:00Z"),
            MigrationErrorType.MISSING_INPUT
      );
      target.state = new PersistedMigrationState(
            1,
            1,
            1,
            1,
            Instant.parse("2026-01-01T00:00:00Z"),
            true,
            Instant.parse("2026-01-01T00:01:00Z"),
            List.of(),
            List.of(oldError),
            new MigrationCheckpoint(Instant.parse("2026-01-01T10:00:00Z"), 1),
            MigrationMode.FULL.name(),
            Set.of("oid-old-error"),
            Map.of("oid-old-error", oldError)
      );
      MigrationTracker tracker = new MigrationTracker(target);
      MigrationEngine engine = register(new MigrationEngine(source, parser, new MigrationItemMapper("super-admin"), target, tracker, 50));

      engine.startMigration(true, MigrationMode.INCREMENTAL, new MigrationContext());
      waitForTerminalState(engine);

      List<MigrationError> recentErrors = engine.getProgress().recentErrors();
      assertThat(recentErrors.size(), is(1));
      assertThat(recentErrors.get(0).evsOid(), is("oid-old-error"));
   }

   @Test
   void getStateResetsToIdleWhenCompletionMarkerDisappears() {
      StubSourceService source = new StubSourceService();
      StubLegacyReportParser parser = new StubLegacyReportParser();
      StubTargetService target = new StubTargetService(true, false);
      MigrationTracker tracker = new MigrationTracker(target);
      MigrationEngine engine = register(new MigrationEngine(source, parser, new MigrationItemMapper("super-admin"), target, tracker, 50));

      MigrationState state = engine.getState();

      assertThat(state, is(MigrationState.IDLE));
      assertThat(engine.getLastMessage(), is("Migration marker not found in target storage"));
   }

   @Test
   void runMigrationMarksFailureWhenValidationReportCannotBeParsed(@TempDir Path tempDir) throws Exception {
      Path inputFile = tempDir.resolve("doc.xml");
      Files.writeString(inputFile, "payload");

      StubSourceService source = new StubSourceService();
      source.batches.add(List.of(row("oid-missing-report")));
      source.batches.add(List.of());
      source.handledObjects = List.of(new EvsHandledObjectSource(1, "INPUT", "doc.xml", inputFile.toString()));

      StubLegacyReportParser parser = new StubLegacyReportParser();
      parser.parsedReport = Optional.empty();
      StubTargetService target = new StubTargetService(false);
      MigrationTracker tracker = new MigrationTracker(target);
      MigrationEngine engine = register(new MigrationEngine(source, parser, new MigrationItemMapper("super-admin"), target, tracker, 50));

      invokeRunMigration(engine, MigrationMode.FULL);

      assertThat(target.recordMigratedItemCalls, is(0));
      assertThat(engine.getProgress().failed(), is(1L));
      assertThat(target.markCompletedCalls, is(1));
   }

   @Test
   void runMigrationPersistsMultipleInputsAsSeparateAttachments(@TempDir Path tempDir) throws Exception {
      Path input1 = tempDir.resolve("doc-1.xml");
      Path input2 = tempDir.resolve("doc-2.xml");
      Files.writeString(input1, "payload-1");
      Files.writeString(input2, "payload-2");

      StubSourceService source = new StubSourceService();
      source.batches.add(List.of(row("oid-multi")));
      source.batches.add(List.of());
      source.handledObjects = List.of(
            new EvsHandledObjectSource(1, "INPUT", "doc-1.xml", input1.toString()),
            new EvsHandledObjectSource(2, "VALIDATED", "doc-2.xml", input2.toString())
      );

      StubLegacyReportParser parser = new StubLegacyReportParser();
      StubTargetService target = new StubTargetService(false);
      MigrationTracker tracker = new MigrationTracker(target);
      MigrationEngine engine = register(new MigrationEngine(source, parser, new MigrationItemMapper("super-admin"), target, tracker, 50));

      invokeRunMigration(engine, MigrationMode.FULL);

      assertThat(target.lastInputAttachmentsCount, is(2));
   }

   private MigrationEngine register(MigrationEngine engine) {
      engines.add(engine);
      return engine;
   }

   private void invokeRunMigration(MigrationEngine engine, MigrationMode mode) throws Exception {
      Method method = MigrationEngine.class.getDeclaredMethod("runMigration", MigrationMode.class);
      method.setAccessible(true);
      method.invoke(engine, mode);
   }

   private void waitForTerminalState(MigrationEngine engine) throws InterruptedException {
      int tries = 0;
      while (tries < 200 && engine.getState() == MigrationState.RUNNING) {
         Thread.sleep(10);
         tries++;
      }
      assertThat(engine.getState() == MigrationState.COMPLETED || engine.getState() == MigrationState.FAILED, is(true));
   }

   private EvsValidationSourceRow row(String oid) {
      return row(1, oid, Instant.parse("2026-01-01T10:00:00Z"));
   }

   private EvsValidationSourceRow row(int id, String oid, Instant date) {
      return new EvsValidationSourceRow(
            id,
            oid,
            date,
            "DONE_PASSED",
            "CDA",
            "svc",
            "1.0",
            "validator",
            "2.0",
            "owner",
            "acme",
            false,
            null,
            "GUI",
            "/tmp/validation.zip"
      );
   }

   private static final class StubSourceService extends EvsSourceService {
      private long fetchBatchCalls;
      private long fetchBatchAfterCalls;
      private long fetchBatchByOidsCalls;
      private long countReportsAfterCalls;
      private Set<String> lastRequestedOids = Set.of();
      private final List<List<EvsValidationSourceRow>> batches = new ArrayList<>();
      private final List<List<EvsValidationSourceRow>> incrementalBatches = new ArrayList<>();
      private final List<List<EvsValidationSourceRow>> retryBatches = new ArrayList<>();
      private List<EvsHandledObjectSource> handledObjects = List.of();

      @Override
      public long countReports() {
         return 1;
      }

      @Override
      public long countReportsAfter(MigrationCheckpoint checkpoint) {
         countReportsAfterCalls++;
         return 1;
      }

      @Override
      public List<EvsValidationSourceRow> fetchBatch(long offset, int limit) {
         fetchBatchCalls++;
         int index = (int) fetchBatchCalls - 1;
         return index < batches.size() ? batches.get(index) : List.of();
      }

      @Override
      public List<EvsValidationSourceRow> fetchBatchByOids(Set<String> oids, long offset, int limit) {
         fetchBatchByOidsCalls++;
         lastRequestedOids = oids == null ? Set.of() : oids;
         int index = (int) fetchBatchByOidsCalls - 1;
         return index < retryBatches.size() ? retryBatches.get(index) : List.of();
      }

      @Override
      public List<EvsValidationSourceRow> fetchBatchAfter(MigrationCheckpoint checkpoint, long offset, int limit) {
         fetchBatchAfterCalls++;
         int index = (int) fetchBatchAfterCalls - 1;
         return index < incrementalBatches.size() ? incrementalBatches.get(index) : List.of();
      }

      @Override
      public List<EvsHandledObjectSource> fetchHandledObjects(int processingId) {
         return handledObjects;
      }
   }

   private static final class StubLegacyReportParser extends LegacyReportParser {
      private Optional<ValidationReport> parsedReport = Optional.of(new ValidationReport());

      @Override
      public Optional<ValidationReport> parseValidationReport(String archivePath) {
         return parsedReport;
      }
   }

   private static final class StubTargetService implements MigrationTargetService {
      private final Deque<Boolean> completionChecks = new ArrayDeque<>();
      private final Map<String, MigrationError> failedReports = new HashMap<>();
      private PersistedMigrationState state;
      private int recordMigratedItemCalls;
      private int markCompletedCalls;
      private int lastInputAttachmentsCount;
      private int clearFailedReportsCalls;
      private MigrationMode lastMarkedMode;
      private MigrationCheckpoint lastMarkedCheckpoint;

      private StubTargetService(boolean... completionValues) {
         for (boolean value : completionValues) {
            completionChecks.add(value);
         }
         if (completionChecks.isEmpty()) {
            completionChecks.add(false);
         }
      }

      @Override
      public String getTargetType() {
         return "stub";
      }

      @Override
      public TargetHealth checkHealth() {
         return new TargetHealth(true, "ok");
      }

      @Override
      public String recordMigratedItem(Item item, List<InputAttachmentSource> inputAttachments) {
         recordMigratedItemCalls++;
         lastInputAttachmentsCount = inputAttachments == null ? 0 : inputAttachments.size();
         return "item-1";
      }

      @Override
      public boolean isMigrationCompleted() {
         Boolean value = completionChecks.pollFirst();
         if (value != null) {
            return value;
         }
         return state != null && state.completed();
      }

      @Override
      public Instant getCompletedAt() {
         return state == null ? null : state.completedAt();
      }

      @Override
      public Optional<CompletedMigrationMetrics> getCompletedMetrics() {
         return Optional.empty();
      }

      @Override
      public Optional<PersistedMigrationState> readMigrationState() {
         return Optional.ofNullable(state);
      }

      @Override
      public void writeMigrationState(PersistedMigrationState state) {
         this.state = state;
      }

      @Override
      public void markCompleted(net.ihe.gazelle.validation.gateway.migration.dto.MigrationProgress progress,
                                MigrationMode mode,
                                MigrationCheckpoint checkpoint) {
         markCompletedCalls++;
         lastMarkedMode = mode;
         lastMarkedCheckpoint = checkpoint;
         state = new PersistedMigrationState(
               progress.total(),
               progress.processed(),
               progress.succeeded(),
               progress.failed(),
               Instant.now(),
               true,
               Instant.now(),
               progress.previews(),
               progress.recentErrors(),
               checkpoint,
               mode == null ? null : mode.name(),
               Set.of(),
               Map.of()
         );
      }

      @Override
      public String findItemIdByEvsOid(String evsOid) {
         return null;
      }

      @Override
      public Optional<Map<String, Object>> readItemById(String itemId) {
         return Optional.empty();
      }

      @Override
      public Optional<InputAttachmentDownload> readInputAttachmentByItemId(String itemId) {
         return Optional.empty();
      }

      @Override
      public List<InputAttachmentMetadata> readInputAttachmentsByItemId(String itemId) {
         return List.of();
      }

      @Override
      public void recordFailedReport(String oid, MigrationError error) {
         failedReports.put(oid, error);
      }

      @Override
      public void removeFailedReport(String oid) {
         failedReports.remove(oid);
      }

      @Override
      public FailedReportsPage readFailedReports(int offset, int limit) {
         List<MigrationError> all = new ArrayList<>(failedReports.values());
         int end = Math.min(offset + limit, all.size());
         List<MigrationError> page = offset < all.size() ? all.subList(offset, end) : List.of();
         return new FailedReportsPage(page, all.size());
      }

      @Override
      public long countFailedReports() {
         return failedReports.size();
      }

      @Override
      public Set<String> readAllFailedOids() {
         return new HashSet<>(failedReports.keySet());
      }

      @Override
      public void clearFailedReports() {
         clearFailedReportsCalls++;
         failedReports.clear();
      }
   }
}
