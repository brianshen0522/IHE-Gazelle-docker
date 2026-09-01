package net.ihe.gazelle.validation.gateway.migration.output;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kereval.gazelle.datahouse.api.business.record.Item;
import com.kereval.gazelle.datahouse.api.business.record.RefType;
import com.kereval.gazelle.datahouse.api.business.record.Reference;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import net.ihe.gazelle.security.business.acl.AccessControlList;
import net.ihe.gazelle.validation.gateway.migration.dto.FailedReportsPage;
import net.ihe.gazelle.validation.gateway.migration.dto.MigrationError;
import net.ihe.gazelle.validation.gateway.migration.dto.MigrationCheckpoint;
import net.ihe.gazelle.validation.gateway.migration.dto.MigrationProgress;
import net.ihe.gazelle.validation.gateway.migration.engine.MigrationMode;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

@ApplicationScoped
public class MongoTargetService implements MigrationTargetService {

   private static final String TARGET_TYPE = "mongo";
   private static final String ITEMS_COLLECTION = "items";
   private static final String FAILURES_COLLECTION = "migration_failures";
   private static final String MARKER_ITEM_TYPE = "MIGRATION_METADATA";
   private static final String MARKER_NAME = "evs-to-validation-gateway";
   private static final String FIELD_REF_TYPE = "refType";
   private static final String FIELD_VALUE = "value";
   private static final String FIELD_CONTENT = "content";
   private static final String FIELD_ADDITIONAL_PARAMETERS = "additionalParameters";
   private static final String FIELD_REFERENCES = "references";
   private static final String FIELD_ACL = "accessControlListMongo";
   private static final String FIELD_MIGRATION_NAME = "additionalParameters.migration_name";
   private static final String FIELD_EVS_OID = "evsOid";
   private static final String FIELD_ITEM_ID = "itemId";
   private static final String FIELD_MESSAGE = "message";
   private static final String FIELD_OCCURRED_AT = "occurredAt";
   private static final String VALIDATION_INPUT_REFERENCE_TYPE = "VALIDATION_INPUT";
   private static final String LEGACY_INPUT_DOCUMENT_REFERENCE_TYPE = "INPUT_DOCUMENT";
   private static final String ORIGINAL_DOCUMENT_REFERENCE_TYPE = "ORIGINAL_DOCUMENT";

   private final String connectionString;
   private final String databaseName;
   private final boolean autoCreateIndexes;
   private final int maxPoolSize;
   private final int minPoolSize;
   private final int maxConnecting;
   private final long maxWaitTimeMs;
   private final long serverSelectionTimeoutMs;
   private final long connectTimeoutMs;
   private final long readTimeoutMs;
   private final boolean retryWrites;
   private final boolean retryReads;
   private final ValidationReportInputAttachmentUpdater inputAttachmentUpdater;
   private final MongoAttachmentService attachmentService;
   private final MongoMigrationStateService migrationStateService;
   private final MongoFailureService failureService;

   private volatile MongoClient mongoClient;
   private volatile MongoDatabase mongoDatabase;

