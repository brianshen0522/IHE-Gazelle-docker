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

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A structure for the system under test.
 * Business rules <br> systemIdentification mandatory
 */
public class SystemUnderTest implements Serializable {

   @Serial
   private static final long serialVersionUID = -3823347636444508122L;

   /**
    * The identification of the system under test.
    */
   private EntityIdentification systemIdentification;

   /**
    * A list of MAC address strings associated with the system.
    */
   private List<String> macAddresses;

   /**
    * A list of IP address strings associated with the system.
    */
   private List<String> ipAddresses;

   /**
    * A list of host name strings associated with the system.
    */
   private List<String> hostNames;

   /**
    * Default constructor.
    */
   public SystemUnderTest() {
      this.macAddresses = new ArrayList<>();
      this.ipAddresses = new ArrayList<>();
      this.hostNames = new ArrayList<>();
   }

   /**
    * Constructs a SystemUnderTest instance with the specified system identification.
    *
    * @param systemIdentification the identification of the system, must not be null
    */
   public SystemUnderTest(EntityIdentification systemIdentification) {
      this();
      this.systemIdentification = systemIdentification;
   }

   /**
    * Retrieves the system identification associated with this instance.
    *
    * @return the system identification, or null if it is not defined
    */
   public EntityIdentification getSystemIdentification() {
      return systemIdentification;
   }

   /**
    * Sets the system identification for this instance.
    *
    * @param systemIdentification the identification of the system to set; must not be null
    * @return the current instance of {@code SystemUnderTest} for method chaining
    */
   public SystemUnderTest setSystemIdentification(EntityIdentification systemIdentification) {
      this.systemIdentification = systemIdentification;
      return this;
   }

   /**
    * Retrieves the list of MAC addresses associated with the system.
    *
    * @return a list of MAC addresses; the list is a copy of the internal data, ensuring encapsulation
    */
   public List<String> getMacAddresses() {
      return new ArrayList<>(macAddresses);
   }

   /**
    * Sets the list of MAC addresses for the system.
    *
    * @param macAddresses the list of MAC addresses to set; must not be null
    * @return the current instance of {@code SystemUnderTest} for method chaining
    */
   public SystemUnderTest setMacAddresses(List<String> macAddresses) {
      this.macAddresses = new ArrayList<>(macAddresses);
      return this;
   }

   /**
    * Adds a new MAC address to the system.
    *
    * @param macAddress the MAC address to be added; must not be null or empty
    * @return the current instance of {@code SystemUnderTest} for method chaining
    */
   public SystemUnderTest addMacAddress(String macAddress) {
      macAddresses.add(macAddress);
      return this;
   }

   /**
    * Adds a list of MAC addresses to the current instance.
    *
    * @param macAddresses the list of MAC addresses to add; must not be null
    * @return the current instance of {@code SystemUnderTest} for method chaining
    */
   public SystemUnderTest addMacAddresses(List<String> macAddresses) {
      this.macAddresses.addAll(macAddresses);
      return this;
   }

   /**
    * Retrieves the list of IP addresses associated with the system.
    *
    * @return a list of IP addresses
    */
   public List<String> getIpAddresses() {
      return new ArrayList<>(ipAddresses);
   }

   /**
    * Sets the list of IP addresses for the system.
    *
    * @param ipAddresses the list of IP addresses to set; must not be null
    * @return the current instance of {@code SystemUnderTest} for method chaining
    */
   public SystemUnderTest setIpAddresses(List<String> ipAddresses) {
      this.ipAddresses = new ArrayList<>(ipAddresses);
      return this;
   }

   /**
    * Adds an IP address to the list of IP addresses associated with this instance.
    *
    * @param ipAddress the IP address to add; must not be null or empty
    * @return the current instance of {@code SystemUnderTest} for method chaining
    */
   public SystemUnderTest addIpAddress(String ipAddress) {
      ipAddresses.add(ipAddress);
      return this;
   }

   /**
    * Adds a list of IP addresses to the current instance.
    *
    * @param ipAddresses the list of IP addresses to add; must not be null
    * @return the current instance of {@code SystemUnderTest} for method chaining
    */
   public SystemUnderTest addIpAddresses(List<String> ipAddresses) {
      this.ipAddresses.addAll(ipAddresses);
      return this;
   }

   /**
    * Retrieves the list of host names associated with the system.
    *
    * @return a list of host names
    */
   public List<String> getHostNames() {
      return new ArrayList<>(hostNames);
   }

   /**
    * Sets the list of host names for the system.
    *
    * @param hostNames the list of host names to set; must not be null
    * @return the current instance of {@code SystemUnderTest} for method chaining
    */
   public SystemUnderTest setHostNames(List<String> hostNames) {
      this.hostNames = new ArrayList<>(hostNames);
      return this;
   }

   /**
    * Adds a host name to the list of host names associated with the system.
    *
    * @param hostName the host name to be added; must not be null or empty
    * @return the current instance of {@code SystemUnderTest} for method chaining
    */
   public SystemUnderTest addHostName(String hostName) {
      hostNames.add(hostName);
      return this;
   }

   /**
    * Adds a list of host names to the current instance.
    *
    * @param hostNames the list of host names to add; must not be null
    * @return the current instance of {@code SystemUnderTest} for method chaining
    */
   public SystemUnderTest addHostNames(List<String> hostNames) {
      this.hostNames.addAll(hostNames);
      return this;
   }

   /**
    * Checks whether the system identification is defined for this instance.
    *
    * @return true if the system identification is defined (not null); false otherwise
    */
   public boolean isSystemIdentificationDefined() {
      return systemIdentification != null;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      }
      if (!(o instanceof SystemUnderTest that)) {
         return false;
      }
      return Objects.equals(systemIdentification, that.systemIdentification)
            && Objects.equals(macAddresses, that.macAddresses)
            && Objects.equals(ipAddresses, that.ipAddresses)
            && Objects.equals(hostNames, that.hostNames);
   }

   @Override
   public int hashCode() {
      return Objects.hash(systemIdentification, macAddresses, ipAddresses, hostNames);
   }
}
