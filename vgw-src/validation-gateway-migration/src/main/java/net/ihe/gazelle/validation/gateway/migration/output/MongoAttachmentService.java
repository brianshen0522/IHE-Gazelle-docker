package net.ihe.gazelle.validation.gateway.migration.output;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static com.mongodb.client.model.Filters.eq;

class MongoAttachmentService {

   private final Supplier<MongoDatabase> databaseSupplier;
   private final Supplier<MongoCollection<Document>> itemsCollectionSupplier;
   private final String fieldReferences;
   private final String fieldRefType;
   private final String fieldValue;
   private final String fieldContent;
   private final String fieldItemId;
   private final String inputReferenceType;
   private final String legacyReferenceType;
   private final String secondaryLegacyReferenceType;

   MongoAttachmentService(Supplier<MongoDatabase> databaseSupplier,
                          Supplier<MongoCollection<Document>> itemsCollectionSupplier,
                          String fieldReferences,
                          String fieldRefType,
                          String fieldValue,
                          String fieldContent,
                          String fieldItemId,
                          String inputReferenceType,
                          String legacyReferenceType,
                          String secondaryLegacyReferenceType) {
      this.databaseSupplier = databaseSupplier;
      this.itemsCollectionSupplier = itemsCollectionSupplier;
      this.fieldReferences = fieldReferences;
      this.fieldRefType = fieldRefType;
      this.fieldValue = fieldValue;
      this.fieldContent = fieldContent;
      this.fieldItemId = fieldItemId;
      this.inputReferenceType = inputReferenceType;
      this.legacyReferenceType = legacyReferenceType;
      this.secondaryLegacyReferenceType = secondaryLegacyReferenceType;
   }

   String uploadAttachment(InputAttachmentSource source) {
      GridFSBucket bucket = GridFSBuckets.create(databaseSupplier.get());
      String filename = (source.filename() == null || source.filename().isBlank()) ? "input.bin" : source.filename();
      String contentType = (source.contentType() == null || source.contentType().isBlank())
            ? "application/octet-stream"
            : source.contentType();
      Document metadata = new Document("type", contentType);
      if (source.role() != null && !source.role().isBlank()) {
         metadata.append("role", source.role());
      }
      GridFSUploadOptions options = new GridFSUploadOptions().metadata(metadata);
      try (InputStream inputStream = Files.newInputStream(source.path())) {
         ObjectId fileId = bucket.uploadFromStream(filename, inputStream, options);
         return fileId.toHexString();
      } catch (Exception e) {
         throw new IllegalStateException("Failed to upload input attachment from " + source.path(), e);
      }
   }

   Optional<InputAttachmentDownload> readInputAttachmentByItemId(String itemId) {
      List<InputAttachmentMetadata> attachments = readInputAttachmentsByItemId(itemId);
      if (attachments.isEmpty()) {
         return Optional.empty();
      }
      return readInputAttachmentByItemAndAttachmentId(itemId, attachments.getFirst().attachmentId());
   }

   List<InputAttachmentMetadata> readInputAttachmentsByItemId(String itemId) {
      Document item = findItem(itemId);
      if (item == null) {
         return List.of();
      }
      List<String> attachmentIds = extractInputAttachmentIds(item);
      if (attachmentIds.isEmpty()) {
         return List.of();
      }

      List<InputAttachmentMetadata> metadataList = new ArrayList<>();
      for (String attachmentId : attachmentIds) {
         readAttachmentMetadata(attachmentId).ifPresent(metadataList::add);
      }
      return metadataList;
   }

   Optional<InputAttachmentDownload> readInputAttachmentByItemAndAttachmentId(String itemId, String attachmentId) {
      Document item = findItem(itemId);
      if (item == null || attachmentId == null || attachmentId.isBlank() || !ObjectId.isValid(attachmentId)) {
         return Optional.empty();
      }
      List<String> attachmentIds = extractInputAttachmentIds(item);
      if (!attachmentIds.contains(attachmentId)) {
         return Optional.empty();
      }
      return readAttachmentDownload(attachmentId);
   }

