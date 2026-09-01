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

public class WrongIntegrationConfig implements QuarkusTestResourceLifecycleManager {

    protected static File PHSchematronDir;

    protected static File profilesFile;

    protected static ProfileConfigurationDTO wrongIdProfileConfigurationDTO;

    protected static ProfileConfigurationDTO wrongNameProfileConfigurationDTO;

    protected static ProfileConfigurationDTO wrongDomaineProfileConfigurationDTO;

    protected static ProfileConfigurationDTO wrongXSDPathProfileConfigurationDTO;

    protected static ProfileConfigurationDTO wrongSchematronPathProfileConfigurationDTO;

    protected static ProfileConfigurationDTO noXSDFileProfileConfigurationDTO;

    protected static ProfileConfigurationDTO noSchematronFileProfileConfigurationDTO;

    protected static ValidatorBuilderFactory validatorBuilderFactory;

    protected static final String PH_SCHEMATRON_DIR = "/tmp/phschematronTest";


    @Override
    public Map<String, String> start() {
        wrongIdProfileConfigurationDTO = new ProfileConfigurationDTO();
        wrongNameProfileConfigurationDTO = new ProfileConfigurationDTO()
                .setProfileID("wrong_name_profile");
        wrongDomaineProfileConfigurationDTO = new ProfileConfigurationDTO()
                .setProfileID("wrong_domaine_profile")
                .setProfileName("test_profile");
        wrongXSDPathProfileConfigurationDTO = new ProfileConfigurationDTO()
                .setProfileID("wrong_xsd_path_profile")
                .setProfileName("test_profile")
                .setDomain("test_domain");
        noXSDFileProfileConfigurationDTO = new ProfileConfigurationDTO()
                .setProfileID("no_xsd_file_profile")
                .setProfileName("test_profile")
                .setDomain("test_domain")
                .setXsdPath("/profiles/test_domain/noFile.xsd");
        wrongSchematronPathProfileConfigurationDTO = new ProfileConfigurationDTO()
                .setProfileID("valid_profile_without_schematron_path_profile")
                .setProfileName("test_profile")
                .setDomain("test_domain")
                .setXsdPath("/profiles/test_domain/test_schema.xsd");
        noSchematronFileProfileConfigurationDTO = new ProfileConfigurationDTO()
                .setProfileID("no_schematron_file_profile")
                .setProfileName("test_profile")
                .setDomain("test_domain")
                .setXsdPath("/profiles/test_domain/test_schema.xsd")
                .setSchematronPath("/profiles/test_domain/test_profile/noFile.sch");
        initFiles();
        validatorBuilderFactory = new BValidatorBuilderFactory();
        File testProfile = new File(PHSchematronDir, "profiles/test_domain/test_profile/");
        if(!testProfile.mkdirs()){
            throw new RuntimeException("Could not create test profile directory");
        }
        try {
            File testSch = new File(testProfile, "test_profile.sch");
            Files.copy(new File("src/test/resources/profiles/domain1/profile1/valid01.sch").toPath(),
                    testSch.toPath());
            File testSchema = new File(testProfile.getParent(), "test_schema.xsd");
            Files.copy(new File("src/test/resources/profiles/domain1/schema.xsd").toPath(),
                    testSchema.toPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // Point the service to the generated index file so tests use these faulty profiles
        return Map.of("gzl.xmlvalidator.profile.index.path", profilesFile.getAbsolutePath());
    }

    @Override
    public void stop() {
        try{
            if(PHSchematronDir != null && PHSchematronDir.exists()) {
                deleteDirectory(PHSchematronDir.toPath());
                System.out.println("Deleted directory " + PHSchematronDir.getAbsolutePath());
                System.out.println("Deleted file " + profilesFile.getAbsolutePath());
            }
        }catch (Exception e) {
            throw new ConfigurationException("Error while deleting Directory", e);
        }
    }

    private static <R> void writeToFile(File file, R object) {
        try {
            if(!file.exists() && !file.createNewFile()) {
                throw new RuntimeException("Could not create file " + file.getAbsolutePath());
            }
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(file, object);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void initFiles(){
        PHSchematronDir = new File(PH_SCHEMATRON_DIR);
        if (!PHSchematronDir.exists() && !PHSchematronDir.mkdirs()) {
            throw new RuntimeException("Could not create directory " + PHSchematronDir.getAbsolutePath());
        }
        profilesFile = new File(PHSchematronDir, "index.json");
        writeToFile(profilesFile, List.of(wrongIdProfileConfigurationDTO,
                wrongNameProfileConfigurationDTO,
                wrongDomaineProfileConfigurationDTO,
                wrongXSDPathProfileConfigurationDTO,
                wrongSchematronPathProfileConfigurationDTO,
                noXSDFileProfileConfigurationDTO,
                noSchematronFileProfileConfigurationDTO));
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
