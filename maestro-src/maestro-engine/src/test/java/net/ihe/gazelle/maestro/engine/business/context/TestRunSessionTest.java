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

package net.ihe.gazelle.maestro.engine.business.context;

import net.ihe.gazelle.errorhandling.business.UnexpectedError;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class TestRunSessionTest {

    @Test
    void verify_equals_for_simulation_sequence() {
        UnexpectedError error1 = new UnexpectedError()
                .setName("error1")
                .setMessage("message1");
        UnexpectedError error2 = new UnexpectedError()
                .setName("error2")
                .setMessage("message2");

        EqualsVerifier.simple()
                .forClass(TestSuiteSession.class)
                .withPrefabValues(UnexpectedError.class, error1, error2)
                .verify();
    }
}
