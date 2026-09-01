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

package net.ihe.gazelle.maestro.api.technical.dto.report;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import net.ihe.gazelle.maestro.api.business.testreport.SystemUnderTest;
import net.ihe.gazelle.modelmarshaller.technical.dto.DTO;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;

@Schema(
      name = "SystemUnderTest",
      description = "The system that has been tested by the test execution."
)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
      "systemIdentification",
      "macAddresses",
      "ipAddresses",
      "hostNames"
})
public class SystemUnderTestDTO implements DTO<SystemUnderTest> {

   @JsonIgnore
   private final SystemUnderTest systemUnderTest;

   public SystemUnderTestDTO() {
      this(new SystemUnderTest());
   }

   public SystemUnderTestDTO(SystemUnderTest systemUnderTest) {
      this.systemUnderTest = systemUnderTest;
   }

   @Override
   @JsonIgnore
   public SystemUnderTest getBusinessObject() {
      return systemUnderTest;
   }

   @Schema(
         name = "systemIdentification",
         description = "The identification of the system under test.",
         required = true
   )
   @JsonProperty(value = "systemIdentification")
   public EntityIdentificationDTO getSystemIdentification() {
      return new EntityIdentificationDTO(getBusinessObject().getSystemIdentification());
   }

   public void setSystemIdentification(EntityIdentificationDTO entityIdentificationDTO) {
      systemUnderTest.setSystemIdentification(entityIdentificationDTO.getBusinessObject());
   }

   @Schema(
         name = "macAddresses",
         description = "The list of mac addressed of the system under test.",
         examples = {"FF:FF:FF:FF:FF:FF"}
   )
   @JsonProperty(value = "macAddresses")
   public List<String> getMacAddresses() {
      return systemUnderTest.getMacAddresses();
   }

   public void setMacAddresses(List<String> macAddresses) {
      systemUnderTest.setMacAddresses(macAddresses);
   }

   @Schema(
         name = "ipAddresses",
         description = "The list of ip addressed of the system under test.",
         examples = {"10.190.10.10"}
   )
   @JsonProperty(value = "ipAddresses")
   public List<String> getIpAddresses() {
      return systemUnderTest.getIpAddresses();
   }

   public void setIpAddresses(List<String> ipAddresses) {
      systemUnderTest.setIpAddresses(ipAddresses);
   }

   @Schema(
         name = "hostNames",
         description = "The list of host names of the system under test.",
         examples = {"example.com"}
   )
   @JsonProperty(value = "hostNames")
   public List<String> getHostNames() {
      return systemUnderTest.getHostNames();
   }

   public void setHostNames(List<String> hostNames) {
      systemUnderTest.setHostNames(hostNames);
   }
}
