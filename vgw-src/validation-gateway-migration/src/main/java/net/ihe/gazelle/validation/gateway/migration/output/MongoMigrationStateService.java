package net.ihe.gazelle.validation.gateway.migration.output;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.ReplaceOptions;
import net.ihe.gazelle.validation.gateway.migration.dto.MigrationError;
import net.ihe.gazelle.validation.gateway.migration.dto.MigrationErrorType;
import net.ihe.gazelle.validation.gateway.migration.dto.MigrationCheckpoint;
import net.ihe.gazelle.validation.gateway.migration.dto.MigrationPreview;
import net.ihe.gazelle.validation.gateway.migration.dto.MigrationProgress;
import net.ihe.gazelle.validation.gateway.migration.engine.MigrationMode;
import org.bson.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

class MongoMigrationStateService {

   private final Supplier<MongoCollection<Document>> itemsCollectionSupplier;
   private final String markerItemType;
   private final String markerName;
   private final String fieldMigrationName;
   private final String fieldContent;
   private final String fieldAdditionalParameters;
   private final String fieldReferences;
   private final String fieldAcl;
   private final String fieldEvsOid;
   private final String fieldItemId;
   private final String fieldMessage;
   private final String fieldOccurredAt;

   MongoMigrationStateService(Supplier<MongoCollection<Document>> itemsCollectionSupplier,
                              String markerItemType,
                              String markerName,
                              String fieldMigrationName,
                              String fieldContent,
                              String fieldAdditionalParameters,
                              String fieldReferences,
                              String fieldAcl,
                              String fieldEvsOid,
                              String fieldItemId,
                              String fieldMessage,
                              String fieldOccurredAt) {
      this.itemsCollectionSupplier = itemsCollectionSupplier;
      this.markerItemType = markerItemType;
      this.markerName = markerName;
      this.fieldMigrationName = fieldMigrationName;
      this.fieldContent = fieldContent;
      this.fieldAdditionalParameters = fieldAdditionalParameters;
      this.fieldReferences = fieldReferences;
      this.fieldAcl = fieldAcl;
      this.fieldEvsOid = fieldEvsOid;
      this.fieldItemId = fieldItemId;
      this.fieldMessage = fieldMessage;
      this.fieldOccurredAt = fieldOccurredAt;
   }

   Optional<PersistedMigrationState> readMigrationState() {
      Document marker = findMarkerItem();
      if (marker == null) {
         return Optional.empty();
      }
      Document content = marker.get(fieldContent, Document.class);
      if (content == null) {
         return Optional.empty();
      }

      Instant startedAt = parseInstant(content.getString("startedAt"));
      Instant completedAt = parseInstant(content.getString("completedAt"));
      boolean completed = content.get("completed") instanceof Boolean done ? done : completedAt != null;
      long total = content.get("total") instanceof Number nTotal ? nTotal.longValue() : 0L;
      long processed = content.get("processed") instanceof Number nProcessed ? nProcessed.longValue() : 0L;
      long succeeded = content.get("succeeded") instanceof Number nSucceeded ? nSucceeded.longValue() : 0L;
      long failed = content.get("failed") instanceof Number nFailed ? nFailed.longValue() : 0L;

      List<MigrationPreview> previews = parsePreviews(content.get("previews"));
      List<MigrationError> recentErrors = parseErrors(content.get("recentErrors"));
      MigrationCheckpoint checkpoint = parseCheckpoint(content);
      String lastRunMode = content.getString("lastRunMode");
      Set<String> allFailedOids = parseFailedOids(content.get("allFailedOids"));
      Map<String, MigrationError> allFailedReportsMap = parseFailedReportsMap(content.get("allFailedReportsMap"));
      return Optional.of(new PersistedMigrationState(
            total,
            processed,
            succeeded,
            failed,
            startedAt,
            completed,
            completedAt,
            previews,
            recentErrors,
            checkpoint,
            lastRunMode,
            allFailedOids,
            allFailedReportsMap
      ));
   }