   public MongoTargetService(
         @ConfigProperty(name = "mongodb.connection.string", defaultValue = "mongodb://localhost:27017")
         String connectionString,
         @ConfigProperty(name = "mongodb.database", defaultValue = "datahouse")
         String databaseName,
         @ConfigProperty(name = "migration.target.mongo.create-indexes", defaultValue = "true")
         boolean autoCreateIndexes,
         @ConfigProperty(name = "mongodb.connection.max-pool-size", defaultValue = "20")
         int maxPoolSize,
         @ConfigProperty(name = "mongodb.connection.min-pool-size", defaultValue = "0")
         int minPoolSize,
         @ConfigProperty(name = "mongodb.connection.max-connecting", defaultValue = "2")
         int maxConnecting,
         @ConfigProperty(name = "mongodb.connection.max-wait-ms", defaultValue = "15000")
         long maxWaitTimeMs,
         @ConfigProperty(name = "mongodb.connection.server-selection-timeout-ms", defaultValue = "15000")
         long serverSelectionTimeoutMs,
         @ConfigProperty(name = "mongodb.connection.connect-timeout-ms", defaultValue = "10000")
         long connectTimeoutMs,
         @ConfigProperty(name = "mongodb.connection.read-timeout-ms", defaultValue = "0")
         long readTimeoutMs,
         @ConfigProperty(name = "mongodb.connection.retry-writes", defaultValue = "false")
         boolean retryWrites,
         @ConfigProperty(name = "mongodb.connection.retry-reads", defaultValue = "true")
         boolean retryReads
   ) {
      this.connectionString = connectionString;
      this.databaseName = databaseName;
      this.autoCreateIndexes = autoCreateIndexes;
      this.maxPoolSize = maxPoolSize;
      this.minPoolSize = minPoolSize;
      this.maxConnecting = maxConnecting;
      this.maxWaitTimeMs = maxWaitTimeMs;
      this.serverSelectionTimeoutMs = serverSelectionTimeoutMs;
      this.connectTimeoutMs = connectTimeoutMs;
      this.readTimeoutMs = readTimeoutMs;
      this.retryWrites = retryWrites;
      this.retryReads = retryReads;
      this.inputAttachmentUpdater = new ValidationReportInputAttachmentUpdater(new ObjectMapper());
      this.attachmentService = new MongoAttachmentService(
            this::database,
            this::itemsCollection,
            FIELD_REFERENCES,
            FIELD_REF_TYPE,
            FIELD_VALUE,
            FIELD_CONTENT,
            FIELD_ITEM_ID,
            VALIDATION_INPUT_REFERENCE_TYPE,
            ORIGINAL_DOCUMENT_REFERENCE_TYPE,
            LEGACY_INPUT_DOCUMENT_REFERENCE_TYPE
      );
      this.migrationStateService = new MongoMigrationStateService(
            this::itemsCollection,
            MARKER_ITEM_TYPE,
            MARKER_NAME,
            FIELD_MIGRATION_NAME,
            FIELD_CONTENT,
            FIELD_ADDITIONAL_PARAMETERS,
            FIELD_REFERENCES,
            FIELD_ACL,
            FIELD_EVS_OID,
            FIELD_ITEM_ID,
            FIELD_MESSAGE,
            FIELD_OCCURRED_AT
      );
      this.failureService = new MongoFailureService(
            this::failuresCollection,
            FIELD_MESSAGE,
            FIELD_OCCURRED_AT
      );
   }

   public String getTargetType() {
      return TARGET_TYPE;
   }

   public TargetHealth checkHealth() {
      try {
         database().runCommand(new Document("ping", 1));
         return new TargetHealth(true, "MongoDB target accessible");
      } catch (Exception e) {
         return new TargetHealth(false, "MongoDB target unreachable: " + e.getMessage());
      }
   }

   public String recordMigratedItem(Item item, List<InputAttachmentSource> inputAttachments) {
      if (item == null) {
         throw new IllegalArgumentException("Item must not be null");
      }

      List<Document> references = toReferenceDocuments(item.getReferences());
      List<InputAttachmentMetadata> uploadedAttachments = new ArrayList<>();
      if (inputAttachments != null) {
         for (InputAttachmentSource source : inputAttachments) {
            if (source == null || source.path() == null) {
               continue;
            }
            String attachmentId = attachmentService.uploadAttachment(source);
            uploadedAttachments.add(new InputAttachmentMetadata(attachmentId, source.filename(), source.contentType()));
            references.add(new Document()
                  .append(FIELD_REF_TYPE, RefType.ATTACHMENT.name())
                  .append("type", VALIDATION_INPUT_REFERENCE_TYPE)
                  .append("name", source.filename())
                  .append(FIELD_VALUE, attachmentId));
         }
      }
      if (!uploadedAttachments.isEmpty()) {
         item.setContent(inputAttachmentUpdater.withInputAttachmentReferences(item.getContent(), uploadedAttachments));
      }

      Document document = new Document()
            .append("type", item.getType())
            .append("date", item.getDate() == null ? new Date() : item.getDate())
            .append(FIELD_CONTENT, parseContent(item.getContent()))
            .append(FIELD_ADDITIONAL_PARAMETERS, item.getAdditionalParameters() == null ? Map.of() : item.getAdditionalParameters())
            .append(FIELD_REFERENCES, references)
            .append(FIELD_ACL, toAclDocument(item.getAccessControlList()));

      ObjectId insertedId = itemsCollection().insertOne(document).getInsertedId().asObjectId().getValue();
      return insertedId.toHexString();
   }

