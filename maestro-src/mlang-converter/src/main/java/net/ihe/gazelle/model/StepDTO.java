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

package net.ihe.gazelle.model;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = InstructionStepDTO.class, name = "INSTRUCTION"),
        @JsonSubTypes.Type(value = ValidationStepDTO.class, name = "VALIDATION")
})
public abstract class StepDTO {
    private String id;
    private String name;
    private String description;

    // Getters et Setters
    @JsonGetter("id")
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    @JsonGetter("name")
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    @JsonGetter("description")
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}