   void writeMigrationState(PersistedMigrationState state) {
      Document content = new Document()
            .append("total", state.total())
            .append("processed", state.processed())
            .append("succeeded", state.succeeded())
            .append("failed", state.failed())
            .append("startedAt", state.startedAt() == null ? null : state.startedAt().toString())
            .append("completed", state.completed())
            .append("completedAt", state.completedAt() == null ? null : state.completedAt().toString())
            .append("previews", toPreviewDocuments(state.previews()))
            .append("recentErrors", toErrorDocuments(state.recentErrors()))
            .append("lastSuccessfulValidationDate", state.lastSuccessfulCheckpoint() == null
                  ? null
                  : state.lastSuccessfulCheckpoint().validationDate() == null
                  ? null
                  : state.lastSuccessfulCheckpoint().validationDate().toString())
            .append("lastSuccessfulSourceId", state.lastSuccessfulCheckpoint() == null
                  ? null
                  : state.lastSuccessfulCheckpoint().sourceId())
            .append("lastRunMode", state.lastRunMode())
            .append("allFailedOids", new ArrayList<>(state.allFailedOids()))
            .append("allFailedReportsMap", toFailedReportsMapDocument(state.allFailedReportsMap()));

      Document marker = new Document()
            .append("type", markerItemType)
            .append("date", new java.util.Date())
            .append(fieldContent, content)
            .append(fieldAdditionalParameters, Map.of(
                  "migration_name", markerName,
                  "completed_at", state.completedAt() == null ? "" : state.completedAt().toString()))
            .append(fieldReferences, List.of())
            .append(fieldAcl, new Document()
                  .append("entries", List.of())
                  .append("public", true)
                  .append("readAccessKey", ""));

      itemsCollectionSupplier.get().replaceOne(
            and(eq("type", markerItemType), eq(fieldMigrationName, markerName)),
            marker,
            new ReplaceOptions().upsert(true)
      );
   }

   void markCompleted(MigrationProgress progress, MigrationMode mode, MigrationCheckpoint runCheckpoint) {
      PersistedMigrationState current = readMigrationState().orElse(new PersistedMigrationState(
            progress.total(),
            progress.processed(),
            progress.succeeded(),
            progress.failed(),
            Instant.now().minusSeconds(progress.elapsedSeconds()),
            false,
            null,
            progress.previews(),
            progress.recentErrors(),
            null,
            null,
            new java.util.HashSet<>(),
            new java.util.HashMap<>()
      ));

      MigrationCheckpoint checkpoint = selectMostRecentCheckpoint(current.lastSuccessfulCheckpoint(), runCheckpoint);

      PersistedMigrationState completed = new PersistedMigrationState(
            progress.total(),
            progress.total(),
            progress.succeeded(),
            progress.failed(),
            current.startedAt() == null ? Instant.now().minusSeconds(progress.elapsedSeconds()) : current.startedAt(),
            true,
            Instant.now(),
            progress.previews(),
            progress.recentErrors(),
            checkpoint,
            mode == null ? current.lastRunMode() : mode.name(),
            new java.util.HashSet<>(),
            new java.util.HashMap<>()
      );
      writeMigrationState(completed);
   }

   private MigrationCheckpoint parseCheckpoint(Document content) {
      Instant validationDate = parseInstant(content.getString("lastSuccessfulValidationDate"));
      Integer sourceId = content.get("lastSuccessfulSourceId") instanceof Number n ? n.intValue() : null;
      if (validationDate == null || sourceId == null) {
         return null;
      }
      return new MigrationCheckpoint(validationDate, sourceId);
   }

   private MigrationCheckpoint selectMostRecentCheckpoint(MigrationCheckpoint current, MigrationCheckpoint candidate) {
      if (current == null) {
         return candidate;
      }
      if (candidate == null) {
         return current;
      }
      int compare = candidate.validationDate().compareTo(current.validationDate());
      if (compare > 0) {
         return candidate;
      }
      if (compare < 0) {
         return current;
      }
      return candidate.sourceId() > current.sourceId() ? candidate : current;
   }

   private Document findMarkerItem() {
      return itemsCollectionSupplier.get().find(and(
            eq("type", markerItemType),
            eq(fieldMigrationName, markerName)
      )).first();
   }

   private Instant parseInstant(String raw) {
      if (raw == null || raw.isBlank() || "null".equals(raw)) {
         return null;
      }
      try {
         return Instant.parse(raw);
      } catch (Exception e) {
         return null;
      }
   }

