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
import net.ihe.gazelle.maestro.api.business.testreport.validator.TestValidator;

/**
 * Builder for {@link Test}.
 */
public class TestBuilder extends AbstractBuilder<Test> {

   private String id;
   private String name;
   private String version;
   private String description;

   /**
    * Default constructor.
    */
   public TestBuilder() {
      super();
   }

   /**
    * Constructs a new TestBuilder instance using the provided ValidatorBuilderFactory.
    *
    * @param validatorBuilderFactory the factory used to create a validator builder; must not be null
    */
   public TestBuilder(ValidatorBuilderFactory validatorBuilderFactory) {
      super(validatorBuilderFactory);
   }

   /**
    * Sets the unique identifier for the test.
    *
    * @param id the unique identifier for the test; must not be null or blank
    * @return the current instance of {@code TestBuilder} to allow method chaining
    */
   public TestBuilder setId(String id) {
      this.id = id;
      return this;
   }

   /**
    * Sets the name for the test.
    *
    * @param name the name of the test
    * @return the current instance of {@code TestBuilder} to allow method chaining
    */
   public TestBuilder setName(String name) {
      this.name = name;
      return this;
   }

   /**
    * Sets the version for the test.
    *
    * @param version the version of the test as a String
    * @return the current instance of {@code TestBuilder} to allow method chaining
    */
   public TestBuilder setVersion(String version) {
      this.version = version;
      return this;
   }

   /**
    * Sets the description for the test.
    *
    * @param description the description of the test
    * @return the current instance of {@code TestBuilder} to allow method chaining
    */
   public TestBuilder setDescription(String description) {
      this.description = description;
      return this;
   }

   @Override
   protected AbstractValidator<Test> instantiateValidator(ValidatorBuilderFactory validatorBuilderFactory) {
      return new TestValidator(validatorBuilderFactory);
   }

   @Override
   protected Test instantiate() {
      return new Test();
   }

   @Override
   protected void make(Test test) {
      test.setId(id)
            .setName(name)
            .setVersion(version)
            .setDescription(description);
   }
}
