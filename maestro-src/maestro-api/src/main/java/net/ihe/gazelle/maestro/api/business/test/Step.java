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

package net.ihe.gazelle.maestro.api.business.test;

import net.ihe.gazelle.maestro.api.business.property.MissingPropertyException;
import net.ihe.gazelle.maestro.api.business.property.Property;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A structure defining a step that can be executed in a Test run.
 */
public class Step implements Serializable {

   @Serial
   private static final long serialVersionUID = 4126739543481925460L;

   /**
    * Default timeout for step runs in milliseconds.
    */
   public static final long STEP_DEFAULT_TIMEOUT = 180 * 1000L;

   /**
    * The name of the step.
    */
   private String name;

   /**
    * The type of the step.
    */
   private String type;

   /**
    * The timeout for the step in milliseconds.
    */
   private long timeout = STEP_DEFAULT_TIMEOUT;

   /**
    * The properties of the step.
    */
   // The base Map interface is not serializable, so we declare properties as ConcurrentHashMap.
   private ConcurrentHashMap<String, Property> properties = new ConcurrentHashMap<>();

   /**
    * The output mappings of the step.
    */
   private ConcurrentHashMap<String, String> outputMappings = new ConcurrentHashMap<>();

   /**
    * Default constructor
    */
   public Step() {
      // Empty for serialization
   }

   /**
    * Copy constructor for creating a new Step object by copying the properties of the provided Step object.
    *
    * @param other the Step object from which to copy the values. Must not be null.
    */
   public Step(Step other) {
      this(other.name, other.type, other.timeout, other.getProperties(), other.getOutputMappings());
   }

   /**
    * Constructs a new {@code Step} object with the given parameters.
    *
    * @param name           the name of the step; used for identifying the step
    * @param type           the type of the step; specifies the functionality category of the step
    * @param timeout        the timeout for the step execution in milliseconds; if {@code null}, a default timeout is applied
    * @param properties     the list of {@code Property} objects to associate with the step; each property is copied to maintain immutability
    * @param outputMappings a map representing the output mappings of the step; keys and values specify how the step's outputs are mapped
    */
   protected Step(String name, String type, Long timeout, List<Property> properties, Map<String, String> outputMappings) {
      this.name = name;
      this.type = type;
      setTimeout(timeout);
      properties.forEach(property -> this.properties.put(property.getName(), property.copy()));
      this.outputMappings = outputMappings != null ?
            new ConcurrentHashMap<>(outputMappings) :
            new ConcurrentHashMap<>();
   }

   /**
    * Retrieves the name of the step.
    *
    * @return the name of the step as a string
    */
   public String getName() {
      return name;
   }

   /**
    * Sets the name of the step and returns the current instance for method chaining.
    *
    * @param name the name to set for the step
    * @return the current Step instance
    */
   public Step setName(String name) {
      this.name = name;
      return this;
   }

   /**
    * Retrieves the type of the step.
    *
    * @return the type of the step as a string
    */
   public String getType() {
      return type;
   }

   /**
    * Sets the type of the step and returns the current instance for method chaining.
    *
    * @param type the type to set for the step
    * @return the current Step instance
    */
   public Step setType(String type) {
      this.type = type;
      return this;
   }

   /**
    * Retrieves the timeout value for the step execution.
    *
    * @return the timeout value in milliseconds
    */
   public long getTimeout() {
      return timeout;
   }

   /**
    * Sets the timeout value for the step execution.
    *
    * @param timeout the timeout value in milliseconds; can be null to apply the default timeout
    * @return the current {@code Step} instance
    */
   public Step setTimeout(Long timeout) {
      this.timeout = timeout != null
            ? timeout
            : STEP_DEFAULT_TIMEOUT;
      return this;
   }

   /**
    * Retrieves the list of properties associated with the current instance.
    *
    * @return a new list containing all {@code Property} instances associated with this object
    */
   public synchronized List<Property> getProperties() {
      return new ArrayList<>(properties.values());
   }

   /**
    * Sets the properties of the step. The provided list of {@code Property} instances
    * is converted into a {@code ConcurrentHashMap}.
    *
    * @param properties a list of {@code Property} objects to be associated with the step.
    *                   Each {@code Property} is copied into the map to ensure immutability.
    *                   If {@code null}, an empty map will be created.
    * @return the current {@code Step} instance with the updated properties.
    */
   public synchronized Step setProperties(List<Property> properties) {
      this.properties = properties != null
            ? buildMap(properties)
            : new ConcurrentHashMap<>();
      return this;
   }

