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

package net.ihe.gazelle.maestrorecording.adapter;

import com.kereval.gazelle.datahouse.api.business.record.Attachment;
import com.kereval.gazelle.datahouse.technical.rest.client.RecordItemClient;

import java.util.ArrayList;
import java.util.List;

class RecordItemClientMock implements RecordItemClient {
    int itemSeq = 0;
    int attachSeq = 0;
    final List<Object> recordedItems = new ArrayList<>();
    final List<List<Attachment>> uploadedAttachmentsBatches = new ArrayList<>();

    @Override
    public String recordItem(com.kereval.gazelle.datahouse.api.business.record.Item item) {
        recordedItems.add(item);
        return "itm-" + (++itemSeq);
    }

    @Override
    public String recordItem(com.kereval.gazelle.datahouse.api.business.record.Item item, List<Attachment> attachments) {
        // not used by the service for this flow; fallback to single-arg path
        return recordItem(item);
    }

    // New API exposed by datahouse; service calls it via reflection
    public List<String> uploadAttachments(List<Attachment> attachments) {
        uploadedAttachmentsBatches.add(attachments);
        List<String> ids = new ArrayList<>();
        for (int i = 0; i < attachments.size(); i++) {
            ids.add("att-" + (++attachSeq));
        }
        return ids;
    }
}
