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

package net.ihe.gazelle.simulation.business.model;

import net.ihe.gazelle.simulation.business.sequence.SupportedParameter;
import net.ihe.gazelle.simulation.business.setup.ParameterType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a parameter that has been resolved from a supported parameter definition,
 * including value sets options and errors.
 */
public class ResolvedSupportedParameter {

    private String name;
    private String groupName;
    private String description;
    private ParameterType type;
    private boolean required;
    private Option defaultValue;
    private List<Option> options;
    private String valueSetId;
    private String error;

    /**
     * Creates an empty {@code ResolvedSupportedParameter} instance with no values set.
     * Initializes the {@code options} list to an empty list.
     */
    public ResolvedSupportedParameter() {
        options = new ArrayList<>();
    }

    /**
     * Creates a {@code ResolvedSupportedParameter} by copying values
     * from a {@link SupportedParameter}.
     *
     * @param copy the {@link SupportedParameter} to copy from, may be {@code null}
     */
    public ResolvedSupportedParameter(SupportedParameter copy) {
        this();
        if (copy != null) {
            name = copy.getName();
            groupName = copy.getGroupName();
            description = copy.getDescription();
            type = copy.getType();
            required = copy.isRequired();
            defaultValue = copy.getDefaultValue() != null
                    ? new Option(copy.getDefaultValue())
                    : null;
            options = new ArrayList<>(
                    copy.getOptions()
                            .stream()
                            .map(Option::new)
                            .toList()
            );
            valueSetId = copy.getValueSetId();
        }
    }

    /**
     * Creates a deep copy of another {@code ResolvedSupportedParameter}.
     *
     * @param copy the {@code ResolvedSupportedParameter} to copy from, may be {@code null}
     */
    public ResolvedSupportedParameter(ResolvedSupportedParameter copy) {
        this();
        if (copy != null) {
            name = copy.getName();
            groupName = copy.getGroupName();
            description = copy.getDescription();
            type = copy.getType();
            required = copy.isRequired();
            defaultValue = copy.getDefaultValue();
            options = copy.getOptions();
            valueSetId = copy.getValueSetId();
            error = copy.getError();
        }
    }

    /**
     * Gets the parameter name.
     *
     * @return the parameter name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the parameter name.
     *
     * @param name the parameter name
     * @return this instance for method chaining
     */
    public ResolvedSupportedParameter setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Gets the group name this parameter belongs to.
     *
     * @return the group name this parameter belongs to
     */
    public String getGroupName() {
        return groupName;
    }

    /**
     * Sets the group name this parameter belongs to.
     *
     * @param groupName the group name this parameter belongs to
     * @return this instance for method chaining
     */
    public ResolvedSupportedParameter setGroupName(String groupName) {
        this.groupName = groupName;
        return this;
    }

    /**
     * Gets a textual description of the parameter.
     *
     * @return a textual description of the parameter
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets a textual description of the parameter.
     *
     * @param description the textual description of the parameter
     * @return this instance for method chaining
     */
    public ResolvedSupportedParameter setDescription(String description) {
        this.description = description;
        return this;
    }

    /**
     * Gets the type of the parameter.
     *
     * @return the type of the parameter
     */
    public ParameterType getType() {
        return type;
    }

    /**
     * Sets the type of the parameter.
     *
     * @param type the type of the parameter
     * @return this instance for method chaining
     */
    public ResolvedSupportedParameter setType(ParameterType type) {
        this.type = type;
        return this;
    }

    /**
     * Indicates whether this parameter is required.
     *
     * @return {@code true} if this parameter is required, {@code false} otherwise
     */
    public boolean isRequired() {
        return required;
    }

    /**
     * Sets whether this parameter is required.
     *
     * @param required whether this parameter is required
     * @return this instance for method chaining
     */
    public ResolvedSupportedParameter setRequired(boolean required) {
        this.required = required;
        return this;
    }

    /**
     * Gets the default value of this parameter.
     *
     * @return the default value of this parameter, or {@code null} if none
     */
    public Option getDefaultValue() {
        return defaultValue;
    }

    /**
     * Sets the default value of this parameter.
     *
     * @param defaultValue the default value of this parameter
     * @return this instance for method chaining
     */
    public ResolvedSupportedParameter setDefaultValue(Option defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    /**
     * Gets a copy of the available options for this parameter.
     *
     * @return a copy of the available options for this parameter
     */
    public List<Option> getOptions() {
        return new ArrayList<>(options);
    }

    /**
     * Sets the list of available options for this parameter.
     *
     * @param options the list of available options for this parameter
     * @return this instance for method chaining
     */
    public ResolvedSupportedParameter setOptions(List<Option> options) {
        this.options = new ArrayList<>(options);
        return this;
    }

    /**
     * Gets the identifier of the value set this parameter belongs to.
     *
     * @return the identifier of the value set this parameter belongs to
     */
    public String getValueSetId() {
        return valueSetId;
    }

    /**
     * Sets the identifier of the value set this parameter belongs to.
     *
     * @param valueSetId the identifier of the value set this parameter belongs to
     * @return this instance for method chaining
     */
    public ResolvedSupportedParameter setValueSetId(String valueSetId) {
        this.valueSetId = valueSetId;
        return this;
    }

    /**
     * Gets an error message associated with this parameter.
     *
     * @return an error message associated with this parameter, or {@code null} if none
     */
    public String getError() {
        return error;
    }

    /**
     * Sets an error message associated with this parameter.
     *
     * @param error an error message associated with this parameter
     * @return this instance for method chaining
     */
    public ResolvedSupportedParameter setError(String error) {
        this.error = error;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ResolvedSupportedParameter that)) return false;
        return required == that.required
                && Objects.equals(name, that.name)
                && Objects.equals(groupName, that.groupName)
                && Objects.equals(description, that.description)
                && type == that.type
                && Objects.equals(defaultValue, that.defaultValue)
                && Objects.equals(options, that.options)
                && Objects.equals(valueSetId, that.valueSetId)
                && Objects.equals(error, that.error);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, groupName, description, type, required, defaultValue, options, valueSetId, error);
    }
}
