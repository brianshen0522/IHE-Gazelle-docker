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

package net.ihe.gazelle.simulation.technical.dto;

import com.fasterxml.jackson.annotation.*;
import net.ihe.gazelle.modelmarshaller.technical.dto.DTO;
import net.ihe.gazelle.simulation.business.model.Option;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(
        name = "Option",
        description = "An option composed by a technical value, and a human readable label."
)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "value",
        "label"
})
public class OptionDTO implements DTO<Option> {

    @JsonIgnore
    private final Option option;

    public OptionDTO() {
        this.option = new Option();
    }

    public OptionDTO(Option option) {
        this.option = option;
    }

    @Override
    @JsonIgnore
    public Option getBusinessObject() {
        return option;
    }

    @Schema(
            name = "value",
            description = "The technical value of the option of the given parameter.",
            examples = "761337610411265304^^^&2.16.756.5.30.1.127.3.10.3&ISO"
    )
    @JsonGetter("value")
    public String getValue() {
        return option.getValue();
    }

    @JsonSetter("value")
    public void setValue(String value) {
        option.setValue(value);
    }

    @Schema(
            name = "label",
            description = "The string linked to the value that will be displayed.",
            examples = "Patient ID"
    )
    @JsonGetter("label")
    public String getLabel() {
        return option.getLabel();
    }

    @JsonSetter("label")
    public void setLabel(String label) {
        option.setLabel(label);
    }
}
