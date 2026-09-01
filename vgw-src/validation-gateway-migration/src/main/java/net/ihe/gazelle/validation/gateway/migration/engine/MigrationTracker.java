package net.ihe.gazelle.validation.gateway.migration.engine;

import net.ihe.gazelle.validation.gateway.migration.dto.MigrationError;
import net.ihe.gazelle.validation.gateway.migration.dto.MigrationErrorType;
import net.ihe.gazelle.validation.gateway.migration.dto.MigrationPreview;
import net.ihe.gazelle.validation.gateway.migration.dto.MigrationProgress;
import net.ihe.gazelle.validation.gateway.migration.dto.MigrationCheckpoint;
import net.ihe.gazelle.validation.gateway.migration.exception.CategorizedMigrationException;
import net.ihe.gazelle.validation.gateway.migration.output.MigrationTargetService;
import net.ihe.gazelle.validation.gateway.migration.output.PersistedMigrationState;

import jakarta.enterprise.context.ApplicationScoped;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@ApplicationScoped
public class MigrationTracker {

   private static final int FLUSH_INTERVAL_REPORTS = 50;
   private static final long FLUSH_INTERVAL_MILLIS = 5_000;
   private static final int MAX_PREVIEWS = 5;
   private static final int MAX_RECENT_ERRORS = 100;

   private final MigrationTargetService targetService;

   // In-memory state fields
   private volatile boolean active;
   private long total;
   private long processed;
   private long succeeded;
   private long failed;
   private Instant startedAt;
   private List<MigrationPreview> previews = new ArrayList<>();
   private List<MigrationError> recentErrors = new ArrayList<>();
   private Set<String> allFailedOids = new HashSet<>();
   private MigrationCheckpoint lastSuccessfulCheckpoint;
   private String lastRunMode;

   // Flush tracking
   private long reportsSinceLastFlush;
   private long lastFlushTimeMillis;

   public MigrationTracker(MigrationTargetService targetService) {
      this.targetService = targetService;
   }

   public synchronized void start(long totalCount) {
      PersistedMigrationState currentState = readPersistedOrDefault();
      this.active = true;
      this.total = totalCount;
      this.processed = 0;
      this.succeeded = 0;
      this.failed = 0;
      this.startedAt = Instant.now();
      this.previews = new ArrayList<>(currentState.previews());
      this.recentErrors = new ArrayList<>();
      this.allFailedOids = new HashSet<>();
      this.lastSuccessfulCheckpoint = currentState.lastSuccessfulCheckpoint();
      this.lastRunMode = currentState.lastRunMode();
      this.reportsSinceLastFlush = 0;
      this.lastFlushTimeMillis = System.currentTimeMillis();

      flushState(false);
   }

   public synchronized void startRetry(long totalCount, Set<String> targetOids) {
      // Read existing state to preserve cumulative counters
      PersistedMigrationState currentState = readPersistedOrDefault();

      this.active = true;
      this.total = currentState.total();
      this.processed = currentState.processed();
      this.succeeded = currentState.succeeded();
      this.failed = currentState.failed();
      this.startedAt = currentState.startedAt();
      this.previews = new ArrayList<>(currentState.previews());
      this.recentErrors = new ArrayList<>();
      this.allFailedOids = new HashSet<>(targetOids);
      this.lastSuccessfulCheckpoint = currentState.lastSuccessfulCheckpoint();
      this.lastRunMode = currentState.lastRunMode();
      this.reportsSinceLastFlush = 0;
      this.lastFlushTimeMillis = System.currentTimeMillis();

      flushState(false);
   }

   public synchronized void startIncremental(long additionalCount) {
      PersistedMigrationState currentState = readPersistedOrDefault();

      this.active = true;
      this.total = currentState.total() + Math.max(0L, additionalCount);
      this.processed = currentState.processed();
      this.succeeded = currentState.succeeded();
      this.failed = currentState.failed();
      this.startedAt = currentState.startedAt() == null ? Instant.now() : currentState.startedAt();
      this.previews = new ArrayList<>(currentState.previews());
      // Preserve prior errors for incremental runs so operators keep historical context.
      this.recentErrors = new ArrayList<>(currentState.recentErrors());
      this.allFailedOids = readCurrentFailedOids(currentState);
      this.lastSuccessfulCheckpoint = currentState.lastSuccessfulCheckpoint();
      this.lastRunMode = currentState.lastRunMode();
      this.reportsSinceLastFlush = 0;
      this.lastFlushTimeMillis = System.currentTimeMillis();

      flushState(false);
   }

   public synchronized void markSuccess(MigrationPreview preview) {
      // Check if this OID was previously failed (for cumulative retry logic)
      boolean wasPreviouslyFailed = allFailedOids.contains(preview.evsOid());

      // Update in-memory counters
      processed++;
      succeeded++;
      if (wasPreviouslyFailed) {
         failed--;
         allFailedOids.remove(preview.evsOid());
         targetService.removeFailedReport(preview.evsOid());
      }

      // Update previews (keep last MAX_PREVIEWS)
      previews.addFirst(preview);
      if (previews.size() > MAX_PREVIEWS) {
         previews = new ArrayList<>(previews.subList(0, MAX_PREVIEWS));
      }

      maybeFlush();
   }

