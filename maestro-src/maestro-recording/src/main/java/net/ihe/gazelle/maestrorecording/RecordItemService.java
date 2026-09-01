/*
 * Copyright 2026 IHE International.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.ihe.gazelle.maestrorecording;

import com.kereval.gazelle.datahouse.api.business.record.Attachment;
import com.kereval.gazelle.datahouse.api.business.record.Item;
import com.kereval.gazelle.datahouse.api.business.record.RefType;
import com.kereval.gazelle.datahouse.api.business.record.Reference;
import com.kereval.gazelle.datahouse.technical.rest.client.RecordItemClient;
import net.ihe.gazelle.maestro.spi.business.recording.plan.*;
import net.ihe.gazelle.security.business.acl.AccessControlList;

import java.util.*;

/**
 * Persists {@link PersistencePlan} graphs into Datahouse using {@link RecordItemClient}.
 */
public class RecordItemService {

   private final RecordItemClient recordItemClient;

   /**
    * Creates a record service using the provided Datahouse client.
    *
    * @param recordItemClient Datahouse recording client
    */
   public RecordItemService(RecordItemClient recordItemClient) {
      this.recordItemClient = recordItemClient;
   }

   /**
    * Records the full persistence plan and returns the root item identifier.
    *
    * @param persistencePlan persistence plan to record
    * @param <T> root object type
    * @return persisted root item identifier
    */
   public <T> String recordPlan(PersistencePlan<T> persistencePlan) {
      return recordPlan(persistencePlan.getRootItem(), persistencePlan.getAcl());
   }

   /**
    * Records an item plan graph and returns the root item identifier.
    *
    * @param rootItem root item plan to persist
    * @param acl access control list to apply
    * @param <T> root object type
    * @return persisted root item identifier
    */
   public <T> String recordPlan(ItemPlan<T> rootItem, AccessControlList acl) {
      return recordItemPlan(rootItem, acl, new History());
   }

   private <T> String recordItemPlan(ItemPlan<T> itemPlan, AccessControlList acl, History history) {
      if (history.items.containsKey(itemPlan)) {
         return history.items.get(itemPlan);
      }
      history.addVisited(itemPlan);
      Item item = initItem(itemPlan, acl);
      for (FutureReferencePlan<T> futureReferencePlan : itemPlan.futureReferencePlans()) {
         ReferencePlan referencePlan = futureReferencePlan.referencePlan();
         String referenceId = recordReferencedObject(referencePlan, acl, history);
         if (futureReferencePlan.mutation() != null) {
            futureReferencePlan.mutation().mutateReference(itemPlan.itemObject(), referencePlan.getName(), referenceId);
         }
         Reference reference = asReference(referencePlan, referenceId);
         if (!containsSameReference(item.getReferences(), reference)) {
            item.addReference(reference);
         }
      }
      item.setContent(itemPlan.textMarshaller().marshallAsString(itemPlan.itemObject()));
      String id = recordItemClient.recordItem(item);
      history.addRecorded(itemPlan, id);
      return id;

   }

   private String recordReferencedObject(ReferencePlan referencePlan, AccessControlList acl, History history) {
      return switch (referencePlan.getRefType()) {
         case ITEM_ID -> recordItemPlan((ItemPlan<?>) referencePlan.getTarget(), acl, history);
         case ATTACHMENT -> recordAttachmentReference(referencePlan, history);
         case URL -> (String) referencePlan.getTarget();
         default -> throw new IllegalStateException("Unexpected RefType: " + referencePlan.getRefType());
      };
   }

   private String recordAttachmentReference(ReferencePlan referencePlan, History history) {
      Object target = referencePlan.getTarget();
      if (target instanceof AttachmentPlan attachmentPlan) {
         return recordAttachmentPlan(attachmentPlan, history);
      }
      if (target instanceof String attachmentId) {
         return attachmentId;
      }
      throw new IllegalStateException("Unsupported ATTACHMENT reference target: " + target);
   }

   private String recordAttachmentPlan(AttachmentPlan attachmentPlan, History history) {
      if (history.attachments.containsKey(attachmentPlan)) {
         return history.attachments.get(attachmentPlan);
      }
      String id = recordItemClient.uploadAttachments(List.of(
            new Attachment()
                  .setType(attachmentPlan.type())
                  .setFilename(attachmentPlan.filename())
                  .setContent(attachmentPlan.content())
      )).getFirst();
      history.attachments.put(attachmentPlan, id);
      return id;
   }

   private static <T> Item initItem(ItemPlan<T> itemPlan, AccessControlList acl) {
      return new Item()
            .setType(itemPlan.type())
            .setAccessControlList(acl)
            .setAdditionalParameters(itemPlan.additionalParameters());
   }

   private static Reference asReference(ReferencePlan referencePlan, String referenceId) {
      return new Reference()
            .setName(referencePlan.getName())
            .setRefType(toDatahouseRefType(referencePlan.getRefType()))
            .setType(referencePlan.getTargetType())
            .setValue(referenceId);
   }

   private static boolean containsSameReference(List<Reference> references, Reference candidate) {
      if (references == null || candidate == null) {
         return false;
      }
      for (Reference existing : references) {
         if (existing == null) {
            continue;
         }
         if (Objects.equals(existing.getName(), candidate.getName())
               && existing.getRefType() == candidate.getRefType()
               && Objects.equals(existing.getType(), candidate.getType())
               && Objects.equals(existing.getValue(), candidate.getValue())) {
            return true;
         }
      }
      return false;
   }

   private static RefType toDatahouseRefType(MaestroRefType refType) {
      return switch (refType) {
         case ITEM_ID -> RefType.ITEM_ID;
         case ATTACHMENT -> RefType.ATTACHMENT;
         case URL -> RefType.URL;
      };
   }

   private static class History {
      private final Map<ItemPlan<?>, String> items = new IdentityHashMap<>();
      private final Map<AttachmentPlan, String> attachments = new HashMap<>();
      private final Set<ItemPlan<?>> inProgress = Collections.newSetFromMap(new IdentityHashMap<>());

      private void addVisited(ItemPlan<?> itemPlan) {
          if(!inProgress.add(itemPlan)){
             throw new IllegalStateException("Cyclic ItemPlan reference detected on: " + itemPlan.type());
          }
      }

      private void addRecorded(ItemPlan<?> itemPlan, String id) {
         items.put(itemPlan, id);
         inProgress.remove(itemPlan);
      }
   }
}
