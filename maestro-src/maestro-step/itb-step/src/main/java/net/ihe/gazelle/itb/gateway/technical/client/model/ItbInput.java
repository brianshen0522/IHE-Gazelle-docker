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

package net.ihe.gazelle.itb.gateway.technical.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * ITB input descriptor sent in start payload.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ItbInput {

   private String name;
   private String type;
   private String embeddingMethod;
   private String value;
   private List<ItbInput> item;

   public String getName() {
      return name;
   }

   public ItbInput setName(String name) {
      this.name = name;
      return this;
   }

   public String getType() {
      return type;
   }

   public ItbInput setType(String type) {
      this.type = type;
      return this;
   }

   public String getEmbeddingMethod() {
      return embeddingMethod;
   }

   public ItbInput setEmbeddingMethod(String embeddingMethod) {
      this.embeddingMethod = embeddingMethod;
      return this;
   }

   public String getValue() {
      return value;
   }

   public ItbInput setValue(String value) {
      this.value = value;
      return this;
   }

   public List<ItbInput> getItem() {
      return item;
   }

   public ItbInput setItem(List<ItbInput> item) {
      this.item = item;
      return this;
   }
}
