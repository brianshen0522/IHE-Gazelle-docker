/*
 * Copyright 2025-2026 IHE International.
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

package net.ihe.gazelle.maestro.quarkus.websocket.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import net.ihe.gazelle.modelmarshaller.technical.dto.SubTypingDTO;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(
      name = "Message",
      description = "Base element for all messages sent through the websocket."
)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
public abstract class MessageDTO<T> implements SubTypingDTO<T> {

   @JsonIgnore
   protected T message;

   protected MessageDTO(T message) {
      initWithBusinessObject(message);
   }

   @Override
   @JsonIgnore
   public SubTypingDTO<T> initWithBusinessObject(T message) {
      this.message = message;
      return this;
   }

   @Override
   @JsonIgnore
   public T getBusinessObject() {
      return message;
   }
}

