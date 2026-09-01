/*
Copyright 2010-2025 IHE International

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package net.ihe.gazelle.xmlvalidation.technical.service;

import net.ihe.gazelle.validation.v2.api.business.profile.ValidationProfile;
import net.ihe.gazelle.xmlvalidation.business.config.ProfileConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class XsdNamespaceServiceTest {

   @TempDir
   Path tempDir;

   private XsdNamespaceService namespaceService;

   @BeforeEach
   void setUp() {
      namespaceService = new XsdNamespaceService();
   }

   @Test
   void returnsTargetNamespaceWhenPresent() throws IOException {
      ProfileConfiguration profileConfiguration = createProfile("with-ns.xsd", """
            <xs:schema targetNamespace="urn:test:ns" xmlns:xs="http://www.w3.org/2001/XMLSchema">
            </xs:schema>
            """);
      String targetNamespace = namespaceService.getTargetNamespace(profileConfiguration);

      assertEquals("urn:test:ns", targetNamespace);
   }

   @Test
   void returnsNullWhenNoTargetNamespace() throws IOException {
      ProfileConfiguration profileConfiguration = createProfile("no-ns.xsd", """
            <xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema">
            </xs:schema>
            """);
      String targetNamespace = namespaceService.getTargetNamespace(profileConfiguration);

      assertEquals(XsdNamespaceService.UKNOWN_NAMESPACE, targetNamespace);
   }

   @Test
   void returnsNullIfFileNotFound() {
      ProfileConfiguration profileConfiguration = new ProfileConfiguration();
      profileConfiguration.setRootPath(tempDir.toString());
      profileConfiguration.setXsdPath("missing.xsd");
      profileConfiguration.setValidationProfile(new ValidationProfile());

      String targetNamespace = namespaceService.getTargetNamespace(profileConfiguration);

      assertEquals(XsdNamespaceService.UKNOWN_NAMESPACE, targetNamespace);
   }

   private ProfileConfiguration createProfile(String fileName, String content) throws IOException {
      Path xsdFile = tempDir.resolve(fileName);
      Files.writeString(xsdFile, content);
      ProfileConfiguration profileConfiguration = new ProfileConfiguration();
      profileConfiguration.setRootPath(tempDir.toString());
      profileConfiguration.setXsdPath(fileName);
      profileConfiguration.setValidationProfile(new ValidationProfile());
      return profileConfiguration;
   }

}
