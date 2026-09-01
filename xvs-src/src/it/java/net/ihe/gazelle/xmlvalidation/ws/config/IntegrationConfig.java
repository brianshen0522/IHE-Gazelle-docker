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
package net.ihe.gazelle.xmlvalidation.ws.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import net.ihe.gazelle.framework.modelvalidator.business.ValidatorBuilderFactory;
import net.ihe.gazelle.framework.modelvalidator.technical.adapter.bvalid.BValidatorBuilderFactory;
import net.ihe.gazelle.xmlvalidation.business.config.ConfigurationException;
import net.ihe.gazelle.xmlvalidation.technical.config.ProfileConfigurationDTO;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Map;

public class IntegrationConfig implements QuarkusTestResourceLifecycleManager {

   protected static File configDirectory;

   protected static File profilesFile;


   protected static ValidatorBuilderFactory validatorBuilderFactory;

   protected static final String CONFIG_DIRECTORY_PATH = System.getProperty("user.dir")
           + File.separator + "target" + File.separator + "test-tmp" + File.separator + "xml-validation-service";


   @Override
   public Map<String, String> start() {
      ProfileConfigurationDTO profileConfigurationDTO = new ProfileConfigurationDTO()
            .setProfileID("test_profile")
            .setProfileName("test_profile")
            .setDomain("test_domain")
            .setXsdPath("profiles/test_domain/test_schema.xsd")
            .setSchematronPath("profiles/test_domain/test_profile/test_profile.sch")
              .setSchematronVersion("1.0")
            .setXsltPath("cache/test_profile.xslt");

      configDirectory = createConfigDirectory(CONFIG_DIRECTORY_PATH);
      profilesFile = createConfigIndexFile(configDirectory, "index.json", profileConfigurationDTO);
      validatorBuilderFactory = new BValidatorBuilderFactory();

      File testProfileDir = createProfileDirectory(configDirectory, "profiles/test_domain/test_profile/");
      try {
         copyFile("src/test/resources/profiles/domain1/profile1/valid01.sch",
               testProfileDir.getAbsolutePath(), "test_profile.sch");
         copyFile("src/test/resources/profiles/domain1/schema.xsd",
               testProfileDir.getParent(), "test_schema.xsd");
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
      // Override the application property to point to the generated index file
      return Map.of(
            "gzl.xmlvalidator.profile.index.path", profilesFile.getAbsolutePath()
      );
   }

   @Override
   public void stop() {
      try {
         if (configDirectory != null && configDirectory.exists()) {
            deleteDirectory(configDirectory.toPath());
            System.out.println("Deleted directory " + configDirectory.getAbsolutePath());
            System.out.println("Deleted file " + profilesFile.getAbsolutePath());
         }
      } catch (Exception e) {
         throw new ConfigurationException("Error while deleting Directory", e);
      }
   }

   private static File createConfigDirectory(String configDirectory) {
      File configDir = new File(configDirectory);
      if (!configDir.exists() && !configDir.mkdirs()) {
            throw new RuntimeException("Could not create directory " + configDir.getAbsolutePath());
         }
      return configDir;
   }

   private static File createConfigIndexFile(File directory, String fileName,
                                             ProfileConfigurationDTO profileConfigurationDTO) {
      File indexFile = new File(directory, fileName);
      marshallToFile(indexFile, List.of(profileConfigurationDTO));
      return indexFile;
   }

   private static <R> void marshallToFile(File file, R object) {
      try {
         if (!file.exists() && !file.createNewFile()) {
            throw new RuntimeException("Could not create file " + file.getAbsolutePath());
         }
         ObjectMapper mapper = new ObjectMapper();
         mapper.writeValue(file, object);
      } catch (Exception e) {
         throw new RuntimeException(e);
      }
   }

   private static File createProfileDirectory(File configDir, String subDir) {
      File profileDir = new File(configDir, subDir);
      if (!profileDir.mkdirs()) {
         throw new RuntimeException("Could not create test profile directory");
      }
      return profileDir;
   }

   private static void copyFile(String resource, String directory, String fileName) throws IOException {
      File testSch = new File(directory, fileName);
      Files.createDirectories(testSch.getParentFile().toPath());
      Files.copy(new File(resource).toPath(),
            testSch.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
   }

   public static void deleteDirectory(Path directory) throws IOException {
      Files.walkFileTree(directory, new SimpleFileVisitor<>() {
         @Override
         public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) throws IOException {
            Files.delete(file);
            return FileVisitResult.CONTINUE;
         }

         @Override
         public FileVisitResult postVisitDirectory(Path directory, IOException exception) throws IOException {
            if (exception == null) {
               Files.delete(directory);
               return FileVisitResult.CONTINUE;
            } else {
               throw exception;
            }
         }
      });
   }
}
