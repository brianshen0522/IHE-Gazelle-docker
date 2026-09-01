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
import com.kereval.gazelle.datahouse.technical.rest.client.RecordItemClient;
import net.ihe.gazelle.maestro.spi.business.recording.plan.AttachmentPlan;
import net.ihe.gazelle.maestro.spi.business.recording.plan.ItemPlan;
import net.ihe.gazelle.maestro.spi.business.recording.plan.PersistencePlan;
import net.ihe.gazelle.modelmarshaller.technical.marshalling.MarshallingException;
import net.ihe.gazelle.modelmarshaller.technical.marshalling.TextMarshaller;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RecordItemServiceTest {

   @Test
   void shouldDetectCycleInItemPlanReferences() {
      ItemPlan<String> itemA = item("A", "a");
      ItemPlan<String> itemB = item("B", "b");
      itemA.withItemReference("to-b", itemB);
      itemB.withItemReference("to-a", itemA);

      RecordItemClientStub client = new RecordItemClientStub();
      RecordItemService service = new RecordItemService(client);
      PersistencePlan<String> plan = new PersistencePlan<>(itemA, null);

      IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> service.recordPlan(plan)
      );

      assertTrue(exception.getMessage().contains("Cyclic ItemPlan reference detected on: " + itemA.type()));
      assertEquals(0, client.recordedItems.size());
   }

   @Test
   void shouldRecordSharedChildOnlyOnce() {
      ItemPlan<String> root = item("ROOT", "root");
      ItemPlan<String> sharedChild = item("CHILD", "child");
      root.withItemReference("child-1", sharedChild);
      root.withItemReference("child-2", sharedChild);

      RecordItemClientStub client = new RecordItemClientStub();
      RecordItemService service = new RecordItemService(client);

      String rootId = service.recordPlan(new PersistencePlan<>(root, null));

      assertEquals("itm-2", rootId);
      assertEquals(2, client.recordedItems.size(), "Shared child should be recorded once");
   }

   @Test
   void shouldDeduplicateIdenticalReferencesOnRecordedItem() {
      ItemPlan<String> root = item("ROOT", "root");
      root.withAttachmentReference("in1",
            new AttachmentPlan("application/octet-stream", "same".getBytes(StandardCharsets.UTF_8), "a.bin"),
            null);
      root.withAttachmentReference("in1",
            new AttachmentPlan("application/octet-stream", "same".getBytes(StandardCharsets.UTF_8), "a.bin"),
            null);

      RecordItemClientStub client = new RecordItemClientStub();
      RecordItemService service = new RecordItemService(client);

      String rootId = service.recordPlan(new PersistencePlan<>(root, null));

      assertEquals("itm-1", rootId);
      assertEquals(1, client.recordedItems.size());
      Item persisted = client.recordedItems.getFirst();
      assertEquals(1, persisted.getReferences().size());
      assertEquals("in1", persisted.getReferences().getFirst().getName());
      assertEquals(RefType.ATTACHMENT, persisted.getReferences().getFirst().getRefType());
   }

   private static ItemPlan<String> item(String type, String content) {
      return new ItemPlan<>(type, content, new StringMarshaller());
   }

   private static final class RecordItemClientStub implements RecordItemClient {
      private int itemSequence = 0;
      private int attachmentSequence = 0;
      private final List<Item> recordedItems = new ArrayList<>();

      @Override
      public String recordItem(Item item) {
         recordedItems.add(item);
         return "itm-" + (++itemSequence);
      }

      @Override
      public String recordItem(Item item, List<Attachment> attachments) {
         return recordItem(item);
      }

      @Override
      public List<String> uploadAttachments(List<Attachment> attachments) {
         List<String> ids = new ArrayList<>();
         for (int i = 0; i < attachments.size(); i++) {
            ids.add("att-" + (++attachmentSequence));
         }
         return ids;
      }
   }

   private static final class StringMarshaller implements TextMarshaller<String> {
      @Override
      public String marshallAsString(String value) throws MarshallingException {
         return value;
      }

      @Override
      public String unmarshall(String payload) {
         throw new UnsupportedOperationException("String marshalling is one-way for recording");
      }

      @Override
      public byte[] marshall(String value) throws MarshallingException {
         return marshallAsString(value).getBytes(StandardCharsets.UTF_8);
      }

      @Override
      public String unmarshall(byte[] payload) {
         throw new UnsupportedOperationException("String marshalling is one-way for recording");
      }
   }
}
