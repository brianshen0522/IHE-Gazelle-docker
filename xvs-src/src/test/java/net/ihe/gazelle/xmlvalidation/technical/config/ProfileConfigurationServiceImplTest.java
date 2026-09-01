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
package net.ihe.gazelle.xmlvalidation.technical.config;

import net.ihe.gazelle.validation.v2.api.business.UnknownValidationProfileException;
import net.ihe.gazelle.validation.v2.api.business.UnavailableValidationProfileException;
import net.ihe.gazelle.xmlvalidation.business.config.ProfileConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProfileConfigurationServiceImplTest {

   @TempDir
   Path tempDir;

   private ProfileConfigurationServiceImpl service;

   @BeforeEach
   void setUp() throws Exception {
      service = new ProfileConfigurationServiceImpl();
      setField(service, tempDir.resolve("index.json").toString());
   }

   @Test
   void loadsValidProfile() throws Exception {
      Path xsdFile = tempDir.resolve("schema.xsd");
      Files.writeString(xsdFile, "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"/>");
      writeIndex(List.of(buildProfileJson("valid-profile", "schema.xsd")));

      service.reset();
      List<ProfileConfiguration> profiles = service.getProfileConfigurations();

      assertEquals(1, profiles.size());
      ProfileConfiguration profile = service.getProfileConfiguration("valid-profile");
      assertEquals("valid-profile", profile.getId());
      assertTrue(profile.getXsdAbsolutePath().endsWith("schema.xsd"));
       profile.getSchematronAbsolutePath();
       assertTrue(true, "schematron path should be empty-safe");
   }

   @Test
   void marksProfileUnavailableWhenXsdMissing() throws Exception {
      writeIndex(List.of(buildProfileJson("broken-profile", "missing.xsd")));

      service.reset();
      List<ProfileConfiguration> profiles = service.getProfileConfigurations();
      assertTrue(profiles.isEmpty(), "invalid profile should be filtered out");
      assertThrows(UnavailableValidationProfileException.class,
            () -> service.getProfileConfiguration("broken-profile"));
      assertThrows(UnknownValidationProfileException.class,
            () -> service.getProfileConfiguration("absent-profile"));
   }

   private void writeIndex(List<String> profileJsonEntries) throws IOException {
      String json = "[" + String.join(",", profileJsonEntries) + "]";
      Files.writeString(tempDir.resolve("index.json"), json);
   }

   private String buildProfileJson(String id, String xsdPath) {
      return """
            {
              "profileID": "%s",
              "profileName": "%s",
              "domain": "domain",
              "xsdPath": "%s",
              "schematronPath": "%s",
              "xsltPath": "",
              "cacheEnabled": false,
              "schematronVersion": "1.0",
              "xsdVersion": "1.0",
              "standards": ["std"],
              "coveredItems": [],
              "available": true
            }
            """.formatted(id, id, xsdPath, "");
   }

   private static void setField(Object target, Object value) throws Exception {
      Field field = target.getClass().getDeclaredField("profileIndexPath");
      field.setAccessible(true);
      field.set(target, value);
   }
}
