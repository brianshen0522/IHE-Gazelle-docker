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

package net.ihe.gazelle.maestro.spi.business.recording.plan;

import java.util.Arrays;
import java.util.Objects;

/**
 * Immutable payload describing an attachment to store in Datahouse.
 *
 * @param type attachment logical type
 * @param content attachment binary content
 * @param filename attachment file name
 */
public record AttachmentPlan(String type, byte[] content, String filename) {

   /**
    * Builds a defensive-copy attachment plan.
    */
   public AttachmentPlan {
      content = content == null ? null : Arrays.copyOf(content, content.length);
   }

   /**
    * Returns a defensive copy of attachment content.
    *
    * @return copied content bytes, or {@code null}
    */
   @Override
   public byte[] content() {
      return content == null ? null : Arrays.copyOf(content, content.length);
   }

   @Override
   public String toString() {
      return "AttachmentPlan{" +
             "type='" + type + '\'' +
             ", content=" + Arrays.toString(content) +
             ", filename='" + filename + '\'' +
             '}';
   }

   @Override
   public boolean equals(Object o) {
      if (!(o instanceof AttachmentPlan(String type1, byte[] content1, String filename1))) {
         return false;
      }
      return Objects.equals(type, type1) && Objects.deepEquals(content, content1)
            && Objects.equals(filename, filename1);
   }

   @Override
   public int hashCode() {
      return Objects.hash(type, Arrays.hashCode(content), filename);
   }
}
