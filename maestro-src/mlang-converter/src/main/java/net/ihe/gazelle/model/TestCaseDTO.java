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

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TestCaseDTO {
    private String name;
    private String description;
    private List<InputDTO> inputs = new ArrayList<>();
    private List<StepDTO> steps = new ArrayList<>();

    // Getters et Setters
    @JsonGetter("name")
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    @JsonGetter("description")
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @JsonGetter("inputs")
    public List<InputDTO> getInputs() { return inputs; }
    public void addInputs(InputDTO input) { this.inputs.add(input); }

    @JsonGetter("steps")
    public List<StepDTO> getSteps() { return steps; }
    public void addStep(StepDTO step) { this.steps.add(step); }
}