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

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PropertyDTO {
    private String name;
    private String type;
    private String value;

    // Getters et Setters
    @JsonGetter("name")
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    @JsonGetter("type")
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    @JsonGetter("value")
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
}