   /**
    * Checks if a property with the given name exists in the current step's properties.
    *
    * @param name the name of the property to check for existence
    * @return {@code true} if the property exists, {@code false} otherwise
    */
   public boolean hasProperty(String name) {
      return properties.containsKey(name);
   }

   /**
    * Get the property with the given name.
    *
    * @param name the name of the property
    * @return the property
    * @throws MissingPropertyException if no property with the given name is found
    */
   public Property getProperty(String name) {
      return Optional.ofNullable(properties.get(name))
            .orElseThrow(() -> new MissingPropertyException("No property found with name: " + name));
   }

   /**
    * Get the value of the property with the given name.
    *
    * @param name the name of the property
    * @param <T>  the type of the property value
    * @return the value of the property
    * @throws MissingPropertyException if no property with the given name is found
    */
   public synchronized <T> T getPropertyValue(String name) {
      return getProperty(name).getValue();
   }

   /**
    * Retrieves the output mappings of the step. The output mappings define
    * how the step's outputs are mapped.
    *
    * @return a map containing the output mappings, where the keys and values
    *         represent the mapping definitions for the step
    */
   public Map<String, String> getOutputMappings() {
      return new ConcurrentHashMap<>(outputMappings);
   }

   /**
    * Sets the output mappings for the current step. The output mappings define how the step's outputs
    * are mapped to external entities or subsequent steps.
    *
    * @param outputMappings a map where the keys represent the output identifiers of the step,
    *                       and the values indicate their respective mapped destinations.
    *                       If {@code null}, an empty map is created.
    *
    * @return the current {@code Step} instance with the updated output mappings.
    */
   public Step setOutputMappings(Map<String, String> outputMappings) {
      this.outputMappings = outputMappings != null ?
            new ConcurrentHashMap<>(outputMappings) :
            new ConcurrentHashMap<>();
      return this;
   }

   /**
    * Adds a new output mapping to the current step.
    *
    * @param key   the key representing the output variable of the step
    * @param value the value defining the mapping destination for the specified key
    * @return the current {@code Step} instance with the updated output mappings
    */
   public Step addOutputMapping(String key, String value) {
      this.outputMappings.put(key, value);
      return this;
   }

   /**
    * Determines whether the name field is defined by checking if it is not null
    * and not empty.
    *
    * @return true if the name field is not null and not empty, false otherwise
    */
   public boolean isNameDefined() {
      return name != null && !name.isEmpty();
   }

   /**
    * Determines whether the type field is defined by checking if it is not null and not empty.
    *
    * @return true if the type field is not null and not empty, false otherwise
    */
   public boolean isTypeDefined() {
      return type != null && !type.isEmpty();
   }

   /**
    * Validates the output mappings for the current step.
    * The method ensures all entries in the outputMappings map are properly defined,
    * where both keys and values are non-null and non-blank.
    *
    * @return true if all output mappings are valid, false otherwise
    */
   public boolean areOutputMappingsValid() {
      return outputMappings.entrySet().stream()
            .allMatch(Step::isMapEntryDefined);
   }

   /**
    * Constructs a {@code ConcurrentHashMap} from a list of {@code Property} objects,
    * where each property's name is used as the key, and a copy of the property is the value.
    *
    * @param properties a list of {@code Property} objects to be used for building the map.
    *                   Each property must have a valid name, and its value will be copied
    *                   during the map construction.
    * @return a {@code ConcurrentHashMap} containing entries where the key is the property name
    *         and the value is a copy of the associated {@code Property} object.
    */
   protected static ConcurrentHashMap<String, Property> buildMap(List<Property> properties) {
      return properties.stream().collect(
            ConcurrentHashMap::new,
            (map, property) -> map.put(property.getName(), property.copy()),
            ConcurrentHashMap::putAll
      );
   }

   private static boolean isMapEntryDefined(Map.Entry<String, String> entry) {
      return entry.getKey() != null && !entry.getKey().isBlank()
            && entry.getValue() != null && !entry.getValue().isBlank();
   }

}

