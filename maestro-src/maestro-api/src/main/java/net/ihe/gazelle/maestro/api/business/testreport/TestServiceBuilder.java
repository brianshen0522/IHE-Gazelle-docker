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
import net.ihe.gazelle.maestro.api.business.testreport.validator.TestServiceValidator;

/**
 * Builder for {@link TestService}.
 */
public class TestServiceBuilder extends AbstractBuilder<TestService> {

   private EntityIdentificationBuilder serviceIdentification;
   private String disclaimer;

   /**
    * Default constructor.
    */
   public TestServiceBuilder() {
      super();
   }

   /**
    * Constructs a new instance of {@code TestServiceBuilder} with the specified validator builder factory.
    *
    * @param validatorBuilderFactory the factory used to create validator builders
    */
   public TestServiceBuilder(ValidatorBuilderFactory validatorBuilderFactory) {
      super(validatorBuilderFactory);
   }

   /**
    * Sets the service identification for the {@code TestServiceBuilder} instance.
    *
    * @param serviceIdentification the {@code EntityIdentificationBuilder} object that represents the service identification
    * @return the current instance of {@code TestServiceBuilder} to allow method chaining
    */
   public TestServiceBuilder setServiceIdentification(EntityIdentificationBuilder serviceIdentification) {
      this.serviceIdentification = serviceIdentification;
      return this;
   }

   /**
    * Sets the disclaimer for the {@code TestServiceBuilder} instance.
    *
    * @param disclaimer the textual disclaimer to be associated with the test service
    * @return the current instance of {@code TestServiceBuilder} to allow method chaining
    */
   public TestServiceBuilder setDisclaimer(String disclaimer) {
      this.disclaimer = disclaimer;
      return this;
   }

   @Override
   protected AbstractValidator<TestService> instantiateValidator(ValidatorBuilderFactory validatorBuilderFactory) {
      return new TestServiceValidator(validatorBuilderFactory);
   }

   @Override
   protected TestService instantiate() {
      return new TestService();
   }

   @Override
   protected void make(TestService testService) {
      testService.setServiceIdentification(AbstractBuilder.staticBuildWithoutValidation(serviceIdentification))
            .setDisclaimer(disclaimer);
   }
}
