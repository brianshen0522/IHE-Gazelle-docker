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
import net.ihe.gazelle.maestro.api.business.testreport.validator.SystemUnderTestValidator;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder for {@link SystemUnderTest}.
 */
public class SystemUnderTestBuilder extends AbstractBuilder<SystemUnderTest> {

   private EntityIdentificationBuilder systemIdentification;
   private List<String> macAddresses;
   private List<String> ipAddresses;
   private List<String> hostNames;

   /**
    * Default constructor.
    */
   public SystemUnderTestBuilder() {
      super();
      macAddresses = new ArrayList<>();
      ipAddresses = new ArrayList<>();
      hostNames = new ArrayList<>();
   }

   /**
    * Constructs a new instance of SystemUnderTestBuilder using the specified ValidatorBuilderFactory.
    *
    * @param validatorBuilderFactory the factory used to create validator builders
    */
   public SystemUnderTestBuilder(ValidatorBuilderFactory validatorBuilderFactory) {
      super(validatorBuilderFactory);
      macAddresses = new ArrayList<>();
      ipAddresses = new ArrayList<>();
      hostNames = new ArrayList<>();
   }

   /**
    * Constructs a new instance of {@code SystemUnderTestBuilder} using the specified {@code SystemUnderTest}.
    *
    * @param systemUnderTest the system under test to initialize the builder with; can be null
    */
   public SystemUnderTestBuilder(SystemUnderTest systemUnderTest) {
      if (systemUnderTest != null) {
         this.systemIdentification = new EntityIdentificationBuilder(systemUnderTest.getSystemIdentification());
         this.macAddresses = new ArrayList<>(systemUnderTest.getMacAddresses());
         this.ipAddresses = new ArrayList<>(systemUnderTest.getIpAddresses());
         this.hostNames = new ArrayList<>(systemUnderTest.getHostNames());
      } else {
         macAddresses = new ArrayList<>();
         ipAddresses = new ArrayList<>();
         hostNames = new ArrayList<>();
      }
   }

   /**
    * Sets the system identification for the {@code SystemUnderTestBuilder}.
    *
    * @param systemIdentification the {@link EntityIdentificationBuilder} instance
    *                              used to specify the system identification details
    * @return the current instance of {@code SystemUnderTestBuilder} for method chaining
    */
   public SystemUnderTestBuilder setSystemIdentification(EntityIdentificationBuilder systemIdentification) {
      this.systemIdentification = systemIdentification;
      return this;
   }

   /**
    * Sets the list of MAC addresses for the builder.
    *
    * @param macAddresses the list of MAC addresses to set; can be null
    * @return the current instance of {@code SystemUnderTestBuilder} for method chaining
    */
   public SystemUnderTestBuilder setMacAddresses(List<String> macAddresses) {
      this.macAddresses = macAddresses != null
            ? new ArrayList<>(macAddresses)
            : new ArrayList<>();
      return this;
   }

   /**
    * Adds a single MAC address to the list of MAC addresses being built by the {@code SystemUnderTestBuilder}.
    *
    * @param macAddress the MAC address to add to the builder
    * @return the current instance of {@code SystemUnderTestBuilder} for method chaining
    */
   public SystemUnderTestBuilder addMacAddress(String macAddress) {
      this.macAddresses.add(macAddress);
      return this;
   }

   /**
    * Adds a list of MAC addresses to the current list in the builder.
    *
    * @param macAddresses the list of MAC addresses to add; cannot be null
    * @return the current instance of {@code SystemUnderTestBuilder} for method chaining
    */
   public SystemUnderTestBuilder addMacAddresses(List<String> macAddresses) {
      this.macAddresses.addAll(macAddresses);
      return this;
   }

   /**
    * Sets the list of IP addresses for the {@code SystemUnderTestBuilder}.
    *
    * @param ipAddresses the list of IP addresses to set; can be null
    * @return the current instance of {@code SystemUnderTestBuilder} for method chaining
    */
   public SystemUnderTestBuilder setIpAddresses(List<String> ipAddresses) {
      this.ipAddresses = ipAddresses != null
            ? new ArrayList<>(ipAddresses)
            : new ArrayList<>();
      return this;
   }

   /**
    * Adds a single IP address to the list of IP addresses being built by the {@code SystemUnderTestBuilder}.
    *
    * @param ipAddress the IP address to add to the builder
    * @return the current instance of {@code SystemUnderTestBuilder} for method chaining
    */
   public SystemUnderTestBuilder addIpAddress(String ipAddress) {
      this.ipAddresses.add(ipAddress);
      return this;
   }

   /**
    * Adds a list of IP addresses to the current list in the builder.
    *
    * @param ipAddresses the list of IP addresses to add; cannot be null
    * @return the current instance of {@code SystemUnderTestBuilder} for method chaining
    */
   public SystemUnderTestBuilder addIpAddresses(List<String> ipAddresses) {
      this.ipAddresses.addAll(ipAddresses);
      return this;
   }

   /**
    * Sets the list of host names for the builder.
    *
    * @param hostNames the list of host names to set; can be null
    * @return the current instance of {@code SystemUnderTestBuilder} for method chaining
    */
   public SystemUnderTestBuilder setHostNames(List<String> hostNames) {
      this.hostNames = hostNames != null
            ? new ArrayList<>(hostNames)
            : new ArrayList<>();
      return this;
   }

   /**
    * Adds a single host name to the list of host names being built by the {@code SystemUnderTestBuilder}.
    *
    * @param hostName the host name to add to the builder; cannot be null
    * @return the current instance of {@code SystemUnderTestBuilder} for method chaining
    */
   public SystemUnderTestBuilder addHostName(String hostName) {
      this.hostNames.add(hostName);
      return this;
   }

   /**
    * Adds a list of host names to the current list in the builder.
    *
    * @param hostNames the list of host names to add; cannot be null
    * @return the current instance of {@code SystemUnderTestBuilder} for method chaining
    */
   public SystemUnderTestBuilder addHostNames(List<String> hostNames) {
      this.hostNames.addAll(hostNames);
      return this;
   }

   @Override
   protected AbstractValidator<SystemUnderTest> instantiateValidator(ValidatorBuilderFactory validatorBuilderFactory) {
      return new SystemUnderTestValidator(validatorBuilderFactory);
   }

   @Override
   protected SystemUnderTest instantiate() {
      return new SystemUnderTest();
   }

   @Override
   protected void make(SystemUnderTest systemUnderTest) {
      systemUnderTest
            .setSystemIdentification(AbstractBuilder.staticBuildWithoutValidation(systemIdentification))
            .setMacAddresses(macAddresses)
            .setIpAddresses(ipAddresses)
            .setHostNames(hostNames);
   }
}
