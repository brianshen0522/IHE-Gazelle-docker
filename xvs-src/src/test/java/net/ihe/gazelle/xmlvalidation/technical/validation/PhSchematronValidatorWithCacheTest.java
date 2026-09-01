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
package net.ihe.gazelle.xmlvalidation.technical.validation;

import net.ihe.gazelle.framework.modelvalidator.technical.adapter.bvalid.BValidatorBuilderFactory;
import net.ihe.gazelle.xmlvalidation.business.SchematronValidator;
import net.ihe.gazelle.xmlvalidation.technical.phschematron.PhSchematronValidator;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class PhSchematronValidatorWithCacheTest extends PhSchematronValidatorTest {


    private SchematronValidator schematronValidator;

    @Override
    protected SchematronValidator getPhSchematronService() {
        if(schematronValidator == null) {
            schematronValidator = new PhSchematronValidator(new BValidatorBuilderFactory());
        }
        return schematronValidator;
    }

    @Test
    @Override
    public void testValidate() throws IOException {
        super.testValidate();
        assertTrue(Files.exists(Path.of(getProfileConfiguration().getXsltAbsolutePath())));
    }

    @Test
    public void testValidateWithCache() throws IOException {
        super.testValidate();
        assertTrue(Files.exists(Path.of(getProfileConfiguration().getXsltAbsolutePath())));
    }

    @AfterAll
    public static void cleanUp() {
        File file = new File(getProfileConfiguration().getXsltPath());
        if(file.delete()){
            System.out.println(file.getName() + " was deleted!");
        }
    }
}