   public synchronized void markFailure(String oid, Exception exception) {
      String message = exception == null ? "Unknown migration error" : exception.getMessage();
      MigrationErrorType errorType = determineErrorType(exception);
      MigrationError error = new MigrationError(oid, message, Instant.now(), errorType);

      // Update in-memory counters
      processed++;
      boolean wasAlreadyFailed = allFailedOids.contains(oid);
      if (!wasAlreadyFailed) {
         failed++;
         allFailedOids.add(oid);
      }

      // Update recent errors (keep last MAX_RECENT_ERRORS)
      recentErrors.addFirst(error);
      if (recentErrors.size() > MAX_RECENT_ERRORS) {
         recentErrors = new ArrayList<>(recentErrors.subList(0, MAX_RECENT_ERRORS));
      }

      // Write individual failure to separate collection
      targetService.recordFailedReport(oid, error);

      maybeFlush();
   }

   public synchronized void complete() {
      active = false;
      flushState(true);
   }

   public synchronized MigrationProgress snapshot() {
      if (active) {
         return buildProgressFromMemory();
      }
      // When not active, fall back to reading from persisted state (for post-restart dashboard)
      return buildProgressFromPersisted();
   }

   private MigrationProgress buildProgressFromMemory() {
      Instant end = Instant.now();
      long elapsedMillis = startedAt == null ? 0 : Math.max(0L, Duration.between(startedAt, end).toMillis());
      long elapsedSeconds = elapsedMillis / 1000;
      double reportsPerSecond = elapsedMillis == 0 ? 0.0 : (double) processed / elapsedMillis * 1000.0;
      double percentage = total == 0 ? 0.0 : (processed * 100.0) / total;

      Long etaSeconds = null;
      if (processed > 0 && reportsPerSecond > 0.0 && total > processed) {
         etaSeconds = Math.round((total - processed) / reportsPerSecond);
      }

      return new MigrationProgress(
            total, processed, succeeded, failed,
            percentage, reportsPerSecond, elapsedSeconds, etaSeconds,
            new ArrayList<>(previews),
            new ArrayList<>(recentErrors)
      );
   }

   private MigrationProgress buildProgressFromPersisted() {
      PersistedMigrationState state = targetService.readMigrationState().orElseGet(() -> new PersistedMigrationState(
            0, 0, 0, 0, null, false, null, List.of(), List.of(), null, null, new HashSet<>(), new HashMap<>()
      ));
      Instant stStartedAt = state.startedAt();
      Instant end = state.completed() && state.completedAt() != null ? state.completedAt() : Instant.now();
      long elapsedMillis = stStartedAt == null ? 0 : Math.max(0L, Duration.between(stStartedAt, end).toMillis());
      long elapsedSeconds = elapsedMillis / 1000;
      double reportsPerSecond = elapsedMillis == 0 ? 0.0 : (double) state.processed() / elapsedMillis * 1000.0;
      double percentage = state.total() == 0 ? 0.0 : (state.processed() * 100.0) / state.total();
      if (state.completed()) {
         percentage = 100.0;
      }
      Long etaSeconds = null;
      if (!state.completed() && state.processed() > 0 && reportsPerSecond > 0.0 && state.total() > state.processed()) {
         etaSeconds = Math.round((state.total() - state.processed()) / reportsPerSecond);
      }
      return new MigrationProgress(
            state.total(), state.processed(), state.succeeded(), state.failed(),
            percentage, reportsPerSecond, elapsedSeconds, etaSeconds,
            new ArrayList<>(state.previews()),
            new ArrayList<>(state.recentErrors())
      );
   }

   private void maybeFlush() {
      reportsSinceLastFlush++;
      long now = System.currentTimeMillis();
      if (reportsSinceLastFlush >= FLUSH_INTERVAL_REPORTS || (now - lastFlushTimeMillis) >= FLUSH_INTERVAL_MILLIS) {
         flushState(false);
      }
   }

   private void flushState(boolean completed) {
      PersistedMigrationState state = new PersistedMigrationState(
            total, processed, succeeded, failed,
            startedAt,
            completed,
            completed ? Instant.now() : null,
            new ArrayList<>(previews),
            new ArrayList<>(recentErrors),
            lastSuccessfulCheckpoint,
            lastRunMode,
            new HashSet<>(),     // allFailedOids stored separately now
            new HashMap<>()      // allFailedReportsMap stored separately now
      );
      targetService.writeMigrationState(state);
      reportsSinceLastFlush = 0;
      lastFlushTimeMillis = System.currentTimeMillis();
   }

   private MigrationErrorType determineErrorType(Exception exception) {
      if (exception instanceof CategorizedMigrationException) {
         return ((CategorizedMigrationException) exception).getType();
      }
      return MigrationErrorType.UNKNOWN_ERROR;
   }

   private PersistedMigrationState readPersistedOrDefault() {
      return targetService.readMigrationState().orElseGet(() -> new PersistedMigrationState(
            0, 0, 0, 0, Instant.now(), false, null, List.of(), List.of(), null, null, new HashSet<>(), new HashMap<>()
      ));
   }

   private Set<String> readCurrentFailedOids(PersistedMigrationState currentState) {
      try {
         return new HashSet<>(targetService.readAllFailedOids());
      } catch (UnsupportedOperationException ignored) {
         return new HashSet<>(currentState.allFailedOids());
      }
   }
}
