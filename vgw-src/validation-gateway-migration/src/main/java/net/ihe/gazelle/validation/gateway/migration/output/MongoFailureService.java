package net.ihe.gazelle.validation.gateway.migration.output;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.ReplaceOptions;
import net.ihe.gazelle.validation.gateway.migration.dto.FailedReportsPage;
import net.ihe.gazelle.validation.gateway.migration.dto.MigrationError;
import net.ihe.gazelle.validation.gateway.migration.dto.MigrationErrorType;
import org.bson.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import static com.mongodb.client.model.Filters.eq;

class MongoFailureService {

   private final Supplier<MongoCollection<Document>> failuresCollectionSupplier;
   private final String fieldMessage;
   private final String fieldOccurredAt;

   MongoFailureService(Supplier<MongoCollection<Document>> failuresCollectionSupplier,
                       String fieldMessage,
                       String fieldOccurredAt) {
      this.failuresCollectionSupplier = failuresCollectionSupplier;
      this.fieldMessage = fieldMessage;
      this.fieldOccurredAt = fieldOccurredAt;
   }

   void recordFailedReport(String oid, MigrationError error) {
      Document doc = new Document("_id", oid)
            .append(fieldMessage, error.message())
            .append(fieldOccurredAt, error.occurredAt() == null ? null : error.occurredAt().toString())
            .append("type", error.type() == null ? null : error.type().name());
      failuresCollectionSupplier.get().replaceOne(eq("_id", oid), doc, new ReplaceOptions().upsert(true));
   }

   void removeFailedReport(String oid) {
      failuresCollectionSupplier.get().deleteOne(eq("_id", oid));
   }

   FailedReportsPage readFailedReports(int offset, int limit) {
      long total = failuresCollectionSupplier.get().countDocuments();
      List<MigrationError> errors = new ArrayList<>();
      for (Document doc : failuresCollectionSupplier.get().find()
            .sort(new Document(fieldOccurredAt, -1))
            .skip(offset)
            .limit(limit)) {
         MigrationErrorType errorType = parseErrorType(doc.getString("type"));
         errors.add(new MigrationError(
               doc.getString("_id"),
               doc.getString(fieldMessage),
               parseInstant(doc.getString(fieldOccurredAt)),
               errorType
         ));
      }
      return new FailedReportsPage(errors, total);
   }

   long countFailedReports() {
      return failuresCollectionSupplier.get().countDocuments();
   }

   Set<String> readAllFailedOids() {
      Set<String> oids = new java.util.HashSet<>();
      for (Document doc : failuresCollectionSupplier.get().find().projection(new Document("_id", 1))) {
         oids.add(doc.getString("_id"));
      }
      return oids;
   }

   void clearFailedReports() {
      failuresCollectionSupplier.get().drop();
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
}