   public boolean isMigrationCompleted() {
      return readMigrationState().map(PersistedMigrationState::completed).orElse(false);
   }

   public Instant getCompletedAt() {
      return readMigrationState().map(PersistedMigrationState::completedAt).orElse(null);
   }

   public Optional<CompletedMigrationMetrics> getCompletedMetrics() {
      return readMigrationState()
            .map(state -> new CompletedMigrationMetrics(
                  state.total(),
                  state.succeeded(),
                  state.failed(),
                  state.startedAt() == null ? 0L : Math.max(0L, java.time.Duration.between(
                        state.startedAt(),
                        state.completedAt() == null ? Instant.now() : state.completedAt()
                  ).getSeconds())
            ));
   }

   public Optional<PersistedMigrationState> readMigrationState() {
      return migrationStateService.readMigrationState();
   }

   public void writeMigrationState(PersistedMigrationState state) {
      migrationStateService.writeMigrationState(state);
   }

   public void markCompleted(MigrationProgress progress, MigrationMode mode, MigrationCheckpoint checkpoint) {
      migrationStateService.markCompleted(progress, mode, checkpoint);
   }

   public void recordFailedReport(String oid, MigrationError error) {
      failureService.recordFailedReport(oid, error);
   }

   public void removeFailedReport(String oid) {
      failureService.removeFailedReport(oid);
   }

   public FailedReportsPage readFailedReports(int offset, int limit) {
      return failureService.readFailedReports(offset, limit);
   }

   public long countFailedReports() {
      return failureService.countFailedReports();
   }

   public Set<String> readAllFailedOids() {
      return failureService.readAllFailedOids();
   }

   public void clearFailedReports() {
      failureService.clearFailedReports();
   }

   public String findItemIdByEvsOid(String evsOid) {
      if (evsOid == null || evsOid.isBlank()) {
         return null;
      }
      Document item = itemsCollection().find(and(
            eq("type", "VALIDATION_REPORT"),
            eq("additionalParameters.evs_oid", evsOid)
      )).first();
      if (item == null) {
         return null;
      }
      ObjectId id = item.getObjectId("_id");
      return id == null ? null : id.toHexString();
   }

   public Optional<Map<String, Object>> readItemById(String itemId) {
      if (itemId == null || itemId.isBlank() || !ObjectId.isValid(itemId)) {
         return Optional.empty();
      }
      Document item = itemsCollection().find(eq("_id", new ObjectId(itemId))).first();
      if (item == null) {
         return Optional.empty();
      }

      Map<String, Object> payload = new LinkedHashMap<>();
      ObjectId id = item.getObjectId("_id");
      payload.put("id", id == null ? null : id.toHexString());
      payload.put("type", item.getString("type"));
      payload.put("date", item.getDate("date"));
      payload.put(FIELD_CONTENT, item.get(FIELD_CONTENT));
      payload.put(FIELD_ADDITIONAL_PARAMETERS, item.get(FIELD_ADDITIONAL_PARAMETERS));
      payload.put(FIELD_REFERENCES, item.get(FIELD_REFERENCES));
      payload.put(FIELD_ACL, item.get(FIELD_ACL));
      return Optional.of(payload);
   }

   public Optional<InputAttachmentDownload> readInputAttachmentByItemId(String itemId) {
      return attachmentService.readInputAttachmentByItemId(itemId);
   }

   public List<InputAttachmentMetadata> readInputAttachmentsByItemId(String itemId) {
      return attachmentService.readInputAttachmentsByItemId(itemId);
   }

