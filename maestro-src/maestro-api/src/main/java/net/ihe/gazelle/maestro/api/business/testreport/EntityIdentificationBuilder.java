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

package net.ihe.gazelle.maestro.api.business.testreport;

import net.ihe.gazelle.framework.modelvalidator.business.ValidatorBuilderFactory;
import net.ihe.gazelle.framework.modelvalidator.business.utils.AbstractBuilder;
import net.ihe.gazelle.framework.modelvalidator.business.utils.AbstractValidator;
import net.ihe.gazelle.maestro.api.business.testreport.validator.EntityIdentificationValidator;

/**
 * Builder for {@link EntityIdentification}.
 */
public class EntityIdentificationBuilder extends AbstractBuilder<EntityIdentification> {

   private String version;
   private String name;

   /**
    * Default constructor.
    */
   public EntityIdentificationBuilder() {
      super();
   }

   /**
    * Constructs an instance of {@code EntityIdentificationBuilder} with a provided {@code ValidatorBuilderFactory}.
    *
    * @param validatorBuilderFactory the factory to be used for creating validators
    */
   public EntityIdentificationBuilder(ValidatorBuilderFactory validatorBuilderFactory) {
      super(validatorBuilderFactory);
   }

   /**
    * Constructs an instance of {@code EntityIdentificationBuilder} using an existing {@code EntityIdentification}.
    *
    * @param entityIdentification the entity identification object whose values will
    *                              be used to initialize this builder; can be null
    */
   public EntityIdentificationBuilder(EntityIdentification entityIdentification) {
      if (entityIdentification != null) {
         this.version = entityIdentification.getVersion();
         this.name = entityIdentification.getName();
      }
   }

   /**
    * Sets the version for the {@code EntityIdentification} instance.
    *
    * @param version the version value to set; may be null or a non-empty string
    * @return the current instance of {@code EntityIdentificationBuilder} for method chaining
    */
   public EntityIdentificationBuilder setVersion(String version) {
      this.version = version;
      return this;
   }

   /**
    * Sets the name for the {@code EntityIdentification} instance.
    *
    * @param name the name value to set; may be null or a non-empty string
    * @return the current instance of {@code EntityIdentificationBuilder} for method chaining
    */
   public EntityIdentificationBuilder setName(String name) {
      this.name = name;
      return this;
   }

   @Override
   protected AbstractValidator<EntityIdentification> instantiateValidator(ValidatorBuilderFactory validatorBuilderFactory) {
      return new EntityIdentificationValidator(validatorBuilderFactory);
   }

   @Override
   protected EntityIdentification instantiate() {
      return new EntityIdentification();
   }

   @Override
   protected void make(EntityIdentification entityIdentification) {
      entityIdentification
            .setVersion(version)
            .setName(name);
   }
}
