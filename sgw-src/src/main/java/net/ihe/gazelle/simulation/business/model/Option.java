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

import java.util.Objects;

/**
 * Represents a selectable option consisting of a value and a human-readable label.
 * <p>
 * This class is commonly used for enumerating possible parameter values in
 * simulations, configurations, or user interfaces.
 */
public class Option {

    private String value;
    private String label;

    /**
     * Creates an empty {@code Option} instance.
     * <p>
     * Intended primarily for frameworks and DTO deserialization.
     */
    public Option() {
    }

    /**
     * Creates an {@code Option} where both the value and the label are set
     * to the given string.
     *
     * @param value the option value, also used as the label
     */
    public Option(String value) {
        this.value = value;
        this.label = value;
    }

    /**
     * Creates an {@code Option} with a distinct value and label.
     *
     * @param value the underlying value of the option
     * @param label the human-readable label for display
     */
    public Option(String value, String label) {
        this.value = value;
        this.label = label;
    }

    /**
     * Gets the underlying value of the option.
     *
     * @return the underlying value of the option
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the underlying value of the option.
     *
     * @param value the underlying value of the option
     * @return this instance for method chaining
     */
    public Option setValue(String value) {
        this.value = value;
        return this;
    }

    /**
     * Gets the human-readable label of the option.
     *
     * @return the human-readable label of the option
     */
    public String getLabel() {
        return label;
    }

    /**
     * Sets the human-readable label of the option.
     *
     * @param label the human-readable label of the option
     * @return this instance for method chaining
     */
    public Option setLabel(String label) {
        this.label = label;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Option option)) return false;
        return Objects.equals(value, option.value) && Objects.equals(label, option.label);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, label);
    }
}