   public Optional<InputAttachmentDownload> readInputAttachmentByItemAndAttachmentId(String itemId, String attachmentId) {
      return attachmentService.readInputAttachmentByItemAndAttachmentId(itemId, attachmentId);
   }

   private List<Document> toReferenceDocuments(List<Reference> references) {
      if (references == null || references.isEmpty()) {
         return new ArrayList<>();
      }
      List<Document> mapped = new ArrayList<>(references.size());
      for (Reference reference : references) {
         mapped.add(new Document()
               .append(FIELD_REF_TYPE, reference.getRefType() == null ? null : reference.getRefType().name())
               .append("type", reference.getType())
               .append("name", reference.getName())
               .append(FIELD_VALUE, reference.getValue()));
      }
      return mapped;
   }

   private Document parseContent(String content) {
      if (content == null || content.isBlank()) {
         return new Document();
      }
      return Document.parse(content);
   }

   private Document toAclDocument(AccessControlList acl) {
      AccessControlList safeAcl = acl == null ? new AccessControlList().setPublic(true).setOwners(Set.of()) : acl;
      List<Document> entries = new ArrayList<>();
      safeAcl.getOwners().forEach(owner -> entries.add(new Document("role", "owner").append("user", owner)));
      safeAcl.getReaders().forEach(reader -> entries.add(new Document("role", "reader").append("user", reader)));
      safeAcl.getEditors().forEach(editor -> entries.add(new Document("role", "editor").append("user", editor)));

      return new Document()
            .append("entries", entries)
            .append("public", safeAcl.isPublic())
            .append("readAccessKey", safeAcl.getReadAccessKey() == null ? "" : safeAcl.getReadAccessKey());
   }

   private MongoCollection<Document> itemsCollection() {
      return database().getCollection(ITEMS_COLLECTION);
   }

   private MongoCollection<Document> failuresCollection() {
      return database().getCollection(FAILURES_COLLECTION);
   }

   private MongoDatabase database() {
      MongoDatabase local = mongoDatabase;
      if (local == null) {
         synchronized (this) {
            local = mongoDatabase;
            if (local == null) {
               MongoClientSettings settings = MongoClientSettings.builder()
                     .applyConnectionString(new ConnectionString(connectionString))
                     .retryWrites(retryWrites)
                     .retryReads(retryReads)
                     .applyToClusterSettings(builder ->
                           builder.serverSelectionTimeout(serverSelectionTimeoutMs, TimeUnit.MILLISECONDS))
                     .applyToConnectionPoolSettings(builder -> builder
                           .maxSize(maxPoolSize)
                           .minSize(minPoolSize)
                           .maxConnecting(maxConnecting)
                           .maxWaitTime(maxWaitTimeMs, TimeUnit.MILLISECONDS))
                     .applyToSocketSettings(builder -> builder
                           .connectTimeout((int) connectTimeoutMs, TimeUnit.MILLISECONDS)
                           .readTimeout((int) readTimeoutMs, TimeUnit.MILLISECONDS))
                     .build();
               mongoClient = MongoClients.create(settings);
               local = mongoClient.getDatabase(databaseName);
               mongoDatabase = local;
               if (autoCreateIndexes) {
                  ensureIndexes(local);
               }
            }
         }
      }
      return local;
   }

   private void ensureIndexes(MongoDatabase database) {
      MongoCollection<Document> collection = database.getCollection(ITEMS_COLLECTION);
      collection.createIndex(new Document("type", 1));
      collection.createIndex(new Document("type", 1).append("additionalParameters.evs_oid", 1));
      collection.createIndex(new Document("type", 1).append(FIELD_MIGRATION_NAME, 1));

      MongoCollection<Document> failures = database.getCollection(FAILURES_COLLECTION);
      failures.createIndex(new Document(FIELD_OCCURRED_AT, -1));
   }

   @PreDestroy
   void destroy() {
      MongoClient local = mongoClient;
      if (local != null) {
         local.close();
      }
   }
}
