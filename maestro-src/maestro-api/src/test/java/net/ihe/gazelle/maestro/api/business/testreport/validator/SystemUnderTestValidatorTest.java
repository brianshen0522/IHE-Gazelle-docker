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

package net.ihe.gazelle.maestro.api.business.testreport.validator;

import net.ihe.gazelle.framework.modelvalidator.business.ObjectResult;
import net.ihe.gazelle.maestro.api.business.testreport.EntityIdentification;
import net.ihe.gazelle.maestro.api.business.testreport.SystemUnderTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SystemUnderTestValidatorTest {

    private final SystemUnderTestValidator validator = new SystemUnderTestValidator();

    @Test
    void shouldValidateSystemUnderTestWithIdentification() {
        SystemUnderTest systemUnderTest = new SystemUnderTest()
                .setSystemIdentification(new EntityIdentification("sut-1"));

        ObjectResult result = validator.validate(systemUnderTest);

        assertTrue(result.isValid(), () -> "Unexpected failures: " + result);
    }

    @Test
    void shouldRejectSystemUnderTestWithoutIdentification() {
        SystemUnderTest systemUnderTest = new SystemUnderTest();

        ObjectResult result = validator.validate(systemUnderTest);

        assertFalse(result.isValid());
    }
}