   private List<Document> toPreviewDocuments(List<MigrationPreview> previews) {
      if (previews == null) {
         return List.of();
      }
      List<Document> documents = new ArrayList<>();
      for (MigrationPreview preview : previews) {
         documents.add(new Document()
               .append(fieldEvsOid, preview.evsOid())
               .append(fieldItemId, preview.itemId())
               .append("result", preview.result())
               .append("validationDate", preview.validationDate() == null ? null : preview.validationDate().toString()));
      }
      return documents;
   }

   private List<Document> toErrorDocuments(List<MigrationError> errors) {
      if (errors == null) {
         return List.of();
      }
      List<Document> documents = new ArrayList<>();
      for (MigrationError error : errors) {
         documents.add(new Document()
               .append(fieldEvsOid, error.evsOid())
               .append(fieldMessage, error.message())
               .append(fieldOccurredAt, error.occurredAt() == null ? null : error.occurredAt().toString())
               .append("type", error.type() == null ? null : error.type().name()));
      }
      return documents;
   }

   private List<MigrationPreview> parsePreviews(Object raw) {
      if (!(raw instanceof List<?> list)) {
         return List.of();
      }
      List<MigrationPreview> previews = new ArrayList<>();
      for (Object entry : list) {
         if (!(entry instanceof Document doc)) {
            continue;
         }
         previews.add(new MigrationPreview(
               doc.getString(fieldEvsOid),
               doc.getString(fieldItemId),
               doc.getString("result"),
               parseInstant(doc.getString("validationDate"))
         ));
      }
      return previews;
   }

   private List<MigrationError> parseErrors(Object raw) {
      if (!(raw instanceof List<?> list)) {
         return List.of();
      }
      List<MigrationError> errors = new ArrayList<>();
      for (Object entry : list) {
         if (!(entry instanceof Document doc)) {
            continue;
         }
         MigrationErrorType type = parseErrorType(doc.getString("type"));
         errors.add(new MigrationError(
               doc.getString(fieldEvsOid),
               doc.getString(fieldMessage),
               parseInstant(doc.getString(fieldOccurredAt)),
               type
         ));
      }
      return errors;
   }

   private Set<String> parseFailedOids(Object raw) {
      if (!(raw instanceof List<?> list)) {
         return new java.util.HashSet<>();
      }
      Set<String> oids = new java.util.HashSet<>();
      for (Object entry : list) {
         if (entry instanceof String oid) {
            oids.add(oid);
         }
      }
      return oids;
   }

   private MigrationErrorType parseErrorType(String raw) {
      if (raw == null || raw.isBlank()) {
         return MigrationErrorType.UNKNOWN_ERROR;
      }
      try {
         return MigrationErrorType.valueOf(raw);
      } catch (IllegalArgumentException e) {
         return MigrationErrorType.UNKNOWN_ERROR;
      }
   }

   private Document toFailedReportsMapDocument(Map<String, MigrationError> map) {
      if (map == null || map.isEmpty()) {
         return new Document();
      }
      Document document = new Document();
      for (Map.Entry<String, MigrationError> entry : map.entrySet()) {
         MigrationError error = entry.getValue();
         document.append(entry.getKey(), new Document()
               .append(fieldEvsOid, error.evsOid())
               .append(fieldMessage, error.message())
               .append(fieldOccurredAt, error.occurredAt() == null ? null : error.occurredAt().toString())
               .append("type", error.type() == null ? null : error.type().name()));
      }
      return document;
   }

   private Map<String, MigrationError> parseFailedReportsMap(Object raw) {
      if (!(raw instanceof Document doc)) {
         return new java.util.HashMap<>();
      }
      Map<String, MigrationError> map = new java.util.HashMap<>();
      for (String key : doc.keySet()) {
         Object value = doc.get(key);
         if (!(value instanceof Document errorDoc)) {
            continue;
         }
         MigrationErrorType type = parseErrorType(errorDoc.getString("type"));
         map.put(key, new MigrationError(
               errorDoc.getString(fieldEvsOid),
               errorDoc.getString(fieldMessage),
               parseInstant(errorDoc.getString(fieldOccurredAt)),
               type
         ));
      }
      return map;
   }
}
