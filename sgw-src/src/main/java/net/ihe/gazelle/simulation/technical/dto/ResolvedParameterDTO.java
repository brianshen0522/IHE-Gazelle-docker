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
import net.ihe.gazelle.simulation.business.model.ResolvedSupportedParameter;
import net.ihe.gazelle.simulation.business.setup.ParameterType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;

@Schema(
        name = "SupportedParameter",
        description = "Represents a parameter that has been resolved from a supported parameter definition, including value sets options and errors."
)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
        "name",
        "groupName",
        "description",
        "type",
        "required",
        "defaultValue",
        "options",
        "valueSetId"
})
public class ResolvedParameterDTO implements DTO<ResolvedSupportedParameter> {

    @JsonIgnore
    private final ResolvedSupportedParameter resolvedSupportedParameter;

    public ResolvedParameterDTO() {
        this(new ResolvedSupportedParameter());
    }

    public ResolvedParameterDTO(ResolvedSupportedParameter resolvedSupportedParameter) {
        this.resolvedSupportedParameter = resolvedSupportedParameter;
    }

    @Override
    @JsonIgnore
    public ResolvedSupportedParameter getBusinessObject() {
        return resolvedSupportedParameter;
    }

    @Schema(
            name = "name",
            description = "The name of the given supported parameter.",
            examples = "PatientId",
            required = true
    )
    @JsonGetter("name")
    public String getName() {
        return resolvedSupportedParameter.getName();
    }

    @JsonSetter("name")
    public void setName(String name) {
        resolvedSupportedParameter.setName(name);
    }

    @Schema(
            name = "groupName",
            description = "All parameters shall belong to a group of parameters for UI purpose.",
            examples = "Sequence settings",
            required = true
    )
    @JsonGetter("groupName")
    public String getGroupName() {
        return resolvedSupportedParameter.getGroupName();
    }

    @JsonSetter("groupName")
    public void setGroupName(String groupName) {
        resolvedSupportedParameter.setGroupName(groupName);
    }

    @Schema(
            name = "description",
            description = "A description of the given supported parameter.",
            examples = "The patient to use in the sequence.",
            required = true
    )
    @JsonGetter("description")
    public String getDescription() {
        return resolvedSupportedParameter.getDescription();
    }

    @JsonSetter("description")
    public void setDescription(String description) {
        resolvedSupportedParameter.setDescription(description);
    }

    @Schema(
            name = "type",
            description = "The type format of the parameter.",
            enumeration = {ParameterType.TYPE_TEXT, ParameterType.TYPE_FILE, ParameterType.TYPE_BOOLEAN},
            required = true
    )
    @JsonGetter("type")
    public String getType() {
        return resolvedSupportedParameter.getType().name();
    }

    @JsonSetter("type")
    public void setType(String type) {
        resolvedSupportedParameter.setType(ParameterType.valueOf(type));
    }

    @Schema(
            name = "required",
            description = "Used to tell if the given parameter is mandatory or optional.",
            required = true
    )
    @JsonGetter("required")
    public boolean isRequired() {
        return resolvedSupportedParameter.isRequired();
    }

    @JsonSetter("required")
    public void setRequired(boolean required) {
        resolvedSupportedParameter.setRequired(required);
    }

    @Schema(
            name = "defaultValue",
            description = "The simulator might provide a default value to the given parameter. The default value becomes mandatory if it has a list of options."
    )
    @JsonGetter("defaultValue")
    public OptionDTO getDefaultValue() {
        Option option = resolvedSupportedParameter.getDefaultValue();
        return option != null ? new OptionDTO(resolvedSupportedParameter.getDefaultValue()) : null;
    }

    @JsonSetter("defaultValue")
    public void setDefaultValue(OptionDTO defaultValue) {
        resolvedSupportedParameter.setDefaultValue(defaultValue.getBusinessObject());
    }

    @Schema(
            name = "options",
            description = "A list of possible values for the current parameter."
    )
    @JsonGetter("options")
    public List<OptionDTO> getOptions() {
        return getBusinessObject().getOptions().stream().map(OptionDTO::new).toList();
    }

    @JsonSetter("options")
    public void setOptions(List<OptionDTO> dto) {
        getBusinessObject().setOptions(
                dto.stream().map(OptionDTO::getBusinessObject).toList()
        );
    }

    @Schema(
            name = "valueSetId",
            description = "A possible value set defined in SVSimulator, if the given parameter is restricted.",
            examples = "valueSet1"
    )
    @JsonGetter("valueSetId")
    public String getValueSetId() {
        return resolvedSupportedParameter.getValueSetId();
    }

    @JsonSetter("valueSetId")
    public void setValueSetId(String valueSetId) {
        resolvedSupportedParameter.setValueSetId(valueSetId);
    }

    @Schema(
            name = "error",
            description = "An error message if the value set could not be retrieved"
    )
    @JsonGetter("error")
    public String getError() {
        return resolvedSupportedParameter.getError();
    }

    @JsonSetter("error")
    public void setError(String error) {
        resolvedSupportedParameter.setError(error);
    }
}