   private Optional<InputAttachmentMetadata> readAttachmentMetadata(String attachmentId) {
      if (attachmentId == null || attachmentId.isBlank() || !ObjectId.isValid(attachmentId)) {
         return Optional.empty();
      }
      ObjectId attachmentObjectId = new ObjectId(attachmentId);
      Document attachmentFile = databaseSupplier.get().getCollection("fs.files").find(eq("_id", attachmentObjectId)).first();
      if (attachmentFile == null) {
         return Optional.empty();
      }
      String filename = attachmentFile.getString("filename");
      Document metadata = attachmentFile.get("metadata", Document.class);
      String contentType = metadata == null ? null : metadata.getString("type");
      return Optional.of(new InputAttachmentMetadata(
            attachmentId,
            (filename == null || filename.isBlank()) ? "input.bin" : filename,
            (contentType == null || contentType.isBlank()) ? "application/octet-stream" : contentType
      ));
   }

   private Optional<InputAttachmentDownload> readAttachmentDownload(String attachmentId) {
      Optional<InputAttachmentMetadata> metadata = readAttachmentMetadata(attachmentId);
      if (metadata.isEmpty()) {
         return Optional.empty();
      }
      ObjectId attachmentObjectId = new ObjectId(attachmentId);
      InputAttachmentMetadata info = metadata.get();
      return Optional.of(new InputAttachmentDownload(
            info.filename(),
            info.contentType(),
            outputStream -> {
               GridFSBucket bucket = GridFSBuckets.create(databaseSupplier.get());
               try (var stream = bucket.openDownloadStream(attachmentObjectId)) {
                  stream.transferTo(outputStream);
               }
            }
      ));
   }

   private Document findItem(String itemId) {
      if (itemId == null || itemId.isBlank() || !ObjectId.isValid(itemId)) {
         return null;
      }
      return itemsCollectionSupplier.get().find(eq("_id", new ObjectId(itemId))).first();
   }

   private List<String> extractInputAttachmentIds(Document item) {
      LinkedHashSet<String> attachmentIds = new LinkedHashSet<>();
      addAttachmentIdsFromReferences(item, attachmentIds);
      addAttachmentIdsFromContentInputs(item, attachmentIds);
      return new ArrayList<>(attachmentIds);
   }

   private void addAttachmentIdsFromReferences(Document item, LinkedHashSet<String> attachmentIds) {
      Object rawReferences = item.get(fieldReferences);
      if (!(rawReferences instanceof List<?> references)) {
         return;
      }
      for (Object reference : references) {
         if (!(reference instanceof Document referenceDoc)) {
            continue;
         }
         String refType = referenceDoc.getString(fieldRefType);
         String type = referenceDoc.getString("type");
         String value = referenceDoc.getString(fieldValue);
         if (!"ATTACHMENT".equals(refType) || value == null || value.isBlank()) {
            continue;
         }
         if (inputReferenceType.equals(type)
               || legacyReferenceType.equals(type)
               || (secondaryLegacyReferenceType != null && secondaryLegacyReferenceType.equals(type))) {
            attachmentIds.add(value);
         }
      }
   }

   private void addAttachmentIdsFromContentInputs(Document item, LinkedHashSet<String> attachmentIds) {
      Document content = item.get(fieldContent, Document.class);
      if (content == null) {
         return;
      }
      Object rawInputs = content.get("inputs");
      if (!(rawInputs instanceof List<?> inputs)) {
         return;
      }
      for (Object input : inputs) {
         if (!(input instanceof Document inputDoc)) {
            continue;
         }
         String itemId = inputDoc.getString(fieldItemId);
         if (itemId != null && !itemId.isBlank()) {
            attachmentIds.add(itemId);
         }
      }
   }
}
