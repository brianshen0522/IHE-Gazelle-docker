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

package net.ihe.gazelle.maestro.engine.technical.userinteract;

import net.ihe.gazelle.maestro.engine.business.mock.RecordingMaestroObserver;
import org.junit.jupiter.api.Test;
import technical.userinteract.UserInteractionHandlerImpl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserInteractionHandlerTest {

    @Test
    void test_display_message() {
        UserInteractionHandlerImpl handler = new UserInteractionHandlerImpl(new RecordingMaestroObserver());
        assertTrue(handler.isAvailable());
        assertDoesNotThrow(() ->handler.displayMessage("title", "message", 10));
    }
}
